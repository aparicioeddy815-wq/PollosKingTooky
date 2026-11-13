import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ConfirmationService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { Reserva } from '../../../core/models/reserva.model';
import { AvisoService } from '../../../core/services/aviso.service';
import { ReservaService } from '../../../core/services/reserva.service';
import { ESTADOS_RESERVA } from '../../../core/utils/etiquetas';
import { aFechaIso } from '../../../core/utils/fechas';
import { EstadoTag } from '../../../shared/components/estado-tag/estado-tag';
import { BolivianosPipe } from '../../../shared/pipes/bolivianos.pipe';

@Component({
  selector: 'app-reservas',
  imports: [DatePipe, FormsModule, ButtonModule, DatePickerModule, SelectModule, TableModule, EstadoTag, BolivianosPipe],
  templateUrl: './reservas.html',
  styleUrl: './reservas.scss',
})
export class Reservas implements OnInit {
  private readonly reservasApi = inject(ReservaService);
  private readonly aviso = inject(AvisoService);
  private readonly confirmar = inject(ConfirmationService);

  protected readonly estados = ESTADOS_RESERVA;
  protected readonly reservas = signal<Reserva[]>([]);
  protected readonly cargando = signal(true);

  protected estado: string | null = null;
  protected fecha: Date | null = null;

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.reservasApi.gestion(this.estado, this.fecha ? aFechaIso(this.fecha) : null).subscribe({
      next: (pagina) => {
        this.reservas.set(pagina.contenido);
        this.cargando.set(false);
      },
      error: (e) => {
        this.aviso.error(e, 'No pudimos cargar las reservas');
        this.cargando.set(false);
      },
    });
  }

  protected registrarPago(reserva: Reserva): void {
    this.reservasApi.pagar(reserva.idReserva).subscribe({
      next: () => {
        this.aviso.exito(`Pago registrado para ${reserva.codigo}.`);
        this.cargar();
      },
      error: (e) => this.aviso.error(e),
    });
  }

  protected completar(reserva: Reserva): void {
    this.reservasApi.completar(reserva.idReserva).subscribe({
      next: () => {
        this.aviso.exito(`La reserva ${reserva.codigo} se marcó como completada.`);
        this.cargar();
      },
      error: (e) => this.aviso.error(e),
    });
  }

  protected cancelar(reserva: Reserva): void {
    this.confirmar.confirm({
      header: 'Cancelar reserva',
      message: `¿Cancelar la reserva ${reserva.codigo} de ${reserva.nombreCliente}?`,
      acceptLabel: 'Sí, cancelar',
      rejectLabel: 'Volver',
      accept: () =>
        this.reservasApi.cancelar(reserva.idReserva).subscribe({
          next: () => {
            this.aviso.exito('Reserva cancelada.');
            this.cargar();
          },
          error: (e) => this.aviso.error(e),
        }),
    });
  }
}
