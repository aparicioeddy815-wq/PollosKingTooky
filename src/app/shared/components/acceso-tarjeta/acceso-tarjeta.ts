import { Component, input } from '@angular/core';

/** Marco común de las pantallas de acceso: logo, título y contenido. */
@Component({
  selector: 'app-acceso-tarjeta',
  templateUrl: './acceso-tarjeta.html',
  styleUrl: './acceso-tarjeta.scss',
})
export class AccesoTarjeta {
  readonly titulo = input.required<string>();
  readonly subtitulo = input('');
}
