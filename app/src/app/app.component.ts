import {Component, inject} from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {MatIconRegistry} from '@angular/material/icon';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  private readonly matIconReg: MatIconRegistry = inject(MatIconRegistry);
  constructor() {
    this.matIconReg.setDefaultFontSetClass('material-symbols-outlined');
  }
}
