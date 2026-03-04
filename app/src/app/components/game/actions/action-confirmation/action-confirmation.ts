import {ChangeDetectionStrategy, Component, inject, input, InputSignal, output, OutputEmitterRef} from '@angular/core';
import {MatButton} from '@angular/material/button';
import {MatSlider, MatSliderThumb} from '@angular/material/slider';
import {ActionType, Game} from '../../../../model';
import {ApiRestService} from '../../../../services';
import {ActionConfirmationContext} from '../actions.component';

@Component({
  selector: 'poker-game-action-confirmation',
  standalone: true,
  imports: [
    MatButton,
    MatSlider,
    MatSliderThumb
  ],
  templateUrl: './action-confirmation.html',
  styleUrl: './action-confirmation.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ActionConfirmation {
  private readonly apiRestService: ApiRestService = inject(ApiRestService);

  game: InputSignal<Game> = input.required<Game>();
  mobile: InputSignal<boolean> = input.required<boolean>();
  context: InputSignal<ActionConfirmationContext> = input.required<ActionConfirmationContext>();
  cancelRequest: OutputEmitterRef<void> = output<void>();

  sendAction(): void {
    const game: Game = this.game();
    const actionType: ActionType = this.context().type;

    if (actionType === ActionType.ALL_IN) {
      this.apiRestService.sendAllIn(game.gameId, game.myId);
      this.cancelRequest.emit();
    } else {
      console.error("Illegal action type " + actionType);
    }
  }
}
