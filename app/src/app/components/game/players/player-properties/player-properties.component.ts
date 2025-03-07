import {ChangeDetectionStrategy, Component, computed, inject, input, InputSignal, Signal} from '@angular/core';
import {Card, compareCards, Game, GamePhase, Player, Rank} from '../../../../model';
import {PlayerSelectionService} from '../player-selection.service';
import {MatIcon} from '@angular/material/icon';
import {CardComponent, CardSize} from "../../hand";

@Component({
  selector: 'poker-game-player-properties',
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
  private readonly playerSelectionService: PlayerSelectionService = inject(PlayerSelectionService);
  private readonly unknownCards: Card[] = [
    <Card>{rank: Rank.TWO},
    <Card>{rank: Rank.THREE},
    <Card>{rank: Rank.FOUR},
    <Card>{rank: Rank.FIVE},
    <Card>{rank: Rank.SIX}
  ];

  game: InputSignal<Game> = input.required<Game>()
  mobile: InputSignal<boolean> = input.required<boolean>();

  protected cardSize: Signal<CardSize> = computed(() => {
    return this.mobile() ? CardSize.MOBILE_SMALL : CardSize.DESKTOP_SMALL;
  });

  protected selectedPlayer: Signal<Player | undefined> = computed(() => {
    const game: Game = this.game();
    let selectedPlayerIndex: number | undefined = this.playerSelectionService.selectedPlayerIndex();
    if (!selectedPlayerIndex) {
      if (game.phase === GamePhase.SHOWDOWN) {
        selectedPlayerIndex = game.winnerIndex;
      } else {
        selectedPlayerIndex = game.currentPlayerIndex;
      }
    }
    return game.players.find(player => player.index === selectedPlayerIndex);
  })

  protected selectedPlayerCards: Signal<Card[]> = computed(() => {
    return this.selectedPlayer()?.cards?.sort(compareCards) ?? this.unknownCards;
  });
}
