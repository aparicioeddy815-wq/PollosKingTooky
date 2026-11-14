import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { interval } from 'rxjs';
import { ConfirmationService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { Pedido } from '../../../core/models/pedido.model';
import { AuthService } from '../../../core/services/auth.service';
import { AvisoService } from '../../../core/services/aviso.service';
import { PedidoService } from '../../../core/services/pedido.service';
import { ComandasEnVivoService, SonidoService } from '../../../core/services/tiempo-real.service';
import { ESTADOS_PEDIDO, TIPOS_PEDIDO, etiquetaDe } from '../../../core/utils/etiquetas';
import { aFechaIso } from '../../../core/utils/fechas';
import { EstadoTag } from '../../../shared/components/estado-tag/estado-tag';
import { BolivianosPipe } from '../../../shared/pipes/bolivianos.pipe';

@Component({
  selector: 'app-pedidos',
  imports: [DatePipe, FormsModule, RouterLink, ButtonModule, DatePickerModule, SelectModule, TableModule, EstadoTag, BolivianosPipe],
  templateUrl: './pedidos.html',
  styleUrl: './pedidos.scss',
})
export class Pedidos implements OnInit {
  private readonly pedidosApi = inject(PedidoService);
  private readonly aviso = inject(AvisoService);
  private readonly confirmar = inject(ConfirmationService);
  private readonly enVivo = inject(ComandasEnVivoService);
  protected readonly auth = inject(AuthService);
  protected readonly sonido = inject(SonidoService);

  protected readonly estados = ESTADOS_PEDIDO;
  protected readonly hoy = new Date();

  protected readonly pedidos = signal<Pedido[]>([]);
  protected readonly cargando = signal(true);
  protected fecha: Date | null = null;
  protected estado: string | null = null;

  constructor() {
    // Respaldo: refresco periódico por si se corta la conexión en vivo
    interval(20000)
      .pipe(takeUntilDestroyed())
      .subscribe(() => this.cargar(false));

    // Tiempo real (SSE): cualquier cambio en las comandas actualiza la lista al instante
    this.enVivo
      .escuchar()
      .pipe(takeUntilDestroyed())
      .subscribe((evento) => {
        this.cargar(false);
        if (evento === 'pedido-listo' || evento === 'timbre') {
          this.sonido.timbre();
        }
      });
  }

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(mostrarCarga = true): void {
    if (mostrarCarga) this.cargando.set(true);
    this.pedidosApi.listar(this.fecha ? aFechaIso(this.fecha) : null, this.estado).subscribe({
      next: (pedidos) => {
        this.pedidos.set(pedidos);
        this.cargando.set(false);
      },
      error: (e) => {
        if (mostrarCarga) this.aviso.error(e, 'No pudimos cargar los pedidos');
        this.cargando.set(false);
      },
    });
  }

  protected tipo(pedido: Pedido): string {
    return pedido.tipoPedido === 'RESERVA' ? 'Reserva' : etiquetaDe(TIPOS_PEDIDO, pedido.tipoPedido);
  }

  protected alternarSonido(): void {
    if (this.sonido.activo()) {
      this.sonido.desactivar();
    } else {
      this.sonido.activar();
    }
  }

  protected marcarListo(pedido: Pedido): void {
    this.ejecutar(this.pedidosApi.marcarListo(pedido.idPedido), `Turno ${pedido.numeroTurno} listo para entregar.`);
  }

  protected entregar(pedido: Pedido): void {
    this.ejecutar(this.pedidosApi.cambiarEstado(pedido.idPedido, 'ENTREGADO'), `Turno ${pedido.numeroTurno} entregado.`);
  }

  protected tocarTimbre(pedido: Pedido): void {
    this.ejecutar(this.pedidosApi.timbre(pedido.idPedido), `Se llamó al turno ${pedido.numeroTurno}.`);
  }

  protected cancelar(pedido: Pedido): void {
    this.confirmar.confirm({
      header: 'Cancelar pedido',
      message: `¿Cancelar el turno ${pedido.numeroTurno}? Esta acción no se puede deshacer.`,
      acceptLabel: 'Sí, cancelar',
      rejectLabel: 'Volver',
      accept: () => this.ejecutar(this.pedidosApi.cancelar(pedido.idPedido), `Turno ${pedido.numeroTurno} cancelado.`),
    });
  }

  private ejecutar(peticion: ReturnType<PedidoService['cancelar']>, mensaje: string): void {
    peticion.subscribe({
      next: () => {
        this.aviso.exito(mensaje);
        this.cargar(false);
      },
      error: (e) => this.aviso.error(e),
    });
  }
}
