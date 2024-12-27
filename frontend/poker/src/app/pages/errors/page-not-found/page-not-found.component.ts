import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {MatButton} from '@angular/material/button';
import {RouterService} from '../../../services/router.service';

@Component({
  selector: 'app-page-not-found',
  standalone: true,
  imports: [
    MatButton
  ],
  templateUrl: './page-not-found.component.html',
  styleUrl: './page-not-found.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PageNotFoundComponent {

  private routerService: RouterService = inject(RouterService);

  leave(): void {
    this.routerService.navigateToStartPage().subscribe();
  }
}
