import {inject, Injectable} from '@angular/core';
import {ApiRestService} from './api/api-rest.service';
import {MatDialog} from '@angular/material/dialog';
import {PlayerNamePromptComponent} from '../components/start-page/player-name-prompt/player-name-prompt.component';
import {filter, map, mergeMap, Observable, of, tap} from 'rxjs';
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


  createAndJoinGame() {
    return this.dialog.open(PlayerNamePromptComponent, {hasBackdrop: false}).afterClosed().pipe(
      filter(playerName => playerName !== null),
      mergeMap(playerName => this.apiRestService.createGame().pipe(
        mergeMap((gameId) =>
          this.apiRestService.joinGame(gameId, playerName).pipe(
            tap(myId => localStorage.setItem(gameId, myId)),
            map(() => gameId))
        ))),
      mergeMap((gameId) => this.router.navigate(['game', gameId]))
    )
  }

  joinGame(gameId: string) {
    return this.apiRestService.gameExists(gameId).pipe(
      tap(value => {
        if (!value) {
          this.snackBar.open("Game with provided id not exists.", "Close");
        }
      }),
      filter(value => value === true),
      mergeMap(() => this.dialog.open(PlayerNamePromptComponent, {hasBackdrop: false}).afterClosed()),
      mergeMap(playerName => this.apiRestService.joinGame(gameId, playerName).pipe(
        tap(myId => localStorage.setItem(gameId, myId)),
        map(() => gameId))
      ),
      mergeMap((gameId) => this.router.navigate(['game', gameId]))
    )
  }

  connectToGameStream(gameId: string): Observable<Game> {
    return this.apiRestService.gameExists(gameId).pipe(
      mergeMap(gameExists => {
        if (gameExists) {
          return of(true);
        }
        return this.router.navigate(['game', 'not-found']).then(() => false);
      }),
      filter(Boolean),
      mergeMap(() => {
        const myId: string | null = localStorage.getItem(gameId);
        if (myId) {
          return of(myId);
        }
        return this.dialog.open(PlayerNamePromptComponent, {hasBackdrop: false}).afterClosed().pipe(
          mergeMap(playerName => this.apiRestService.joinGame(gameId, playerName)),
          tap(myId => localStorage.setItem(gameId, myId)),
        )
      }),
      tap(myId => this.apiStreamService.initStream(gameId, myId)),
      mergeMap(() => this.apiStreamService.game$)
    )

  }
}
