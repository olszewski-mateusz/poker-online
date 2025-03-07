import {Injectable, Signal, signal, WritableSignal} from '@angular/core';
import {Card} from '../../../model';

@Injectable({
  providedIn: 'root'
})
export class ReplaceCardsService {
  private readonly _cardsToReplace: WritableSignal<Card[]> = signal<Card[]>([]);
  readonly cardsToReplace: Signal<Card[]> = this._cardsToReplace.asReadonly();

  toggleCard(card: Card): void {
    const cardsToReplace: Card[] = this._cardsToReplace();
    if (cardsToReplace.find(c => c.rank === card.rank && c.suit === card.suit)) {
      this._cardsToReplace.set(cardsToReplace.filter(c => !(c.rank === card.rank && c.suit === card.suit)));
    } else {
      this._cardsToReplace.set([...cardsToReplace, card]);
    }
  }

  clearSelection(): void {
    this._cardsToReplace.set([]);
  }
}
