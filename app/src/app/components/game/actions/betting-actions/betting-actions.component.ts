import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  input,
  InputSignal, signal,
  Signal,
  WritableSignal
} from '@angular/core';
import {MatButton} from '@angular/material/button';
import {buildMyPlayerSignal, buildMyTurnSignal, Game, Player} from '../../../../model';
import {ApiRestService} from '../../../../services';
import {ChipsAmountSetterComponent} from './chips-amount-setter/chips-amount-setter.component';

@Component({
  selector: 'poker-game-betting-actions',
  standalone: true,
  imports: [
    MatButton,
    ChipsAmountSetterComponent
  ],
  templateUrl: './betting-actions.component.html',
  styleUrl: './betting-actions.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BettingActionsComponent {
  private readonly apiRestService: ApiRestService = inject(ApiRestService);

  game: InputSignal<Game> = input.required<Game>();
  mobile: InputSignal<boolean> = input.required<boolean>();

  protected readonly raisePanelActive: WritableSignal<boolean> = signal<boolean>(false);

  protected readonly myPlayer: Signal<Player | undefined> = buildMyPlayerSignal(this.game);

  protected readonly myTurn: Signal<boolean> = buildMyTurnSignal(this.game);

  protected readonly betPlaced: Signal<boolean> = computed(() => {
    const game: Game = this.game();
    return game.betPlacedInCurrentPhase;
  });

  protected readonly callIllegal: Signal<boolean> = computed(() => {
    const currentBet: number = this.game().players
      .map(p => p.bet ?? 0)
      .reduce((a, b) => {
        return Math.max(a, b);
      }, 0);

    return currentBet - (this.myPlayer()?.bet ?? 0) >= (this.myPlayer()?.money ?? 0);
  });

  sendCheckOrCall(): void {
    const game: Game = this.game();
    this.apiRestService.sendCheckOrCall(game.gameId, game.myId).subscribe();
  }

  sendAllIn(): void {
    const game: Game = this.game();
    this.apiRestService.sendAllIn(game.gameId, game.myId).subscribe();
    this.raisePanelActive.set(false);
  }

  sendFold(): void {
    const game: Game = this.game();
    this.apiRestService.sendFold(game.gameId, game.myId).subscribe();
  }

  cancelRaisePanel(): void {
    this.raisePanelActive.set(false)
  }

  activateRaisePanel(): void {
    this.raisePanelActive.set(true)
  }
}
