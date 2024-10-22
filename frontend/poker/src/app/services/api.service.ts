import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';

const API_HOST: string = 'http://localhost:8080';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private readonly httpClient: HttpClient = inject(HttpClient);

  createGame() {
    return this.httpClient.post<{gameId: string}>(`${API_HOST}/game`, undefined);
  }

  gameExists(gameId: string) {
    return this.httpClient.get<boolean>(`${API_HOST}/game/${gameId}/exists`);
  }

  joinGame(gameId: string, displayName: string) {
    return this.httpClient.post<{myId: string}>(`${API_HOST}/game/${gameId}/action/join`,{
      displayName: displayName
    });
  }


}
