import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { DiaReserva, HoraReserva } from '../../../core/models/gestion.model';
import { ProductoPublico } from '../../../core/models/producto.model';
import { AuthService } from '../../../core/services/auth.service';
import { AvisoService } from '../../../core/services/aviso.service';
import { DisponibilidadService } from '../../../core/services/gestion.service';
import { PublicoService } from '../../../core/services/publico.service';
import { ReservaService } from '../../../core/services/reserva.service';
import { BolivianosPipe } from '../../../shared/pipes/bolivianos.pipe';

@Component({
  selector: 'app-nueva-reserva',
  imports: [ReactiveFormsModule, ButtonModule, InputNumberModule, InputTextModule, TextareaModule, BolivianosPipe],
  template: `
    <section class="reserva">
      <h2>Reserva tu mesa</h2>

      <ol class="pasos" aria-label="Pasos de la reserva">
        <li [class.activo]="paso() === 1" [class.hecho]="paso() > 1"><span>1</span> Día</li>
        <li [class.activo]="paso() === 2" [class.hecho]="paso() > 2"><span>2</span> Hora</li>
        <li [class.activo]="paso() === 3"><span>3</span> Datos y platos</li>
      </ol>

      @if (paso() === 1) {
        <p class="texto-tenue">Elige el día de tu visita.</p>
        <div class="opciones">
          @for (d of dias(); track d.fecha) {
            <button type="button" class="opcion" [class.elegida]="fechaElegida() === d.fecha" [disabled]="!d.disponible" (click)="elegirDia(d)">
              <strong>{{ d.diaSemana }}</strong>
              <span>{{ d.etiqueta }}</span>
              @if (!d.disponible) { <small>Sin cupo</small> }
            </button>
          }
        </div>
        @if (cargandoDias()) { <p class="texto-tenue">Cargando días…</p> }
      }

      @if (paso() === 2) {
        <p class="texto-tenue">Horarios disponibles para el {{ etiquetaFecha() }}.</p>
        <div class="opciones">
          @for (h of horas(); track h.hora) {
            <button type="button" class="opcion" [class.elegida]="horaElegida() === h.hora" [disabled]="!h.disponible" (click)="horaElegida.set(h.hora)">
              <strong>{{ corta(h.hora) }}</strong>
              <span>{{ h.turno }}</span>
              <small>{{ h.disponible ? h.cupoRestante + ' lugares' : 'Completo' }}</small>
            </button>
          }
        </div>
        @if (cargandoHoras()) { <p class="texto-tenue">Cargando horarios…</p> }
        @else if (horas().length === 0) { <p class="texto-tenue">No hay horarios para ese día.</p> }
      }

      @if (paso() === 3) {
        <p class="resumen-visita"><i class="pi pi-calendar"></i> {{ etiquetaFecha() }} a las {{ corta(horaElegida() ?? '') }}</p>

        <form class="formulario" [formGroup]="formulario" novalidate>
          <div class="fila">
            <div class="campo">
              <label for="nombre">Nombre</label>
              <input pInputText id="nombre" formControlName="nombreCliente" />
            </div>
            <div class="campo">
              <label for="telefono">Teléfono</label>
              <input pInputText id="telefono" formControlName="telefono" inputmode="tel" />
              @if (formulario.controls.telefono.touched && formulario.controls.telefono.invalid) { <small class="error">Escribe un teléfono válido.</small> }
            </div>
          </div>
          <div class="fila">
            <div class="campo">
              <label for="correo">Correo (opcional)</label>
              <input pInputText id="correo" type="email" formControlName="correo" />
            </div>
            <div class="campo">
              <label for="personas">Personas</label>
              <p-inputnumber inputId="personas" formControlName="personas" [min]="1" [max]="50" [showButtons]="true" />
            </div>
          </div>
          <div class="campo">
            <label for="notas">Notas (opcional)</label>
            <textarea pTextarea id="notas" rows="2" formControlName="notas"></textarea>
          </div>
        </form>

        <h3>Elige tus platos</h3>
        <ul class="platos">
          @for (p of platos(); track p.idProducto) {
            <li>
              <div><strong>{{ p.nombre }}</strong><span class="detalle">{{ p.precio | bs }}</span></div>
              <div class="cantidad">
                <p-button icon="pi pi-minus" size="small" severity="secondary" [rounded]="true" ariaLabel="Menos" (onClick)="cambiar(p, -1)" />
                <span>{{ cantidadDe(p) }}</span>
                <p-button icon="pi pi-plus" size="small" severity="secondary" [rounded]="true" ariaLabel="Más" (onClick)="cambiar(p, 1)" />
              </div>
            </li>
          }
        </ul>
        @if (sinPlatos()) { <small class="error">Elige al menos un plato.</small> }
        <p class="total">Total: <strong>{{ total() | bs }}</strong></p>
      }

      <div class="navegacion">
        @if (paso() > 1) { <p-button label="Atrás" severity="secondary" [outlined]="true" (onClick)="atras()" /> }
        @if (paso() === 1) { <p-button label="Siguiente" [disabled]="!fechaElegida()" (onClick)="irAHoras()" /> }
        @if (paso() === 2) { <p-button label="Siguiente" [disabled]="!horaElegida()" (onClick)="paso.set(3)" /> }
        @if (paso() === 3) { <p-button label="Confirmar reserva" icon="pi pi-check" [loading]="enviando()" (onClick)="reservar()" /> }
      </div>
    </section>
  `,
  styles: [
    `
      .pasos { display: flex; flex-wrap: wrap; gap: 0.75rem; margin: 1rem 0 1.5rem; padding: 0; list-style: none; }
      .pasos li { display: flex; align-items: center; gap: 0.5rem; padding: 0.4rem 0.9rem; border-radius: 999px; background: var(--kt-superficie-alta); color: var(--kt-texto-suave); font-weight: 600; }
      .pasos li span { display: grid; place-items: center; width: 1.5rem; height: 1.5rem; border-radius: 50%; background: var(--kt-superficie); font-size: 0.85rem; }
      .pasos li.activo { background: var(--kt-accion); color: #fff; }
      .pasos li.hecho { color: var(--kt-exito); }
      .opciones { display: grid; gap: 0.75rem; margin: 1rem 0; grid-template-columns: repeat(auto-fill, minmax(130px, 1fr)); }
      .opcion { display: grid; gap: 0.2rem; padding: 0.9rem; border: 2px solid transparent; border-radius: var(--kt-radio); background: var(--kt-superficie); color: inherit; font: inherit; text-align: center; cursor: pointer; }
      .opcion:hover:not(:disabled) { border-color: var(--kt-accion); }
      .opcion.elegida { border-color: var(--kt-accion); background: var(--kt-primary-90); }
      .opcion:disabled { opacity: 0.5; cursor: not-allowed; }
      .opcion small { color: var(--kt-texto-tenue); }
      .resumen-visita { display: flex; align-items: center; gap: 0.5rem; padding: 0.75rem 1rem; border-radius: var(--kt-radio-chico); background: var(--kt-superficie); font-weight: 600; }
      .formulario { display: grid; gap: 1rem; margin-bottom: 1.5rem; }
      .campo { display: grid; gap: 0.35rem; min-width: 0; }
      .campo label { font-weight: 600; font-size: 0.9rem; }
      .fila { display: grid; gap: 1rem; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); }
      .error { color: var(--kt-primary-40); }
      .platos { display: grid; gap: 0.5rem; margin: 0.75rem 0; padding: 0; list-style: none; }
      .platos li { display: flex; align-items: center; justify-content: space-between; gap: 1rem; padding: 0.7rem 1rem; border-radius: var(--kt-radio-chico); background: var(--kt-superficie); }
      .platos li div:first-child { display: grid; }
      .detalle { font-size: 0.88rem; color: var(--kt-texto-suave); }
      .cantidad { display: flex; align-items: center; gap: 0.6rem; font-weight: 700; }
      .total { margin: 1rem 0; font-size: 1.15rem; }
      .navegacion { display: flex; flex-wrap: wrap; gap: 0.75rem; margin-top: 1.5rem; }
    `,
  ],
})
export class NuevaReserva implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly publico = inject(PublicoService);
  private readonly reservas = inject(ReservaService);
  private readonly disponibilidad = inject(DisponibilidadService);
  private readonly aviso = inject(AvisoService);
  private readonly router = inject(Router);

  protected readonly paso = signal<1 | 2 | 3>(1);
  protected readonly dias = signal<DiaReserva[]>([]);
  protected readonly horas = signal<HoraReserva[]>([]);
  protected readonly fechaElegida = signal<string | null>(null);
  protected readonly horaElegida = signal<string | null>(null);
  protected readonly cargandoDias = signal(true);
  protected readonly cargandoHoras = signal(false);
  protected readonly platos = signal<ProductoPublico[]>([]);
  protected readonly cantidades = signal<Record<number, number>>({});
  protected readonly enviando = signal(false);
  protected readonly sinPlatos = signal(false);

  protected readonly etiquetaFecha = computed(() => {
    const dia = this.dias().find((d) => d.fecha === this.fechaElegida());
    return dia ? `${dia.diaSemana} ${dia.etiqueta}` : '';
  });

  protected readonly seleccion = computed(() =>
    this.platos()
      .filter((p) => (this.cantidades()[p.idProducto] ?? 0) > 0)
      .map((p) => ({ producto: p, cantidad: this.cantidades()[p.idProducto] ?? 0 })),
  );

  protected readonly total = computed(() => this.seleccion().reduce((suma, l) => suma + l.producto.precio * l.cantidad, 0));

  protected readonly formulario = this.fb.nonNullable.group({
    nombreCliente: [this.auth.sesion()?.nombreCompleto ?? '', [Validators.required, Validators.maxLength(255)]],
    telefono: ['', [Validators.required, Validators.pattern(/^[0-9+() -]{7,30}$/)]],
    correo: [this.auth.sesion()?.correo ?? '', Validators.email],
    personas: [2, [Validators.required, Validators.min(1), Validators.max(50)]],
    notas: ['', Validators.maxLength(500)],
  });

  ngOnInit(): void {
    this.disponibilidad.dias().subscribe({
      next: (d) => {
        this.dias.set(d);
        this.cargandoDias.set(false);
      },
      error: (e) => {
        this.aviso.error(e, 'No pudimos cargar los días disponibles');
        this.cargandoDias.set(false);
      },
    });
    this.publico.menu().subscribe({
      next: (menu) => this.platos.set(menu.categorias.flatMap((c) => c.productos).filter((p) => p.disponible)),
      error: (e) => this.aviso.error(e, 'No pudimos cargar el menú'),
    });
  }

  protected corta(hora: string): string {
    return hora.slice(0, 5);
  }

  protected elegirDia(dia: DiaReserva): void {
    this.fechaElegida.set(dia.fecha);
    this.horaElegida.set(null);
  }

  protected irAHoras(): void {
    const fecha = this.fechaElegida();
    if (!fecha) return;
    this.paso.set(2);
    this.cargandoHoras.set(true);
    this.horas.set([]);
    this.disponibilidad.horarios(fecha).subscribe({
      next: (h) => {
        this.horas.set(h);
        this.cargandoHoras.set(false);
      },
      error: (e) => {
        this.aviso.error(e, 'No pudimos cargar los horarios');
        this.cargandoHoras.set(false);
      },
    });
  }

  protected atras(): void {
    this.paso.update((p) => (p === 3 ? 2 : 1));
  }

  protected cantidadDe(p: ProductoPublico): number {
    return this.cantidades()[p.idProducto] ?? 0;
  }

  protected cambiar(p: ProductoPublico, cambio: number): void {
    const nueva = Math.min(50, Math.max(0, this.cantidadDe(p) + cambio));
    this.cantidades.update((actual) => ({ ...actual, [p.idProducto]: nueva }));
    this.sinPlatos.set(false);
  }

  protected reservar(): void {
    const fecha = this.fechaElegida();
    const hora = this.horaElegida();
    if (!fecha || !hora) return;
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    if (this.seleccion().length === 0) {
      this.sinPlatos.set(true);
      return;
    }
    const v = this.formulario.getRawValue();
    const cupo = this.horas().find((h) => h.hora === hora)?.cupoRestante;
    if (cupo !== undefined && v.personas > cupo) {
      this.aviso.error(new Error('cupo'), `Solo quedan ${cupo} lugares en ese horario`);
      return;
    }

    this.enviando.set(true);
    this.reservas
      .crear({
        nombreCliente: v.nombreCliente,
        telefono: v.telefono,
        correo: v.correo || null,
        fecha,
        hora: hora.slice(0, 5),
        personas: v.personas,
        notas: v.notas || null,
        items: this.seleccion().map((l) => ({ idProducto: l.producto.idProducto, cantidad: l.cantidad })),
      })
      .subscribe({
        next: (reserva) => {
          this.aviso.exito(reserva.advertenciaPago ?? `Tu código de reserva es ${reserva.codigo}.`, 'Reserva registrada');
          this.router.navigate(['/mi-cuenta/reservas']);
        },
        error: (e) => {
          this.aviso.error(e);
          this.enviando.set(false);
        },
      });
  }
}
