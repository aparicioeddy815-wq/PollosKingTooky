import { Component, inject, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConfirmationService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { TableLazyLoadEvent, TableModule } from 'primeng/table';
import { TextareaModule } from 'primeng/textarea';
import { ToggleSwitchModule } from 'primeng/toggleswitch';
import { CategoriaProducto, Producto } from '../../../core/models/producto.model';
import { AuthService } from '../../../core/services/auth.service';
import { AvisoService } from '../../../core/services/aviso.service';
import { ProductoService } from '../../../core/services/producto.service';
import { CATEGORIAS_PRODUCTO, etiquetaDe } from '../../../core/utils/etiquetas';
import { BolivianosPipe } from '../../../shared/pipes/bolivianos.pipe';

@Component({
  selector: 'app-productos',
  imports: [
    FormsModule,
    ReactiveFormsModule,
    ButtonModule,
    DialogModule,
    InputNumberModule,
    InputTextModule,
    SelectModule,
    TableModule,
    TextareaModule,
    ToggleSwitchModule,
    BolivianosPipe,
  ],
  templateUrl: './productos.html',
  styleUrl: './productos.scss',
})
export class Productos {
  private readonly fb = inject(FormBuilder);
  private readonly productosApi = inject(ProductoService);
  private readonly aviso = inject(AvisoService);
  private readonly confirmar = inject(ConfirmationService);
  protected readonly auth = inject(AuthService);

  protected readonly categorias = CATEGORIAS_PRODUCTO;
  protected readonly filas = 10;

  protected readonly productos = signal<Producto[]>([]);
  protected readonly total = signal(0);
  protected readonly cargando = signal(false);

  protected busqueda = '';
  protected categoriaFiltro: string | null = null;
  private paginaActual = 0;

  // Diálogo de creación y edición
  protected readonly dialogoAbierto = signal(false);
  protected readonly editando = signal<Producto | null>(null);
  protected readonly guardando = signal(false);

  protected readonly formulario = this.fb.group({
    nombre: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(255)]),
    descripcion: this.fb.control<string | null>(null),
    precio: this.fb.control<number | null>(null, [Validators.required, Validators.min(0)]),
    categoria: this.fb.control<CategoriaProducto | null>(null, Validators.required),
    imagenUrl: this.fb.control<string | null>(null, Validators.maxLength(2000)),
    disponible: this.fb.nonNullable.control(true),
  });

  protected nombreCategoria(categoria: string | null): string {
    return etiquetaDe(CATEGORIAS_PRODUCTO, categoria as CategoriaProducto);
  }

  protected alCargarPagina(evento: TableLazyLoadEvent): void {
    this.paginaActual = Math.floor((evento.first ?? 0) / this.filas);
    this.cargar();
  }

  protected buscar(): void {
    this.paginaActual = 0;
    this.cargar();
  }

  private cargar(): void {
    this.cargando.set(true);
    this.productosApi
      .listar({ q: this.busqueda.trim(), categoria: this.categoriaFiltro, page: this.paginaActual, size: this.filas })
      .subscribe({
        next: (pagina) => {
          this.productos.set(pagina.contenido);
          this.total.set(pagina.totalElementos);
          this.cargando.set(false);
        },
        error: (e) => {
          this.aviso.error(e, 'No pudimos cargar los productos');
          this.cargando.set(false);
        },
      });
  }

  protected abrirNuevo(): void {
    this.editando.set(null);
    this.formulario.reset({ disponible: true });
    this.dialogoAbierto.set(true);
  }

  protected abrirEdicion(producto: Producto): void {
    this.editando.set(producto);
    this.formulario.reset({
      nombre: producto.nombre,
      descripcion: producto.descripcion,
      precio: producto.precio,
      categoria: producto.categoria,
      imagenUrl: producto.imagenUrl,
      disponible: producto.disponible,
    });
    this.dialogoAbierto.set(true);
  }

  protected guardar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const valores = this.formulario.getRawValue();
    const datos = {
      ...valores,
      precio: valores.precio ?? 0,
      descripcion: valores.descripcion?.trim() || null,
      imagenUrl: valores.imagenUrl?.trim() || null,
    };
    const actual = this.editando();
    const peticion = actual ? this.productosApi.actualizar(actual.idProducto, datos) : this.productosApi.crear(datos);

    this.guardando.set(true);
    peticion.subscribe({
      next: () => {
        this.aviso.exito(actual ? 'Producto actualizado.' : 'Producto creado.');
        this.dialogoAbierto.set(false);
        this.guardando.set(false);
        this.cargar();
      },
      error: (e) => {
        this.aviso.error(e);
        this.guardando.set(false);
      },
    });
  }

  protected cambiarDisponibilidad(producto: Producto, disponible: boolean): void {
    this.productosApi.cambiarDisponibilidad(producto.idProducto, disponible).subscribe({
      next: (actualizado) =>
        this.productos.update((lista) => lista.map((p) => (p.idProducto === actualizado.idProducto ? actualizado : p))),
      error: (e) => {
        this.aviso.error(e);
        this.cargar();
      },
    });
  }

  protected eliminar(producto: Producto): void {
    this.confirmar.confirm({
      header: 'Eliminar producto',
      message: `¿Eliminar “${producto.nombre}” del menú?`,
      acceptLabel: 'Sí, eliminar',
      rejectLabel: 'Volver',
      accept: () =>
        this.productosApi.eliminar(producto.idProducto).subscribe({
          next: () => {
            this.aviso.exito('Producto eliminado.');
            this.cargar();
          },
          error: (e) => this.aviso.error(e),
        }),
    });
  }
}
