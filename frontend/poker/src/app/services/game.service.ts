import {inject, Injectable} from '@angular/core';
import {ApiService} from './api.service';
import {MatDialog} from '@angular/material/dialog';
import {PlayerNamePromptComponent} from '../components/start-page/player-name-prompt/player-name-prompt.component';
import {filter, map, mergeMap, tap} from 'rxjs';
import {Router} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';

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
        map(({gameId}) => ({gameId, playerName}))
      )),
      mergeMap(({gameId, playerName}) =>
        this.apiService.joinGame(gameId, playerName).pipe(
          map(({myId}) => ({gameId, myId}))
        )
      ),
      tap(({gameId, myId}) => {
          sessionStorage.setItem(gameId, myId);
      }),
      mergeMap(({gameId}) => this.router.navigate(['game', gameId]))
    )
  }

  joinGame(gameId: string) {
    return this.apiService.gameExists(gameId).pipe(
      tap(value => {
        if (!value) {
          this.snackBar.open("Game with provided id not exists.", "Close")
        }
      }),
      filter(value => value === true),
      mergeMap(() => this.dialog.open(PlayerNamePromptComponent, {hasBackdrop: false}).afterClosed()),
      mergeMap(playerName => this.apiService.joinGame(gameId, playerName).pipe(
        map(({myId}) => ({gameId, myId}))
      )),
      tap(({gameId, myId}) => {
        sessionStorage.setItem(gameId, myId);
      }),
      mergeMap(({gameId}) => this.router.navigate(['game', gameId]))

    )
  }
}
