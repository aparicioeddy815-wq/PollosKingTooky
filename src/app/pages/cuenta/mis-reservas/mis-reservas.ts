import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ConfirmationService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { DialogModule } from 'primeng/dialog';
import { SelectModule } from 'primeng/select';
import { Reserva } from '../../../core/models/reserva.model';
import { AvisoService } from '../../../core/services/aviso.service';
import { ReservaService } from '../../../core/services/reserva.service';
import { ESTADOS_RESERVA } from '../../../core/utils/etiquetas';
import { aFechaIso, aHoraIso } from '../../../core/utils/fechas';
import { EstadoTag } from '../../../shared/components/estado-tag/estado-tag';
import { EstadoVacio } from '../../../shared/components/estado-vacio/estado-vacio';
import { BolivianosPipe } from '../../../shared/pipes/bolivianos.pipe';

@Component({
  selector: 'app-mis-reservas',
  imports: [
    DatePipe,
    FormsModule,
    RouterLink,
    ButtonModule,
    DatePickerModule,
    DialogModule,
    SelectModule,
    EstadoTag,
    EstadoVacio,
    BolivianosPipe,
  ],
  templateUrl: './mis-reservas.html',
  styleUrl: './mis-reservas.scss',
})
export class MisReservas implements OnInit {
  private readonly reservasApi = inject(ReservaService);
  private readonly aviso = inject(AvisoService);
  private readonly confirmar = inject(ConfirmationService);

  protected readonly estados = ESTADOS_RESERVA;
  protected readonly hoy = new Date();

  protected readonly reservas = signal<Reserva[]>([]);
  protected readonly cargando = signal(true);
  protected estadoFiltro: string | null = null;

  // Diálogo de reprogramación
  protected readonly reprogramando = signal<Reserva | null>(null);
  protected nuevaFecha: Date | null = null;
  protected nuevaHora: Date | null = null;

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.reservasApi.misReservas(this.estadoFiltro).subscribe({
      next: (pagina) => {
        this.reservas.set(pagina.contenido);
        this.cargando.set(false);
      },
      error: (e) => {
        this.aviso.error(e, 'No pudimos cargar tus reservas');
        this.cargando.set(false);
      },
    });
  }

  protected pedirCancelacion(reserva: Reserva): void {
    this.confirmar.confirm({
      header: 'Cancelar reserva',
      message: `¿Seguro que quieres cancelar la reserva ${reserva.codigo}?`,
      acceptLabel: 'Sí, cancelar',
      rejectLabel: 'Volver',
      accept: () =>
        this.reservasApi.cancelar(reserva.idReserva).subscribe({
          next: () => {
            this.aviso.exito('La reserva fue cancelada.');
            this.cargar();
          },
          error: (e) => this.aviso.error(e),
        }),
    });
  }

  protected abrirReprogramar(reserva: Reserva): void {
    this.reprogramando.set(reserva);
    this.nuevaFecha = null;
    this.nuevaHora = null;
  }

  protected confirmarReprogramacion(): void {
    const reserva = this.reprogramando();
    if (!reserva || !this.nuevaFecha || !this.nuevaHora) {
      this.aviso.info('Elige la nueva fecha y hora.');
      return;
    }
    this.reservasApi
      .reprogramar(reserva.idReserva, { fecha: aFechaIso(this.nuevaFecha), hora: aHoraIso(this.nuevaHora) })
      .subscribe({
        next: () => {
          this.aviso.exito('Tu reserva fue reprogramada.');
          this.reprogramando.set(null);
          this.cargar();
        },
        error: (e) => this.aviso.error(e),
      });
  }

  protected descargarRecibo(reserva: Reserva): void {
    this.reservasApi.recibo(reserva.idReserva).subscribe({
      next: (archivo) => {
        const url = URL.createObjectURL(archivo);
        const enlace = document.createElement('a');
        enlace.href = url;
        enlace.download = `recibo-${reserva.codigo}.pdf`;
        enlace.click();
        URL.revokeObjectURL(url);
      },
      error: (e) => this.aviso.error(e, 'No pudimos descargar el recibo'),
    });
  }
}
