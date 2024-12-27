import {inject, Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {fromPromise} from 'rxjs/internal/observable/innerFrom';
import {Observable, tap} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RouterService {

  private readonly router: Router = inject(Router);

  navigateToStartPage(): Observable<boolean> {
    return fromPromise(this.router.navigate(['start'])).pipe(
      tap(success => {
        if (!success) {
          console.error("Navigation to start page fail");
        }
      })
    );
  }

  navigateToGameNotFoundPage(gameId: string): Observable<boolean> {
    return fromPromise(this.router.navigate(['game', 'not-found'], { state: { gameId: gameId } })).pipe(
      tap(success => {
        if (!success) {
          console.error("Navigation to game not found page fail");
        }
      })
    );
  }

  navigateToGamePage(gameId: string): Observable<boolean> {
    return fromPromise(this.router.navigate(['game', gameId])).pipe(
      tap(success => {
        if (!success) {
          console.error("Navigation to game page fail");
        }
      })
    );
  }

  getPageState(): any {
    return this.router.getCurrentNavigation()?.extras.state;
  }
}
