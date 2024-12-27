import {ChangeDetectionStrategy, Component, inject, Signal} from '@angular/core';
import {
  ActionsComponent,
  GameStateComponent,
  HandComponent,
  HistoryComponent,
  PlayerPropertiesComponent,
  PlayerSelectionComponent,
  ToolbarComponent
} from "../../components/game";
import {ActivatedRoute} from '@angular/router';
import {Game} from '../../model';
import {toSignal} from '@angular/core/rxjs-interop';
import {map, mergeMap, tap} from 'rxjs';
import {GameConnectorService} from '../../services';

@Component({
  selector: 'app-game-page',
  standalone: true,
  imports: [
    ToolbarComponent,
    ActionsComponent,
    HandComponent,
    HistoryComponent,
    GameStateComponent,
    PlayerSelectionComponent,
    PlayerPropertiesComponent
  ],
  templateUrl: './game-page.component.html',
  styleUrl: './game-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GamePageComponent {
  private readonly route: ActivatedRoute = inject(ActivatedRoute);
  private readonly gameService: GameConnectorService = inject(GameConnectorService);

  protected readonly game: Signal<Game | undefined> = toSignal(this.route.params.pipe(
    map(value => value['gameId']),
    mergeMap(gameId => this.gameService.subscribeToGameChanges(gameId)),
    tap(game => console.log(game))
  ));
}
