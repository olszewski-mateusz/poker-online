import {ChangeDetectionStrategy, Component, computed, inject, input, InputSignal, Signal} from '@angular/core';
import {Card, compareCards, Game, GamePhase, Player, Rank} from '../../../../model';
import {PlayerSelectionService} from '../player-selection.service';
import {MatIcon} from '@angular/material/icon';
import {CardComponent, CardSize} from "../../hand";

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
  protected readonly CardSize = CardSize;

  game: InputSignal<Game> = input.required<Game>()

  private readonly playerSelectionService: PlayerSelectionService = inject(PlayerSelectionService);

  private readonly unknownCards: Card[] = [
    <Card>{rank: Rank.TWO},
    <Card>{rank: Rank.THREE},
    <Card>{rank: Rank.FOUR},
    <Card>{rank: Rank.FIVE},
    <Card>{rank: Rank.SIX}
  ];

  protected selectedPlayer: Signal<Player | undefined> = computed(() => {
    const game: Game = this.game();
    let selectedPlayerId: string | undefined = this.playerSelectionService.selectedPlayerId();
    if (!selectedPlayerId) {
      if (game.phase === GamePhase.SHOWDOWN) {
        selectedPlayerId = game.winnerId;
      } else {
        selectedPlayerId = game.currentPlayerId;
      }
    }
    return game.players.find(player => player.id === selectedPlayerId);
  })

  protected selectedPlayerCards: Signal<Card[]> = computed(() => {
    return this.selectedPlayer()?.cards?.sort(compareCards) ?? this.unknownCards;
  });
}
