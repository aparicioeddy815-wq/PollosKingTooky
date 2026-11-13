import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { AvatarModule } from 'primeng/avatar';
import { ButtonModule } from 'primeng/button';
import { MenuModule } from 'primeng/menu';
import { AuthService } from '../../core/services/auth.service';

interface Enlace {
  texto: string;
  ruta: string;
}

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive, ButtonModule, MenuModule, AvatarModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss',
})
export class Navbar {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly menuAbierto = signal(false);

  protected readonly enlaces: Enlace[] = [
    { texto: 'Inicio', ruta: '/' },
    { texto: 'Menú', ruta: '/menu' },
    { texto: 'Turnos', ruta: '/turnos' },
    { texto: 'Nosotros', ruta: '/nosotros' },
    { texto: 'Ubicación', ruta: '/ubicacion' },
  ];

  protected readonly iniciales = computed(() => {
    const nombre = this.auth.sesion()?.nombreCompleto ?? '';
    return nombre
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map((p) => p[0].toUpperCase())
      .join('');
  });

  protected readonly opcionesUsuario = computed<MenuItem[]>(() => {
    const esPersonal = this.auth.esPersonal();
    return [
      {
        label: esPersonal ? 'Panel de gestión' : 'Mi cuenta',
        icon: esPersonal ? 'pi pi-th-large' : 'pi pi-user',
        command: () => this.router.navigate([esPersonal ? '/panel' : '/mi-cuenta']),
      },
      ...(this.auth.tienePermiso('RESERVA_CREAR')
        ? [
            {
              label: 'Reservar mesa',
              icon: 'pi pi-calendar-plus',
              command: () => this.router.navigate(['/mi-cuenta/reservas/nueva']),
            },
          ]
        : []),
      { separator: true },
      { label: 'Cerrar sesión', icon: 'pi pi-sign-out', command: () => this.cerrarSesion() },
    ];
  });

  protected alternarMenu(): void {
    this.menuAbierto.update((abierto) => !abierto);
  }

  protected cerrarMenu(): void {
    this.menuAbierto.set(false);
  }

  private cerrarSesion(): void {
    this.auth.logout();
    this.router.navigate(['/']);
  }
}
