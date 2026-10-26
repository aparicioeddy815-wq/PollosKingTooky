import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login';
import { DespachoComponent } from './pages/despacho/despacho';
import { SalonMenuComponent } from './pages/salon-menu/salon-menu';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'despacho', component: DespachoComponent },
  { path: 'menu', component: SalonMenuComponent },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' }
];
