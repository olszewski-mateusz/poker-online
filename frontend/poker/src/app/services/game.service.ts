import {inject, Injectable} from '@angular/core';
import {ApiService} from './api.service';
import {MatDialog} from '@angular/material/dialog';
import {PlayerNamePromptComponent} from '../components/start-page/player-name-prompt/player-name-prompt.component';
import {filter, map, mergeMap, Observable, of, tap} from 'rxjs';
import {Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Game} from '../model/game';

@Injectable({
  providedIn: 'root'
})
export class GameService {

  private readonly apiService: ApiService = inject(ApiService);
  private readonly dialog: MatDialog = inject(MatDialog);
  private readonly router: Router = inject(Router);
  private readonly snackBar: MatSnackBar = inject(MatSnackBar);


  createAndJoinGame() {
    return this.dialog.open(PlayerNamePromptComponent, {hasBackdrop: false}).afterClosed().pipe(
      filter(playerName => playerName !== null),
      mergeMap(playerName => this.apiService.createGame().pipe(
        mergeMap((gameId) =>
          this.apiService.joinGame(gameId, playerName).pipe(
            tap(myId => localStorage.setItem(gameId, myId)),
            map(() => gameId))
        ))),
      mergeMap((gameId) => this.router.navigate(['game', gameId]))
    )
  }

  joinGame(gameId: string) {
    return this.apiService.gameExists(gameId).pipe(
      tap(value => {
        if (!value) {
          this.snackBar.open("Game with provided id not exists.", "Close");
        }
      }),
      filter(value => value === true),
      mergeMap(() => this.dialog.open(PlayerNamePromptComponent, {hasBackdrop: false}).afterClosed()),
      mergeMap(playerName => this.apiService.joinGame(gameId, playerName).pipe(
        tap(myId => localStorage.setItem(gameId, myId)),
        map(() => gameId))
      ),
      mergeMap((gameId) => this.router.navigate(['game', gameId]))
    )
  }

  getGame(gameId: string): Observable<Game> {
    return this.apiService.gameExists(gameId).pipe(
      mergeMap(value => {
        if (!value) {
          return this.router.navigate(['game', 'not-found']).then(() => value);
        }
        return of(value);
      }),
      filter(value => value === true),
      mergeMap(() => {
        const myId = localStorage.getItem(gameId);
        if (myId) {
          return this.apiService.getGame(gameId, myId);
        }
        return this.dialog.open(PlayerNamePromptComponent, {hasBackdrop: false}).afterClosed().pipe(
          mergeMap(playerName => this.apiService.joinGame(gameId, playerName)),
          tap(myId => localStorage.setItem(gameId, myId)),
          mergeMap(myId => this.apiService.getGame(gameId, myId))
        )
      })
    )

  }
}
