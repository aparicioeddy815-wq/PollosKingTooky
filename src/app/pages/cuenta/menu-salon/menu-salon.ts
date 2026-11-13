import { Component, OnInit, inject, signal } from '@angular/core';
import { CategoriaSalon, ProductoSalon } from '../../../core/models/cliente.model';
import { AvisoService } from '../../../core/services/aviso.service';
import { ClienteService } from '../../../core/services/cliente.service';
import { EstadoVacio } from '../../../shared/components/estado-vacio/estado-vacio';
import { ProductoCard } from '../../../shared/components/producto-card/producto-card';

@Component({
  selector: 'app-menu-salon',
  imports: [ProductoCard, EstadoVacio],
  templateUrl: './menu-salon.html',
  styleUrl: './menu-salon.scss',
})
export class MenuSalon implements OnInit {
  private readonly cliente = inject(ClienteService);
  private readonly aviso = inject(AvisoService);

  protected readonly categorias = signal<CategoriaSalon[]>([]);
  protected readonly cargando = signal(true);

  ngOnInit(): void {
    this.cliente.menuSalon().subscribe({
      next: (categorias) => {
        this.categorias.set(categorias);
        this.cargando.set(false);
      },
      error: (e) => {
        this.aviso.error(e, 'No pudimos cargar el menú');
        this.cargando.set(false);
      },
    });
  }

  protected alternarMeGusta(producto: ProductoSalon): void {
    this.cliente.alternarMeGusta(producto.idProducto).subscribe({
      next: (respuesta) => {
        this.categorias.update((categorias) =>
          categorias.map((categoria) => ({
            ...categoria,
            productos: categoria.productos.map((p) =>
              p.idProducto === respuesta.idProducto ? { ...p, meGusta: respuesta.meGusta } : p,
            ),
          })),
        );
      },
      error: (e) => this.aviso.error(e),
    });
  }
}
