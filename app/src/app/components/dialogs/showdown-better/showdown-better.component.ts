import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  OnInit,
  Signal,
  computed,
  inject,
  signal
} from '@angular/core';
import {MatButton} from '@angular/material/button';
import {MatDialogActions, MatDialogContent, MatDialogTitle, MAT_DIALOG_DATA} from '@angular/material/dialog';
import {MatIcon} from '@angular/material/icon';
import {ApiRestService} from '../../../services';
import {Card, compareCards, Game, Player, translateHandType} from '../../../model';
import {CardComponent, CardSize} from '../../game';

export type ShowdownBetterDialogData = {
  game: Game
  mobile: boolean
}

type ShowdownBetterPlayerView = {
  index: number
  name: string
  cards: Card[]
  hand?: string
  currentChips: number
  finalChips: number
}

type ShowdownBetterPotWinnerView = {
  index: number
  name: string
  chipsWon: number
}

type ShowdownBetterPotView = {
  name: string
  type: 'Main' | 'Side'
  chips: number
  winners: ShowdownBetterPotWinnerView[]
}

type ShowdownBetterViewData = {
  players: ShowdownBetterPlayerView[]
  pots: ShowdownBetterPotView[]
}

@Component({
  selector: 'poker-dialog-showdown-better',
  standalone: true,
  imports: [
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatButton,
    MatIcon,
    CardComponent
  ],
  templateUrl: './showdown-better.component.html',
  styleUrl: './showdown-better.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ShowdownBetterComponent implements OnInit {
  private readonly apiRestService: ApiRestService = inject(ApiRestService);
  private readonly data: ShowdownBetterDialogData = inject(MAT_DIALOG_DATA);
  private readonly destroyRef: DestroyRef = inject(DestroyRef);

  private readonly gameSignal = signal<Game>(this.data.game);
  private readonly playersSignal = signal<ShowdownBetterPlayerView[]>([]);
  private readonly potsSignal = signal<ShowdownBetterPotView[]>([]);

  private readonly revealedCardKeysSignal = signal<Set<string>>(new Set<string>());
  private readonly handTypesRevealedSignal = signal(false);
  private readonly revealedPotIndexesSignal = signal<Set<number>>(new Set<number>());
  private readonly highlightedPlayerIndexesSignal = signal<Set<number>>(new Set<number>());
  private readonly activePotIndexSignal = signal<number | null>(null);

  private readonly readyRequestInFlightSignal = signal(false);
  private readonly waitingForOthersSignal = signal(false);

  private sequenceStarted = false;
  private destroyed = false;

  protected readonly mobile: boolean = this.data.mobile;
  protected readonly cardSize: CardSize = this.mobile ? CardSize.MOBILE_SMALL : CardSize.DESKTOP_SMALL;

  protected readonly players: Signal<ShowdownBetterPlayerView[]> = computed(() => this.playersSignal());
  protected readonly pots: Signal<ShowdownBetterPotView[]> = computed(() => this.potsSignal());
  protected readonly handTypesRevealed: Signal<boolean> = computed(() => this.handTypesRevealedSignal());
  protected readonly activePotIndex: Signal<number | null> = computed(() => this.activePotIndexSignal());
  protected readonly readyButtonLabel: Signal<string> = computed(() =>
    this.waitingForOthersSignal() ? 'Waiting for others' : 'Ready for next round'
  );
  protected readonly readyButtonDisabled: Signal<boolean> = computed(() =>
    this.waitingForOthersSignal() || this.readyRequestInFlightSignal()
  );

  constructor() {
    const data = this.buildViewData(this.data.game);
    this.playersSignal.set(data.players);
    this.potsSignal.set(data.pots);
    this.syncReadyState(this.data.game);
    this.destroyRef.onDestroy(() => this.destroyed = true);
  }

  ngOnInit(): void {
    void this.runRevealSequence();
  }

  updateGame(game: Game): void {
    this.gameSignal.set(game);
    this.syncReadyState(game);
  }

  protected isCardRevealed(playerIndex: number, cardIndex: number): boolean {
    return this.revealedCardKeysSignal().has(this.cardRevealKey(playerIndex, cardIndex));
  }

  protected isPotRevealed(potIndex: number): boolean {
    return this.revealedPotIndexesSignal().has(potIndex);
  }

  protected isPlayerHighlighted(playerIndex: number): boolean {
    return this.highlightedPlayerIndexesSignal().has(playerIndex);
  }

  protected readyForNextRound(): void {
    if (this.readyButtonDisabled()) {
      return;
    }

    const game = this.gameSignal();
    this.readyRequestInFlightSignal.set(true);
    this.apiRestService.sendReady(game.gameId, game.myId, true).subscribe({
      next: () => {
        this.readyRequestInFlightSignal.set(false);
        this.waitingForOthersSignal.set(true);
      },
      error: () => {
        this.readyRequestInFlightSignal.set(false);
      }
    });
  }

  private async runRevealSequence(): Promise<void> {
    if (this.sequenceStarted) {
      return;
    }
    this.sequenceStarted = true;

    await this.delay(260);
    if (this.destroyed) {
      return;
    }

    for (const player of this.playersSignal()) {
      for (let cardIndex = 0; cardIndex < player.cards.length; cardIndex++) {
        this.revealCard(player.index, cardIndex);
        await this.delay(this.mobile ? 180 : 220);
        if (this.destroyed) {
          return;
        }
      }
      await this.delay(this.mobile ? 80 : 120);
      if (this.destroyed) {
        return;
      }
    }

    this.handTypesRevealedSignal.set(true);
    await this.delay(700);
    if (this.destroyed) {
      return;
    }

    for (let potIndex = 0; potIndex < this.potsSignal().length; potIndex++) {
      const pot = this.potsSignal()[potIndex];
      this.revealPot(potIndex);
      this.activePotIndexSignal.set(potIndex);

      const winners = pot.winners.map((winner) => winner.index);
      this.highlightPlayers(winners);

      await this.delay(220);
      if (this.destroyed) {
        return;
      }

      await Promise.all(pot.winners.map((winner) => this.animateChipGain(winner.index, winner.chipsWon)));
      if (this.destroyed) {
        return;
      }

      await this.delay(760);
      if (this.destroyed) {
        return;
      }

      this.clearHighlightedPlayers();
    }

    this.activePotIndexSignal.set(null);
  }

  private buildViewData(game: Game): ShowdownBetterViewData {
    const playersByIndex = new Map<number, Player>(game.players.map((player) => [player.index, player]));
    const pots: ShowdownBetterPotView[] = [];

    if (game.pot) {
      pots.push(this.toPotView('Main pot', 'Main', game.pot.mainPot.chips, game.pot.mainPot.winnerIndexes, playersByIndex));
      game.pot.sidePots.forEach((sidePot, sidePotIndex) => {
        pots.push(this.toPotView(`Side pot ${sidePotIndex + 1}`, 'Side', sidePot.chips, sidePot.winnerIndexes, playersByIndex));
      });
    }

    const totalWonByPlayer = new Map<number, number>();
    pots.forEach((pot) => {
      pot.winners.forEach((winner) => {
        totalWonByPlayer.set(winner.index, (totalWonByPlayer.get(winner.index) ?? 0) + winner.chipsWon);
      });
    });

    const players: ShowdownBetterPlayerView[] = game.players
      .map((player) => {
        const won = totalWonByPlayer.get(player.index) ?? 0;
        const baseChips = Math.max(0, player.chips - won);
        return {
          index: player.index,
          name: player.name,
          cards: [...(player.cards ?? [])].sort(compareCards),
          hand: player.handType ? translateHandType(player.handType) : undefined,
          currentChips: baseChips,
          finalChips: player.chips
        };
      })
      .sort((left, right) => left.index - right.index);

    return {players, pots};
  }

  private toPotView(name: string, type: 'Main' | 'Side', chips: number, winnerIndexes: number[],
                    playersByIndex: Map<number, Player>): ShowdownBetterPotView {
    const uniqueWinnerIndexes = [...new Set(winnerIndexes)];
    const split = this.distributeChips(chips, uniqueWinnerIndexes.length);

    return {
      name,
      type,
      chips,
      winners: uniqueWinnerIndexes.map((winnerIndex, winnerPosition) => {
        const player = playersByIndex.get(winnerIndex);
        return {
          index: winnerIndex,
          name: player?.name ?? `Player #${winnerIndex}`,
          chipsWon: split[winnerPosition] ?? 0
        };
      })
    };
  }

  private distributeChips(chips: number, winnerCount: number): number[] {
    if (winnerCount === 0) {
      return [];
    }

    const base = Math.floor(chips / winnerCount);
    let remainder = chips % winnerCount;

    return Array.from({length: winnerCount}, () => {
      const value = base + (remainder > 0 ? 1 : 0);
      remainder = Math.max(0, remainder - 1);
      return value;
    });
  }

  private syncReadyState(game: Game): void {
    const me = game.players.find((player) => player.index === game.myIndex);
    if (me?.ready) {
      this.waitingForOthersSignal.set(true);
      this.readyRequestInFlightSignal.set(false);
    }
  }

  private revealCard(playerIndex: number, cardIndex: number): void {
    this.revealedCardKeysSignal.update((current) => {
      const next = new Set(current);
      next.add(this.cardRevealKey(playerIndex, cardIndex));
      return next;
    });
  }

  private revealPot(index: number): void {
    this.revealedPotIndexesSignal.update((current) => {
      const next = new Set(current);
      next.add(index);
      return next;
    });
  }

  private highlightPlayers(indexes: number[]): void {
    this.highlightedPlayerIndexesSignal.set(new Set(indexes));
  }

  private clearHighlightedPlayers(): void {
    this.highlightedPlayerIndexesSignal.set(new Set<number>());
  }

  private cardRevealKey(playerIndex: number, cardIndex: number): string {
    return `${playerIndex}-${cardIndex}`;
  }

  private currentChips(playerIndex: number): number {
    return this.playersSignal().find((player) => player.index === playerIndex)?.currentChips ?? 0;
  }

  private setCurrentChips(playerIndex: number, chips: number): void {
    this.playersSignal.update((players) =>
      players.map((player) => player.index === playerIndex ? {...player, currentChips: chips} : player)
    );
  }

  private animateChipGain(playerIndex: number, chipsWon: number): Promise<void> {
    if (chipsWon <= 0 || this.destroyed) {
      return Promise.resolve();
    }

    const startChips = this.currentChips(playerIndex);
    const endChips = Math.min(
      this.playersSignal().find((player) => player.index === playerIndex)?.finalChips ?? (startChips + chipsWon),
      startChips + chipsWon
    );

    return new Promise((resolve) => {
      const durationMs = 920;
      const startTime = Date.now();
      const timer = setInterval(() => {
        if (this.destroyed) {
          clearInterval(timer);
          resolve();
          return;
        }

        const elapsed = Date.now() - startTime;
        const progress = Math.min(1, elapsed / durationMs);
        const easedProgress = 1 - Math.pow(1 - progress, 3);
        const nextValue = Math.round(startChips + (endChips - startChips) * easedProgress);
        this.setCurrentChips(playerIndex, nextValue);

        if (progress >= 1) {
          clearInterval(timer);
          this.setCurrentChips(playerIndex, endChips);
          resolve();
        }
      }, 26);
    });
  }

  private delay(ms: number): Promise<void> {
    return new Promise((resolve) => setTimeout(resolve, ms));
  }
}
