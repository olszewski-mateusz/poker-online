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
  action: InputSignal<ValidAction> = input.required<ValidAction>();
  cancelRequest: OutputEmitterRef<void> = output<void>();

  protected amount: ModelSignal<number> = model<number>(0);

  protected actionLabel = computed(() => {
    switch (this.action().type) {
      case ActionType.BET:
        return "Bet";
      case ActionType.RAISE:
        return "Raise";
      default:
        return "Unknown";
    }
  })

  protected readonly minValue = computed(() => {
    return +this.action().properties["min"];
  });

  protected readonly maxValue = computed(() => {
    return +this.action().properties["max"];
  });

  protected readonly actionIllegal = computed(() => {
    return this.maxValue() < this.minValue();
  });

  constructor() {
    effect(() => {
      const minValue: number = this.minValue();
      const maxValue: number = this.maxValue();
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
    const min: number = this.minValue();
    const max: number = this.maxValue();
    let amount: number = this.amount() + value;
    amount = Math.max(min, (Math.min(max, amount)));
    this.amount.set(amount);
  }

  sendAction(): void {
    const game: Game = this.game();
    const actionType: ActionType = this.action().type;
    switch (actionType) {
      case ActionType.BET:
        this.apiRestService.sendBet(game.gameId, game.myId, this.amount()).subscribe();
        this.cancelRequest.emit();
        break
      case ActionType.RAISE:
        this.apiRestService.sendRaise(game.gameId, game.myId, this.amount()).subscribe();
        this.cancelRequest.emit();
        break;
      default:
        console.error("Illegal action type " + actionType);
    }
  }
}
