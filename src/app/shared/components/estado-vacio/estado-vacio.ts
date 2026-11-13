import { Component, input } from '@angular/core';

/** Mensaje para listas sin datos: explica qué pasó y qué se puede hacer. */
@Component({
  selector: 'app-estado-vacio',
  templateUrl: './estado-vacio.html',
  styleUrl: './estado-vacio.scss',
})
export class EstadoVacio {
  readonly icono = input('pi pi-inbox');
  readonly titulo = input.required<string>();
  readonly detalle = input('');
}
