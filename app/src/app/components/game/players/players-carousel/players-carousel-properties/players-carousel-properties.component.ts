import {ChangeDetectionStrategy, Component, input, InputSignal} from '@angular/core';
import {Game, GamePhase, Player} from '../../../../../model';
import {MatIcon} from '@angular/material/icon';

@Component({
  selector: 'poker-game-players-carousel-properties',
  standalone: true,
  templateUrl: './players-carousel-properties.component.html',
  styleUrl: './players-carousel-properties.component.scss',
  imports: [
    MatIcon
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PlayersCarouselPropertiesComponent {
  game: InputSignal<Game> = input.required<Game>();
  player: InputSignal<Player> = input.required<Player>();
  highlighted: InputSignal<boolean> = input.required<boolean>();
  vertical: InputSignal<boolean> = input.required<boolean>();

  showAllInIcon(player: Player): boolean {
    return !player.folded && player.chips === 0 &&
      (this.game().phase === GamePhase.FIRST_BETTING || this.game().phase === GamePhase.SECOND_BETTING);
  }

  showWinnerIcon(player: Player): boolean {
    return this.game().phase === GamePhase.SHOWDOWN && this.getWinnerIndexes(this.game()).includes(player.index);
  }

  private getWinnerIndexes(game: Game): number[] {
    const allWinners: number[] = [
      ...(game.pot?.mainPot?.winnerIndexes ?? []),
      ...((game.pot?.sidePots ?? []).flatMap((sidePot) => sidePot.winnerIndexes ?? []))
    ];
    return [...new Set(allWinners)];
  }
}
