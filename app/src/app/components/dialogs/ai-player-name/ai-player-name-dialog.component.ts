import {ChangeDetectionStrategy, Component} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MatDialogActions, MatDialogClose, MatDialogContent, MatDialogTitle} from '@angular/material/dialog';
import {MatError, MatFormField, MatLabel, MatSuffix} from '@angular/material/form-field';
import {MatIcon} from '@angular/material/icon';
import {MatInput} from '@angular/material/input';

@Component({
  selector: 'poker-dialog-ai-player-name',
  standalone: true,
  imports: [
    FormsModule,
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatDialogClose,
    MatFormField,
    MatLabel,
    MatInput,
    MatError,
    MatButton,
    MatIconButton,
    MatIcon,
    MatSuffix
  ],
  templateUrl: './ai-player-name-dialog.component.html',
  styleUrl: './ai-player-name-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AiPlayerNameDialogComponent {
  playerName: string = this.generateRandomName();

  clearName(): void {
    this.playerName = '';
  }

  randomizeName(): void {
    this.playerName = this.generateRandomName();
  }

  private generateRandomName(): string {
    return AI_NAMES[Math.floor(Math.random() * AI_NAMES.length)];
  }
}

const AI_NAMES: string[] = ['Ace', 'Maverick', 'Shadow', 'Blaze', 'Viper', 'Titan', 'Rogue', 'Falcon', 'Ghost', 'Hunter', 'Cobra', 'Phoenix', 'Bullet', 'Wolf', 'Diesel', 'Kingpin', 'Razor', 'Knight', 'Bandit', 'Jet'];
