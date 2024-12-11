import {
  ChangeDetectionStrategy,
  Component,
  effect,
  inject,
  input,
  InputSignal, Signal,
  signal,
  viewChild,
  WritableSignal
} from '@angular/core';
import {Game, Player} from '../../../model/game';
import {MatList, MatListItem, MatListItemTitle, MatListOption, MatSelectionList} from '@angular/material/list';
import {PlayerSelectionService} from '../../../services/player-selection.service';

@Component({
  selector: 'app-player-selection',
  standalone: true,
  imports: [
    MatList,
    MatListItem,
    MatListItemTitle,
    MatSelectionList,
    MatListOption
  ],
  templateUrl: './player-selection.component.html',
  styleUrl: './player-selection.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PlayerSelectionComponent {

  game: InputSignal<Game> = input.required<Game>();
  private readonly playerSelectionService: PlayerSelectionService = inject(PlayerSelectionService);

  protected selectedPlayerId: Signal<string|undefined> = this.playerSelectionService.selectedPlayerId;

  constructor() {
    effect(() => {
      const game: Game = this.game();
      const currentSelection: string|undefined = this.selectedPlayerId();
      if (!currentSelection && game.currentPlayerId) {
        this.playerSelectionService.setSelectedPlayer(game.currentPlayerId);
      }
    }, {allowSignalWrites: true});
  }

  onPlayerChange(player: Player): void {
    this.playerSelectionService.setSelectedPlayer(player.id);
  }
}
