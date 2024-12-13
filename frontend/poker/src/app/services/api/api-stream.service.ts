import {Injectable, Signal} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {Game} from '../../model/game';
import {API_HOST} from './api-rest.service';
import {toSignal} from '@angular/core/rxjs-interop';

@Injectable({
  providedIn: 'root'
})
export class ApiStreamService {
  private eventSource?: EventSource;

  private _game: Subject<Game> = new Subject();
  readonly game$: Observable<Game> = this._game.asObservable();
  readonly game: Signal<Game | undefined> = toSignal(this.game$);

  initStream(gameId: string, myId: string): void {
    if (this.eventSource) {
      throw new Error('EventSource already created');
    }

    const url: string = `${API_HOST}/game/${gameId}/subscribe?myId=${myId}`;
    this.eventSource = new EventSource(url);

    if (this.eventSource) {
      this.eventSource.onerror = error => {
        console.error(error);
      };

      this.eventSource.onmessage = event => {
        const messageData: Game = JSON.parse(event.data);
        this._game.next(messageData);
      };
    } else {
      throw Error('event source not initialized');
    }
  }

  closeStream(): void {
    if (!this.eventSource) {
      throw new Error('EventSource already closed');
    }

    this.eventSource.close();
    this.eventSource = undefined;
  }
}
