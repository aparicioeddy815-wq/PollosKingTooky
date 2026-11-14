import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { DashboardAdmin } from '../../../core/models/admin.model';
import { VentaDia } from '../../../core/models/inventario.model';
import { AdminService } from '../../../core/services/admin.service';
import { AvisoService } from '../../../core/services/aviso.service';
import { GraficasService } from '../../../core/services/graficas.service';
import { guardarArchivo } from '../../../core/utils/descargas';
import { aFechaIso, etiquetaDia } from '../../../core/utils/fechas';
import { Grafica, SerieGrafica } from '../../../shared/components/grafica/grafica';
import { BolivianosPipe } from '../../../shared/pipes/bolivianos.pipe';

@Component({
  selector: 'app-admin-dashboard',
  imports: [DatePipe, DecimalPipe, FormsModule, ButtonModule, DatePickerModule, Grafica, BolivianosPipe],
  template: `
    <header class="encabezado">
      <div class="cabecera-pagina">
        <h1>Dashboard</h1>
        <p>Un vistazo rápido a ventas, operación y actividad del local.</p>
      </div>
      <div class="acciones">
        <p-datepicker [(ngModel)]="fecha" [maxDate]="hoy" dateFormat="dd/mm/yy" [showIcon]="true" [showClear]="true" placeholder="Hoy" (onSelect)="cargar()" (onClear)="cargar()" />
        <p-button label="Exportar resumen diario" icon="pi pi-file-pdf" [loading]="exportando()" (onClick)="exportar()" />
      </div>
    </header>

    @if (cargando() && !datos()) {
      <p class="texto-tenue">Cargando…</p>
    } @else if (datos(); as d) {
      <div class="indicadores">
        <article class="indicador principal">
          <span>Ventas totales</span>
          <strong>{{ d.ventasTotales | bs }}</strong>
          <small [class.sube]="d.variacionVsPromedioPct >= 0" [class.baja]="d.variacionVsPromedioPct < 0">
            <i [class]="d.variacionVsPromedioPct >= 0 ? 'pi pi-arrow-up' : 'pi pi-arrow-down'"></i>
            {{ d.variacionVsPromedioPct | number: '1.0-1' }}% vs. promedio
          </small>
        </article>
        <article class="indicador">
          <span>Comandas atendidas</span>
          <strong>{{ d.comandasAtendidas }}</strong>
        </article>
        <article class="indicador">
          <span>Margen operativo</span>
          <strong>{{ d.margenOperativoPct | number: '1.0-1' }}%</strong>
        </article>
        <article class="indicador">
          <span>Sesiones en vivo</span>
          <strong>{{ d.sesionesEnVivo }}</strong>
        </article>
      </div>

      <div class="columnas">
        <section class="bloque">
          <h2>Ritmo de ventas por hora</h2>
          <app-grafica tipo="barras" formato="bs" titulo="Ventas por hora" [etiquetas]="etiquetasRitmo()" [series]="seriesRitmo()" />
        </section>

        <section class="bloque">
          <h2>Ventas de los últimos 7 días</h2>
          <app-grafica tipo="linea" formato="bs" titulo="Ventas diarias" [etiquetas]="etiquetasTendencia()" [series]="seriesTendencia()" />
        </section>

        <section class="bloque">
          <h2>Top del menú</h2>
          @if (d.topMenu.length === 0) {
            <p class="texto-tenue">Sin ventas registradas en este día.</p>
          } @else {
            <ul class="barras">
              @for (t of d.topMenu; track t.idProducto) {
                <li>
                  <div class="etiqueta">
                    <span>{{ t.nombre }}</span>
                    <strong>{{ t.unidades }} uds · {{ t.monto | bs }}</strong>
                  </div>
                  <div class="pista"><span class="relleno" [style.width.%]="anchoTop(t.unidades)"></span></div>
                </li>
              }
            </ul>
          }
        </section>

        <section class="bloque">
          <h2>Operaciones en el mostrador</h2>
          <ul class="operaciones">
            <li>
              <span>Tiempo promedio de despacho</span>
              <strong [class.bien]="d.operaciones.tiempoPromedioDespachoMin <= d.operaciones.objetivoDespachoMin" [class.mal]="d.operaciones.tiempoPromedioDespachoMin > d.operaciones.objetivoDespachoMin">
                {{ d.operaciones.tiempoPromedioDespachoMin | number: '1.0-1' }} min
              </strong>
              <small>Objetivo: {{ d.operaciones.objetivoDespachoMin }} min</small>
            </li>
            <li>
              <span>Quejas del mes</span>
              <strong>{{ d.operaciones.quejasDelMes }}</strong>
            </li>
            <li>
              <span>Stock de pollo</span>
              <strong>{{ d.operaciones.stockPolloKg | number: '1.0-1' }} kg</strong>
              <small>Mínimo {{ d.operaciones.umbralPolloKg | number: '1.0-1' }} kg · alcanza ~{{ d.operaciones.diasCoberturaPollo | number: '1.0-1' }} días</small>
            </li>
            <li>
              <span>Alertas de stock abiertas</span>
              <strong [class.mal]="d.operaciones.alertasDeStock > 0">{{ d.operaciones.alertasDeStock }}</strong>
            </li>
          </ul>
        </section>

        <section class="bloque ancho">
          <h2>Bitácora de auditoría reciente</h2>
          @if (d.bitacoraReciente.length === 0) {
            <p class="texto-tenue">Sin eventos recientes.</p>
          } @else {
            <ul class="bitacora">
              @for (e of d.bitacoraReciente; track e.idBitacora) {
                <li [class.destacado]="e.nivel !== 'INFO'">
                  <time>{{ e.fechaHora | date: 'dd/MM HH:mm' }}</time>
                  <span class="modulo">{{ e.modulo }}</span>
                  <span class="usuario">{{ e.usuario }}</span>
                  <span class="accion">{{ e.accion }}</span>
                </li>
              }
            </ul>
          }
        </section>
      </div>
    }
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
      .acciones {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: 0.5rem;
      }
      .indicadores {
        display: grid;
        gap: 1rem;
        margin-bottom: 1.5rem;
        grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
      }
      .indicador {
        display: grid;
        gap: 0.3rem;
        padding: 1.25rem 1.4rem;
        border-radius: var(--kt-radio);
        background: var(--kt-superficie);
      }
      .indicador span {
        font-size: 0.88rem;
        font-weight: 600;
        color: var(--kt-texto-suave);
      }
      .indicador strong {
        font-family: var(--kt-fuente-titulo);
        font-size: clamp(1.6rem, 2.6vw, 2rem);
        font-weight: 800;
        line-height: 1.1;
        white-space: nowrap;
      }
      .indicador.principal {
        background: var(--kt-neutral);
      }
      .indicador.principal span {
        color: var(--kt-neutral-80);
      }
      .indicador.principal strong {
        color: var(--kt-tertiary);
      }
      .indicador small {
        font-weight: 600;
      }
      .sube {
        color: var(--kt-tertiary-80);
      }
      .baja {
        color: var(--kt-primary-70);
      }
      .columnas {
        display: grid;
        gap: 1.5rem;
      }
      @media (min-width: 960px) {
        .columnas {
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
        margin-bottom: 1.1rem;
        font-size: 1.2rem;
      }
      .barras {
        display: grid;
        gap: 1rem;
        margin: 0;
        padding: 0;
        list-style: none;
      }
      .etiqueta {
        display: flex;
        justify-content: space-between;
        gap: 1rem;
        margin-bottom: 0.35rem;
        font-size: 0.92rem;
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
        background: var(--kt-secondary-40);
        transition: width 0.4s ease;
      }
      .operaciones {
        display: grid;
        gap: 1rem;
        margin: 0;
        padding: 0;
        list-style: none;
      }
      .operaciones li {
        display: grid;
        gap: 0.15rem;
      }
      .operaciones span {
        font-size: 0.88rem;
        font-weight: 600;
        color: var(--kt-texto-suave);
      }
      .operaciones strong {
        font-family: var(--kt-fuente-titulo);
        font-size: 1.5rem;
      }
      .operaciones small {
        color: var(--kt-texto-tenue);
      }
      .bien {
        color: var(--kt-exito);
      }
      .mal {
        color: var(--kt-primary-40);
      }
      .bitacora {
        display: grid;
        gap: 0.5rem;
        margin: 0;
        padding: 0;
        list-style: none;
      }
      .bitacora li {
        display: grid;
        gap: 0.15rem 1rem;
        padding: 0.6rem 0.9rem;
        border-radius: var(--kt-radio-chico);
        background: var(--kt-fondo);
        font-size: 0.9rem;
      }
      @media (min-width: 760px) {
        .bitacora li {
          grid-template-columns: 7.5rem 8rem 10rem 1fr;
          align-items: center;
        }
      }
      .bitacora li.destacado {
        border-left: 4px solid var(--kt-accion);
      }
      .bitacora time,
      .bitacora .modulo {
        color: var(--kt-texto-tenue);
      }
      .bitacora .usuario {
        font-weight: 700;
      }
    `,
  ],
})
export class AdminDashboard implements OnInit {
  private readonly admin = inject(AdminService);
  private readonly graficasApi = inject(GraficasService);
  private readonly aviso = inject(AvisoService);

  protected readonly datos = signal<DashboardAdmin | null>(null);
  protected readonly tendencia = signal<VentaDia[]>([]);
  protected readonly cargando = signal(true);
  protected readonly exportando = signal(false);
  protected fecha: Date | null = null;
  protected readonly hoy = new Date();

  private readonly ritmo = computed(() => {
    const filas = this.datos()?.ritmoDeVentas ?? [];
    if (filas.length === 0) return [];
    const mapa = new Map<number, number>();
    filas.forEach((f) => mapa.set(f.hora, f.monto));
    const horas = filas.map((f) => f.hora);
    const desde = Math.min(...horas);
    const hasta = Math.max(...horas);
    const completo: { hora: number; monto: number }[] = [];
    for (let hora = desde; hora <= hasta; hora++) {
      completo.push({ hora, monto: mapa.get(hora) ?? 0 });
    }
    return completo;
  });

  protected readonly etiquetasRitmo = computed(() => this.ritmo().map((f) => `${f.hora}h`));
  protected readonly seriesRitmo = computed<SerieGrafica[]>(() => [{ nombre: 'Ventas', valores: this.ritmo().map((f) => f.monto) }]);
  protected readonly etiquetasTendencia = computed(() => this.tendencia().map((v) => etiquetaDia(v.fecha)));
  protected readonly seriesTendencia = computed<SerieGrafica[]>(() => [{ nombre: 'Ventas', valores: this.tendencia().map((v) => v.monto) }]);
  private readonly maxTop = computed(() => Math.max(1, ...(this.datos()?.topMenu.map((t) => t.unidades) ?? [])));

  ngOnInit(): void {
    this.cargar();
    this.graficasApi.ventas(7).subscribe({ next: (v) => this.tendencia.set(v), error: () => undefined });
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.admin.dashboard(this.fecha ? aFechaIso(this.fecha) : null).subscribe({
      next: (datos) => {
        this.datos.set(datos);
        this.cargando.set(false);
      },
      error: (e) => {
        this.aviso.error(e, 'No pudimos cargar el dashboard');
        this.cargando.set(false);
      },
    });
  }

  protected exportar(): void {
    const dia = this.fecha ? aFechaIso(this.fecha) : aFechaIso(new Date());
    this.exportando.set(true);
    this.admin.exportarResumenDiario(dia).subscribe({
      next: (blob) => {
        guardarArchivo(blob, `resumen-diario-${dia}.pdf`);
        this.exportando.set(false);
      },
      error: (e) => {
        this.aviso.error(e, 'No se pudo exportar');
        this.exportando.set(false);
      },
    });
  }

  protected anchoTop(unidades: number): number {
    return Math.round((unidades / this.maxTop()) * 100);
  }
}
