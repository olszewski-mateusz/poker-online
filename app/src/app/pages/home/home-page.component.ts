import {ChangeDetectionStrategy, Component, inject, model, ModelSignal} from '@angular/core';
import {MatButton} from '@angular/material/button';
import {MatFormField, MatFormFieldModule} from '@angular/material/form-field';
import {MatInput, MatInputModule} from '@angular/material/input';
import {MatIcon} from '@angular/material/icon';
import {FormsModule} from '@angular/forms';
import {GameConnectorService} from '../../services';
import {DeviceDetectorService} from 'ngx-device-detector';

@Component({
  selector: 'poker-page-home',
  standalone: true,
  imports: [
    MatButton,
    MatFormField,
    MatInput,
    MatFormFieldModule,
    MatInputModule,
    MatIcon,
    FormsModule
  ],
  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HomePageComponent {
  private readonly deviceService: DeviceDetectorService = inject(DeviceDetectorService);
  private readonly gameService: GameConnectorService = inject(GameConnectorService);

  protected gameId: ModelSignal<string | null> = model<string | null>(null);
  protected readonly isMobile: boolean = this.deviceService.isMobile();

  createGame(): void {
    this.gameService.createNewGame().subscribe();
  }

  joinGame(): void {
    const gameId: string | null = this.gameId();
    if (gameId) {
      this.gameService.joinToExistingGame(gameId).subscribe();
    } else {
      console.error("Game id is required!")
    }
  }

  protected readonly model = model;
}
