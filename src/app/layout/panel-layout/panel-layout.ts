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
  { texto: 'Resumen', icono: 'pi pi-chart-bar', ruta: '/panel/resumen', permiso: 'PANEL_GERENTE' },
  { texto: 'Pedidos', icono: 'pi pi-receipt', ruta: '/panel/pedidos', permiso: 'PEDIDO_LEER' },
  { texto: 'Nuevo pedido', icono: 'pi pi-plus-circle', ruta: '/panel/pedidos/nuevo', permiso: 'PEDIDO_CREAR' },
  { texto: 'Productos', icono: 'pi pi-box', ruta: '/panel/productos', permiso: 'PRODUCTO_LEER' },
  { texto: 'Reservas', icono: 'pi pi-calendar', ruta: '/panel/reservas', permiso: 'RESERVA_GESTIONAR' },
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
