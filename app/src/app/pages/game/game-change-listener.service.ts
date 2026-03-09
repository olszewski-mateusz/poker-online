import {inject, Injectable} from '@angular/core';
import {Game} from '../../model';
import {DeviceDetectorService} from 'ngx-device-detector';
import {ShowdownDialogService} from '../../components/dialogs/showdown/showdown-dialog.service';

@Injectable({
  providedIn: 'root'
})
export class GameChangeListenerService {

  private readonly showdownDialogService: ShowdownDialogService = inject(ShowdownDialogService);
  private readonly deviceService: DeviceDetectorService = inject(DeviceDetectorService);
  private readonly mobile: boolean = this.deviceService.isMobile();

  public onChange(game: Game): void {
      this.showdownDialogService.sync(game, this.mobile);
  }
}
