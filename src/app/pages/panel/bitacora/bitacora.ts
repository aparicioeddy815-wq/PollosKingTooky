import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { EventoBitacora } from '../../../core/models/admin.model';
import { FiltrosBitacora, ResumenBitacora } from '../../../core/models/gestion.model';
import { AvisoService } from '../../../core/services/aviso.service';
import { BitacoraService } from '../../../core/services/gestion.service';
import { guardarArchivo } from '../../../core/utils/descargas';
import { aFechaIso } from '../../../core/utils/fechas';
import { Grafica, SerieGrafica } from '../../../shared/components/grafica/grafica';
import { ESTILOS_PANEL } from '../../../shared/panel-comun';

@Component({
  selector: 'app-bitacora',
  imports: [DatePipe, FormsModule, ButtonModule, DatePickerModule, InputTextModule, SelectModule, TableModule, TagModule, Grafica],
  template: `
    <header class="encabezado">
      <div class="cabecera-pagina">
        <h1>Bitácora de auditoría</h1>
        <p>Quién hizo qué y cuándo. Los registros no se pueden modificar.</p>
      </div>
      <div class="acciones">
        <p-button label="CSV" icon="pi pi-file" severity="secondary" [text]="true" (onClick)="exportar('csv')" />
        <p-button label="PDF" icon="pi pi-file-pdf" severity="secondary" [text]="true" (onClick)="exportar('pdf')" />
      </div>
    </header>

    @if (resumen(); as r) {
      <div class="tarjetas">
        <article class="tarjeta"><span>Eventos hoy</span><strong>{{ r.eventosHoy }}</strong></article>
        <article class="tarjeta"><span>Inicios de sesión hoy</span><strong>{{ r.iniciosDeSesionHoy }}</strong></article>
        <article class="tarjeta"><span>Sesiones activas</span><strong>{{ r.sesionesActivas }}</strong></article>
        <article class="tarjeta" [class.alerta]="r.suspensionesActivas > 0"><span>Suspensiones activas</span><strong>{{ r.suspensionesActivas }}</strong></article>
        <article class="tarjeta"><span>Ajustes de stock hoy</span><strong>{{ r.ajustesDeStockHoy }}</strong></article>
        <article class="tarjeta" [class.alerta]="r.alertasDeSeguridadHoy > 0"><span>Alertas de seguridad hoy</span><strong>{{ r.alertasDeSeguridadHoy }}</strong></article>
      </div>
      <section class="bloque separado">
        <h2>Actividad de hoy</h2>
        <app-grafica tipo="barras" titulo="Actividad de hoy" [etiquetas]="etiquetasActividad" [series]="seriesActividad()" [alto]="200" />
      </section>
    }

    <div class="filtros">
      <input pInputText type="search" placeholder="Buscar en las acciones" aria-label="Buscar" [(ngModel)]="q" (keyup.enter)="buscar()" />
      <p-select [options]="categorias" optionLabel="etiqueta" optionValue="valor" [(ngModel)]="categoria" (onChange)="buscar()" />
      <p-select [options]="actores" optionLabel="etiqueta" optionValue="valor" [(ngModel)]="actor" (onChange)="buscar()" />
      <p-datepicker [(ngModel)]="desde" [maxDate]="hoy" dateFormat="dd/mm/yy" [showIcon]="true" [showClear]="true" placeholder="Desde" (onSelect)="buscar()" (onClear)="buscar()" />
      <p-datepicker [(ngModel)]="hasta" [maxDate]="hoy" dateFormat="dd/mm/yy" [showIcon]="true" [showClear]="true" placeholder="Hasta" (onSelect)="buscar()" (onClear)="buscar()" />
      <p-button label="Buscar" icon="pi pi-search" (onClick)="buscar()" />
    </div>

    <p-table [value]="eventos()" dataKey="idBitacora" [loading]="cargando()" [tableStyle]="{ 'min-width': '60rem' }">
      <ng-template #header>
        <tr><th>Fecha y hora</th><th>Tipo</th><th>Módulo</th><th>Usuario</th><th>Acción</th><th>Nivel</th><th>IP</th></tr>
      </ng-template>
      <ng-template #body let-e>
        <tr>
          <td>{{ e.fechaHora | date: 'dd/MM/yyyy HH:mm:ss' }}</td>
          <td>{{ e.tipo }}</td>
          <td>{{ e.modulo }}</td>
          <td><strong>{{ e.usuario }}</strong><div class="sub">{{ e.rolCodigo }}</div></td>
          <td class="accion">{{ e.accion }}</td>
          <td><p-tag [value]="e.nivel" [severity]="severidad(e.nivel)" /></td>
          <td>{{ e.ip ?? '—' }}</td>
        </tr>
      </ng-template>
      <ng-template #emptymessage><tr><td colspan="7" class="vacio">No hay eventos con esos filtros.</td></tr></ng-template>
    </p-table>

    <div class="paginador">
      <span>Página {{ pagina() + 1 }} de {{ paginas() }} · {{ total() }} eventos</span>
      <p-button icon="pi pi-chevron-left" [text]="true" [disabled]="pagina() === 0" ariaLabel="Anterior" (onClick)="irA(pagina() - 1)" />
      <p-button icon="pi pi-chevron-right" [text]="true" [disabled]="pagina() + 1 >= paginas()" ariaLabel="Siguiente" (onClick)="irA(pagina() + 1)" />
    </div>
  `,
  styles: [ESTILOS_PANEL, `.accion { max-width: 28rem; }`],
})
export class Bitacora implements OnInit {
  private readonly api = inject(BitacoraService);
  private readonly aviso = inject(AvisoService);

  protected readonly categorias = [
    { etiqueta: 'Todos los eventos', valor: 'TODOS' },
    { etiqueta: 'Seguridad', valor: 'SEGURIDAD' },
    { etiqueta: 'Anulaciones', valor: 'ANULACIONES' },
    { etiqueta: 'Permisos', valor: 'PERMISOS' },
  ];
  protected readonly actores = [
    { etiqueta: 'Todos los actores', valor: 'TODOS' },
    { etiqueta: 'Administrador', valor: 'ADMIN' },
    { etiqueta: 'Gerente', valor: 'GERENTE' },
    { etiqueta: 'Despacho', valor: 'DESPACHO' },
    { etiqueta: 'Cliente', valor: 'CLIENTE' },
  ];
  protected readonly etiquetasActividad = ['Eventos', 'Inicios de sesión', 'Ajustes de stock', 'Alertas seguridad'];
  protected readonly hoy = new Date();

  protected readonly resumen = signal<ResumenBitacora | null>(null);
  protected readonly eventos = signal<EventoBitacora[]>([]);
  protected readonly pagina = signal(0);
  protected readonly paginas = signal(1);
  protected readonly total = signal(0);
  protected readonly cargando = signal(true);

  protected q = '';
  protected categoria = 'TODOS';
  protected actor = 'TODOS';
  protected desde: Date | null = null;
  protected hasta: Date | null = null;

  protected readonly seriesActividad = computed<SerieGrafica[]>(() => {
    const r = this.resumen();
    return [{ nombre: 'Hoy', valores: r ? [r.eventosHoy, r.iniciosDeSesionHoy, r.ajustesDeStockHoy, r.alertasDeSeguridadHoy] : [] }];
  });

  ngOnInit(): void {
    this.cargar();
    this.api.resumen().subscribe({ next: (r) => this.resumen.set(r), error: () => undefined });
  }

  private filtros(): FiltrosBitacora {
    return {
      categoria: this.categoria,
      actor: this.actor,
      desde: this.desde ? aFechaIso(this.desde) : null,
      hasta: this.hasta ? aFechaIso(this.hasta) : null,
      q: this.q.trim() || null,
    };
  }

  protected buscar(): void {
    this.pagina.set(0);
    this.cargar();
  }

  protected irA(pagina: number): void {
    this.pagina.set(pagina);
    this.cargar();
  }

  private cargar(): void {
    this.cargando.set(true);
    this.api.listar(this.filtros(), this.pagina()).subscribe({
      next: (p) => {
        this.eventos.set(p.contenido);
        this.paginas.set(Math.max(1, p.totalPaginas));
        this.total.set(p.totalElementos);
        this.cargando.set(false);
      },
      error: (e) => {
        this.aviso.error(e, 'No pudimos cargar la bitácora');
        this.cargando.set(false);
      },
    });
  }

  protected exportar(formato: 'csv' | 'pdf'): void {
    this.api.exportar(formato, this.filtros()).subscribe({
      next: (blob) => guardarArchivo(blob, `bitacora.${formato}`),
      error: (e) => this.aviso.error(e, 'No se pudo descargar'),
    });
  }

  protected severidad(nivel: string): 'info' | 'warn' | 'danger' {
    return nivel === 'INFO' ? 'info' : nivel === 'CRITICO' || nivel === 'CRITICA' ? 'danger' : 'warn';
  }
}
