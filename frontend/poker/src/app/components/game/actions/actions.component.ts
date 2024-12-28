import {ChangeDetectionStrategy, Component, computed, inject, input, InputSignal, Signal} from '@angular/core';
import {buildMyPlayerSignal, buildMyTurnSignal, Card, Game, GamePhase, Player} from '../../../model';
import {ApiRestService} from '../../../services';
import {ReplaceCardsService} from './replace-cards.service';
import {ChipsAmountSetterComponent} from './betting-actions/chips-amount-setter/chips-amount-setter.component';
import {BettingActionsComponent} from './betting-actions/betting-actions.component';
import {MatButton} from '@angular/material/button';

@Component({
  selector: 'app-actions',
  standalone: true,
  imports: [
    ChipsAmountSetterComponent,
    BettingActionsComponent,
    MatButton
  ],
  templateUrl: './actions.component.html',
  styleUrl: './actions.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ActionsComponent {
  game: InputSignal<Game> = input.required<Game>();

  private readonly apiRestService: ApiRestService = inject(ApiRestService);
  private readonly replaceCardsService: ReplaceCardsService = inject(ReplaceCardsService);

  protected readonly myPlayer: Signal<Player | undefined> = buildMyPlayerSignal(this.game);

  protected readonly myTurn: Signal<boolean> = buildMyTurnSignal(this.game);

  protected readonly cardsToReplaceCount: Signal<number> = computed(() => {
    return this.replaceCardsService.cardsToReplace().length;
  });

  sendReady(): void {
    const game: Game = this.game();
    const myPlayer = this.myPlayer();
    if (myPlayer) {
      this.apiRestService.sendReady(game.gameId, game.myId, !myPlayer.ready).subscribe();
    }
  }

  sendReplace(): void {
    const game: Game = this.game();
    const cardsToReplace: Card[] = this.replaceCardsService.cardsToReplace();
    this.apiRestService.sendReplace(game.gameId, game.myId, cardsToReplace).subscribe();
    this.replaceCardsService.clearSelection();
  }

  actionsForGameNotStarted(): boolean {
    return this.game().phase === GamePhase.NOT_STARTED;
  }

  actionsForBetting(): boolean {
    return this.game().phase === GamePhase.FIRST_BETTING || this.game().phase === GamePhase.SECOND_BETTING;
  }

  actionsForDrawing(): boolean {
    return this.game().phase === GamePhase.DRAWING;
  }

  actionsForShowdown(): boolean {
    return this.game().phase === GamePhase.SHOWDOWN;
  }
}
