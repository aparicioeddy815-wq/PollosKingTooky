import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePickerModule } from 'primeng/datepicker';
import { DashboardGerente } from '../../../core/models/panel.model';
import { AvisoService } from '../../../core/services/aviso.service';
import { PanelService } from '../../../core/services/panel.service';
import { aFechaIso } from '../../../core/utils/fechas';
import { EstadoVacio } from '../../../shared/components/estado-vacio/estado-vacio';
import { BolivianosPipe } from '../../../shared/pipes/bolivianos.pipe';

@Component({
  selector: 'app-resumen',
  imports: [FormsModule, DatePickerModule, EstadoVacio, BolivianosPipe],
  templateUrl: './resumen.html',
  styleUrl: './resumen.scss',
})
export class Resumen implements OnInit {
  private readonly panel = inject(PanelService);
  private readonly aviso = inject(AvisoService);

  protected readonly datos = signal<DashboardGerente | null>(null);
  protected readonly cargando = signal(true);
  protected fecha: Date | null = null;
  protected readonly hoy = new Date();

  protected readonly maxCategoria = computed(() =>
    Math.max(1, ...(this.datos()?.ventasPorCategoria.map((v) => v.monto) ?? [])),
  );

  ngOnInit(): void {
    this.cargar();
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
