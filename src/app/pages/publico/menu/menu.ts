import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ButtonModule } from 'primeng/button';
import { SkeletonModule } from 'primeng/skeleton';
import { CategoriaMenu } from '../../../core/models/producto.model';
import { PublicoService } from '../../../core/services/publico.service';
import { EstadoVacio } from '../../../shared/components/estado-vacio/estado-vacio';
import { ProductoCard } from '../../../shared/components/producto-card/producto-card';

@Component({
  selector: 'app-menu',
  imports: [ButtonModule, SkeletonModule, ProductoCard, EstadoVacio],
  templateUrl: './menu.html',
  styleUrl: './menu.scss',
})
export class Menu implements OnInit {
  private readonly publico = inject(PublicoService);

  protected readonly categorias = signal<CategoriaMenu[]>([]);
  protected readonly cargando = signal(true);
  protected readonly fallo = signal(false);
  protected readonly seleccionada = signal<string | null>(null);

  protected readonly categoriaActual = computed(
    () => this.categorias().find((c) => c.codigo === this.seleccionada()) ?? this.categorias()[0] ?? null,
  );

  ngOnInit(): void {
    this.publico.menu().subscribe({
      next: (menu) => {
        this.categorias.set(menu.categorias);
        this.cargando.set(false);
      },
      error: () => {
        this.fallo.set(true);
        this.cargando.set(false);
      },
    });
  }

  protected elegir(codigo: string): void {
    this.seleccionada.set(codigo);
  }
}
