import {ChangeDetectionStrategy, Component, computed, inject, input, InputSignal, Signal} from '@angular/core';
import {Card, compareCards, Game, GamePhase, Rank} from '../../../model';
import {CardComponent, CardSize} from './card/card.component';
import {ReplaceCardsService} from '../actions';

@Component({
  selector: 'poker-game-hand',
  standalone: true,
  imports: [
    CardComponent
  ],
  templateUrl: './hand.component.html',
  styleUrl: './hand.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HandComponent {
  private readonly unknownCards: Card[] = [
    <Card>{rank: Rank.TWO},
    <Card>{rank: Rank.THREE},
    <Card>{rank: Rank.FOUR},
    <Card>{rank: Rank.FIVE},
    <Card>{rank: Rank.SIX}
  ];

  private readonly replaceCardService: ReplaceCardsService = inject(ReplaceCardsService);

  game: InputSignal<Game> = input.required<Game>();
  mobile: InputSignal<boolean> = input.required<boolean>();

  myCards: Signal<Card[]> = computed(() => {
    const game: Game = this.game();
    return game.players.find(value => value.id === game.myId)?.cards?.sort(compareCards) ?? this.unknownCards;
  });

  cardsInteractive: Signal<boolean> = computed(() => {
    const game: Game = this.game();
    return game.phase === GamePhase.DRAWING && game.myId === game.currentPlayerId;
  });

  cardsSize: Signal<CardSize> = computed(() => {
    return this.mobile() ? CardSize.MOBILE_BIG : CardSize.DESKTOP_BIG;
  })

  cardClicked(card: Card): void {
    this.replaceCardService.toggleCard(card);
  }

  isCardSelected(card: Card): boolean {
    return !!this.replaceCardService.cardsToReplace().find(c => c.rank === card.rank && c.suit === card.suit);
  }
}
