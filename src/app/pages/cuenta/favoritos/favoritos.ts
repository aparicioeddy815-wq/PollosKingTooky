import { Component, OnInit, inject, signal } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { ProductoSalon } from '../../../core/models/cliente.model';
import { AvisoService } from '../../../core/services/aviso.service';
import { ClienteService } from '../../../core/services/cliente.service';
import { EstadoVacio } from '../../../shared/components/estado-vacio/estado-vacio';
import { BolivianosPipe } from '../../../shared/pipes/bolivianos.pipe';

@Component({
  selector: 'app-favoritos',
  imports: [ButtonModule, EstadoVacio, BolivianosPipe],
  template: `
    <section>
      <h2>Mis favoritos</h2>
      <p class="texto-tenue">Los platos que marcaste con “me gusta” y cuántas veces los has pedido.</p>

      @if (cargando()) {
        <p class="texto-tenue">Cargando…</p>
      } @else if (favoritos().length === 0) {
        <app-estado-vacio icono="pi pi-heart" titulo="Aún no tienes favoritos" detalle="Marca el corazón en el menú del salón para guardarlos aquí." />
      } @else {
        <ul class="rejilla">
          @for (plato of favoritos(); track plato.idProducto) {
            <li class="tarjeta">
              @if (plato.imagenUrl) {
                <img [src]="plato.imagenUrl" [alt]="plato.nombre" loading="lazy" />
              }
              <div class="cuerpo">
                <strong>{{ plato.nombre }}</strong>
                @if (plato.descripcion) { <span class="detalle">{{ plato.descripcion }}</span> }
                <span class="detalle">
                  @if (plato.vecesConsumidas > 0) { Lo pediste {{ plato.vecesConsumidas }} {{ plato.vecesConsumidas === 1 ? 'vez' : 'veces' }} } @else { Aún no lo has pedido }
                </span>
                <div class="pie">
                  <strong class="precio">{{ plato.precio | bs }}</strong>
                  <p-button icon="pi pi-heart-fill" severity="danger" [text]="true" ariaLabel="Quitar de favoritos" (onClick)="quitar(plato)" />
                </div>
                @if (!plato.disponible) { <span class="agotado">No disponible por ahora</span> }
              </div>
            </li>
          }
        </ul>
      }
    </section>
  `,
  styles: [
    `
      .rejilla { display: grid; gap: 1rem; margin: 1.25rem 0 0; padding: 0; list-style: none; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); }
      .tarjeta { overflow: hidden; border-radius: var(--kt-radio-grande); background: var(--kt-superficie); }
      .tarjeta img { display: block; width: 100%; height: 150px; object-fit: cover; }
      .cuerpo { display: grid; gap: 0.35rem; padding: 1rem 1.1rem; }
      .detalle { font-size: 0.88rem; color: var(--kt-texto-suave); }
      .pie { display: flex; align-items: center; justify-content: space-between; margin-top: 0.4rem; }
      .precio { font-family: var(--kt-fuente-titulo); font-size: 1.2rem; }
      .agotado { font-size: 0.82rem; font-weight: 600; color: var(--kt-primary-40); }
    `,
  ],
})
export class Favoritos implements OnInit {
  private readonly api = inject(ClienteService);
  private readonly aviso = inject(AvisoService);

  protected readonly favoritos = signal<ProductoSalon[]>([]);
  protected readonly cargando = signal(true);

  ngOnInit(): void {
    this.api.favoritos().subscribe({
      next: (lista) => {
        this.favoritos.set(lista);
        this.cargando.set(false);
      },
      error: (e) => {
        this.aviso.error(e, 'No pudimos cargar tus favoritos');
        this.cargando.set(false);
      },
    });
  }

  protected quitar(plato: ProductoSalon): void {
    this.api.alternarMeGusta(plato.idProducto).subscribe({
      next: (r) => {
        if (!r.meGusta) this.favoritos.update((lista) => lista.filter((p) => p.idProducto !== plato.idProducto));
      },
      error: (e) => this.aviso.error(e),
    });
  }
}
