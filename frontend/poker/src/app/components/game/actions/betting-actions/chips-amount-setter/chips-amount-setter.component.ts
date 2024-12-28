import {
  ChangeDetectionStrategy,
  Component, computed, effect, inject,
  input,
  InputSignal,
  model,
  ModelSignal,
  output,
  OutputEmitterRef, Signal
} from '@angular/core';
import {MatButton} from '@angular/material/button';
import {MatSlider, MatSliderThumb} from '@angular/material/slider';
import {buildMyPlayerSignal, buildMyTurnSignal, Game, Player} from '../../../../../model';
import {ApiRestService} from '../../../../../services';

@Component({
  selector: 'poker-game-chips-amount-setter',
  standalone: true,
  imports: [
    MatButton,
    MatSlider,
    MatSliderThumb
  ],
  templateUrl: './chips-amount-setter.component.html',
  styleUrl: './chips-amount-setter.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ChipsAmountSetterComponent {
  private readonly apiRestService: ApiRestService = inject(ApiRestService);

  game: InputSignal<Game> = input.required<Game>();
  cancelRequest: OutputEmitterRef<void> = output<void>();

  protected amount: ModelSignal<number> = model<number>(0);
  protected myTurn: Signal<boolean> = buildMyTurnSignal(this.game);
  protected myPlayer: Signal<Player | undefined> = buildMyPlayerSignal(this.game);

  protected readonly betPlaced: Signal<boolean> = computed(() => {
    const game: Game = this.game();
    return game.betPlacedInCurrentPhase;
  });

  protected readonly minBet: Signal<number> = computed(() => {
    return 2 * this.game().players.map(p => p.bet)
      .reduce((a, b) => Math.max(a, b), 0);
  });

  protected readonly maxBet: Signal<number> = computed(() => {
    return this.myPlayer()?.money ?? this.minBet();
  });

  protected readonly raiseIllegal: Signal<boolean> = computed(() => {
    return this.maxBet() <= this.minBet();
  });

  constructor() {
    effect(() => {
      const minBet: number = this.minBet();
      const amount: number = this.amount();
      if (amount < minBet) {
        this.amount.set(minBet);
      }
    }, {allowSignalWrites: true});
  }

  addToAmount(value: number): void {
    const min: number = this.minBet();
    const max: number = this.maxBet();
    let amount: number = this.amount() + value;
    amount = Math.max(min, (Math.min(max, amount)));
    this.amount.set(amount);
  }

  sendBetOrRaise(): void {
    const game: Game = this.game();
    this.apiRestService.sendBetOrRaise(game.gameId, game.myId, this.amount()).subscribe();
    this.cancelRequest.emit();
  }

  sendAllIn(): void {
    const game: Game = this.game();
    this.apiRestService.sendAllIn(game.gameId, game.myId).subscribe();
    this.cancelRequest.emit();
  }
}
