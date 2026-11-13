import { Component, input } from '@angular/core';
import { BolivianosPipe } from '../../pipes/bolivianos.pipe';

export interface ProductoMostrable {
  nombre: string;
  descripcion: string | null;
  precio: number;
  imagenUrl: string | null;
  disponible: boolean;
}

/** Tarjeta de producto. El contenido proyectado se muestra al pie (botones, corazón, etc.). */
@Component({
  selector: 'app-producto-card',
  imports: [BolivianosPipe],
  templateUrl: './producto-card.html',
  styleUrl: './producto-card.scss',
})
export class ProductoCard {
  readonly producto = input.required<ProductoMostrable>();
}
