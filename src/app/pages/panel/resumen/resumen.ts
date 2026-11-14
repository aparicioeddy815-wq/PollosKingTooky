import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePickerModule } from 'primeng/datepicker';
import { VentaDia } from '../../../core/models/inventario.model';
import { DashboardGerente } from '../../../core/models/panel.model';
import { AvisoService } from '../../../core/services/aviso.service';
import { GraficasService } from '../../../core/services/graficas.service';
import { PanelService } from '../../../core/services/panel.service';
import { aFechaIso, etiquetaDia } from '../../../core/utils/fechas';
import { EstadoVacio } from '../../../shared/components/estado-vacio/estado-vacio';
import { Grafica, SerieGrafica } from '../../../shared/components/grafica/grafica';
import { BolivianosPipe } from '../../../shared/pipes/bolivianos.pipe';

@Component({
  selector: 'app-resumen',
  imports: [FormsModule, DatePickerModule, EstadoVacio, Grafica, BolivianosPipe],
  templateUrl: './resumen.html',
  styleUrl: './resumen.scss',
})
export class Resumen implements OnInit {
  private readonly panel = inject(PanelService);
  private readonly graficasApi = inject(GraficasService);
  private readonly aviso = inject(AvisoService);

  protected readonly datos = signal<DashboardGerente | null>(null);
  protected readonly tendencia = signal<VentaDia[]>([]);
  protected readonly cargando = signal(true);
  protected fecha: Date | null = null;
  protected readonly hoy = new Date();

  protected readonly maxCategoria = computed(() =>
    Math.max(1, ...(this.datos()?.ventasPorCategoria.map((v) => v.monto) ?? [])),
  );

  protected readonly etiquetasTendencia = computed(() => this.tendencia().map((v) => etiquetaDia(v.fecha)));
  protected readonly seriesTendencia = computed<SerieGrafica[]>(() => [
    { nombre: 'Ventas', valores: this.tendencia().map((v) => v.monto) },
  ]);
  protected readonly etiquetasCategoria = computed(() => this.datos()?.ventasPorCategoria.map((v) => v.nombre) ?? []);
  protected readonly seriesCategoria = computed<SerieGrafica[]>(() => [
    { nombre: 'Ventas', valores: this.datos()?.ventasPorCategoria.map((v) => v.monto) ?? [] },
  ]);

  ngOnInit(): void {
    this.cargar();
    this.graficasApi.ventas(7).subscribe({ next: (v) => this.tendencia.set(v), error: () => undefined });
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.panel.dashboardGerente(this.fecha ? aFechaIso(this.fecha) : null).subscribe({
      next: (datos) => {
        this.datos.set(datos);
        this.cargando.set(false);
      },
      error: (e) => {
        this.aviso.error(e, 'No pudimos cargar el resumen');
        this.cargando.set(false);
      },
    });
  }

  protected porcentajeStock(actual: number, minimo: number): number {
    // Barra llena = el doble del mínimo; cuanto más baja, más crítico
    return Math.min(100, Math.round((actual / Math.max(minimo * 2, 1)) * 100));
  }

  protected porcentajeCategoria(monto: number): number {
    return Math.round((monto / this.maxCategoria()) * 100);
  }
}
