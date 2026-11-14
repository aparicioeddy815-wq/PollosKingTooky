import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

// Sección inicial del panel según los permisos de cada rol (el orden importa)
const SECCIONES = [
  { permiso: 'PANEL_ADMIN', ruta: '/panel/admin' },
  { permiso: 'PANEL_GERENTE', ruta: '/panel/resumen' },
  { permiso: 'PEDIDO_LEER', ruta: '/panel/pedidos' },
  { permiso: 'PRODUCTO_LEER', ruta: '/panel/productos' },
  { permiso: 'RESERVA_GESTIONAR', ruta: '/panel/reservas' },
  { permiso: 'USUARIO_LEER', ruta: '/panel/usuarios' },
];

export const panelInicioGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const destino = SECCIONES.find((s) => auth.tienePermiso(s.permiso));
  return router.parseUrl(destino?.ruta ?? '/');
};
