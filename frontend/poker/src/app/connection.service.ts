import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ConnectionService {
  private readonly httpClient: HttpClient = inject(HttpClient);
  createGame() {
    return this.httpClient.post<{gameId: string}>("http://localhost:8080/game", undefined);
  }
}
