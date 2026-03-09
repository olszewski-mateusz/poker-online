import {ChangeDetectionStrategy, Component, input, InputSignal} from '@angular/core';
import {Game} from '../../model';
import {
  ActionsComponent,
  GameStateComponent,
  HandBetterComponent,
  HistoryComponent,
  PlayersCarouselComponent,
  ToolbarComponent
} from '../../components/game';

@Component({
  selector: 'poker-game-mobile-layout',
  standalone: true,
  imports: [
    ToolbarComponent,
    ActionsComponent,
    GameStateComponent,
    HandBetterComponent,
    HistoryComponent,
    PlayersCarouselComponent
  ],
  templateUrl: './game-mobile-layout.component.html',
  styleUrl: './game-mobile-layout.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GameMobileLayoutComponent {
  game: InputSignal<Game> = input.required<Game>();
}

