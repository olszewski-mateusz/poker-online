import {ChangeDetectionStrategy, Component, computed, input, InputSignal, Signal} from '@angular/core';
import {MatToolbar} from '@angular/material/toolbar';
import {MatIconButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {Game, Player} from '../../../model';

@Component({
  selector: 'app-toolbar',
  standalone: true,
  imports: [
    MatToolbar,
    MatIconButton,
    MatIcon
  ],
  templateUrl: './toolbar.component.html',
  styleUrl: './toolbar.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ToolbarComponent {
  game: InputSignal<Game> = input.required<Game>();

  myPlayer: Signal<Player | undefined> = computed(() => {
    const game: Game = this.game();
    return game.players.find(value => value.id === game.myId);
  });
}
