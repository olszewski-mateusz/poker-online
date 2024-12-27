import {ChangeDetectionStrategy, Component, computed, inject, input, InputSignal, Signal} from '@angular/core';
import {MatToolbar} from '@angular/material/toolbar';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {Game, Player} from '../../../model';
import {Router} from '@angular/router';

@Component({
  selector: 'app-toolbar',
  standalone: true,
  imports: [
    MatToolbar,
    MatIconButton,
    MatIcon,
    MatButton
  ],
  templateUrl: './toolbar.component.html',
  styleUrl: './toolbar.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ToolbarComponent {
  game: InputSignal<Game> = input.required<Game>();

  router: Router = inject(Router);

  myPlayer: Signal<Player | undefined> = computed(() => {
    const game: Game = this.game();
    return game.players.find(value => value.id === game.myId);
  });

  leave() {
    this.router.navigate(['start']);
  }
}
