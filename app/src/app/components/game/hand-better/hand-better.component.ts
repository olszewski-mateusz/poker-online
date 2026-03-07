import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  effect,
  inject,
  input,
  signal
} from '@angular/core';
import {ActionType, Card, Game, GamePhase, compareCards} from '../../../model';
import {CardComponent, CardSize} from '../hand';
import {ReplaceCardsService} from '../actions';
import {MatIcon} from '@angular/material/icon';

@Component({
  selector: 'poker-game-hand-better',
  standalone: true,
  imports: [CardComponent, MatIcon],
  templateUrl: './hand-better.component.html',
  styleUrl: './hand-better.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HandBetterComponent {
  private static readonly HAND_SIZE = 5;
  private static readonly DEAL_STAGGER_MS = 130;
  private static readonly DEAL_MOVE_MS = 280;
  private static readonly DISCARD_MOVE_MS = 260;
  private static readonly REARRANGE_MOVE_MS = 260;
  private static readonly FLIP_HALF_MS = 120;

  private readonly replaceCardsService = inject(ReplaceCardsService);
  private readonly destroyRef = inject(DestroyRef);

  game = input.required<Game>();
  mobile = input.required<boolean>();

  protected readonly cardsInteractive = computed(() =>
    this.game().validActions.some(value => value.type === ActionType.REPLACE)
  );

  protected readonly cardsSize = computed(() =>
    this.mobile() ? CardSize.MOBILE_BIG : CardSize.DESKTOP_BIG
  );

  protected readonly layout = computed(() =>
    this.mobile() ? MOBILE_LAYOUT : DESKTOP_LAYOUT
  );

  private readonly _renderCards = signal<RenderCard[]>([]);
  protected readonly renderCards = this._renderCards.asReadonly();

  private readonly myStateSnapshot = computed<HandStateSnapshot>(() => {
    const game: Game = this.game();
    const cards: Card[] = game.players.find(value => value.index === game.myIndex)?.cards ?? [];
    return {phase: game.phase, cards: [...cards].sort(compareCards)};
  });

  private nextCardId = 1;
  private queuedSnapshot: HandStateSnapshot | null = null;
  private processingQueue = false;
  private readonly timeouts = new Set<number>();
  private destroyed = false;
  private lastObservedSnapshotSignature = '';
  private displayedCards: Card[] = [];
  private displayedPhase: GamePhase | null = null;
  private animationRunning = false;

  constructor() {
    this.destroyRef.onDestroy(() => {
      this.destroyed = true;
      this.clearTimers();
    });

    effect(() => {
      const snapshot: HandStateSnapshot = this.myStateSnapshot();
      const signature: string = this.snapshotSignature(snapshot);
      if (signature === this.lastObservedSnapshotSignature) {
        return;
      }

      this.lastObservedSnapshotSignature = signature;
      this.enqueueSnapshot(snapshot);
    }, {allowSignalWrites: true});
  }

  protected cardClicked(renderCard: RenderCard): void {
    if (this.canInteract(renderCard)) {
      this.replaceCardsService.toggleCard(renderCard.card);
    }
  }

  protected canInteract(renderCard: RenderCard): boolean {
    return this.cardsInteractive()
      && renderCard.faceUp
      && !renderCard.discarding
      && !renderCard.movingFromDealer
      && !renderCard.flipping
      && !this.animationRunning;
  }

  protected isCardSelected(card: Card): boolean {
    return this.replaceCardsService.cardsToReplace().some(c => c.rank === card.rank && c.suit === card.suit);
  }

  protected cardTranslate(renderCard: RenderCard): string {
    const inDealerArea: boolean = renderCard.discarding || renderCard.movingFromDealer;
    const x: number = inDealerArea ? 0 : this.cardSlotX(renderCard.slotIndex);
    const y: number = inDealerArea ? this.layout().dealerOffsetY : 0;
    return `translate(calc(-50% + ${x}px), ${y}px)`;
  }

  protected cardZIndex(renderCard: RenderCard): number {
    return renderCard.slotIndex + 1;
  }

  protected labelY(): string {
    return `${this.layout().cardHeight + 6}px`;
  }

  private enqueueSnapshot(snapshot: HandStateSnapshot): void {
    this.queuedSnapshot = {phase: snapshot.phase, cards: [...snapshot.cards]};
    if (!this.processingQueue) {
      void this.processSnapshotQueue();
    }
  }

  private async processSnapshotQueue(): Promise<void> {
    if (this.processingQueue) {
      return;
    }

    this.processingQueue = true;
    this.animationRunning = true;
    try {
      while (this.queuedSnapshot && !this.destroyed) {
        const snapshot: HandStateSnapshot = this.queuedSnapshot;
        this.queuedSnapshot = null;
        await this.transitionTo(snapshot);
      }
    } finally {
      this.processingQueue = false;
      this.animationRunning = false;
    }
  }

  private async transitionTo(targetSnapshot: HandStateSnapshot): Promise<void> {
    const targetCards: Card[] = [...targetSnapshot.cards].sort(compareCards);
    const startNewRoundAnimation: boolean = this.isNewRoundStart(targetSnapshot.phase, targetCards);
    const cardsChanged: boolean = !this.sameCards(this.displayedCards, targetCards);

    if (!cardsChanged && !startNewRoundAnimation) {
      this.displayedPhase = targetSnapshot.phase;
      return;
    }

    if (cardsChanged) {
      this.replaceCardsService.clearSelection();
    }

    if (!targetCards.length) {
      await this.clearHand();
      this.markDisplayed(targetSnapshot.phase, []);
      return;
    }

    if (startNewRoundAnimation || !this.displayedCards.length || !this.hasSharedCards(targetCards)) {
      await this.resetAndDeal(targetCards);
      this.markDisplayed(targetSnapshot.phase, targetCards);
      return;
    }

    await this.runReplaceFlow(targetCards);
    this.markDisplayed(targetSnapshot.phase, targetCards);
  }

  private async runReplaceFlow(targetCards: Card[]): Promise<void> {
    const targetIndexes: Map<string, number> = new Map(
      targetCards.map((card, index) => [this.cardKey(card), index])
    );
    const targetKeys: Set<string> = new Set<string>(targetIndexes.keys());

    await this.discardCards(this._renderCards().filter(card => !targetKeys.has(card.key)).map(card => card.id));

    this._renderCards.update(cards => cards.map(card => ({
      ...card,
      slotIndex: targetIndexes.get(card.key) ?? card.slotIndex
    })));
    await this.sleep(HandBetterComponent.REARRANGE_MOVE_MS);

    const existingKeys: Set<string> = new Set<string>(this._renderCards().map(card => card.key));
    const replacementCards: Card[] = targetCards.filter(card => !existingKeys.has(this.cardKey(card)));
    const dealtIds: number[] = [];

    for (const card of replacementCards) {
      const slotIndex: number = targetIndexes.get(this.cardKey(card)) ?? 0;
      dealtIds.push(await this.dealOneCard(card, slotIndex));
    }

    await this.sleep(HandBetterComponent.DEAL_MOVE_MS);
    for (const cardId of dealtIds) {
      await this.flipCard(cardId);
    }

    this.sortRenderCardsBySlot();
  }

  private async dealCards(cards: Card[]): Promise<void> {
    this._renderCards.set([]);
    const dealtIds: number[] = [];

    for (const [index, card] of cards.entries()) {
      dealtIds.push(await this.dealOneCard(card, index));
    }

    await this.sleep(HandBetterComponent.DEAL_MOVE_MS);
    for (const cardId of dealtIds) {
      await this.flipCard(cardId);
    }

    this.sortRenderCardsBySlot();
  }

  private async dealOneCard(card: Card, slotIndex: number): Promise<number> {
    const renderCard: RenderCard = this.buildRenderCard(card, slotIndex, false, true);
    this._renderCards.update(cards => [...cards, renderCard]);
    await this.sleep(16);
    this.patchCard(renderCard.id, {movingFromDealer: false});
    await this.sleep(HandBetterComponent.DEAL_STAGGER_MS);
    return renderCard.id;
  }

  private async flipCard(cardId: number): Promise<void> {
    this.patchCard(cardId, {flipping: true});
    await this.sleep(HandBetterComponent.FLIP_HALF_MS);
    this.patchCard(cardId, {faceUp: true});
    await this.sleep(HandBetterComponent.FLIP_HALF_MS);
    this.patchCard(cardId, {flipping: false});
  }

  private async clearHand(): Promise<void> {
    await this.discardCards(this.renderCardIds());
    this._renderCards.set([]);
  }

  private async resetAndDeal(cards: Card[]): Promise<void> {
    await this.clearHand();
    await this.dealCards(cards);
  }

  private async discardCards(cardIds: number[]): Promise<void> {
    if (!cardIds.length) {
      return;
    }

    const ids: Set<number> = new Set<number>(cardIds);
    this._renderCards.update(cards => cards.map(card => {
      if (!ids.has(card.id)) {
        return card;
      }
      return {...card, discarding: true, movingFromDealer: false, flipping: false};
    }));

    await this.sleep(HandBetterComponent.DISCARD_MOVE_MS);
    this._renderCards.update(cards => cards.filter(card => !ids.has(card.id)));
  }

  private hasSharedCards(targetCards: Card[]): boolean {
    const targetKeys: Set<string> = new Set<string>(targetCards.map(card => this.cardKey(card)));
    return this.displayedCards.some(card => targetKeys.has(this.cardKey(card)));
  }

  private renderCardIds(): number[] {
    return this._renderCards().map(card => card.id);
  }

  private markDisplayed(phase: GamePhase, cards: Card[]): void {
    this.displayedCards = [...cards];
    this.displayedPhase = phase;
  }

  private sortRenderCardsBySlot(): void {
    this._renderCards.update(cards => [...cards].sort((a, b) => a.slotIndex - b.slotIndex));
  }

  private patchCard(cardId: number, patch: Partial<RenderCard>): void {
    this._renderCards.update(cards => cards.map(card => {
      if (card.id !== cardId) {
        return card;
      }
      return {...card, ...patch};
    }));
  }

  private cardSlotX(slotIndex: number): number {
    const layout: HandLayout = this.layout();
    const totalWidth: number = (layout.cardWidth * HandBetterComponent.HAND_SIZE)
      + (layout.gap * (HandBetterComponent.HAND_SIZE - 1));
    return (-totalWidth / 2) + (layout.cardWidth / 2) + (slotIndex * (layout.cardWidth + layout.gap));
  }

  private buildRenderCard(card: Card, slotIndex: number, faceUp: boolean, movingFromDealer: boolean): RenderCard {
    return {
      id: this.nextCardId++,
      key: this.cardKey(card),
      card,
      slotIndex,
      faceUp,
      movingFromDealer,
      discarding: false,
      flipping: false
    };
  }

  private isNewRoundStart(targetPhase: GamePhase, targetCards: Card[]): boolean {
    if (!this.displayedPhase || !targetCards.length || !this.displayedCards.length) {
      return false;
    }
    return targetPhase === GamePhase.FIRST_BETTING
      && [GamePhase.SHOWDOWN, GamePhase.FINISHED].includes(this.displayedPhase);
  }

  private sameCards(a: Card[], b: Card[]): boolean {
    if (a.length !== b.length) {
      return false;
    }

    for (let i = 0; i < a.length; i++) {
      if (a[i].rank !== b[i].rank || a[i].suit !== b[i].suit) {
        return false;
      }
    }
    return true;
  }

  private snapshotSignature(snapshot: HandStateSnapshot): string {
    return `${snapshot.phase}|${snapshot.cards.map(card => this.cardKey(card)).join('|')}`;
  }

  private cardKey(card: Card): string {
    return `${card.rank}-${card.suit}`;
  }

  private sleep(ms: number): Promise<void> {
    if (this.destroyed) {
      return Promise.resolve();
    }

    return new Promise<void>(resolve => {
      const handle: number = window.setTimeout(() => {
        this.timeouts.delete(handle);
        resolve();
      }, ms);
      this.timeouts.add(handle);
    });
  }

  private clearTimers(): void {
    for (const handle of this.timeouts) {
      window.clearTimeout(handle);
    }
    this.timeouts.clear();
  }
}

type HandStateSnapshot = {
  cards: Card[]
  phase: GamePhase
};

type RenderCard = {
  id: number
  key: string
  card: Card
  slotIndex: number
  faceUp: boolean
  movingFromDealer: boolean
  discarding: boolean
  flipping: boolean
};

type HandLayout = {
  cardWidth: number
  cardHeight: number
  cardRadius: number
  gap: number
  dealerOffsetY: number
};

const DESKTOP_LAYOUT: HandLayout = {
  cardWidth: 100,
  cardHeight: 140,
  cardRadius: 10,
  gap: 20,
  dealerOffsetY: 200
};

const MOBILE_LAYOUT: HandLayout = {
  cardWidth: 55,
  cardHeight: 85,
  cardRadius: 10,
  gap: 8,
  dealerOffsetY: 110
};
