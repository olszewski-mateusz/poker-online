import {ChangeDetectionStrategy, Component, computed, inject, input, InputSignal, Signal} from '@angular/core';
import {Game, Player} from '../../../model/game';
import {PlayerSelectionService} from '../../../services/player-selection.service';
import {MatIcon} from '@angular/material/icon';

@Component({
  selector: 'app-player-properties',
  standalone: true,
  imports: [
    MatIcon
  ],
  templateUrl: './player-properties.component.html',
  styleUrl: './player-properties.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PlayerPropertiesComponent {
  game: InputSignal<Game> = input.required<Game>()
  private readonly playerSelectionService: PlayerSelectionService = inject(PlayerSelectionService);

  protected selectedPlayer: Signal<Player | undefined> = computed(() => {
    const selectedPlayerId: string | undefined = this.playerSelectionService.selectedPlayerId();
    return this.game().players.find(player => player.id === selectedPlayerId);
  })

}
