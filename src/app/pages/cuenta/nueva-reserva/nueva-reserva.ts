import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { DatePickerModule } from 'primeng/datepicker';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { ProductoPublico } from '../../../core/models/producto.model';
import { AuthService } from '../../../core/services/auth.service';
import { AvisoService } from '../../../core/services/aviso.service';
import { PublicoService } from '../../../core/services/publico.service';
import { ReservaService } from '../../../core/services/reserva.service';
import { aFechaIso, aHoraIso } from '../../../core/utils/fechas';
import { BolivianosPipe } from '../../../shared/pipes/bolivianos.pipe';

@Component({
  selector: 'app-nueva-reserva',
  imports: [
    ReactiveFormsModule,
    ButtonModule,
    DatePickerModule,
    InputNumberModule,
    InputTextModule,
    TextareaModule,
    BolivianosPipe,
  ],
  templateUrl: './nueva-reserva.html',
  styleUrl: './nueva-reserva.scss',
})
export class NuevaReserva implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly publico = inject(PublicoService);
  private readonly reservas = inject(ReservaService);
  private readonly aviso = inject(AvisoService);
  private readonly router = inject(Router);

  protected readonly hoy = new Date();
  protected readonly platos = signal<ProductoPublico[]>([]);
  protected readonly cantidades = signal<Record<number, number>>({});
  protected readonly enviando = signal(false);
  protected readonly sinPlatos = signal(false);

  protected readonly seleccion = computed(() =>
    this.platos()
      .filter((p) => (this.cantidades()[p.idProducto] ?? 0) > 0)
      .map((p) => ({ producto: p, cantidad: this.cantidades()[p.idProducto] })),
  );

  protected readonly total = computed(() =>
    this.seleccion().reduce((suma, linea) => suma + linea.producto.precio * linea.cantidad, 0),
  );

  protected readonly formulario = this.fb.nonNullable.group({
    nombreCliente: [this.auth.sesion()?.nombreCompleto ?? '', [Validators.required, Validators.maxLength(255)]],
    telefono: ['', [Validators.required, Validators.pattern(/^[0-9+() -]{7,30}$/)]],
    correo: [this.auth.sesion()?.correo ?? '', Validators.email],
    fecha: this.fb.control<Date | null>(null, Validators.required),
    hora: this.fb.control<Date | null>(null, Validators.required),
    personas: [2, [Validators.required, Validators.min(1), Validators.max(50)]],
    notas: ['', Validators.maxLength(500)],
  });

  ngOnInit(): void {
    this.publico.menu().subscribe({
      next: (menu) => this.platos.set(menu.categorias.flatMap((c) => c.productos).filter((p) => p.disponible)),
      error: (e) => this.aviso.error(e, 'No pudimos cargar el menú'),
    });
  }

  protected cantidadDe(producto: ProductoPublico): number {
    return this.cantidades()[producto.idProducto] ?? 0;
  }

  protected cambiarCantidad(producto: ProductoPublico, cambio: number): void {
    const nueva = Math.min(50, Math.max(0, this.cantidadDe(producto) + cambio));
    this.cantidades.update((actual) => ({ ...actual, [producto.idProducto]: nueva }));
    this.sinPlatos.set(false);
  }

  protected reservar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    if (this.seleccion().length === 0) {
      this.sinPlatos.set(true);
      return;
    }

    const valores = this.formulario.getRawValue();
    this.enviando.set(true);

    this.reservas
      .crear({
        nombreCliente: valores.nombreCliente,
        telefono: valores.telefono,
        correo: valores.correo || null,
        fecha: aFechaIso(valores.fecha!),
        hora: aHoraIso(valores.hora!),
        personas: valores.personas,
        notas: valores.notas || null,
        items: this.seleccion().map((l) => ({ idProducto: l.producto.idProducto, cantidad: l.cantidad })),
      })
      .subscribe({
        next: (reserva) => {
          this.aviso.exito(
            reserva.advertenciaPago ?? `Tu código de reserva es ${reserva.codigo}.`,
            'Reserva registrada',
          );
          this.router.navigate(['/mi-cuenta/reservas']);
        },
        error: (e) => {
          this.aviso.error(e);
          this.enviando.set(false);
        },
      });
  }
}
