import {ChangeDetectionStrategy, Component} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {MatButton, MatIconButton} from '@angular/material/button';
import {MatDialogActions, MatDialogClose, MatDialogContent, MatDialogTitle} from '@angular/material/dialog';
import {MatError, MatFormField, MatLabel, MatSuffix} from '@angular/material/form-field';
import {MatIcon} from '@angular/material/icon';
import {MatInput} from '@angular/material/input';

const AI_ADJECTIVES: string[] = ['Bold', 'Lucky', 'Swift', 'Sharp', 'Calm', 'Fierce', 'Clever', 'Steady'];
const AI_ANIMALS: string[] = ['Fox', 'Wolf', 'Tiger', 'Falcon', 'Shark', 'Lynx', 'Raven', 'Panther'];

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
    const adjective = AI_ADJECTIVES[Math.floor(Math.random() * AI_ADJECTIVES.length)];
    const animal = AI_ANIMALS[Math.floor(Math.random() * AI_ANIMALS.length)];
    return `AI ${adjective} ${animal}`;
  }
}
