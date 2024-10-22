import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
  InputSignal,
  output,
  OutputEmitterRef
} from '@angular/core';
import {MatIcon} from '@angular/material/icon';
import {Rank} from '../model/rank';
import {Suit} from '../model/suit';

@Component({
  selector: 'card',
  standalone: true,
  imports: [
    MatIcon
  ],
  templateUrl: './card.component.html',
  styleUrl: './card.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CardComponent {
  rank: InputSignal<Rank> = input.required<Rank>();
  suit: InputSignal<Suit> = input.required<Suit>();
  size: InputSignal<CardSize> = input.required<CardSize>();
  interactive: InputSignal<boolean> = input(false);
  clicked: OutputEmitterRef<void> = output<void>();

  isRed = computed(() => {
    return [Suit.HEARTS, Suit.DIAMONDS].includes(this.suit());
  })

  isSmall() {
    return this.size() === CardSize.SMALL;
  }

  isMedium() {
    return this.size() === CardSize.MEDIUM;
  }

  isBig() {
    return this.size() === CardSize.BIG;
  }


  onCardClick() {
    if (this.interactive()) {
      this.clicked.emit();
    }
  }
}

export enum CardSize {
  SMALL, MEDIUM, BIG
}
