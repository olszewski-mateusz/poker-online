import {inject, Injectable} from '@angular/core';
import {ApiRestService} from './api/api-rest.service';
import {MatDialog} from '@angular/material/dialog';
import {PlayerNamePromptComponent} from '../components/start-page/player-name-prompt/player-name-prompt.component';
import {catchError, filter, map, mergeMap, Observable, of, retry, tap} from 'rxjs';
import {Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Game} from '../model';
import {ApiStreamService} from './api/api-stream.service';

@Injectable({
  providedIn: 'root'
})
export class GameConnectorService {

  private readonly apiRestService: ApiRestService = inject(ApiRestService);
  private readonly apiStreamService: ApiStreamService = inject(ApiStreamService);

  private readonly dialog: MatDialog = inject(MatDialog);
  private readonly router: Router = inject(Router);
  private readonly snackBar: MatSnackBar = inject(MatSnackBar);


  createNewGame(): Observable<boolean> {
    return this.apiRestService.createGame().pipe(
      mergeMap((gameId) => this.router.navigate(['game', gameId]))
    )
  }

  joinToExistingGame(gameId: string): Observable<boolean> {
    return this.checkGameExistence(gameId, false).pipe(
      mergeMap(() => this.router.navigate(['game', gameId]))
    )
  }

  subscribeToGameChanges(gameId: string): Observable<Game> {
    return this.checkGameExistence(gameId, true).pipe(
      map(() => this.tryToGetExistingConnection(gameId)),
      mergeMap(conn => conn ? of(conn) : this.createNewConnection(gameId)),
      mergeMap(conn => {
        return of(conn).pipe(
          tap(conn => this.apiStreamService.initStream(conn.gameId, conn.myId)),
          mergeMap(() => this.apiStreamService.game$),
          catchError(err => {
            if (err instanceof Event && err.target instanceof EventSource) {
              console.log("Trying to reconnect to game...");
            } else {
              console.error(err);
            }
            this.apiStreamService.closeStream();
            throw err;
          }),
          retry({delay: 5000})
        )
      })
    )
  }

  private tryToGetExistingConnection(gameId: string): GameConnection | null {
    const myId: string | null = localStorage.getItem(gameId);
    if (myId) {
      return <GameConnection> {gameId, myId};
    }
    return null;
  }

  private createNewConnection(gameId: string): Observable<GameConnection> {
    return of(gameId).pipe(
      mergeMap(() => this.dialog.open(PlayerNamePromptComponent, {hasBackdrop: false}).afterClosed()),
      mergeMap(playerName => this.apiRestService.joinGame(gameId, playerName)),
      retry(),
      tap(myId => localStorage.setItem(gameId, myId)),
      map(myId => <GameConnection> {gameId, myId})
    );
  };

  private checkGameExistence(gameId: string, reroute: boolean): Observable<boolean> {
    return this.apiRestService.gameExists(gameId).pipe(
      mergeMap(gameExists => {
        if (gameExists) {
          return of(true);
        }

        if (reroute) {
          return this.router.navigate(['game', 'not-found']).then(() => false);
        }

        this.snackBar.open("Game with provided id doesn't exist", "Close");
        return of(false);

      }),
      filter(Boolean)
    );
  }
}

type GameConnection = {
  gameId: string,
  myId: string
}
