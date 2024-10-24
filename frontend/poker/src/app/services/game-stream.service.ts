import { Injectable } from '@angular/core';
import {Observable} from 'rxjs';
import {Game} from '../model/game';
import {API_HOST} from './api.service';

@Injectable({
  providedIn: 'root'
})
export class GameStreamService {
  private eventSource?: EventSource;

  createEventSource(gameId: string, myId: string): Observable<Game> {
    if (this.eventSource) {
      throw new Error('EventSource already created');
    }

    const url: string = `${API_HOST}/game/${gameId}/subscribe?myId=${myId}`;
    this.eventSource = new EventSource(url);

    return new Observable(observer => {
      if (this.eventSource) {
        this.eventSource.onerror = error => {
          observer.error(error);
        };

        this.eventSource.onmessage = event => {
          const messageData: Game = JSON.parse(event.data);
          observer.next(messageData);
        };
      }
    });
  }

  close(): void {
    if (!this.eventSource) {
      throw new Error('EventSource already closed');
    }

    this.eventSource.close();
    this.eventSource = undefined;
  }

  constructor() { }
}
