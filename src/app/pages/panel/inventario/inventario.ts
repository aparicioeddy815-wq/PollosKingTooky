import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { TextareaModule } from 'primeng/textarea';
import {
  Cobertura,
  GraficasInventario,
  Insumo,
  Merma,
  MotivoMerma,
  Proveedor,
  ResumenInventario,
} from '../../../core/models/inventario.model';
import { AuthService } from '../../../core/services/auth.service';
import { AvisoService } from '../../../core/services/aviso.service';
import { GraficasService } from '../../../core/services/graficas.service';
import { InventarioService } from '../../../core/services/inventario.service';
import { guardarArchivo } from '../../../core/utils/descargas';
import { MOTIVOS_MERMA, etiquetaDe } from '../../../core/utils/etiquetas';
import { etiquetaDia } from '../../../core/utils/fechas';
import { Grafica, SerieGrafica } from '../../../shared/components/grafica/grafica';
import { BolivianosPipe } from '../../../shared/pipes/bolivianos.pipe';

const ETIQUETA_ESTADO: Record<string, string> = { CRITICO: 'Crítico', BAJO: 'Bajo', NORMAL: 'Normal' };

@Component({
  selector: 'app-inventario',
  imports: [
    DatePipe,
    DecimalPipe,
    FormsModule,
    ReactiveFormsModule,
    ButtonModule,
    DialogModule,
    InputNumberModule,
    InputTextModule,
    SelectModule,
    TableModule,
    TagModule,
    TextareaModule,
    Grafica,
    BolivianosPipe,
  ],
  template: `
    <header class="encabezado">
      <div class="cabecera-pagina">
        <h1>Inventario</h1>
        <p>Existencias, alertas y proyección de consumo a {{ resumen()?.diasProyeccion ?? 7 }} días.</p>
      </div>
      <div class="acciones-cabecera">
        @if (puedeEscribir) {
          <p-button label="Registrar entrada" icon="pi pi-plus" (onClick)="abrirEntrada()" />
          <p-button label="Registrar merma" icon="pi pi-minus-circle" severity="secondary" (onClick)="abrirMerma()" />
        }
        <p-button label="Historial de mermas" icon="pi pi-history" severity="secondary" [outlined]="true" (onClick)="alternarMermas()" />
        <p-button label="Excel" icon="pi pi-file-excel" severity="secondary" [text]="true" (onClick)="descargar('xlsx')" />
        <p-button label="PDF" icon="pi pi-file-pdf" severity="secondary" [text]="true" (onClick)="descargar('pdf')" />
      </div>
    </header>

    @if (resumen(); as r) {
      <div class="tarjetas">
        <article class="tarjeta" [class.alerta]="r.alertasUrgentes > 0">
          <span>Alertas urgentes</span>
          <strong>{{ r.alertasUrgentes }}</strong>
          <small>insumos en stock crítico</small>
        </article>
        <article class="tarjeta">
          <span>Abastecimiento</span>
          <strong>{{ r.abastecimientoNecesario }}</strong>
          <small>con déficit proyectado a {{ r.diasProyeccion }} días</small>
        </article>
        <article class="tarjeta">
          <span>Valoración del inventario</span>
          <strong>{{ r.valoracionInventario | bs }}</strong>
          <small>{{ r.totalInsumos }} insumos de alimentos</small>
        </article>
        <article class="tarjeta">
          <span>Artículos a reponer</span>
          <strong>{{ r.articulosAReponer }}</strong>
          <small>críticos + bajos</small>
        </article>
      </div>
      @if (r.articulosAReponer > 0) {
        <p class="aviso-reponer"><i class="pi pi-exclamation-triangle"></i> {{ r.articulosAReponer }} artículos deben reponerse sí o sí.</p>
      }
    }

    <section class="graficas">
      <article class="bloque">
        <h2>Valor del stock por categoría</h2>
        <app-grafica tipo="barras" formato="bs" titulo="Valor del stock por categoría" [etiquetas]="etiquetasCategoria()" [series]="seriesCategoria()" />
      </article>

      <article class="bloque">
        <h2>Estado del stock</h2>
        <app-grafica tipo="dona" titulo="Insumos por estado" centro="insumos" [etiquetas]="etiquetasEstado()" [series]="seriesEstado()" [colores]="coloresEstado" />
      </article>

      <article class="bloque ancho">
        <h2>Entradas, consumo y mermas · últimos {{ graf()?.dias ?? 14 }} días (en Bs)</h2>
        <app-grafica tipo="linea" formato="bs" titulo="Movimientos diarios" [etiquetas]="etiquetasMov()" [series]="seriesMov()" />
      </article>

      <article class="bloque">
        <h2>Mermas por motivo · últimos 30 días</h2>
        <app-grafica tipo="barras" formato="bs" titulo="Mermas por motivo" [etiquetas]="etiquetasMerma()" [series]="seriesMerma()" [colores]="coloresMerma" />
      </article>

      <article class="bloque">
        <h2>Menor cobertura de stock</h2>
        @if (cobertura().length === 0) {
          <p class="texto-tenue">Sin datos todavía.</p>
        } @else {
          <ul class="cobertura">
            @for (c of cobertura(); track c.idInsumo) {
              <li>
                <div class="linea-texto">
                  <span>{{ c.nombre }}</span>
                  <strong>
                    {{ c.stockActual | number: '1.0-2' }} / {{ c.umbralMinimo | number: '1.0-2' }} {{ c.unidadMedida }}
                    @if (c.diasCobertura !== null) {
                      <small>· ~{{ c.diasCobertura | number: '1.0-1' }} días</small>
                    }
                  </strong>
                </div>
                <div class="pista"><span class="relleno" [class.critico]="c.estado === 'CRITICO'" [class.bajo]="c.estado === 'BAJO'" [style.width.%]="porcentaje(c)"></span></div>
              </li>
            }
          </ul>
        }
      </article>
    </section>

    @if (verMermas()) {
      <section class="bloque separado">
        <h2>Historial de mermas</h2>
        <p-table [value]="mermas()" [paginator]="true" [rows]="10" [tableStyle]="{ 'min-width': '44rem' }">
          <ng-template #header>
            <tr><th>Fecha</th><th>Insumo</th><th>Cantidad</th><th>Motivo</th><th>Costo</th><th>Registrado por</th></tr>
          </ng-template>
          <ng-template #body let-m>
            <tr>
              <td>{{ m.fechaHora | date: 'dd/MM/yyyy HH:mm' }}</td>
              <td>{{ m.insumo }}</td>
              <td>{{ m.cantidad | number: '1.0-3' }} {{ m.unidadMedida }}</td>
              <td>{{ nombreMotivo(m.motivo) }}</td>
              <td>{{ m.costoTotal | bs }}</td>
              <td>{{ m.registradoPor }}</td>
            </tr>
          </ng-template>
          <ng-template #emptymessage>
            <tr><td colspan="6" class="vacio">No hay mermas en los últimos 30 días.</td></tr>
          </ng-template>
        </p-table>
      </section>
    }

    <div class="filtros">
      <input pInputText type="search" placeholder="Buscar insumo" aria-label="Buscar insumo" [ngModel]="busqueda()" (ngModelChange)="busqueda.set($event)" />
      <p-select [options]="categorias()" optionLabel="etiqueta" optionValue="valor" [ngModel]="categoriaFiltro()" (ngModelChange)="categoriaFiltro.set($event)" [showClear]="true" placeholder="Todas las categorías" />
      <p-select [options]="estadosFiltro" optionLabel="etiqueta" optionValue="valor" [ngModel]="estadoFiltro()" (ngModelChange)="estadoFiltro.set($event)" [showClear]="true" placeholder="Todos los estados" />
      <p-button icon="pi pi-refresh" severity="secondary" [text]="true" ariaLabel="Actualizar" (onClick)="refrescar()" />
    </div>

    <p-table [value]="filtrados()" dataKey="idInsumo" [loading]="cargando()" [paginator]="true" [rows]="15" [tableStyle]="{ 'min-width': '60rem' }">
      <ng-template #header>
        <tr>
          <th>Insumo</th>
          <th>Categoría</th>
          <th>Stock actual</th>
          <th>Umbral mínimo</th>
          <th>Consumo hoy</th>
          <th>Estado</th>
          <th>Proyección</th>
        </tr>
      </ng-template>
      <ng-template #body let-i>
        <tr>
          <td>
            <strong>{{ i.nombre }}</strong>
            <div class="sub">{{ i.codigo }} · actualizado {{ i.actualizadoEn | date: 'dd/MM HH:mm' }}</div>
          </td>
          <td>{{ i.categoria }}</td>
          <td>{{ i.stockActual | number: '1.0-2' }} {{ i.unidadMedida }}</td>
          <td>{{ i.umbralMinimo | number: '1.0-2' }} {{ i.unidadMedida }}</td>
          <td>{{ i.consumoHoy | number: '1.0-2' }} {{ i.unidadMedida }}</td>
          <td><p-tag [value]="textoEstado(i.estado)" [severity]="severidad(i.estado)" /></td>
          <td>
            @if (i.deficitProyectado > 0) {
              <span class="deficit">Déficit {{ i.deficitProyectado | number: '1.0-2' }} {{ i.unidadMedida }}</span>
            } @else {
              <span class="superavit">Superávit seguro {{ i.superavitSeguro | number: '1.0-2' }} {{ i.unidadMedida }}</span>
            }
            <div class="sub">Consumo proyectado: {{ i.consumoProyectado | number: '1.0-2' }} {{ i.unidadMedida }}</div>
          </td>
        </tr>
      </ng-template>
      <ng-template #emptymessage>
        <tr><td colspan="7" class="vacio">No encontramos insumos con esos filtros.</td></tr>
      </ng-template>
    </p-table>

    <p-dialog header="Registrar entrada de mercadería" [visible]="entradaAbierta()" (visibleChange)="entradaAbierta.set($event)" [modal]="true" [style]="{ width: '480px' }">
      <form class="formulario" [formGroup]="formEntrada" (ngSubmit)="guardarEntrada()" novalidate>
        <div class="campo">
          <label for="ent-insumo">Insumo</label>
          <p-select inputId="ent-insumo" [options]="todos()" optionLabel="nombre" optionValue="idInsumo" formControlName="idInsumo" [filter]="true" filterBy="nombre" appendTo="body" placeholder="Elige un insumo" />
          @if (formEntrada.controls.idInsumo.touched && formEntrada.controls.idInsumo.invalid) {
            <small class="error">Elige un insumo.</small>
          }
        </div>
        <div class="fila">
          <div class="campo">
            <label for="ent-cantidad">Cantidad</label>
            <p-inputnumber inputId="ent-cantidad" formControlName="cantidad" mode="decimal" [minFractionDigits]="0" [maxFractionDigits]="3" [min]="0" />
            @if (formEntrada.controls.cantidad.touched && formEntrada.controls.cantidad.invalid) {
              <small class="error">Indica una cantidad mayor a 0.</small>
            }
          </div>
          <div class="campo">
            <label for="ent-costo">Costo unitario (Bs, opcional)</label>
            <p-inputnumber inputId="ent-costo" formControlName="costoUnitario" mode="decimal" [minFractionDigits]="2" [maxFractionDigits]="2" [min]="0" />
          </div>
        </div>
        <div class="campo">
          <label for="ent-proveedor">Proveedor (opcional)</label>
          <p-select inputId="ent-proveedor" [options]="proveedores()" optionLabel="nombre" optionValue="idProveedor" formControlName="idProveedor" [showClear]="true" appendTo="body" placeholder="Sin proveedor" />
        </div>
        <div class="campo">
          <label for="ent-doc">Documento (opcional)</label>
          <input pInputText id="ent-doc" formControlName="documento" placeholder="Factura o nota de entrega" />
        </div>
      </form>
      <ng-template #footer>
        <p-button label="Cancelar" [text]="true" (onClick)="entradaAbierta.set(false)" />
        <p-button label="Guardar entrada" [loading]="guardando()" (onClick)="guardarEntrada()" />
      </ng-template>
    </p-dialog>

    <p-dialog header="Registrar merma" [visible]="mermaAbierta()" (visibleChange)="mermaAbierta.set($event)" [modal]="true" [style]="{ width: '480px' }">
      <form class="formulario" [formGroup]="formMerma" (ngSubmit)="guardarMerma()" novalidate>
        <div class="campo">
          <label for="mer-insumo">Insumo</label>
          <p-select inputId="mer-insumo" [options]="todos()" optionLabel="nombre" optionValue="idInsumo" formControlName="idInsumo" [filter]="true" filterBy="nombre" appendTo="body" placeholder="Elige un insumo" />
          @if (formMerma.controls.idInsumo.touched && formMerma.controls.idInsumo.invalid) {
            <small class="error">Elige un insumo.</small>
          }
        </div>
        <div class="fila">
          <div class="campo">
            <label for="mer-cantidad">Cantidad</label>
            <p-inputnumber inputId="mer-cantidad" formControlName="cantidad" mode="decimal" [minFractionDigits]="0" [maxFractionDigits]="3" [min]="0" />
            @if (formMerma.controls.cantidad.touched && formMerma.controls.cantidad.invalid) {
              <small class="error">Indica una cantidad mayor a 0.</small>
            }
          </div>
          <div class="campo">
            <label for="mer-motivo">Motivo</label>
            <p-select inputId="mer-motivo" [options]="motivos" optionLabel="etiqueta" optionValue="valor" formControlName="motivo" appendTo="body" />
          </div>
        </div>
        <div class="campo">
          <label for="mer-obs">Observación (opcional)</label>
          <textarea pTextarea id="mer-obs" formControlName="observacion" rows="3"></textarea>
        </div>
      </form>
      <ng-template #footer>
        <p-button label="Cancelar" [text]="true" (onClick)="mermaAbierta.set(false)" />
        <p-button label="Guardar merma" [loading]="guardando()" (onClick)="guardarMerma()" />
      </ng-template>
    </p-dialog>
  `,
  styles: [
    `
      .encabezado {
        display: flex;
        flex-wrap: wrap;
        align-items: flex-start;
        justify-content: space-between;
        gap: 1rem;
      }
      .acciones-cabecera {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: 0.5rem;
      }
      .tarjetas {
        display: grid;
        gap: 1rem;
        margin-bottom: 1.25rem;
        grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
      }
      .tarjeta {
        display: grid;
        gap: 0.3rem;
        padding: 1.25rem 1.4rem;
        border-radius: var(--kt-radio);
        background: var(--kt-superficie);
      }
      .tarjeta span {
        font-size: 0.88rem;
        font-weight: 600;
        color: var(--kt-texto-suave);
      }
      .tarjeta strong {
        font-family: var(--kt-fuente-titulo);
        font-size: clamp(1.5rem, 2.4vw, 1.9rem);
        font-weight: 800;
        line-height: 1.1;
        white-space: nowrap;
      }
      .tarjeta small {
        color: var(--kt-texto-tenue);
      }
      .tarjeta.alerta strong {
        color: var(--kt-primary-40);
      }
      .aviso-reponer {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        margin: 0 0 1.5rem;
        padding: 0.75rem 1rem;
        border-radius: var(--kt-radio-chico);
        background: var(--kt-primary-90);
        color: var(--kt-primary-20);
        font-weight: 600;
      }
      .graficas {
        display: grid;
        gap: 1.5rem;
        margin-bottom: 2rem;
      }
      @media (min-width: 960px) {
        .graficas {
          grid-template-columns: 1fr 1fr;
          align-items: start;
        }
        .ancho {
          grid-column: 1 / -1;
        }
      }
      .bloque {
        min-width: 0;
        padding: 1.5rem;
        border-radius: var(--kt-radio-grande);
        background: var(--kt-superficie);
      }
      .bloque h2 {
        margin-bottom: 1rem;
        font-size: 1.15rem;
      }
      .separado {
        margin-bottom: 2rem;
      }
      .cobertura {
        display: grid;
        gap: 0.9rem;
        margin: 0;
        padding: 0;
        list-style: none;
      }
      .linea-texto {
        display: flex;
        justify-content: space-between;
        gap: 1rem;
        margin-bottom: 0.3rem;
        font-size: 0.92rem;
      }
      .linea-texto small {
        font-weight: 500;
        color: var(--kt-texto-tenue);
      }
      .pista {
        height: 8px;
        overflow: hidden;
        border-radius: 999px;
        background: var(--kt-superficie-alta);
      }
      .relleno {
        display: block;
        height: 100%;
        border-radius: 999px;
        background: var(--kt-exito);
        transition: width 0.4s ease;
      }
      .relleno.bajo {
        background: var(--kt-tertiary-70);
      }
      .relleno.critico {
        background: var(--kt-accion);
      }
      .filtros {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: 0.75rem;
        margin-bottom: 1rem;
      }
      .filtros input {
        min-width: 240px;
      }
      .sub {
        font-size: 0.82rem;
        color: var(--kt-texto-tenue);
      }
      .deficit {
        font-weight: 700;
        color: var(--kt-primary-40);
      }
      .superavit {
        font-weight: 700;
        color: var(--kt-exito);
      }
      .vacio {
        padding: 2rem;
        text-align: center;
        color: var(--kt-texto-tenue);
      }
    `,
  ],
})
export class Inventario implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(InventarioService);
  private readonly graficasApi = inject(GraficasService);
  private readonly aviso = inject(AvisoService);
  private readonly auth = inject(AuthService);

  protected readonly puedeEscribir = this.auth.tienePermiso('INVENTARIO_ESCRIBIR');
  protected readonly motivos = MOTIVOS_MERMA;
  protected readonly estadosFiltro = [
    { etiqueta: 'Crítico', valor: 'CRITICO' },
    { etiqueta: 'Bajo', valor: 'BAJO' },
    { etiqueta: 'Normal', valor: 'NORMAL' },
  ];
  protected readonly coloresEstado = ['var(--kt-primary-40)', 'var(--kt-tertiary-70)', 'var(--kt-exito)'];
  protected readonly coloresMerma = [
    'var(--kt-primary-40)',
    'var(--kt-secondary-50)',
    'var(--kt-tertiary-70)',
    'var(--kt-neutral-40)',
    'var(--kt-primary-70)',
  ];

  protected readonly todos = signal<Insumo[]>([]);
  protected readonly resumen = signal<ResumenInventario | null>(null);
  protected readonly graf = signal<GraficasInventario | null>(null);
  protected readonly proveedores = signal<Proveedor[]>([]);
  protected readonly mermas = signal<Merma[]>([]);
  protected readonly verMermas = signal(false);
  protected readonly cargando = signal(true);
  protected readonly guardando = signal(false);
  protected readonly entradaAbierta = signal(false);
  protected readonly mermaAbierta = signal(false);

  protected readonly busqueda = signal('');
  protected readonly categoriaFiltro = signal<number | null>(null);
  protected readonly estadoFiltro = signal<string | null>(null);

  protected readonly categorias = computed(() => {
    const mapa = new Map<number, string>();
    this.todos().forEach((i) => mapa.set(i.idCategoria, i.categoria));
    return [...mapa.entries()]
      .map(([valor, etiqueta]) => ({ etiqueta, valor }))
      .sort((a, b) => a.etiqueta.localeCompare(b.etiqueta));
  });

  protected readonly filtrados = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    const categoria = this.categoriaFiltro();
    const estado = this.estadoFiltro();
    return this.todos().filter(
      (i) =>
        (!texto || i.nombre.toLowerCase().includes(texto) || i.codigo.toLowerCase().includes(texto)) &&
        (categoria === null || i.idCategoria === categoria) &&
        (!estado || i.estado === estado),
    );
  });

  // ---- datos de las gráficas
  protected readonly etiquetasCategoria = computed(() => this.graf()?.valorPorCategoria.map((p) => p.etiqueta) ?? []);
  protected readonly seriesCategoria = computed<SerieGrafica[]>(() => [
    { nombre: 'Valor del stock', valores: this.graf()?.valorPorCategoria.map((p) => p.valor) ?? [] },
  ]);
  protected readonly etiquetasEstado = computed(() => this.graf()?.estadoDelStock.map((p) => this.textoEstado(p.etiqueta)) ?? []);
  protected readonly seriesEstado = computed<SerieGrafica[]>(() => [
    { nombre: 'Insumos', valores: this.graf()?.estadoDelStock.map((p) => p.valor) ?? [] },
  ]);
  protected readonly etiquetasMov = computed(() => this.graf()?.movimientosDiarios.map((m) => etiquetaDia(m.fecha)) ?? []);
  protected readonly seriesMov = computed<SerieGrafica[]>(() => {
    const dias = this.graf()?.movimientosDiarios ?? [];
    return [
      { nombre: 'Entradas', valores: dias.map((d) => d.entradas) },
      { nombre: 'Consumo', valores: dias.map((d) => d.consumos) },
      { nombre: 'Mermas', valores: dias.map((d) => d.mermas) },
    ];
  });
  protected readonly etiquetasMerma = computed(() => this.graf()?.mermasPorMotivo.map((p) => this.nombreMotivo(p.etiqueta)) ?? []);
  protected readonly seriesMerma = computed<SerieGrafica[]>(() => [
    { nombre: 'Mermas', valores: this.graf()?.mermasPorMotivo.map((p) => p.valor) ?? [] },
  ]);
  protected readonly cobertura = computed(() => this.graf()?.menorCobertura ?? []);

  // ---- formularios
  protected readonly formEntrada = this.fb.group({
    idInsumo: this.fb.control<number | null>(null, Validators.required),
    cantidad: this.fb.control<number | null>(null, [Validators.required, Validators.min(0.001)]),
    idProveedor: this.fb.control<number | null>(null),
    costoUnitario: this.fb.control<number | null>(null, Validators.min(0)),
    documento: this.fb.control<string | null>(null, Validators.maxLength(60)),
  });

  protected readonly formMerma = this.fb.group({
    idInsumo: this.fb.control<number | null>(null, Validators.required),
    cantidad: this.fb.control<number | null>(null, [Validators.required, Validators.min(0.001)]),
    motivo: this.fb.nonNullable.control<MotivoMerma>('DETERIORO', Validators.required),
    observacion: this.fb.control<string | null>(null, Validators.maxLength(300)),
  });

  ngOnInit(): void {
    this.refrescar();
    if (this.puedeEscribir) {
      this.api.proveedores().subscribe({ next: (lista) => this.proveedores.set(lista), error: () => undefined });
    }
  }

  protected refrescar(): void {
    this.cargando.set(true);
    this.api.listar().subscribe({
      next: (lista) => {
        this.todos.set(lista);
        this.cargando.set(false);
      },
      error: (e) => {
        this.aviso.error(e, 'No pudimos cargar el inventario');
        this.cargando.set(false);
      },
    });
    this.api.resumen().subscribe({ next: (r) => this.resumen.set(r), error: () => undefined });
    this.graficasApi.inventario(14).subscribe({ next: (g) => this.graf.set(g), error: () => undefined });
    if (this.verMermas()) this.cargarMermas();
  }

  protected alternarMermas(): void {
    this.verMermas.update((v) => !v);
    if (this.verMermas()) this.cargarMermas();
  }

  private cargarMermas(): void {
    this.api.mermas(0, 50).subscribe({ next: (p) => this.mermas.set(p.contenido), error: (e) => this.aviso.error(e) });
  }

  protected descargar(formato: 'xlsx' | 'pdf'): void {
    this.api.exportar(formato).subscribe({
      next: (blob) => guardarArchivo(blob, `inventario.${formato}`),
      error: (e) => this.aviso.error(e, 'No se pudo descargar'),
    });
  }

  protected abrirEntrada(): void {
    this.formEntrada.reset();
    this.entradaAbierta.set(true);
  }

  protected abrirMerma(): void {
    this.formMerma.reset({ motivo: 'DETERIORO' });
    this.mermaAbierta.set(true);
  }

  protected guardarEntrada(): void {
    if (this.formEntrada.invalid) {
      this.formEntrada.markAllAsTouched();
      return;
    }
    const v = this.formEntrada.getRawValue();
    this.guardando.set(true);
    this.api
      .registrarEntrada({
        idInsumo: v.idInsumo as number,
        cantidad: v.cantidad as number,
        idProveedor: v.idProveedor,
        costoUnitario: v.costoUnitario,
        documento: v.documento?.trim() || null,
      })
      .subscribe({
        next: () => {
          this.aviso.exito('Entrada registrada.');
          this.entradaAbierta.set(false);
          this.guardando.set(false);
          this.refrescar();
        },
        error: (e) => {
          this.aviso.error(e);
          this.guardando.set(false);
        },
      });
  }

  protected guardarMerma(): void {
    if (this.formMerma.invalid) {
      this.formMerma.markAllAsTouched();
      return;
    }
    const v = this.formMerma.getRawValue();
    this.guardando.set(true);
    this.api
      .registrarMerma({
        idInsumo: v.idInsumo as number,
        cantidad: v.cantidad as number,
        motivo: v.motivo,
        observacion: v.observacion?.trim() || null,
      })
      .subscribe({
        next: () => {
          this.aviso.exito('Merma registrada.');
          this.mermaAbierta.set(false);
          this.guardando.set(false);
          this.refrescar();
        },
        error: (e) => {
          this.aviso.error(e);
          this.guardando.set(false);
        },
      });
  }

  protected textoEstado(estado: string): string {
    return ETIQUETA_ESTADO[estado] ?? estado;
  }

  protected severidad(estado: string): 'danger' | 'warn' | 'success' {
    return estado === 'CRITICO' ? 'danger' : estado === 'BAJO' ? 'warn' : 'success';
  }

  protected nombreMotivo(motivo: string): string {
    return etiquetaDe<string>(MOTIVOS_MERMA, motivo);
  }

  protected porcentaje(c: Cobertura): number {
    // Barra llena = el doble del mínimo
    return Math.min(100, Math.round((c.stockActual / Math.max(c.umbralMinimo * 2, 0.001)) * 100));
  }
}
