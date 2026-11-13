import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, of, switchMap, timer } from 'rxjs';
import { TurnosPublico } from '../../../core/models/publico.model';
import { PublicoService } from '../../../core/services/publico.service';

@Component({
  selector: 'app-turnos',
  templateUrl: './turnos.html',
  styleUrl: './turnos.scss',
})
export class Turnos {
  private readonly publico = inject(PublicoService);

  protected readonly turnos = signal<TurnosPublico | null>(null);
  protected readonly sinConexion = signal(false);

  constructor() {
    // Consulta cada 5 segundos mientras la pantalla esté abierta
    timer(0, 5000)
      .pipe(
        switchMap(() =>
          this.publico.turnos().pipe(
            catchError(() => {
              this.sinConexion.set(true);
              return of(null);
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((respuesta) => {
        if (respuesta) {
          this.turnos.set(respuesta);
          this.sinConexion.set(false);
        }
      });
  }
}
