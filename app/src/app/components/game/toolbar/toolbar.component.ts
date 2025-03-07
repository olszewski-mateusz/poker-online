import {ChangeDetectionStrategy, Component, inject, input, InputSignal, Signal} from '@angular/core';
import {MatToolbar} from '@angular/material/toolbar';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {buildMyPlayerSignal, Game, Player} from '../../../model';
import {RouterService} from '../../../services/router.service';

@Component({
  selector: 'poker-game-toolbar',
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
  private routerService: RouterService = inject(RouterService);

  game: InputSignal<Game> = input.required<Game>();
  mobile: InputSignal<boolean> = input.required<boolean>();
  myPlayer: Signal<Player | undefined> = buildMyPlayerSignal(this.game);

  leave(): void {
    this.routerService.navigateToStartPage().subscribe();
  }
}
