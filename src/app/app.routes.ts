import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login';
import { DespachoComponent } from './pages/despacho/despacho';
import { GerenteComponent } from './pages/gerente/gerente';
import { AdminComponent } from './pages/admin/admin';
import { ClienteComponent } from './pages/cliente/cliente';
import { SalonMenuComponent } from './pages/salon-menu/salon-menu';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'despacho', component: DespachoComponent },
  { path: 'gerente', component: GerenteComponent },
  { path: 'cliente', component: ClienteComponent },
  { path: 'admin', component: AdminComponent },
  { path: 'menu', component: SalonMenuComponent },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' }
];
