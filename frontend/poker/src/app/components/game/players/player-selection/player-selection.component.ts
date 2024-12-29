import {ChangeDetectionStrategy, Component, inject, input, InputSignal} from '@angular/core';
import {Game, GamePhase, Player} from '../../../../model';
import {MatListItemTitle, MatListOption, MatSelectionList} from '@angular/material/list';
import {PlayerSelectionService} from '../player-selection.service';
import {MatIcon} from '@angular/material/icon';

@Component({
  selector: 'poker-game-player-selection',
  standalone: true,
  imports: [
    MatListItemTitle,
    MatSelectionList,
    MatListOption,
    MatIcon
  ],
  templateUrl: './player-selection.component.html',
  styleUrl: './player-selection.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PlayerSelectionComponent {

  private readonly playerSelectionService: PlayerSelectionService = inject(PlayerSelectionService);

  game: InputSignal<Game> = input.required<Game>();
  mobile: InputSignal<boolean> = input.required<boolean>();

  playerSelected(playerId: string): boolean {
    return playerId === this.playerSelectionService.selectedPlayerId();
  }

  onOptionClicked(player: Player): void {
    const alreadySelected: boolean = this.playerSelected(player.id);
    this.playerSelectionService.setSelectedPlayer(alreadySelected ? undefined : player.id);
  }

  showAllInIcon(player: Player): boolean {
    return !player.folded && player.money === 0 &&
      (this.game().phase === GamePhase.FIRST_BETTING || this.game().phase === GamePhase.SECOND_BETTING);
  }

  showWinnerIcon(player: Player): boolean {
    return this.game().phase === GamePhase.SHOWDOWN && player.id === this.game().winnerId;
  }
}
