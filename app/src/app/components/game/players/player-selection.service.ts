import {Injectable, Signal, signal, WritableSignal} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class PlayerSelectionService {

  private readonly _selectedPlayerIndex: WritableSignal<number|undefined> = signal<number|undefined>(undefined);
  readonly selectedPlayerIndex: Signal<number|undefined> = this._selectedPlayerIndex.asReadonly();

  setSelectedPlayer(playerIndex?: number): void {
    this._selectedPlayerIndex.set(playerIndex);
  }
}
