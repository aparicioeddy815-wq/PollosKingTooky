import { Routes } from '@angular/router';
import { SalonMenuComponent } from './pages/salon-menu/salon-menu';
import { LoginComponent } from './pages/login/login';

export const routes: Routes = [
  { path: '', component: SalonMenuComponent },
  { path: 'menu', component: SalonMenuComponent },
  { path: 'login', component: LoginComponent }
];
