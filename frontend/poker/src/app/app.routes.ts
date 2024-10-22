import { Routes } from '@angular/router';
import {StartPageComponent} from './start-page/start-page.component';
import {PageNotFoundComponent} from './page-not-found/page-not-found.component';
import {GamePageComponent} from './game-page/game-page.component';

export const routes: Routes = [
  { path: 'start', component: StartPageComponent, title: 'First component',},
  { path: 'game/:gameId', component: GamePageComponent },
  { path: '',   redirectTo: '/start', pathMatch: 'full' }, // redirect to `StartPageComponent`
  { path: '**', component: PageNotFoundComponent },  // Wildcard route for a 404 page
];
