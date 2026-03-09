import {ChangeDetectionStrategy, Component, computed, inject, Signal, signal} from '@angular/core';
import {MatButton} from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle
} from '@angular/material/dialog';
import {MatIcon} from '@angular/material/icon';
import {ApiRestService} from '../../../services';
import {Card, compareCards, Game, Player, translateHandType} from '../../../model';
import {CardComponent, CardSize} from '../../game';

export type ShowdownDialogData = {
  game: Game
  mobile: boolean
}

type ShowdownWinnerView = {
  index: number
  name: string
  cards: Card[]
  hand?: string
  chipsWon: number
}

type ShowdownPotView = {
  label: string
  chips: number
  main: boolean
  winners: ShowdownWinnerView[]
}

@Component({
  selector: 'poker-dialog-showdown',
  standalone: true,
  imports: [
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatButton,
    MatIcon,
    CardComponent
  ],
  templateUrl: './showdown-dialog.component.html',
  styleUrl: './showdown-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ShowdownDialogComponent {
  private readonly apiRestService: ApiRestService = inject(ApiRestService);
  private readonly dialogRef: MatDialogRef<ShowdownDialogComponent, string | undefined> = inject(MatDialogRef);
  private readonly data: ShowdownDialogData = inject(MAT_DIALOG_DATA);

  private readonly gameSignal = signal<Game>(this.data.game);

  protected readonly mobile: boolean = this.data.mobile;
  protected readonly cardSize: CardSize = this.mobile ? CardSize.MOBILE_SMALL : CardSize.DESKTOP_SMALL;

  protected readonly pots: Signal<ShowdownPotView[]> = computed(() => {
    const game = this.gameSignal();
    if (!game.pot) {
      return [];
    }

    const playersByIndex = new Map<number, Player>(game.players.map((player) => [player.index, player]));

    const result: ShowdownPotView[] = [
      this.toPotView('Main pot', game.pot.mainPot.chips, game.pot.mainPot.winnerIndexes, true, playersByIndex)
    ];

    game.pot.sidePots.forEach((sidePot, index) => {
      result.push(this.toPotView(`Side pot ${index + 1}`, sidePot.chips, sidePot.winnerIndexes, false, playersByIndex));
    });

    return result;
  });

  updateGame(game: Game): void {
    this.gameSignal.set(game);
  }

  protected readyForNextRound(): void {
    const game = this.gameSignal();
    this.apiRestService.sendReady(game.gameId, game.myId, true).subscribe(() => {
      this.dialogRef.close('ready');
    });
  }

  private toPotView(label: string, chips: number, winnerIndexes: number[], main: boolean,
                    playersByIndex: Map<number, Player>): ShowdownPotView {
    const uniqueWinnerIndexes = [...new Set(winnerIndexes)];
    const split = this.distributeChips(chips, uniqueWinnerIndexes.length);

    return {
      label,
      chips,
      main,
      winners: uniqueWinnerIndexes.map((winnerIndex, winnerPosition) => {
        const player = playersByIndex.get(winnerIndex);
        return {
          index: winnerIndex,
          name: player?.name ?? `Player #${winnerIndex}`,
          cards: [...(player?.cards ?? [])].sort(compareCards),
          hand: player?.handType ? translateHandType(player.handType) : undefined,
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
}
