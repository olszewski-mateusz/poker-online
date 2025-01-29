import {ChangeDetectionStrategy, Component, computed, input, InputSignal, Signal} from '@angular/core';
import {Game, GamePhase, Player} from '../../../model';
import {MatIcon} from '@angular/material/icon';

@Component({
  selector: 'poker-game-state',
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
  mobile: InputSignal<boolean> = input.required<boolean>();

  currentPlayer: Signal<Player|undefined> = computed(() => {
    return this.game().players.find(p => p.index === this.game().currentPlayerIndex);
  });

  roundReward: Signal<number> = computed(() => {
    return this.game().players
      .map(p => p.bet ?? 0)
      .reduce((previousValue, currentValue) => {
        return previousValue + currentValue;
      }, 0)
  });

  translatedGamePhase: Signal<string> = computed(() => {
    return this.translateGamePhase(this.game().phase);
  });

  currentBet: Signal<number> = computed(() => {
    return this.game().players
      .map(p => p.bet ?? 0)
      .reduce((previousValue, currentValue) => {
        return Math.max(previousValue, currentValue);
      }, 0)
  })

  private translateGamePhase(phase: GamePhase): string {
    switch (phase) {
      case GamePhase.NOT_STARTED: return 'Game not started';
      case GamePhase.FIRST_BETTING: return 'First betting phase';
      case GamePhase.DRAWING: return 'Drawing phase';
      case GamePhase.SECOND_BETTING: return 'Second betting phase';
      case GamePhase.SHOWDOWN: return 'Showdown phase';
      case GamePhase.FINISHED: return 'Game finished';
    }
  }
}
