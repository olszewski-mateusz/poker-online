import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {map, Observable} from 'rxjs';
import {Card, Game} from '../model/game';
import {GameStreamService} from './game-stream.service';

export const API_HOST: string = 'http://localhost:8080';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly httpClient: HttpClient = inject(HttpClient);
  private readonly gameStreamService: GameStreamService = inject(GameStreamService);

  createGame(): Observable<string> {
    return this.httpClient.post<{gameId: string}>(`${API_HOST}/game`, undefined)
      .pipe(
        map(value => value.gameId)
      );
  }

  gameExists(gameId: string): Observable<boolean> {
    return this.httpClient.get<boolean>(`${API_HOST}/game/${gameId}/exists`);
  }

  joinGame(gameId: string, displayName: string): Observable<string> {
    return this.httpClient.post<{myId: string}>(`${API_HOST}/game/${gameId}/action/join`,{
      displayName: displayName
    }).pipe(
      map(value => value.myId)
    );
  }

  sendReady(gameId: string, playerId: string, ready: boolean): Observable<never> {
    return <Observable<never>> this.httpClient.post<{myId: string}>(`${API_HOST}/game/${gameId}/action/ready`,{
      playerId: playerId,
      ready: ready
    });
  }

  sendCheckOrCall(gameId: string, playerId: string): Observable<never> {
    return <Observable<never>> this.httpClient.post<{myId: string}>(`${API_HOST}/game/${gameId}/action/check`,{
      playerId: playerId
    });
  }

  sendBetOrRaise(gameId: string, playerId: string, amount: number): Observable<never> {
    return <Observable<never>> this.httpClient.post<{myId: string}>(`${API_HOST}/game/${gameId}/action/raise`,{
      playerId: playerId,
      amount: amount
    });
  }

  sendFold(gameId: string, playerId: string): Observable<never> {
    return <Observable<never>> this.httpClient.post<{myId: string}>(`${API_HOST}/game/${gameId}/action/fold`,{
      playerId: playerId
    });
  }

  sendAllIn(gameId: string, playerId: string): Observable<never> {
    return <Observable<never>> this.httpClient.post<{myId: string}>(`${API_HOST}/game/${gameId}/action/all-in`,{
      playerId: playerId
    });
  }

  sendReplace(gameId: string, playerId: string, cardsToReplace: Card[]): Observable<never> {
    return <Observable<never>> this.httpClient.post<{myId: string}>(`${API_HOST}/game/${gameId}/action/replace`,{
      playerId: playerId,
      cardsToReplace: cardsToReplace
    });
  }

  fetchGameStream(gameId: string, myId: string): Observable<Game> {
    return this.gameStreamService.createEventSource(gameId, myId);
  }
}
