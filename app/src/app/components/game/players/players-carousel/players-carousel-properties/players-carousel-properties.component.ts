import {ChangeDetectionStrategy, Component, input, InputSignal} from '@angular/core';
import {MatIcon} from '@angular/material/icon';
import {Game, GamePhase, Player} from '../../../../../model';

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
    return this.game().phase === GamePhase.SHOWDOWN && this.game().winnerIndexes.includes(player.index);
  }
}
