import {ChangeDetectionStrategy, Component, input, InputSignal} from '@angular/core';
import {Game} from '../../model';
import {
  ActionsComponent, GameStateComponent, HandComponent,
  PlayerPropertiesComponent,
  PlayerSelectionComponent,
  ToolbarComponent
} from '../../components/game';

@Component({
  selector: 'poker-game-mobile-layout',
  standalone: true,
  imports: [
    ToolbarComponent,
    ActionsComponent,
    PlayerPropertiesComponent,
    PlayerSelectionComponent,
    GameStateComponent,
    HandComponent
  ],
  templateUrl: './game-mobile-layout.component.html',
  styleUrl: './game-mobile-layout.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GameMobileLayoutComponent {
  game: InputSignal<Game> = input.required<Game>();
  mobile: InputSignal<boolean> = input.required<boolean>();
}
