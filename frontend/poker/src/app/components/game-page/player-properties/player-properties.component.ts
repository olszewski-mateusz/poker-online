import {ChangeDetectionStrategy, Component, computed, inject, input, InputSignal, Signal} from '@angular/core';
import {Card, compareCards, Game, Player} from '../../../model';
import {PlayerSelectionService} from '../../../services/player-selection.service';
import {MatIcon} from '@angular/material/icon';
import {CardComponent, CardSize} from "../card/card.component";

@Component({
  selector: 'app-player-properties',
  standalone: true,
    imports: [
        MatIcon,
        CardComponent
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

  protected selectedPlayerCards: Signal<Card[]> = computed(() => {
    return this.selectedPlayer()?.cards?.sort(compareCards) ??
      Array(5).fill(<Card>{});
  })

  protected readonly CardSize = CardSize;
}
