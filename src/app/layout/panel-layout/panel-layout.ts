import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

interface Seccion {
  texto: string;
  icono: string;
  ruta: string;
  permiso: string;
}

const SECCIONES: Seccion[] = [
  { texto: 'Dashboard', icono: 'pi pi-chart-line', ruta: '/panel/admin', permiso: 'PANEL_ADMIN' },
  { texto: 'Resumen', icono: 'pi pi-chart-bar', ruta: '/panel/resumen', permiso: 'PANEL_GERENTE' },
  { texto: 'Pedidos', icono: 'pi pi-receipt', ruta: '/panel/pedidos', permiso: 'PEDIDO_LEER' },
  { texto: 'Nuevo pedido', icono: 'pi pi-plus-circle', ruta: '/panel/pedidos/nuevo', permiso: 'PEDIDO_CREAR' },
  { texto: 'Inventario', icono: 'pi pi-database', ruta: '/panel/inventario', permiso: 'INVENTARIO_LEER' },
  { texto: 'Productos', icono: 'pi pi-box', ruta: '/panel/productos', permiso: 'PRODUCTO_LEER' },
  { texto: 'Reservas', icono: 'pi pi-calendar', ruta: '/panel/reservas', permiso: 'RESERVA_GESTIONAR' },
  { texto: 'Quejas', icono: 'pi pi-comments', ruta: '/panel/quejas', permiso: 'QUEJA_GESTIONAR' },
  { texto: 'Entrevistas', icono: 'pi pi-briefcase', ruta: '/panel/entrevistas', permiso: 'ENTREVISTA_GESTIONAR' },
  { texto: 'Entrevistados', icono: 'pi pi-user-plus', ruta: '/panel/entrevistados', permiso: 'ENTREVISTA_DECIDIR' },
  { texto: 'Roles y personal', icono: 'pi pi-id-card', ruta: '/panel/personal', permiso: 'PERSONAL_LEER' },
  { texto: 'Bitácora', icono: 'pi pi-shield', ruta: '/panel/bitacora', permiso: 'BITACORA_LEER' },
  { texto: 'Reportes', icono: 'pi pi-chart-pie', ruta: '/panel/reportes', permiso: 'REPORTE_LEER' },
  { texto: 'Usuarios', icono: 'pi pi-users', ruta: '/panel/usuarios', permiso: 'USUARIO_LEER' },
];

@Component({
  selector: 'app-panel-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './panel-layout.html',
  styleUrl: './panel-layout.scss',
})
export class PanelLayout {
  private readonly auth = inject(AuthService);

  protected readonly rol = computed(() => this.auth.sesion()?.rol ?? '');
  protected readonly secciones = computed(() => SECCIONES.filter((s) => this.auth.tienePermiso(s.permiso)));
}
