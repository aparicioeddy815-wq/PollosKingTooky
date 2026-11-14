import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { ReporteGenerado, ReporteMensual, Respaldo } from '../../../core/models/gestion.model';
import { AuthService } from '../../../core/services/auth.service';
import { AvisoService } from '../../../core/services/aviso.service';
import { ReportesService } from '../../../core/services/gestion.service';
import { guardarArchivo } from '../../../core/utils/descargas';
import { TIPOS_PEDIDO, etiquetaDe } from '../../../core/utils/etiquetas';
import { Grafica, SerieGrafica } from '../../../shared/components/grafica/grafica';
import { ESTILOS_PANEL, nombreConExtension } from '../../../shared/panel-comun';
import { BolivianosPipe } from '../../../shared/pipes/bolivianos.pipe';

function mesActual(): string {
  const hoy = new Date();
  return `${hoy.getFullYear()}-${String(hoy.getMonth() + 1).padStart(2, '0')}`;
}

function tamano(bytes: number): string {
  return bytes >= 1048576 ? `${(bytes / 1048576).toFixed(1)} MB` : `${Math.max(1, Math.round(bytes / 1024))} KB`;
}

@Component({
  selector: 'app-reportes',
  imports: [DatePipe, DecimalPipe, FormsModule, ButtonModule, SelectModule, TableModule, Grafica, BolivianosPipe],
  template: `
    <header class="encabezado">
      <div class="cabecera-pagina">
        <h1>Reportes</h1>
        <p>Ingresos, costos y márgenes del mes, informes descargables y respaldos.</p>
      </div>
      <div class="acciones">
        <input type="month" class="mes" aria-label="Mes del reporte" [ngModel]="mes()" (ngModelChange)="cambiarMes($event)" />
        @if (puedeExportar) {
          <p-button label="Excel" icon="pi pi-file-excel" severity="secondary" [text]="true" (onClick)="excel()" />
          <p-button label="Imprimible" icon="pi pi-print" severity="secondary" [text]="true" (onClick)="imprimible()" />
        }
      </div>
    </header>

    @if (reporte(); as r) {
      <div class="tarjetas">
        <article class="tarjeta"><span>Ingresos</span><strong>{{ r.ingresos | bs }}</strong></article>
        <article class="tarjeta"><span>Costo de alimentos</span><strong>{{ r.costoAlimento | bs }}</strong></article>
        <article class="tarjeta"><span>Costo de mermas</span><strong>{{ r.costoMermas | bs }}</strong><small>{{ r.desperdicioPct | number: '1.0-1' }}% de desperdicio</small></article>
        <article class="tarjeta"><span>Margen operativo</span><strong>{{ r.margenOperativoPct | number: '1.0-1' }}%</strong></article>
        <article class="tarjeta"><span>Pedidos</span><strong>{{ r.pedidos }}</strong></article>
        <article class="tarjeta"><span>Ticket promedio</span><strong>{{ r.ticketPromedio | bs }}</strong></article>
      </div>

      <div class="rejilla">
        <section class="bloque">
          <h2>Ventas por canal</h2>
          <app-grafica tipo="dona" formato="bs" titulo="Ventas por canal" centro="Bs vendidos" [etiquetas]="etiquetasCanal()" [series]="seriesCanal()" />
        </section>
        <section class="bloque">
          <h2>Evolución semanal</h2>
          <app-grafica tipo="linea" formato="bs" titulo="Ventas por semana" [etiquetas]="etiquetasSemana()" [series]="seriesSemana()" />
        </section>
        <section class="bloque ancho">
          <h2>Ingresos por categoría</h2>
          <app-grafica tipo="barras" formato="bs" titulo="Ingresos por categoría" [etiquetas]="etiquetasCategoria()" [series]="seriesCategoria()" />
          <ul class="margenes">
            @for (c of r.distribucion; track c.categoria) {
              <li><span>{{ c.nombre }}</span><strong>{{ c.porcentajeIngresos | number: '1.0-0' }}% de los ingresos · margen neto estimado {{ c.margenNetoEstimadoPct | number: '1.0-0' }}%</strong></li>
            }
          </ul>
        </section>
      </div>
    } @else if (cargando()) {
      <p class="texto-tenue">Cargando…</p>
    }

    @if (puedeExportar) {
      <section class="bloque separado">
        <div class="cabecera-item">
          <h2>Informes generados</h2>
          <div class="acciones">
            <p-select [options]="tiposInforme" optionLabel="etiqueta" optionValue="valor" [(ngModel)]="tipoInforme" />
            <p-select [options]="formatos" optionLabel="etiqueta" optionValue="valor" [(ngModel)]="formatoInforme" />
            <p-button label="Generar" icon="pi pi-plus" [loading]="generando()" (onClick)="generar()" />
          </div>
        </div>
        <p-table [value]="archivos()" [tableStyle]="{ 'min-width': '44rem' }">
          <ng-template #header><tr><th>Informe</th><th>Formato</th><th>Generado</th><th>Tamaño</th><th></th></tr></ng-template>
          <ng-template #body let-a>
            <tr>
              <td><strong>{{ a.nombre }}</strong><div class="sub">{{ a.programado ? 'Programado' : 'Por ' + a.generadoPor }}</div></td>
              <td>{{ a.formato }}</td>
              <td>{{ a.generadoEn | date: 'dd/MM/yyyy HH:mm' }}</td>
              <td>{{ tamano(a.tamanoBytes) }}</td>
              <td><p-button icon="pi pi-download" size="small" [text]="true" ariaLabel="Descargar" (onClick)="descargarArchivo(a)" /></td>
            </tr>
          </ng-template>
          <ng-template #emptymessage><tr><td colspan="5" class="vacio">Todavía no se generó ningún informe.</td></tr></ng-template>
        </p-table>
      </section>
    }

    @if (puedeRespaldar) {
      <section class="bloque separado">
        <div class="cabecera-item">
          <h2>Respaldos de la base de datos</h2>
          <p-button label="Respaldar ahora" icon="pi pi-database" [loading]="respaldando()" (onClick)="respaldar()" />
        </div>
        <p-table [value]="respaldos()" [tableStyle]="{ 'min-width': '44rem' }">
          <ng-template #header><tr><th>Fecha</th><th>Archivo</th><th>Estado</th><th>Tamaño</th><th></th></tr></ng-template>
          <ng-template #body let-r>
            <tr>
              <td>{{ r.fechaHora | date: 'dd/MM/yyyy HH:mm' }}</td>
              <td>{{ r.archivo }}</td>
              <td>{{ r.estado }}<div class="sub">{{ r.duracionMs }} ms</div></td>
              <td>{{ tamano(r.tamanoBytes) }}</td>
              <td>@if (r.disponible) { <p-button icon="pi pi-download" size="small" [text]="true" ariaLabel="Descargar respaldo" (onClick)="descargarRespaldo(r)" /> }</td>
            </tr>
          </ng-template>
          <ng-template #emptymessage><tr><td colspan="5" class="vacio">Todavía no hay respaldos.</td></tr></ng-template>
        </p-table>
      </section>
    }
  `,
  styles: [
    ESTILOS_PANEL,
    `
      .mes { padding: 0.55rem 0.7rem; border: 1px solid var(--kt-linea); border-radius: var(--kt-radio-chico); background: var(--kt-superficie); font: inherit; }
      .margenes { display: grid; gap: 0.4rem; margin: 1rem 0 0; padding: 0; list-style: none; font-size: 0.9rem; }
      .margenes li { display: flex; justify-content: space-between; gap: 1rem; flex-wrap: wrap; }
    `,
  ],
})
export class Reportes implements OnInit {
  private readonly api = inject(ReportesService);
  private readonly aviso = inject(AvisoService);
  private readonly auth = inject(AuthService);

  protected readonly puedeExportar = this.auth.tienePermiso('REPORTE_EXPORTAR');
  protected readonly puedeRespaldar = this.auth.tienePermiso('RESPALDO_GESTIONAR');
  protected readonly tamano = tamano;

  protected readonly tiposInforme = [
    { etiqueta: 'Ventas del mes', valor: 'VENTAS_MENSUAL' },
    { etiqueta: 'Inventario valorizado', valor: 'INVENTARIO' },
    { etiqueta: 'Auditoría semanal', valor: 'AUDITORIA' },
    { etiqueta: 'Resumen diario', valor: 'RESUMEN_DIARIO' },
  ];
  protected readonly formatos = [
    { etiqueta: 'PDF', valor: 'PDF' },
    { etiqueta: 'Excel', valor: 'XLSX' },
  ];
  protected tipoInforme = 'VENTAS_MENSUAL';
  protected formatoInforme = 'PDF';

  protected readonly mes = signal(mesActual());
  protected readonly reporte = signal<ReporteMensual | null>(null);
  protected readonly archivos = signal<ReporteGenerado[]>([]);
  protected readonly respaldos = signal<Respaldo[]>([]);
  protected readonly cargando = signal(true);
  protected readonly generando = signal(false);
  protected readonly respaldando = signal(false);

  protected readonly etiquetasCanal = computed(() => this.reporte()?.canales.map((c) => etiquetaDe<string>(TIPOS_PEDIDO, c.canal)) ?? []);
  protected readonly seriesCanal = computed<SerieGrafica[]>(() => [{ nombre: 'Ventas', valores: this.reporte()?.canales.map((c) => c.monto) ?? [] }]);
  protected readonly etiquetasSemana = computed(() => this.reporte()?.evolucionSemanal.map((s) => s.semana) ?? []);
  protected readonly seriesSemana = computed<SerieGrafica[]>(() => [{ nombre: 'Ventas', valores: this.reporte()?.evolucionSemanal.map((s) => s.monto) ?? [] }]);
  protected readonly etiquetasCategoria = computed(() => this.reporte()?.distribucion.map((d) => d.nombre) ?? []);
  protected readonly seriesCategoria = computed<SerieGrafica[]>(() => [{ nombre: 'Ingresos', valores: this.reporte()?.distribucion.map((d) => d.ingresos) ?? [] }]);

  ngOnInit(): void {
    this.cargar();
    if (this.puedeExportar) this.cargarArchivos();
    if (this.puedeRespaldar) this.cargarRespaldos();
  }

  protected cambiarMes(valor: string): void {
    if (!valor) return;
    this.mes.set(valor);
    this.cargar();
  }

  private cargar(): void {
    this.cargando.set(true);
    this.api.resumen(this.mes()).subscribe({
      next: (r) => {
        this.reporte.set(r);
        this.cargando.set(false);
      },
      error: (e) => {
        this.aviso.error(e, 'No pudimos cargar el reporte');
        this.cargando.set(false);
      },
    });
  }

  private cargarArchivos(): void {
    this.api.archivos().subscribe({ next: (p) => this.archivos.set(p.contenido), error: () => undefined });
  }

  private cargarRespaldos(): void {
    this.api.respaldos().subscribe({ next: (p) => this.respaldos.set(p.contenido), error: () => undefined });
  }

  protected excel(): void {
    this.api.excel(this.mes()).subscribe({
      next: (blob) => guardarArchivo(blob, `reporte-${this.mes()}.xlsx`),
      error: (e) => this.aviso.error(e, 'No se pudo descargar'),
    });
  }

  protected imprimible(): void {
    this.api.imprimible(this.mes()).subscribe({
      next: (blob) => guardarArchivo(blob, nombreConExtension(`reporte-${this.mes()}`, blob)),
      error: (e) => this.aviso.error(e, 'No se pudo descargar'),
    });
  }

  protected generar(): void {
    this.generando.set(true);
    this.api.generar(this.tipoInforme, this.formatoInforme).subscribe({
      next: () => {
        this.aviso.exito('Informe generado.');
        this.generando.set(false);
        this.cargarArchivos();
      },
      error: (e) => {
        this.aviso.error(e);
        this.generando.set(false);
      },
    });
  }

  protected descargarArchivo(a: ReporteGenerado): void {
    this.api.descargar(a.idReporte).subscribe({
      next: (blob) => guardarArchivo(blob, `${a.nombre}.${a.formato.toLowerCase()}`),
      error: (e) => this.aviso.error(e, 'No se pudo descargar'),
    });
  }

  protected respaldar(): void {
    this.respaldando.set(true);
    this.api.respaldar().subscribe({
      next: () => {
        this.aviso.exito('Respaldo creado.');
        this.respaldando.set(false);
        this.cargarRespaldos();
      },
      error: (e) => {
        this.aviso.error(e);
        this.respaldando.set(false);
      },
    });
  }

  protected descargarRespaldo(r: Respaldo): void {
    this.api.descargarRespaldo(r.idRespaldo).subscribe({
      next: (blob) => guardarArchivo(blob, r.archivo),
      error: (e) => this.aviso.error(e, 'No se pudo descargar'),
    });
  }
}
