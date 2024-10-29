import {ChangeDetectionStrategy, Component, computed, input, InputSignal} from '@angular/core';
import {Game} from '../../../model/game';
import {CardComponent, CardSize} from '../card/card.component';

@Component({
  selector: 'app-hand',
  standalone: true,
  imports: [
    CardComponent
  ],
  templateUrl: './hand.component.html',
  styleUrl: './hand.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HandComponent {
  game: InputSignal<Game> = input.required<Game>();

  myPlayer = computed(() => {
    const game: Game = this.game();
    return game.players.find(value => value.id === game.myId);
  });
  protected readonly CardSize = CardSize;
}
