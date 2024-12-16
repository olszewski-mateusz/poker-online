import {ChangeDetectionStrategy, Component, computed, input, InputSignal, signal, Signal} from '@angular/core';
import {Game, Player, translateGamePhase} from '../../../model';
import {MatIcon} from '@angular/material/icon';

@Component({
  selector: 'app-game-state',
  standalone: true,
  imports: [
    MatIcon
  ],
  templateUrl: './game-state.component.html',
  styleUrl: './game-state.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class GameStateComponent {
  game: InputSignal<Game> = input.required<Game>();

  currentPlayer: Signal<Player|undefined> = computed(() => {
    return this.game().players.find(p => p.id === this.game().currentPlayerId);
  });

  collectedBet: Signal<number> = computed(() => {
    return this.game().players
      .map(p => p.bet ?? 0)
      .reduce((previousValue, currentValue) => {
        return previousValue + currentValue;
      }, 0)
  });

  translatedGamePhase: Signal<string> = computed(() => {
    return translateGamePhase(this.game().phase);
  });

  currentBet: Signal<number> = computed(() => {
    return this.game().players
      .map(p => p.bet ?? 0)
      .reduce((previousValue, currentValue) => {
        return Math.max(previousValue, currentValue);
      }, 0)
  })
}
