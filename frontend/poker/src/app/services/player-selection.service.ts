import {Injectable, Signal, signal, WritableSignal} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class PlayerSelectionService {

  private readonly _selectedPlayerId: WritableSignal<string|undefined> = signal<string|undefined>(undefined);
  readonly selectedPlayerId: Signal<string|undefined> = this._selectedPlayerId.asReadonly();

  setSelectedPlayer(playerId: string) {
    this._selectedPlayerId.set(playerId);
  }
}
