import {Component, computed, inject} from '@angular/core';
import {MatButton} from '@angular/material/button';
import {RouterService} from '../../../services/router.service';

@Component({
  selector: 'poker-game-not-found',
  standalone: true,
  imports: [
    MatButton
  ],
  templateUrl: './game-not-found.component.html',
  styleUrl: './game-not-found.component.scss'
})
export class GameNotFoundComponent {
  private routerService: RouterService = inject(RouterService);

  protected gameId: string | undefined = this.routerService.getPageState()?.gameId;

  leave(): void {
    this.routerService.navigateToStartPage().subscribe();
  }
}
