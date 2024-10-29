import {ChangeDetectionStrategy, Component, inject, input, InputSignal} from '@angular/core';
import {Game} from '../../../model/game';
import {MatButton} from '@angular/material/button';
import {ApiService} from '../../../services/api.service';

@Component({
  selector: 'app-actions',
  standalone: true,
  imports: [
    MatButton
  ],
  templateUrl: './actions.component.html',
  styleUrl: './actions.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ActionsComponent {
  game: InputSignal<Game> = input.required<Game>();

  private readonly apiService: ApiService = inject(ApiService);

  sendReady(game: Game) {
    this.apiService.readyAction(game.gameId, game.myId).subscribe();
  }
}
