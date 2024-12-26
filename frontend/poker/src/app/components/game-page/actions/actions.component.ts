import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  input,
  InputSignal,
  model,
  ModelSignal,
  signal,
  Signal,
  WritableSignal
} from '@angular/core';
import {Card, Game, GamePhase, Player} from '../../../model';
import {MatButton} from '@angular/material/button';
import {ApiRestService} from '../../../services/api/api-rest.service';
import {MatSlider, MatSliderThumb} from '@angular/material/slider';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {ReplaceCardsService} from '../../../services/replace-cards.service';

@Component({
  selector: 'app-actions',
  standalone: true,
  imports: [
    MatButton,
    MatSlider,
    MatSliderThumb,
    MatFormField,
    MatInput,
    MatLabel
  ],
  templateUrl: './actions.component.html',
  styleUrl: './actions.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ActionsComponent {
  protected readonly GameState: typeof GamePhase = GamePhase;

  game: InputSignal<Game> = input.required<Game>();

  amount: ModelSignal<number> = model<number>(0);

  private readonly apiRestService: ApiRestService = inject(ApiRestService);
  private readonly replaceCardsService: ReplaceCardsService = inject(ReplaceCardsService);

  protected readonly myPlayer: Signal<Player | undefined> = computed(() => {
    const game: Game = this.game();
    return game.players.find(value => value.id === game.myId);
  });

  protected readonly myTurn: Signal<boolean> = computed(() => {
    const game: Game = this.game();
    return game.currentPlayerId === game.myId;
  });

  protected readonly cardsToReplaceCount: Signal<number> = computed(() => {
    return this.replaceCardsService.cardsToReplace().length;
  });

  protected readonly raisePanelActive: WritableSignal<boolean> = signal<boolean>(false);

  protected readonly betPlacedInCurrentPhase: Signal<boolean> = computed(() => {
    const game: Game = this.game();
    return game.betPlacedInCurrentPhase;
  });

  protected readonly minBet: Signal<number> = computed(() => {
    return 2 * this.game().players.map(p => p.bet)
      .reduce((a, b) => Math.max(a, b), 0);
  });

  protected readonly maxBet: Signal<number> = computed(() => {
    return this.myPlayer()?.money ?? this.minBet();
  });

  protected readonly isRaiseIllegal: Signal<boolean> = computed(() => {
    return this.maxBet() <= this.minBet();
  });

  protected readonly isCallIllegal: Signal<boolean> = computed(() => {
    const currentBet: number = this.game().players
      .map(p => p.bet ?? 0)
      .reduce((a, b) => {
        return Math.max(a, b);
      }, 0);

    return currentBet - (this.myPlayer()?.bet ?? 0) >= (this.myPlayer()?.money ?? 0);
  });

  sendReady(): void {
    const game: Game = this.game();
    const myPlayer = this.myPlayer();
    if (myPlayer) {
      this.apiRestService.sendReady(game.gameId, game.myId, !myPlayer.ready).subscribe();
    }
  }

  sendCheckOrCall(): void {
    const game: Game = this.game();
    this.apiRestService.sendCheckOrCall(game.gameId, game.myId).subscribe();
  }

  sendBetOrRaise(): void {
    const game: Game = this.game();
    this.apiRestService.sendBetOrRaise(game.gameId, game.myId, this.amount()).subscribe();
    this.raisePanelActive.set(false);
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

  sendReplace(): void {
    const game: Game = this.game();
    const cardsToReplace: Card[] = this.replaceCardsService.cardsToReplace();
    this.apiRestService.sendReplace(game.gameId, game.myId, cardsToReplace).subscribe();
    this.replaceCardsService.clearSelection();
  }

  addToAmount(number: number) {
    const min: number = this.minBet();
    const max: number = this.maxBet();
    let amount: number = this.amount() + number;
    amount = Math.max(min, (Math.min(max, amount)));
    this.amount.set(amount);
  }

  cancelRaisePanel(): void {
    this.raisePanelActive.set(false)
  }

  activateRaisePanel(): void {
    this.amount.set(this.minBet());
    this.raisePanelActive.set(true)
  }
}
