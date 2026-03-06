import {ChangeDetectionStrategy, Component, computed, input, InputSignal} from '@angular/core';
import {
  ActionsComponent,
  GameStateComponent,
  HandBetterComponent,
  HistoryComponent, PlayerPropertiesComponent, PlayersCarouselComponent,
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
    HandBetterComponent,
    PlayerSelectionComponent,
    PlayerPropertiesComponent,
    ActionsComponent,
    PlayersCarouselComponent
  ],
  templateUrl: './game-desktop-layout.component.html',
  styleUrl: './game-desktop-layout.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GameDesktopLayoutComponent {
  game: InputSignal<Game> = input.required<Game>();
}

