import {
  ChangeDetectionStrategy,
  Component, computed, effect, inject,
  input,
  InputSignal,
  model,
  ModelSignal,
  output,
  OutputEmitterRef
} from '@angular/core';
import {MatButton} from '@angular/material/button';
import {MatSlider, MatSliderThumb} from '@angular/material/slider';
import {ActionType, Game, ValidAction} from '../../../../model';
import {ApiRestService} from '../../../../services';
import {ChipsAmountSetterContext} from '../actions.component';

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
  mobile: InputSignal<boolean> = input.required<boolean>();
  context: InputSignal<ChipsAmountSetterContext> = input.required<ChipsAmountSetterContext>();
  cancelRequest: OutputEmitterRef<void> = output<void>();

  protected amount: ModelSignal<number> = model<number>(0);

  protected readonly actionIllegal = computed(() => {
    return this.context().minValue < this.context().maxValue;
  });

  constructor() {
    effect(() => {
      const minValue: number = this.context().minValue;
      const maxValue: number = this.context().maxValue;
      const amount: number = this.amount();
      if (amount < minValue) {
        this.amount.set(minValue);
      }
      if (amount > maxValue) {
        this.amount.set(maxValue);
      }
    }, {allowSignalWrites: true});
  }

  addToAmount(value: number): void {
    const min: number = this.context().minValue;
    const max: number = this.context().maxValue;
    let amount: number = this.amount() + value;
    amount = Math.max(min, (Math.min(max, amount)));
    this.amount.set(amount);
  }

  sendAction(): void {
    const game: Game = this.game();
    const actionType: ActionType = this.context().type;
    switch (actionType) {
      case ActionType.BET:
        this.apiRestService.sendBet(game.gameId, game.myId, this.amount()).subscribe();
        this.cancelRequest.emit();
        break
      case ActionType.RAISE:
        this.apiRestService.sendRaise(game.gameId, game.myId, this.amount()).subscribe();
        this.cancelRequest.emit();
        break;
    }
  }
}
