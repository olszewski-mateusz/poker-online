import {ChangeDetectionStrategy, Component, effect, inject, Signal} from '@angular/core';
import {CardComponent} from "./card/card.component";
import {ActivatedRoute} from '@angular/router';
import {Game} from '../../model';
import {toSignal} from '@angular/core/rxjs-interop';
import {map, mergeMap} from 'rxjs';
import {GameConnectorService} from '../../services/game-connector.service';
import {MatButton} from '@angular/material/button';
import {MatList, MatListItem, MatListModule} from '@angular/material/list';
import {ToolbarComponent} from './toolbar/toolbar.component';
import {ActionsComponent} from './actions/actions.component';
import {HandComponent} from './hand/hand.component';
import {HistoryComponent} from './history/history.component';
import {GameStateComponent} from './game-state/game-state.component';
import {PlayerSelectionComponent} from './player-selection/player-selection.component';
import {PlayerPropertiesComponent} from './player-properties/player-properties.component';

@Component({
  selector: 'app-game-page',
  standalone: true,
  imports: [
    CardComponent,
    MatButton,
    MatList,
    MatListItem,
    MatListModule,
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

  game: Signal<Game | undefined> = toSignal(this.route.params.pipe(
    map(value => value['gameId']),
    mergeMap(gameId => this.gameService.connectToGameStream(gameId))
  ));

  constructor() {
    effect(() => {
      const game = this.game();
      console.log(game)
    });
  }
}
