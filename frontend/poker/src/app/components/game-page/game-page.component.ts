import {ChangeDetectionStrategy, Component, effect, inject, OnInit, Signal} from '@angular/core';
import {CardComponent, CardSize} from "./card/card.component";
import {Suit} from '../../model/suit';
import {Rank} from '../../model/rank';
import {ActivatedRoute} from '@angular/router';
import {Game} from '../../model/game';
import {toSignal} from '@angular/core/rxjs-interop';
import {map, mergeMap} from 'rxjs';
import {ApiService} from '../../services/api.service';
import {GameService} from '../../services/game.service';
import {MatButton} from '@angular/material/button';

@Component({
  selector: 'app-game-page',
  standalone: true,
  imports: [
    CardComponent,
    MatButton
  ],
  templateUrl: './game-page.component.html',
  styleUrl: './game-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GamePageComponent {
  protected readonly Object = Object;
  protected readonly Suit = Suit;
  protected readonly Rank = Rank;
  protected readonly CardSize = CardSize;

  private readonly route: ActivatedRoute = inject(ActivatedRoute);
  private readonly gameService: GameService = inject(GameService);
  private readonly apiService: ApiService = inject(ApiService);

  game: Signal<Game | undefined> = toSignal(this.route.params.pipe(
    map(value => value['gameId']),
    mergeMap(value => this.gameService.getGame(value))
  ));

  constructor() {
    this.route.params.subscribe(value => console.log(value))

    effect(() => {
      const gama = this.game();
      console.log(gama)
    });
  }

  sendReady(game: Game) {
    this.apiService.readyAction(game.gameId, game.myId).subscribe();
  }
}
