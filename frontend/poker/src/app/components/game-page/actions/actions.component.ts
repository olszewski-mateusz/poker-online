import {
  ChangeDetectionStrategy,
  Component,
  computed, effect,
  inject,
  input,
  InputSignal, model, ModelSignal,
  signal,
  Signal,
  WritableSignal
} from '@angular/core';
import {Card, Game, GameState, Player} from '../../../model/game';
import {MatButton} from '@angular/material/button';
import {ApiRestService} from '../../../services/api/api-rest.service';
import {MatSlider, MatSliderThumb} from '@angular/material/slider';
import {MatFormField, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {PlayerSelectionService} from '../../../services/player-selection.service';
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
  protected readonly GameState: typeof GameState = GameState;

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

  protected readonly raiseSelected: WritableSignal<boolean> = signal<boolean>(false);

  protected readonly isBet: Signal<boolean> = computed(() => {
    const game: Game = this.game();
    return game.players.some(p => {
      return p.bet > game.configuration.ante;
    })
  });

  protected readonly minBet: Signal<number> = computed(() => {
    return 2 * this.game().players.map(p => p.bet)
      .reduce((a, b) => Math.max(a, b), 0);
  });

  protected readonly maxBet: Signal<number> = computed(() => {
    return this.myPlayer()?.money ?? this.minBet();
  });

  constructor() {
    effect(() => {
      const minBet: number = this.minBet();
      this.amount.update(value => Math.max(minBet, value));
    }, {allowSignalWrites: true});
  }

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
    this.raiseSelected.set(false);
  }

  sendAllIn(): void {
    const game: Game = this.game();
    this.apiRestService.sendAllIn(game.gameId, game.myId).subscribe();
    this.raiseSelected.set(false);
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
}
