import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {map, Observable} from 'rxjs';
import {Card} from '../../model';
import {PropertiesService} from '../properties.service';

@Injectable({
  providedIn: 'root'
})
export class ApiRestService {
  private readonly properties: PropertiesService = inject(PropertiesService);
  private readonly httpClient: HttpClient = inject(HttpClient);

  createGame(): Observable<string> {
    return this.httpClient.post<{gameId: string}>(`${this.properties.serverHost}/game`, undefined)
      .pipe(
        map(response => response.gameId)
      );
  }

  gameExists(gameId: string): Observable<boolean> {
    return this.httpClient.get<boolean>(`${this.properties.serverHost}/game/${gameId}/exists`);
  }

  joinGame(gameId: string, displayName: string): Observable<string> {
    return this.httpClient.post<{myId: string}>(`${this.properties.serverHost}/game/${gameId}/action/join`,{
      displayName: displayName
    }).pipe(
      map(response => response.myId)
    );
  }

  sendReady(gameId: string, playerId: string, ready: boolean): Observable<never> {
    return <Observable<never>> this.httpClient.post<{myId: string}>(`${this.properties.serverHost}/game/${gameId}/action/ready`,{
      playerId: playerId,
      ready: ready
    });
  }

  sendCheckOrCall(gameId: string, playerId: string): Observable<never> {
    return <Observable<never>> this.httpClient.post<{myId: string}>(`${this.properties.serverHost}/game/${gameId}/action/check`,{
      playerId: playerId
    });
  }

  sendBetOrRaise(gameId: string, playerId: string, amount: number): Observable<never> {
    return <Observable<never>> this.httpClient.post<{myId: string}>(`${this.properties.serverHost}/game/${gameId}/action/raise`,{
      playerId: playerId,
      amount: amount
    });
  }

  sendFold(gameId: string, playerId: string): Observable<never> {
    return <Observable<never>> this.httpClient.post<{myId: string}>(`${this.properties.serverHost}/game/${gameId}/action/fold`,{
      playerId: playerId
    });
  }

  sendAllIn(gameId: string, playerId: string): Observable<never> {
    return <Observable<never>> this.httpClient.post<{myId: string}>(`${this.properties.serverHost}/game/${gameId}/action/all-in`,{
      playerId: playerId
    });
  }

  sendReplace(gameId: string, playerId: string, cardsToReplace: Card[]): Observable<never> {
    return <Observable<never>> this.httpClient.post<{myId: string}>(`${this.properties.serverHost}/game/${gameId}/action/replace`,{
      playerId: playerId,
      cardsToReplace: cardsToReplace
    });
  }
}
