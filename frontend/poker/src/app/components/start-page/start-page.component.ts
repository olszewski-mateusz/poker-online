import {ChangeDetectionStrategy, Component, inject, model} from '@angular/core';
import {MatButton} from '@angular/material/button';
import {MatFormField, MatFormFieldModule} from '@angular/material/form-field';
import {MatInput, MatInputModule} from '@angular/material/input';
import {MatIcon} from '@angular/material/icon';
import {Router} from '@angular/router';
import {FormsModule} from '@angular/forms';
import {GameService} from '../../services/game.service';

@Component({
  selector: 'app-start-page',
  standalone: true,
  imports: [
    MatButton,
    MatFormField,
    MatInput,
    MatFormFieldModule,
    MatInputModule,
    MatIcon,
    FormsModule
  ],
  templateUrl: './start-page.component.html',
  styleUrl: './start-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StartPageComponent {

  private readonly gameService: GameService = inject(GameService);
  private readonly router: Router = inject(Router);
  gameId = model<string|null>(null);

  createGame() {
    this.gameService.createAndJoinGame().subscribe();
  }

  joinGame() {
    const gameId: string|null = this.gameId();
    if (gameId) {
      this.gameService.joinGame(gameId).subscribe();
    }
  }
}
