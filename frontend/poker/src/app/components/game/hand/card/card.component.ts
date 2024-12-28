import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
  InputSignal,
  output,
  OutputEmitterRef, Signal
} from '@angular/core';
import {MatIcon} from '@angular/material/icon';
import {Rank, Suit} from '../../../../model';

@Component({
  selector: 'poker-game-card',
  standalone: true,
  imports: [
    MatIcon
  ],
  templateUrl: './card.component.html',
  styleUrl: './card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CardComponent {
  size: InputSignal<CardSize> = input.required<CardSize>();
  rank: InputSignal<Rank|undefined> = input<Rank>();
  suit: InputSignal<Suit|undefined> = input<Suit>();
  interactive: InputSignal<boolean> = input(false);

  clicked: OutputEmitterRef<void> = output<void>();

  rankValue: Signal<string> = computed(() => {
    switch (this.rank()) {
      case Rank.TWO:
        return '2';
      case Rank.THREE:
        return '3';
      case Rank.FOUR:
        return '4';
      case Rank.FIVE:
        return '5';
      case Rank.SIX:
        return '6';
      case Rank.SEVEN:
        return '7';
      case Rank.EIGHT:
        return '8';
      case Rank.NINE:
        return '9';
      case Rank.TEN:
        return '10';
      case Rank.JACK:
        return 'J';
      case Rank.QUEEN:
        return 'Q';
      case Rank.KING:
        return 'K';
      case Rank.ACE:
        return 'A';
    }
    return '';
  })

  suitValue: Signal<string> = computed(() => {
    switch (this.suit()) {
      case Suit.HEARTS:
        return '♥';
      case Suit.SPADES:
        return '♠';
      case Suit.DIAMONDS:
        return '♦';
      case Suit.CLUBS:
        return '♣';
    }
    return '';
  })

  isRed: Signal<boolean> = computed(() => {
    const suit: Suit | undefined = this.suit();
    return !!suit && [Suit.HEARTS, Suit.DIAMONDS].includes(suit);
  })

  isSmall: Signal<boolean> = computed(() => {
    return this.size() === CardSize.SMALL;
  })

  isMedium: Signal<boolean> = computed(() => {
    return this.size() === CardSize.MEDIUM;
  })

  isBig: Signal<boolean> = computed(() => {
    return this.size() === CardSize.BIG;
  })

  isCard: Signal<boolean> = computed(() => {
    return !!this.rank() && !!this.suit();
  })

  onCardClick(): void {
    if (this.interactive()) {
      this.clicked.emit();
    }
  }
}

export enum CardSize {
  SMALL, MEDIUM, BIG
}
