import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CarouselModule } from 'primeng/carousel';
import { SkeletonModule } from 'primeng/skeleton';
import { InicioPublico } from '../../../core/models/publico.model';
import { ProductoPublico } from '../../../core/models/producto.model';
import { PublicoService } from '../../../core/services/publico.service';
import { BolivianosPipe } from '../../../shared/pipes/bolivianos.pipe';
import { ProductoCard } from '../../../shared/components/producto-card/producto-card';

@Component({
  selector: 'app-inicio',
  imports: [RouterLink, ButtonModule, CarouselModule, SkeletonModule, ProductoCard, BolivianosPipe],
  templateUrl: './inicio.html',
  styleUrl: './inicio.scss',
})
export class Inicio implements OnInit {
  private readonly publico = inject(PublicoService);

  protected readonly datos = signal<InicioPublico | null>(null);
  protected readonly ofertas = signal<ProductoPublico[]>([]);
  protected readonly cargandoOfertas = signal(true);

  protected readonly urlMapa = computed(() => {
    const ubicacion = this.datos()?.ubicacion;
    return ubicacion ? `https://www.google.com/maps?q=${ubicacion.latitud},${ubicacion.longitud}` : '#';
  });

  protected readonly opcionesCarrusel = [
    { breakpoint: '1100px', numVisible: 2, numScroll: 1 },
    { breakpoint: '700px', numVisible: 1, numScroll: 1 },
  ];

  ngOnInit(): void {
    this.publico.inicio().subscribe({ next: (datos) => this.datos.set(datos) });

    // Las ofertas del día son los combos populares; si no hay, se muestra todo lo disponible
    this.publico.menu().subscribe({
      next: (menu) => {
        const combos = menu.categorias.find((c) => c.codigo === 'COMBOS_POPULARES')?.productos ?? [];
        const todos = menu.categorias.flatMap((c) => c.productos);
        const elegidos = (combos.length ? combos : todos).filter((p) => p.disponible);
        this.ofertas.set(elegidos.slice(0, 8));
        this.cargandoOfertas.set(false);
      },
      error: () => this.cargandoOfertas.set(false),
    });
  }
}
