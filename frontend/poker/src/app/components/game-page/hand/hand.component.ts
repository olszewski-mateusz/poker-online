import {ChangeDetectionStrategy, Component, computed, inject, input, InputSignal, Signal} from '@angular/core';
import {Card, compareCards, Game, GamePhase, Player} from '../../../model';
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

  private readonly replaceCardService: ReplaceCardsService = inject(ReplaceCardsService);

  game: InputSignal<Game> = input.required<Game>();

  myCards: Signal<Card[]> = computed(() => {
    const game: Game = this.game();
    return game.players.find(value => value.id === game.myId)?.cards?.sort(compareCards) ?? [];
  });

  isCardInteractive: Signal<boolean> = computed(() => {
    const game: Game = this.game();
    return game.phase === GamePhase.DRAWING && game.myId === game.currentPlayerId;
  });


  cardClicked(card: Card): void {
    this.replaceCardService.toggleCard(card);
  }

  isCardSelected(card: Card): boolean {
    return !!this.replaceCardService.cardsToReplace().find(c => c.rank === card.rank && c.suit === card.suit);
  }
}
