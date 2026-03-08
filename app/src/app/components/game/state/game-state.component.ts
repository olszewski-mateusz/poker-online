import {ChangeDetectionStrategy, Component, computed, effect, input, InputSignal, Signal, signal} from '@angular/core';
import {Game, GamePhase, Player} from '../../../model';
import {MatIcon} from '@angular/material/icon';
import {MatButton} from '@angular/material/button';
import {animationFrames, map, takeWhile} from 'rxjs';

@Component({
  selector: 'poker-game-state',
  standalone: true,
  imports: [
    MatIcon,
    MatButton
  ],
  templateUrl: './game-state.component.html',
  styleUrl: './game-state.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class GameStateComponent {
  game: InputSignal<Game> = input.required<Game>();
  mobile: InputSignal<boolean> = input.required<boolean>();
  animatedPotReward = signal<number>(0);

  currentPlayer: Signal<Player|undefined> = computed(() => {
    return this.game().players.find(p => p.index === this.game().currentPlayerIndex);
  });

  potReward: Signal<number> = computed(() => {
    return this.game().players
      .map(p => p.bet ?? 0)
      .reduce((previousValue, currentValue) => {
        return previousValue + currentValue;
      }, 0)
  });

  translatedGamePhase: Signal<string> = computed(() => {
    return this.translateGamePhase(this.game().phase);
  });

  constructor() {
    effect((onCleanup) => {
      const targetValue = this.potReward();
      const startValue = this.animatedPotReward();

      if (targetValue === startValue) {
        return;
      }

      const diff = targetValue - startValue;
      const minDuration = 1000;
      const maxDuration = 2000;
      const multiplier = 10;

      const durationMs = Math.min(maxDuration, Math.max(minDuration, Math.abs(diff) * multiplier));
      const easeOut = (progress: number): number => 1 - Math.pow(1 - progress, 3);

      const subscription = animationFrames().pipe(
        map(({elapsed}) => Math.min(elapsed / durationMs, 1)),
        map((progress) => {
          const animatedValue = startValue + diff * easeOut(progress);
          return Math.round(animatedValue);
        }),
        takeWhile((value) => value !== targetValue, true)
      ).subscribe((value) => {
        this.animatedPotReward.set(value);
      });

      onCleanup(() => subscription.unsubscribe());
    }, {allowSignalWrites: true});
  }

  private translateGamePhase(phase: GamePhase): string {
    const currentPlayer = this.currentPlayer();
    const playerName = currentPlayer?.name ?? 'unknown';
    switch (phase) {
      case GamePhase.NOT_STARTED: return 'Game not started';
      case GamePhase.FIRST_BETTING: return `First Bet - ${playerName}`;
      case GamePhase.DRAWING: return `Draw - ${playerName}`;
      case GamePhase.SECOND_BETTING: return `Second Bet - ${playerName}`;
      case GamePhase.SHOWDOWN: return 'Showdown';
      case GamePhase.FOLDOUT: return 'Foldout';
      case GamePhase.FINISHED: return 'Game finished';
    }
  }
}
