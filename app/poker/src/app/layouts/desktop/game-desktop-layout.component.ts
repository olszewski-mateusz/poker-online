import {ChangeDetectionStrategy, Component, input, InputSignal} from '@angular/core';
import {
  ActionsComponent,
  GameStateComponent,
  HandComponent,
  HistoryComponent, PlayerPropertiesComponent,
  PlayerSelectionComponent,
  ToolbarComponent
} from '../../components/game';
import {Game} from '../../model';

@Component({
  selector: 'poker-game-desktop-layout',
  standalone: true,
  imports: [
    ToolbarComponent,
    HistoryComponent,
    GameStateComponent,
    HandComponent,
    PlayerSelectionComponent,
    PlayerPropertiesComponent,
    ActionsComponent
  ],
  templateUrl: './game-desktop-layout.component.html',
  styleUrl: './game-desktop-layout.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GameDesktopLayoutComponent {
  game: InputSignal<Game> = input.required<Game>();
}
