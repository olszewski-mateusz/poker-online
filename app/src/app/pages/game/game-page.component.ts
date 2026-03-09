import {ChangeDetectionStrategy, Component, inject, OnDestroy, Signal} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Game} from '../../model';
import {toSignal} from '@angular/core/rxjs-interop';
import {map, mergeMap, tap} from 'rxjs';
import {GameConnectorService} from '../../services';
import {GameDesktopLayoutComponent} from '../../layouts/desktop/game-desktop-layout.component';
import {DeviceDetectorService} from 'ngx-device-detector';
import {GameMobileLayoutComponent} from '../../layouts/mobile/game-mobile-layout.component';
import {GameChangeListenerService} from './game-change-listener.service';

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
export class GamePageComponent implements OnDestroy{
  private readonly route: ActivatedRoute = inject(ActivatedRoute);
  private readonly gameService: GameConnectorService = inject(GameConnectorService);
  private readonly deviceService: DeviceDetectorService = inject(DeviceDetectorService);
  private readonly gameChangeListener: GameChangeListenerService = inject(GameChangeListenerService);
  protected readonly isMobile: boolean = this.deviceService.isMobile();

  protected readonly game: Signal<Game | undefined> = toSignal(this.route.params.pipe(
    map(value => value['gameId']),
    mergeMap(gameId => this.gameService.openConnectionToGameChanges(gameId)),
    tap(game => this.gameChangeListener.onChange(game)),
    tap(game => console.log(game))
  ));

  ngOnDestroy(): void {
    // this.gameService.closeConnectionToGameChanges();
  }
}
