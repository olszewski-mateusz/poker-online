import {Injectable, Signal} from '@angular/core';
import {EMPTY, Observable, Subject} from 'rxjs';
import {Game} from '../../model';
import {API_HOST} from './api-rest.service';
import {toSignal} from '@angular/core/rxjs-interop';

@Injectable({
  providedIn: 'root'
})
export class ApiStreamService {
  private eventSource?: EventSource;
  private _game?: Subject<Game>;

  get game$(): Observable<Game> {
    return this._game?.asObservable() ??
      new Observable<never>(subscriber => subscriber.error("Stream not initialized"));
  }

  initStream(gameId: string, myId: string): void {
    if (this.eventSource) {
      throw new Error('EventSource already created');
    }

    const url: string = `${API_HOST}/game/${gameId}/subscribe?myId=${myId}`;
    this.eventSource = new EventSource(url);
    this._game = new Subject<Game>();

    if (this.eventSource) {
      this.eventSource.onerror = error => {
        this._game?.error(error);
      };

      this.eventSource.onmessage = event => {
        const messageData: Game = JSON.parse(event.data);
        this._game?.next(messageData);
      };

      this.eventSource.onopen = () => {
        console.log("Connection established with game " + gameId);
      }

    } else {
      throw Error('event source not initialized');
    }
  }

  closeStream(): void {
    if (!this.eventSource) {
      const errMessage: string = 'EventSource already closed'
      console.log(errMessage);
      throw new Error(errMessage);
    }

    this.eventSource.close();
    this.eventSource = undefined;
    this._game = undefined;
  }
}
