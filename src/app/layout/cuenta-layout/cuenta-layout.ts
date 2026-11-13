import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-cuenta-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './cuenta-layout.html',
  styleUrl: './cuenta-layout.scss',
})
export class CuentaLayout {
  private readonly auth = inject(AuthService);

  protected readonly nombre = computed(() => this.auth.sesion()?.nombreCompleto ?? '');
  protected readonly puedeReservar = computed(() => this.auth.tienePermiso('RESERVA_CREAR'));
}
