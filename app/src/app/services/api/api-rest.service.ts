import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {map, MonoTypeOperatorFunction, Observable, tap} from 'rxjs';
import {Card} from '../../model';
import {environment} from '../../../environments/environment';
import {MatSnackBar} from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root'
})
export class ApiRestService {
  private readonly httpClient: HttpClient = inject(HttpClient);
  private readonly snackBar: MatSnackBar = inject(MatSnackBar);
  private readonly serverHost: string = environment.serverHost;

  createGame(): Observable<string> {
    return this.httpClient.post<{gameId: string}>(`${this.serverHost}/game`, undefined)
      .pipe(
        map(response => response.gameId),
        this.showErrorInSnackbar()
      );
  }

  gameExists(gameId: string): Observable<boolean> {
    return this.httpClient.get<boolean>(`${this.serverHost}/game/${gameId}/exists`)
      .pipe(this.showErrorInSnackbar());
  }

  joinGame(gameId: string, displayName: string): Observable<string> {
    return this.httpClient.post<{myId: string}>(`${this.serverHost}/game/${gameId}/action/join`,{
      displayName: displayName
    }).pipe(
      map(response => response.myId),
      this.showErrorInSnackbar()
    );
  }

  sendAiJoin(gameId: string, displayName: string): Observable<string> {
    return this.httpClient.post<{myId: string}>(`${this.serverHost}/game/${gameId}/action/aijoin`, {
      displayName: displayName
    }).pipe(
      map(response => response.myId),
      this.showErrorInSnackbar()
    );
  }

  sendReady(gameId: string, playerId: string, ready: boolean): Observable<never> {
    return <Observable<never>> this.httpClient.post<{myId: string}>(`${this.serverHost}/game/${gameId}/action/ready`,{
      playerId: playerId,
      ready: ready
    }).pipe(this.showErrorInSnackbar());
  }

  sendCheck(gameId: string, playerId: string): Observable<never> {
    return <Observable<never>> this.httpClient.post<{myId: string}>(`${this.serverHost}/game/${gameId}/action/check`,{
      playerId: playerId
    }).pipe(this.showErrorInSnackbar());
  }

  sendCall(gameId: string, playerId: string): Observable<never> {
    return <Observable<never>> this.httpClient.post<{myId: string}>(`${this.serverHost}/game/${gameId}/action/call`,{
      playerId: playerId
    }).pipe(this.showErrorInSnackbar());
  }

  sendBet(gameId: string, playerId: string, amount: number): Observable<never> {
    return <Observable<never>> this.httpClient.post<{myId: string}>(`${this.serverHost}/game/${gameId}/action/bet`,{
      playerId: playerId,
      amount: amount
    }).pipe(this.showErrorInSnackbar());
  }

  sendRaise(gameId: string, playerId: string, amount: number): Observable<never> {
    return <Observable<never>> this.httpClient.post<{myId: string}>(`${this.serverHost}/game/${gameId}/action/raise`,{
      playerId: playerId,
      amount: amount
    }).pipe(this.showErrorInSnackbar());
  }

  sendFold(gameId: string, playerId: string): Observable<never> {
    return <Observable<never>> this.httpClient.post<{myId: string}>(`${this.serverHost}/game/${gameId}/action/fold`,{
      playerId: playerId
    }).pipe(this.showErrorInSnackbar());
  }

  sendAllIn(gameId: string, playerId: string): Observable<never> {
    return <Observable<never>> this.httpClient.post<{myId: string}>(`${this.serverHost}/game/${gameId}/action/all-in`,{
      playerId: playerId
    }).pipe(this.showErrorInSnackbar());
  }

  sendReplace(gameId: string, playerId: string, cardsToReplace: Card[]): Observable<never> {
    return <Observable<never>> this.httpClient.post<{myId: string}>(`${this.serverHost}/game/${gameId}/action/replace`,{
      playerId: playerId,
      cardsToReplace: cardsToReplace
    }).pipe(this.showErrorInSnackbar());
  }

  private showErrorInSnackbar<T>(): MonoTypeOperatorFunction<T> {
    return tap({
      error: (error: unknown) => {
        if (error instanceof HttpErrorResponse) {
          this.snackBar.open(this.extractErrorMessage(error), 'Close', {panelClass: 'poker-error-snackbar'});
          return;
        }

        this.snackBar.open('Request failed. Please try again.', 'Close', {panelClass: 'poker-error-snackbar'});
      }
    });
  }

  private extractErrorMessage(error: HttpErrorResponse): string {
    const fallbackMessage = 'Request failed. Please try again.';
    const errorResponse: unknown = error?.error;

    if (typeof errorResponse === 'string' && errorResponse.trim().length > 0) {
      return errorResponse;
    }

    if (errorResponse && typeof errorResponse === 'object') {
      const response = errorResponse as Record<string, unknown>;
      const candidateFields: Array<keyof typeof response> = ['errorMessage', 'message', 'error', 'detail', 'title'];

      for (const field of candidateFields) {
        const value = response[field];
        if (typeof value === 'string' && value.trim().length > 0) {
          return value;
        }
      }
    }

    if (typeof error?.message === 'string' && error.message.trim().length > 0) {
      return error.message;
    }

    return fallbackMessage;
  }
}

