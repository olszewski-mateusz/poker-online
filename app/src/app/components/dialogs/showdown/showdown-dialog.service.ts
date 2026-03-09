import {inject, Injectable} from '@angular/core';
import {MatDialog, MatDialogRef} from '@angular/material/dialog';
import {Game, GamePhase} from '../../../model';
import {ShowdownDialogComponent, ShowdownDialogData} from '../index';

@Injectable({
  providedIn: 'root'
})
export class ShowdownDialogService {
  private readonly dialog: MatDialog = inject(MatDialog);

  private showdownDismissed = false;
  private showdownDialogRef?: MatDialogRef<ShowdownDialogComponent, string | undefined>;

  public sync(game: Game, mobile: boolean): void {
    const showdownPhase = game.phase === GamePhase.SHOWDOWN;

    if (!showdownPhase) {
      this.showdownDismissed = false;
      this.closeShowdownDialog();
      return;
    }

    if (this.showdownDismissed) {
      this.closeShowdownDialog();
      return;
    }

    if (this.showdownDialogRef) {
      this.closeShowdownDialog();
    }

    if (!this.showdownDialogRef) {
      const data: ShowdownDialogData = {game, mobile};
      this.showdownDialogRef = this.dialog.open(ShowdownDialogComponent, {
        data,
        disableClose: true,
        hasBackdrop: true,
        autoFocus: false,
        restoreFocus: false,
        width: mobile ? '96vw' : '78vw',
        maxWidth: mobile ? '620px' : '980px',
        panelClass: 'poker-showdown-dialog-panel',
        backdropClass: 'poker-showdown-backdrop'
      });

      this.showdownDialogRef.afterClosed().subscribe((result) => {
        if (result === 'ready') {
          this.showdownDismissed = true;
        }
        this.showdownDialogRef = undefined;
      });
      return;
    }

    this.showdownDialogRef.componentInstance.updateGame(game);
  }

  private closeShowdownDialog(): void {
    this.showdownDialogRef?.close();
    this.showdownDialogRef = undefined;
  }
}
