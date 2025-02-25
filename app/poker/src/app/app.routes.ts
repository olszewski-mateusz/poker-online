import { Routes } from '@angular/router';
import {HomePageComponent} from './pages/home/home-page.component';
import {PageNotFoundComponent} from './pages/errors/page-not-found/page-not-found.component';
import {GamePageComponent} from './pages/game/game-page.component';
import {GameNotFoundComponent} from './pages/errors/game-not-found/game-not-found.component';

export const routes: Routes = [
  { path: 'start', component: HomePageComponent, title: 'First component',},
  { path: 'game/not-found', component: GameNotFoundComponent },
  { path: 'game/:gameId', component: GamePageComponent },
  { path: '',   redirectTo: '/start', pathMatch: 'full' }, // redirect to `HomePageComponent`
  { path: '**', component: PageNotFoundComponent },  // Wildcard route for a 404 page
];
