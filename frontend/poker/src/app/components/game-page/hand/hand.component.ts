import {ChangeDetectionStrategy, Component, computed, inject, input, InputSignal, Signal} from '@angular/core';
import {Card, Game, GameState, Player} from '../../../model/game';
import {CardComponent, CardSize} from '../card/card.component';
import {ReplaceCardsService} from '../../../services/replace-cards.service';

@Component({
  selector: 'app-hand',
  standalone: true,
  imports: [
    CardComponent
  ],
  templateUrl: './hand.component.html',
  styleUrl: './hand.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HandComponent {

  protected readonly CardSize = CardSize;

  private replaceCardService: ReplaceCardsService = inject(ReplaceCardsService);

  game: InputSignal<Game> = input.required<Game>();

  myPlayer: Signal<Player | undefined> = computed(() => {
    const game: Game = this.game();
    return game.players.find(value => value.id === game.myId);
  });
  isCardInteractive = computed(() => {
    const game: Game = this.game();
    return game.state === GameState.DRAWING && game.myId === game.currentPlayerId;
  })


  cardClicked(card: Card): void {
    this.replaceCardService.toggleCard(card);
  }

  isCardSelected(card: Card): boolean {
    return !!this.replaceCardService.cardsToReplace().find(c => c.rank === card.rank && c.suit === card.suit);
  }
}
