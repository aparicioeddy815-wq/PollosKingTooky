import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { CategoriaMenu, ProductoPublico } from '../../../core/models/producto.model';
import { TipoPedido } from '../../../core/models/pedido.model';
import { AvisoService } from '../../../core/services/aviso.service';
import { PedidoService } from '../../../core/services/pedido.service';
import { PublicoService } from '../../../core/services/publico.service';
import { TIPOS_PEDIDO } from '../../../core/utils/etiquetas';
import { BolivianosPipe } from '../../../shared/pipes/bolivianos.pipe';

interface LineaTicket {
  producto: ProductoPublico;
  cantidad: number;
}

@Component({
  selector: 'app-pedido-nuevo',
  imports: [FormsModule, ButtonModule, SelectModule, BolivianosPipe],
  templateUrl: './pedido-nuevo.html',
  styleUrl: './pedido-nuevo.scss',
})
export class PedidoNuevo implements OnInit {
  private readonly publico = inject(PublicoService);
  private readonly pedidosApi = inject(PedidoService);
  private readonly aviso = inject(AvisoService);
  private readonly router = inject(Router);

  protected readonly tipos = TIPOS_PEDIDO;
  protected tipoPedido: TipoPedido = 'LOCAL';

  protected readonly categorias = signal<CategoriaMenu[]>([]);
  protected readonly seleccionada = signal<string | null>(null);
  protected readonly ticket = signal<LineaTicket[]>([]);
  protected readonly enviando = signal(false);

  protected readonly categoriaActual = computed(
    () => this.categorias().find((c) => c.codigo === this.seleccionada()) ?? this.categorias()[0] ?? null,
  );

  protected readonly total = computed(() =>
    this.ticket().reduce((suma, linea) => suma + linea.producto.precio * linea.cantidad, 0),
  );

  ngOnInit(): void {
    this.publico.menu().subscribe({
      next: (menu) => this.categorias.set(menu.categorias),
      error: (e) => this.aviso.error(e, 'No pudimos cargar el menú'),
    });
  }

  protected agregar(producto: ProductoPublico): void {
    this.ticket.update((lineas) => {
      const existente = lineas.find((l) => l.producto.idProducto === producto.idProducto);
      if (existente) {
        return lineas.map((l) => (l === existente ? { ...l, cantidad: l.cantidad + 1 } : l));
      }
      return [...lineas, { producto, cantidad: 1 }];
    });
  }

  protected cambiar(linea: LineaTicket, cambio: number): void {
    this.ticket.update((lineas) =>
      lineas
        .map((l) => (l === linea ? { ...l, cantidad: l.cantidad + cambio } : l))
        .filter((l) => l.cantidad > 0),
    );
  }

  protected vaciar(): void {
    this.ticket.set([]);
  }

  protected confirmar(): void {
    if (this.ticket().length === 0) {
      this.aviso.info('Agrega al menos un producto al ticket.');
      return;
    }
    this.enviando.set(true);
    this.pedidosApi
      .crear({
        tipoPedido: this.tipoPedido,
        idUsuarioCliente: null,
        items: this.ticket().map((l) => ({
          idProducto: l.producto.idProducto,
          cantidad: l.cantidad,
          observaciones: null,
          guarniciones: [],
        })),
      })
      .subscribe({
        next: (pedido) => {
          this.aviso.exito(`Pedido registrado. Turno #${pedido.numeroTurno}.`, 'Listo');
          this.router.navigate(['/panel/pedidos']);
        },
        error: (e) => {
          this.aviso.error(e);
          this.enviando.set(false);
        },
      });
  }
}
