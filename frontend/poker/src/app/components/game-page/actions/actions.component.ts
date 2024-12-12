import {
  ChangeDetectionStrategy,
  Component,
  computed, effect,
  inject,
  input,
  InputSignal, model, ModelSignal,
  signal,
  Signal,
  WritableSignal
} from '@angular/core';
import {Game, GameState, Player} from '../../../model/game';
import {MatButton} from '@angular/material/button';
import {ApiService} from '../../../services/api.service';
import {MatSlider, MatSliderThumb} from '@angular/material/slider';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';

@Component({
  selector: 'app-actions',
  standalone: true,
  imports: [
    MatButton,
    MatSlider,
    MatSliderThumb,
    MatFormField,
    MatInput,
    MatLabel
  ],
  templateUrl: './actions.component.html',
  styleUrl: './actions.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ActionsComponent {
  protected readonly GameState: typeof GameState = GameState;

  game: InputSignal<Game> = input.required<Game>();

  amount: ModelSignal<number> = model<number>(0);

  private readonly apiService: ApiService = inject(ApiService);

  protected readonly myPlayer: Signal<Player | undefined> = computed(() => {
    const game: Game = this.game();
    return game.players.find(value => value.id === game.myId);
  });

  protected readonly myTurn: Signal<boolean> = computed(() => {
    const game: Game = this.game();
    return game.currentPlayerId === game.myId;
  })

  protected readonly raiseSelected: WritableSignal<boolean> = signal<boolean>(false);

  protected readonly isBet: Signal<boolean> = computed(() => {
    const game: Game = this.game();
    return game.players.some(p => {
      return p.bet > game.configuration.ante;
    })
  })

  protected readonly minBet: Signal<number> = computed(() => {
    return 2 * this.game().players.map(p => p.bet)
      .reduce((a, b) => Math.max(a, b), 0);
  })

  protected readonly maxBet: Signal<number> = computed(() => {
    return this.myPlayer()?.money ?? this.minBet();
  })

  constructor() {
    effect(() => {
      const minBet: number = this.minBet();
      this.amount.update(value => Math.max(minBet, value));
    }, {allowSignalWrites: true});
  }

  sendReady(): void {
    const game: Game = this.game();
    const myPlayer = this.myPlayer();
    if (myPlayer) {
      this.apiService.sendReady(game.gameId, game.myId, !myPlayer.ready).subscribe();
    }
  }

  sendCheckOrCall(): void {
    const game: Game = this.game();
    this.apiService.sendCheckOrCall(game.gameId, game.myId).subscribe();
  }

  sendBetOrRaise(): void {
    const game: Game = this.game();
    this.apiService.sendBetOrRaise(game.gameId, game.myId, this.amount()).subscribe();
    this.raiseSelected.set(false);
  }

  sendAllIn(): void {
    const game: Game = this.game();
    this.apiService.sendAllIn(game.gameId, game.myId).subscribe();
    this.raiseSelected.set(false);
  }

  sendFold(): void {
    const game: Game = this.game();
    this.apiService.sendFold(game.gameId, game.myId).subscribe();
  }
}
