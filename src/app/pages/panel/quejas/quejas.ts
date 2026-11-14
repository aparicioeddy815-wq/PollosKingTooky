import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormControl, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { TextareaModule } from 'primeng/textarea';
import { QuejaGestion, ResumenQuejas } from '../../../core/models/gestion.model';
import { AvisoService } from '../../../core/services/aviso.service';
import { QuejasService } from '../../../core/services/gestion.service';
import { guardarArchivo } from '../../../core/utils/descargas';
import { CATEGORIAS_QUEJA, TIPOS_QUEJA, etiquetaDe } from '../../../core/utils/etiquetas';
import { Grafica, SerieGrafica } from '../../../shared/components/grafica/grafica';
import { ESTILOS_PANEL } from '../../../shared/panel-comun';

@Component({
  selector: 'app-quejas-panel',
  imports: [DatePipe, DecimalPipe, FormsModule, ReactiveFormsModule, ButtonModule, DialogModule, SelectModule, TableModule, TagModule, TextareaModule, Grafica],
  template: `
    <header class="encabezado">
      <div class="cabecera-pagina">
        <h1>Quejas y sugerencias</h1>
        <p>Lo que dicen los clientes y cómo les respondemos.</p>
      </div>
      <div class="acciones">
        <p-button label="Excel" icon="pi pi-file-excel" severity="secondary" [text]="true" (onClick)="exportar('xlsx')" />
        <p-button label="PDF" icon="pi pi-file-pdf" severity="secondary" [text]="true" (onClick)="exportar('pdf')" />
      </div>
    </header>

    @if (resumen(); as r) {
      <div class="tarjetas">
        <article class="tarjeta"><span>Recibidas este mes</span><strong>{{ r.recibidasMes }}</strong></article>
        <article class="tarjeta" [class.alerta]="r.pendientes > 0"><span>Pendientes</span><strong>{{ r.pendientes }}</strong><small>por responder</small></article>
        <article class="tarjeta"><span>Satisfacción</span><strong>{{ r.satisfaccionPorcentaje | number: '1.0-0' }}%</strong></article>
        <article class="tarjeta"><span>Respuesta promedio</span><strong>{{ r.horasRespuestaPromedio | number: '1.0-1' }} h</strong></article>
        <article class="tarjeta">
          <span>Despacho promedio</span>
          <strong [class.alerta]="r.tiempoPromedioDespachoMin > r.objetivoDespachoMin">{{ r.tiempoPromedioDespachoMin | number: '1.0-1' }} min</strong>
          <small>objetivo {{ r.objetivoDespachoMin }} min</small>
        </article>
      </div>
      <div class="rejilla">
        <section class="bloque">
          <h2>Tipos de mensajes del mes</h2>
          <app-grafica tipo="dona" titulo="Tipos de mensajes" centro="mensajes" [etiquetas]="etiquetasTipos" [series]="seriesTipos()" [colores]="coloresTipos" />
        </section>
        <section class="bloque">
          <h2>Satisfacción de los clientes</h2>
          <div class="pista"><span class="relleno" [style.width.%]="r.satisfaccionPorcentaje"></span></div>
          <p class="sub">{{ r.satisfaccionPorcentaje | number: '1.0-0' }}% de valoraciones positivas.</p>
        </section>
      </div>
    }

    <div class="filtros">
      <p-select [options]="tipos" optionLabel="etiqueta" optionValue="valor" [ngModel]="tipo()" (ngModelChange)="cambiarTipo($event)" [showClear]="true" placeholder="Todos los tipos" />
      <p-button icon="pi pi-refresh" severity="secondary" [text]="true" ariaLabel="Actualizar" (onClick)="cargar()" />
    </div>

    <p-table [value]="quejas()" dataKey="idQueja" [loading]="cargando()" [tableStyle]="{ 'min-width': '60rem' }">
      <ng-template #header>
        <tr><th>Fecha</th><th>Cliente</th><th>Tipo</th><th>Mensaje</th><th>Valoración</th><th>Estado</th><th style="width: 14rem">Acciones</th></tr>
      </ng-template>
      <ng-template #body let-q>
        <tr>
          <td>{{ q.fechaCreacion | date: 'dd/MM HH:mm' }}</td>
          <td>{{ q.cliente }}<div class="sub">{{ nombreCategoria(q.categoria) }}@if (q.numeroPedido) { · turno #{{ q.numeroPedido }} }</div></td>
          <td><p-tag [value]="nombreTipo(q.tipo)" [severity]="severidadTipo(q.tipo)" /></td>
          <td class="mensaje">{{ q.mensaje }}@if (q.respuesta) { <div class="sub">Respuesta: {{ q.respuesta }}</div> }</td>
          <td>{{ estrellas(q.valoracion) }}</td>
          <td><p-tag [value]="nombreEstado(q.estado)" [severity]="q.estado === 'RESPONDIDA' ? 'success' : q.estado === 'EN_REVISION' ? 'warn' : 'info'" /></td>
          <td>
            <div class="acciones">
              @if (q.estado !== 'RESPONDIDA') {
                <p-button label="Responder" size="small" (onClick)="abrir(q)" />
              }
              @if (q.estado === 'RECIBIDA') {
                <p-button label="En revisión" size="small" severity="secondary" [text]="true" (onClick)="enRevision(q)" />
              }
            </div>
          </td>
        </tr>
      </ng-template>
      <ng-template #emptymessage><tr><td colspan="7" class="vacio">No hay mensajes con ese filtro.</td></tr></ng-template>
    </p-table>

    <div class="paginador">
      <span>Página {{ pagina() + 1 }} de {{ paginas() }} · {{ total() }} registros</span>
      <p-button icon="pi pi-chevron-left" [text]="true" [disabled]="pagina() === 0" ariaLabel="Anterior" (onClick)="irA(pagina() - 1)" />
      <p-button icon="pi pi-chevron-right" [text]="true" [disabled]="pagina() + 1 >= paginas()" ariaLabel="Siguiente" (onClick)="irA(pagina() + 1)" />
    </div>

    <p-dialog [header]="'Responder a ' + (seleccionada()?.cliente ?? '')" [visible]="dialogo()" (visibleChange)="dialogo.set($event)" [modal]="true" [style]="{ width: '520px' }">
      @if (seleccionada(); as q) {
        <blockquote class="cita">{{ q.mensaje }}</blockquote>
      }
      <div class="formulario">
        <div class="campo">
          <label for="respuesta">Tu respuesta</label>
          <textarea pTextarea id="respuesta" rows="5" [formControl]="respuesta"></textarea>
          @if (respuesta.touched && respuesta.invalid) {
            <small class="error">Escribe una respuesta (máximo 1000 caracteres).</small>
          }
        </div>
      </div>
      <ng-template #footer>
        <p-button label="Cancelar" [text]="true" (onClick)="dialogo.set(false)" />
        <p-button label="Enviar respuesta" [loading]="guardando()" (onClick)="responder()" />
      </ng-template>
    </p-dialog>
  `,
  styles: [
    ESTILOS_PANEL,
    `
      .mensaje { max-width: 26rem; }
      .cita { margin: 0 0 1rem; padding: 0.75rem 1rem; border-left: 4px solid var(--kt-accion); border-radius: var(--kt-radio-chico); background: var(--kt-fondo); }
    `,
  ],
})
export class QuejasPanel implements OnInit {
  private readonly api = inject(QuejasService);
  private readonly aviso = inject(AvisoService);

  protected readonly tipos = TIPOS_QUEJA;
  protected readonly etiquetasTipos = ['Reclamos', 'Sugerencias', 'Felicitaciones'];
  protected readonly coloresTipos = ['var(--kt-primary-40)', 'var(--kt-tertiary-70)', 'var(--kt-exito)'];

  protected readonly resumen = signal<ResumenQuejas | null>(null);
  protected readonly quejas = signal<QuejaGestion[]>([]);
  protected readonly pagina = signal(0);
  protected readonly total = signal(0);
  protected readonly paginas = signal(1);
  protected readonly tipo = signal<string | null>(null);
  protected readonly cargando = signal(true);
  protected readonly guardando = signal(false);
  protected readonly dialogo = signal(false);
  protected readonly seleccionada = signal<QuejaGestion | null>(null);
  protected readonly respuesta = new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(1000)] });

  protected readonly seriesTipos = computed<SerieGrafica[]>(() => {
    const r = this.resumen();
    return [{ nombre: 'Mensajes', valores: r ? [r.reclamos, r.sugerencias, r.felicitaciones] : [] }];
  });

  ngOnInit(): void {
    this.cargar();
    this.cargarResumen();
  }

  private cargarResumen(): void {
    this.api.resumen().subscribe({ next: (r) => this.resumen.set(r), error: () => undefined });
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.api.listar(this.tipo(), this.pagina()).subscribe({
      next: (p) => {
        this.quejas.set(p.contenido);
        this.total.set(p.totalElementos);
        this.paginas.set(Math.max(1, p.totalPaginas));
        this.cargando.set(false);
      },
      error: (e) => {
        this.aviso.error(e, 'No pudimos cargar los mensajes');
        this.cargando.set(false);
      },
    });
  }

  protected cambiarTipo(valor: string | null): void {
    this.tipo.set(valor);
    this.pagina.set(0);
    this.cargar();
  }

  protected irA(pagina: number): void {
    this.pagina.set(pagina);
    this.cargar();
  }

  protected abrir(queja: QuejaGestion): void {
    this.seleccionada.set(queja);
    this.respuesta.reset('');
    this.dialogo.set(true);
  }

  protected responder(): void {
    const queja = this.seleccionada();
    if (!queja) return;
    if (this.respuesta.invalid) {
      this.respuesta.markAsTouched();
      return;
    }
    this.guardando.set(true);
    this.api.responder(queja.idQueja, this.respuesta.value.trim()).subscribe({
      next: () => {
        this.aviso.exito('Respuesta enviada.');
        this.dialogo.set(false);
        this.guardando.set(false);
        this.cargar();
        this.cargarResumen();
      },
      error: (e) => {
        this.aviso.error(e);
        this.guardando.set(false);
      },
    });
  }

  protected enRevision(queja: QuejaGestion): void {
    this.api.enRevision(queja.idQueja).subscribe({
      next: () => {
        this.cargar();
        this.cargarResumen();
      },
      error: (e) => this.aviso.error(e),
    });
  }

  protected exportar(formato: 'xlsx' | 'pdf'): void {
    this.api.exportar(formato, this.tipo()).subscribe({
      next: (blob) => guardarArchivo(blob, `quejas.${formato}`),
      error: (e) => this.aviso.error(e, 'No se pudo descargar'),
    });
  }

  protected nombreTipo(tipo: string): string {
    return etiquetaDe<string>(TIPOS_QUEJA, tipo);
  }

  protected nombreCategoria(categoria: string): string {
    return etiquetaDe<string>(CATEGORIAS_QUEJA, categoria);
  }

  protected nombreEstado(estado: string): string {
    return estado === 'RESPONDIDA' ? 'Respondida' : estado === 'EN_REVISION' ? 'En revisión' : 'Recibida';
  }

  protected severidadTipo(tipo: string): 'danger' | 'warn' | 'success' {
    return tipo === 'RECLAMO' ? 'danger' : tipo === 'SUGERENCIA' ? 'warn' : 'success';
  }

  protected estrellas(valoracion: number | null): string {
    return valoracion ? '★'.repeat(valoracion) + '☆'.repeat(5 - valoracion) : '—';
  }
}
