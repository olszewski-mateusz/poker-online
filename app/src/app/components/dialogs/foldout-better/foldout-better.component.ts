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
import {Game} from '../../../model';

export type FoldoutBetterDialogData = {
  game: Game
  mobile: boolean
}

type FoldoutWinnerView = {
  index: number
  name: string
  chipsWon: number
  currentChips: number
  finalChips: number
}

type FoldoutViewData = {
  winner: FoldoutWinnerView | null
  totalPotChips: number
}

@Component({
  selector: 'poker-dialog-foldout-better',
  standalone: true,
  imports: [
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatButton,
    MatIcon
  ],
  templateUrl: './foldout-better.component.html',
  styleUrl: './foldout-better.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FoldoutBetterComponent implements OnInit {
  private readonly apiRestService: ApiRestService = inject(ApiRestService);
  private readonly data: FoldoutBetterDialogData = inject(MAT_DIALOG_DATA);
  private readonly destroyRef: DestroyRef = inject(DestroyRef);

  private readonly gameSignal = signal<Game>(this.data.game);
  private readonly winnerSignal = signal<FoldoutWinnerView | null>(null);
  private readonly totalPotChipsSignal = signal(0);
  private readonly winnerRevealedSignal = signal(false);

  private readonly readyRequestInFlightSignal = signal(false);
  private readonly waitingForOthersSignal = signal(false);

  private sequenceStarted = false;
  private destroyed = false;

  protected readonly mobile: boolean = this.data.mobile;
  protected readonly winner: Signal<FoldoutWinnerView | null> = computed(() => this.winnerSignal());
  protected readonly totalPotChips: Signal<number> = computed(() => this.totalPotChipsSignal());
  protected readonly winnerRevealed: Signal<boolean> = computed(() => this.winnerRevealedSignal());
  protected readonly readyButtonLabel: Signal<string> = computed(() =>
    this.waitingForOthersSignal() ? 'Waiting for others' : 'Ready for next round'
  );
  protected readonly readyButtonDisabled: Signal<boolean> = computed(() =>
    this.waitingForOthersSignal() || this.readyRequestInFlightSignal()
  );

  constructor() {
    const data = this.buildViewData(this.data.game);
    this.winnerSignal.set(data.winner);
    this.totalPotChipsSignal.set(data.totalPotChips);
    this.syncReadyState(this.data.game);
    this.destroyRef.onDestroy(() => this.destroyed = true);
  }

  ngOnInit(): void {
    void this.runRevealSequence();
  }

  updateGame(game: Game): void {
    this.gameSignal.set(game);
    const data = this.buildViewData(game);
    this.winnerSignal.set(data.winner);
    this.totalPotChipsSignal.set(data.totalPotChips);
    this.syncReadyState(game);
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

    await this.delay(280);
    if (this.destroyed) {
      return;
    }

    this.winnerRevealedSignal.set(true);
    const winner = this.winnerSignal();
    if (!winner) {
      return;
    }

    await this.delay(240);
    if (this.destroyed) {
      return;
    }

    await this.animateChipGain(winner.index, winner.chipsWon);
  }

  private buildViewData(game: Game): FoldoutViewData {
    if (!game.pot) {
      return {winner: null, totalPotChips: 0};
    }

    const totalPotChips = game.pot.mainPot.chips + game.pot.sidePots.reduce((sum, pot) => sum + pot.chips, 0);
    const totalWonByPlayer = this.buildTotalWonByPlayer(game);
    const winnerIndex = this.resolveWinnerIndex(game, totalWonByPlayer);

    if (winnerIndex === null) {
      return {winner: null, totalPotChips};
    }

    const winnerPlayer = game.players.find((player) => player.index === winnerIndex);
    const chipsWon = totalWonByPlayer.get(winnerIndex) ?? totalPotChips;

    if (!winnerPlayer) {
      return {
        winner: {
          index: winnerIndex,
          name: `Player #${winnerIndex}`,
          chipsWon,
          currentChips: 0,
          finalChips: chipsWon
        },
        totalPotChips
      };
    }

    const baseChips = Math.max(0, winnerPlayer.chips - chipsWon);
    return {
      winner: {
        index: winnerPlayer.index,
        name: winnerPlayer.name,
        chipsWon,
        currentChips: baseChips,
        finalChips: winnerPlayer.chips
      },
      totalPotChips
    };
  }

  private buildTotalWonByPlayer(game: Game): Map<number, number> {
    const totalWonByPlayer = new Map<number, number>();
    const mainPotWinnerIndexes = game.pot.mainPot.winnerIndexes ?? [];
    const allPots = [
      {chips: game.pot.mainPot.chips, winnerIndexes: mainPotWinnerIndexes},
      ...game.pot.sidePots.map((sidePot) => ({chips: sidePot.chips, winnerIndexes: sidePot.winnerIndexes ?? []}))
    ];

    allPots.forEach((pot) => {
      const uniqueWinnerIndexes = [...new Set(pot.winnerIndexes)];
      const split = this.distributeChips(pot.chips, uniqueWinnerIndexes.length);

      uniqueWinnerIndexes.forEach((winnerIndex, winnerPosition) => {
        const chipsWon = split[winnerPosition] ?? 0;
        totalWonByPlayer.set(winnerIndex, (totalWonByPlayer.get(winnerIndex) ?? 0) + chipsWon);
      });
    });

    return totalWonByPlayer;
  }

  private resolveWinnerIndex(game: Game, totalWonByPlayer: Map<number, number>): number | null {
    const mainPotWinner = [...new Set(game.pot.mainPot.winnerIndexes ?? [])][0];
    if (mainPotWinner !== undefined) {
      return mainPotWinner;
    }

    const winnerFromPots = totalWonByPlayer.keys().next().value;
    if (winnerFromPots !== undefined) {
      return winnerFromPots;
    }

    const nonFoldedPlayer = game.players.find((player) => !player.folded);
    return nonFoldedPlayer?.index ?? null;
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

  private currentChips(playerIndex: number): number {
    return this.winnerSignal()?.index === playerIndex ? this.winnerSignal()?.currentChips ?? 0 : 0;
  }

  private setCurrentChips(playerIndex: number, chips: number): void {
    this.winnerSignal.update((winner) => {
      if (!winner || winner.index !== playerIndex) {
        return winner;
      }

      return {
        ...winner,
        currentChips: chips
      };
    });
  }

  private animateChipGain(playerIndex: number, chipsWon: number): Promise<void> {
    if (chipsWon <= 0 || this.destroyed) {
      return Promise.resolve();
    }

    const winner = this.winnerSignal();
    if (!winner || winner.index !== playerIndex) {
      return Promise.resolve();
    }

    const startChips = this.currentChips(playerIndex);
    const endChips = Math.min(winner.finalChips, startChips + chipsWon);

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
