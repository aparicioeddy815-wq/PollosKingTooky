import { Routes } from '@angular/router';
import { autenticadoGuard, invitadoGuard, permisoGuard, personalGuard } from './core/guards/auth.guard';
import { panelInicioGuard } from './core/guards/panel-inicio.guard';

export const routes: Routes = [
  // Sitio público
  { path: '', title: 'King Tooky', loadComponent: () => import('./pages/publico/inicio/inicio').then((m) => m.Inicio) },
  { path: 'menu', title: 'Menú · King Tooky', loadComponent: () => import('./pages/publico/menu/menu').then((m) => m.Menu) },
  { path: 'turnos', title: 'Turnos · King Tooky', loadComponent: () => import('./pages/publico/turnos/turnos').then((m) => m.Turnos) },
  { path: 'nosotros', title: 'Nosotros · King Tooky', loadComponent: () => import('./pages/publico/nosotros/nosotros').then((m) => m.Nosotros) },
  { path: 'ubicacion', title: 'Ubicación y horarios · King Tooky', loadComponent: () => import('./pages/publico/ubicacion/ubicacion').then((m) => m.Ubicacion) },

  // Acceso
  { path: 'ingresar', title: 'Ingresar · King Tooky', canActivate: [invitadoGuard], loadComponent: () => import('./pages/acceso/login/login').then((m) => m.Login) },
  { path: 'registrarse', title: 'Crear cuenta · King Tooky', canActivate: [invitadoGuard], loadComponent: () => import('./pages/acceso/registro/registro').then((m) => m.Registro) },
  { path: 'olvide-contrasena', title: 'Recuperar contraseña · King Tooky', loadComponent: () => import('./pages/acceso/olvide-contrasena/olvide-contrasena').then((m) => m.OlvideContrasena) },
  { path: 'restablecer-contrasena', title: 'Nueva contraseña · King Tooky', loadComponent: () => import('./pages/acceso/restablecer-contrasena/restablecer-contrasena').then((m) => m.RestablecerContrasena) },
  { path: 'perfil', title: 'Mi perfil · King Tooky', canActivate: [autenticadoGuard], loadComponent: () => import('./pages/acceso/perfil/perfil').then((m) => m.Perfil) },

  // Zona del cliente
  {
    path: 'mi-cuenta',
    canActivate: [permisoGuard('CLIENTE_PORTAL')],
    loadComponent: () => import('./layout/cuenta-layout/cuenta-layout').then((m) => m.CuentaLayout),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'reservas' },
      { path: 'reservas', title: 'Mis reservas · King Tooky', loadComponent: () => import('./pages/cuenta/mis-reservas/mis-reservas').then((m) => m.MisReservas) },
      { path: 'reservas/nueva', title: 'Reservar · King Tooky', canActivate: [permisoGuard('RESERVA_CREAR')], loadComponent: () => import('./pages/cuenta/nueva-reserva/nueva-reserva').then((m) => m.NuevaReserva) },
      { path: 'menu-salon', title: 'Menú del salón · King Tooky', loadComponent: () => import('./pages/cuenta/menu-salon/menu-salon').then((m) => m.MenuSalon) },
      { path: 'favoritos', title: 'Favoritos · King Tooky', loadComponent: () => import('./pages/cuenta/favoritos/favoritos').then((m) => m.Favoritos) },
      { path: 'quejas', title: 'Quejas y sugerencias · King Tooky', loadComponent: () => import('./pages/cuenta/quejas/quejas').then((m) => m.Quejas) },
      { path: 'evaluacion', title: 'Evaluar el día · King Tooky', loadComponent: () => import('./pages/cuenta/evaluacion/evaluacion').then((m) => m.Evaluacion) },
      { path: 'ayuda', title: 'Ayuda · King Tooky', loadComponent: () => import('./pages/cuenta/ayuda/ayuda').then((m) => m.Ayuda) },
    ],
  },

  // Panel del personal
  {
    path: 'panel',
    canActivate: [personalGuard],
    loadComponent: () => import('./layout/panel-layout/panel-layout').then((m) => m.PanelLayout),
    children: [
      { path: '', pathMatch: 'full', canActivate: [panelInicioGuard], children: [] },
      { path: 'admin', title: 'Dashboard · Panel', canActivate: [permisoGuard('PANEL_ADMIN')], loadComponent: () => import('./pages/panel/admin-dashboard/admin-dashboard').then((m) => m.AdminDashboard) },
      { path: 'resumen', title: 'Resumen · Panel', canActivate: [permisoGuard('PANEL_GERENTE')], loadComponent: () => import('./pages/panel/resumen/resumen').then((m) => m.Resumen) },
      { path: 'pedidos', title: 'Pedidos · Panel', canActivate: [permisoGuard('PEDIDO_LEER')], loadComponent: () => import('./pages/panel/pedidos/pedidos').then((m) => m.Pedidos) },
      { path: 'pedidos/nuevo', title: 'Nuevo pedido · Panel', canActivate: [permisoGuard('PEDIDO_CREAR')], loadComponent: () => import('./pages/panel/pedido-nuevo/pedido-nuevo').then((m) => m.PedidoNuevo) },
      { path: 'inventario', title: 'Inventario · Panel', canActivate: [permisoGuard('INVENTARIO_LEER')], loadComponent: () => import('./pages/panel/inventario/inventario').then((m) => m.Inventario) },
      { path: 'productos', title: 'Productos · Panel', canActivate: [permisoGuard('PRODUCTO_LEER')], loadComponent: () => import('./pages/panel/productos/productos').then((m) => m.Productos) },
      { path: 'reservas', title: 'Reservas · Panel', canActivate: [permisoGuard('RESERVA_GESTIONAR')], loadComponent: () => import('./pages/panel/reservas/reservas').then((m) => m.Reservas) },
      { path: 'quejas', title: 'Quejas · Panel', canActivate: [permisoGuard('QUEJA_GESTIONAR')], loadComponent: () => import('./pages/panel/quejas/quejas').then((m) => m.QuejasPanel) },
      { path: 'entrevistas', title: 'Entrevistas · Panel', canActivate: [permisoGuard('ENTREVISTA_GESTIONAR')], loadComponent: () => import('./pages/panel/entrevistas/entrevistas').then((m) => m.Entrevistas) },
      { path: 'entrevistados', title: 'Entrevistados · Panel', canActivate: [permisoGuard('ENTREVISTA_DECIDIR')], loadComponent: () => import('./pages/panel/entrevistados/entrevistados').then((m) => m.Entrevistados) },
      { path: 'personal', title: 'Roles y personal · Panel', canActivate: [permisoGuard('PERSONAL_LEER')], loadComponent: () => import('./pages/panel/personal/personal').then((m) => m.RolesPersonal) },
      { path: 'bitacora', title: 'Bitácora · Panel', canActivate: [permisoGuard('BITACORA_LEER')], loadComponent: () => import('./pages/panel/bitacora/bitacora').then((m) => m.Bitacora) },
      { path: 'reportes', title: 'Reportes · Panel', canActivate: [permisoGuard('REPORTE_LEER')], loadComponent: () => import('./pages/panel/reportes/reportes').then((m) => m.Reportes) },
      { path: 'usuarios', title: 'Usuarios · Panel', canActivate: [permisoGuard('USUARIO_LEER')], loadComponent: () => import('./pages/panel/usuarios/usuarios').then((m) => m.Usuarios) },
    ],
  },

  { path: '**', redirectTo: '' },
];
