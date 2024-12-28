import {ChangeDetectionStrategy, Component, inject, Signal} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Game} from '../../model';
import {toSignal} from '@angular/core/rxjs-interop';
import {map, mergeMap, tap} from 'rxjs';
import {GameConnectorService} from '../../services';
import {GameDesktopLayoutComponent} from '../../layouts/desktop/game-desktop-layout.component';
import {DeviceDetectorService} from 'ngx-device-detector';
import {GameMobileLayoutComponent} from '../../layouts/mobile/game-mobile-layout.component';

@Component({
  selector: 'poker-page-game',
  standalone: true,
  imports: [
    GameDesktopLayoutComponent,
    GameMobileLayoutComponent
  ],
  templateUrl: './game-page.component.html',
  styleUrl: './game-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GamePageComponent {
  private readonly route: ActivatedRoute = inject(ActivatedRoute);
  private readonly gameService: GameConnectorService = inject(GameConnectorService);
  private readonly deviceService: DeviceDetectorService = inject(DeviceDetectorService);

  protected readonly isMobile: boolean = this.deviceService.isMobile();

  protected readonly game: Signal<Game | undefined> = toSignal(this.route.params.pipe(
    map(value => value['gameId']),
    mergeMap(gameId => this.gameService.subscribeToGameChanges(gameId)),
    tap(game => console.log(game))
  ));

  constructor() {

  }
}
