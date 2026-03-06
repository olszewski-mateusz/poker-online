import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  effect,
  inject,
  input,
  InputSignal,
  Signal,
  signal,
  WritableSignal
} from '@angular/core';
import {ActionType, Card, compareCards, Game, GamePhase} from '../../../model';
import {CardComponent, CardSize} from '../hand/card/card.component';
import {ReplaceCardsService} from '../actions';

@Component({
  selector: 'poker-game-hand-better',
  standalone: true,
  imports: [
    CardComponent
  ],
  templateUrl: './hand-better.component.html',
  styleUrl: './hand-better.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HandBetterComponent {
  private static readonly HAND_SIZE: number = 5;
  private static readonly DEAL_STAGGER_MS: number = 130;
  private static readonly DEAL_MOVE_MS: number = 280;
  private static readonly DISCARD_MOVE_MS: number = 260;
  private static readonly REARRANGE_MOVE_MS: number = 260;
  private static readonly FLIP_HALF_MS: number = 120;

  private readonly replaceCardsService: ReplaceCardsService = inject(ReplaceCardsService);
  private readonly destroyRef: DestroyRef = inject(DestroyRef);

  game: InputSignal<Game> = input.required<Game>();
  mobile: InputSignal<boolean> = input.required<boolean>();

  protected readonly cardsInteractive: Signal<boolean> = computed(() => {
    return this.game().validActions.some(value => value.type === ActionType.REPLACE);
  });

  protected readonly cardsSize: Signal<CardSize> = computed(() => {
    return this.mobile() ? CardSize.MOBILE_BIG : CardSize.DESKTOP_BIG;
  });

  protected readonly layout: Signal<HandLayout> = computed(() => {
    return this.mobile() ? MOBILE_LAYOUT : DESKTOP_LAYOUT;
  });

  private readonly _renderCards: WritableSignal<RenderCard[]> = signal<RenderCard[]>([]);
  protected readonly renderCards: Signal<RenderCard[]> = this._renderCards.asReadonly();
  private readonly myStateSnapshot: Signal<HandStateSnapshot> = computed(() => {
    const game: Game = this.game();
    const cards: Card[] = game.players.find(value => value.index === game.myIndex)?.cards ?? [];
    return {
      phase: game.phase,
      cards: [...cards].sort(compareCards)
    };
  });

  private nextCardId: number = 1;
  private queuedSnapshot: HandStateSnapshot | null = null;
  private processingQueue: boolean = false;
  private readonly timeouts: Set<number> = new Set<number>();
  private destroyed: boolean = false;
  private lastObservedSnapshotSignature: string = '';
  private displayedCards: Card[] = [];
  private displayedPhase: GamePhase | null = null;
  private animationRunning: boolean = false;

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
    if (!this.canInteract(renderCard)) {
      return;
    }
    this.replaceCardsService.toggleCard(renderCard.card);
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
    const x: number = renderCard.discarding || renderCard.movingFromDealer
      ? 0
      : this.cardSlotX(renderCard.slotIndex);
    const y: number = renderCard.discarding || renderCard.movingFromDealer
      ? this.layout().dealerOffsetY
      : 0;
    return `translate(calc(-50% + ${x}px), ${y}px)`;
  }

  protected cardZIndex(renderCard: RenderCard): number {
    return renderCard.slotIndex + 1;
  }

  protected labelY(): string {
    return `${this.layout().cardHeight + 6}px`;
  }

  private enqueueSnapshot(snapshot: HandStateSnapshot): void {
    this.queuedSnapshot = {
      phase: snapshot.phase,
      cards: [...snapshot.cards]
    };
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

    if (targetCards.length === 0) {
      await this.discardCards(this._renderCards().map(value => value.id));
      this._renderCards.set([]);
      this.displayedCards = [];
      this.displayedPhase = targetSnapshot.phase;
      return;
    }

    if (startNewRoundAnimation) {
      await this.discardCards(this._renderCards().map(value => value.id));
      this._renderCards.set([]);
      await this.dealCards(targetCards);
      this.displayedCards = [...targetCards];
      this.displayedPhase = targetSnapshot.phase;
      return;
    }

    if (this.displayedCards.length === 0) {
      await this.dealCards(targetCards);
      this.displayedCards = [...targetCards];
      this.displayedPhase = targetSnapshot.phase;
      return;
    }

    const targetKeys: Set<string> = new Set(targetCards.map(value => this.cardKey(value)));
    const sharedCardsCount: number = this.displayedCards.filter(value => targetKeys.has(this.cardKey(value))).length;

    if (sharedCardsCount === 0) {
      await this.discardCards(this._renderCards().map(value => value.id));
      this._renderCards.set([]);
      await this.dealCards(targetCards);
      this.displayedCards = [...targetCards];
      this.displayedPhase = targetSnapshot.phase;
      return;
    }

    await this.runReplaceFlow(targetCards);
    this.displayedCards = [...targetCards];
    this.displayedPhase = targetSnapshot.phase;
  }

  private async runReplaceFlow(targetCards: Card[]): Promise<void> {
    const targetIndexes: Map<string, number> = new Map<string, number>(
      targetCards.map((value, index) => [this.cardKey(value), index])
    );
    const targetKeySet: Set<string> = new Set<string>(targetIndexes.keys());

    const cardsToDiscard: number[] = this._renderCards()
      .filter(value => !targetKeySet.has(value.key))
      .map(value => value.id);
    await this.discardCards(cardsToDiscard);

    this._renderCards.update(values => values.map(value => {
      const nextIndex: number | undefined = targetIndexes.get(value.key);
      return {
        ...value,
        slotIndex: nextIndex ?? value.slotIndex
      };
    }));
    await this.sleep(HandBetterComponent.REARRANGE_MOVE_MS);

    const existingKeys: Set<string> = new Set<string>(this._renderCards().map(value => value.key));
    const replacementCards: Card[] = targetCards.filter(value => !existingKeys.has(this.cardKey(value)));

    for (const card of replacementCards) {
      const key: string = this.cardKey(card);
      const slotIndex: number = targetIndexes.get(key) ?? 0;
      const renderCard: RenderCard = this.buildRenderCard(card, slotIndex, false, true);
      this._renderCards.update(values => [...values, renderCard]);
      await this.sleep(16);
      this.patchCard(renderCard.id, {movingFromDealer: false});
      await this.sleep(HandBetterComponent.DEAL_STAGGER_MS);
    }

    await this.sleep(HandBetterComponent.DEAL_MOVE_MS);
    for (const card of replacementCards) {
      const cardId: number | undefined = this._renderCards().find(value => value.key === this.cardKey(card))?.id;
      if (cardId !== undefined) {
        await this.flipCard(cardId);
      }
    }

    this.sortRenderCardsBySlot();
  }

  private async dealCards(cards: Card[]): Promise<void> {
    this._renderCards.set([]);
    for (let i = 0; i < cards.length; i++) {
      const renderCard: RenderCard = this.buildRenderCard(cards[i], i, false, true);
      this._renderCards.update(values => [...values, renderCard]);
      await this.sleep(16);
      this.patchCard(renderCard.id, {movingFromDealer: false});
      await this.sleep(HandBetterComponent.DEAL_STAGGER_MS);
    }

    await this.sleep(HandBetterComponent.DEAL_MOVE_MS);
    const cardsInFlipOrder: RenderCard[] = [...this._renderCards()].sort((a, b) => a.slotIndex - b.slotIndex);
    for (const renderCard of cardsInFlipOrder) {
      await this.flipCard(renderCard.id);
    }

    this.sortRenderCardsBySlot();
  }

  private async flipCard(cardId: number): Promise<void> {
    this.patchCard(cardId, {flipping: true});
    await this.sleep(HandBetterComponent.FLIP_HALF_MS);
    this.patchCard(cardId, {faceUp: true});
    await this.sleep(HandBetterComponent.FLIP_HALF_MS);
    this.patchCard(cardId, {flipping: false});
  }

  private async discardCards(cardIds: number[]): Promise<void> {
    if (!cardIds.length) {
      return;
    }
    const ids: Set<number> = new Set<number>(cardIds);
    this._renderCards.update(values => values.map(value => {
      if (!ids.has(value.id)) {
        return value;
      }
      return {
        ...value,
        discarding: true,
        movingFromDealer: false,
        flipping: false
      };
    }));
    await this.sleep(HandBetterComponent.DISCARD_MOVE_MS);
    this._renderCards.update(values => values.filter(value => !ids.has(value.id)));
  }

  private sortRenderCardsBySlot(): void {
    this._renderCards.update(values => [...values].sort((a, b) => a.slotIndex - b.slotIndex));
  }

  private patchCard(cardId: number, patch: Partial<RenderCard>): void {
    this._renderCards.update(values => values.map(value => {
      if (value.id !== cardId) {
        return value;
      }
      return {
        ...value,
        ...patch
      };
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
    const previousPhase: GamePhase | null = this.displayedPhase;
    if (!previousPhase) {
      return false;
    }
    if (targetCards.length === 0 || this.displayedCards.length === 0) {
      return false;
    }
    const newRoundStartsAfter: GamePhase[] = [GamePhase.SHOWDOWN, GamePhase.FINISHED];
    return targetPhase === GamePhase.FIRST_BETTING && newRoundStartsAfter.includes(previousPhase);
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
    return `${snapshot.phase}|${this.cardsSignature(snapshot.cards)}`;
  }

  private cardsSignature(cards: Card[]): string {
    return cards.map(value => this.cardKey(value)).join('|');
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
}

type RenderCard = {
  id: number
  key: string
  card: Card
  slotIndex: number
  faceUp: boolean
  movingFromDealer: boolean
  discarding: boolean
  flipping: boolean
}

type HandLayout = {
  cardWidth: number
  cardHeight: number
  cardRadius: number
  gap: number
  dealerOffsetY: number
}

const DESKTOP_LAYOUT: HandLayout = {
  cardWidth: 100,
  cardHeight: 140,
  cardRadius: 10,
  gap: 20,
  dealerOffsetY: 160
};

const MOBILE_LAYOUT: HandLayout = {
  cardWidth: 55,
  cardHeight: 85,
  cardRadius: 10,
  gap: 8,
  dealerOffsetY: 110
};






