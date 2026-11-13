import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputNumberModule } from 'primeng/inputnumber';
import { SelectModule } from 'primeng/select';
import { TextareaModule } from 'primeng/textarea';
import { CategoriaQueja, Queja, TipoQueja } from '../../../core/models/cliente.model';
import { AvisoService } from '../../../core/services/aviso.service';
import { ClienteService } from '../../../core/services/cliente.service';
import { CATEGORIAS_QUEJA, TIPOS_QUEJA, etiquetaDe } from '../../../core/utils/etiquetas';
import { EstadoVacio } from '../../../shared/components/estado-vacio/estado-vacio';

@Component({
  selector: 'app-quejas',
  imports: [ReactiveFormsModule, DatePipe, ButtonModule, InputNumberModule, SelectModule, TextareaModule, EstadoVacio],
  templateUrl: './quejas.html',
  styleUrl: './quejas.scss',
})
export class Quejas implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly cliente = inject(ClienteService);
  private readonly aviso = inject(AvisoService);

  protected readonly tipos = TIPOS_QUEJA;
  protected readonly categorias = CATEGORIAS_QUEJA;

  protected readonly historial = signal<Queja[]>([]);
  protected readonly enviando = signal(false);

  protected readonly formulario = this.fb.group({
    tipo: this.fb.nonNullable.control<TipoQueja>('SUGERENCIA', Validators.required),
    categoria: this.fb.nonNullable.control<CategoriaQueja>('OTRO', Validators.required),
    numeroPedido: this.fb.control<number | null>(null),
    mensaje: this.fb.nonNullable.control('', [Validators.required, Validators.maxLength(1000)]),
  });

  ngOnInit(): void {
    this.cargarHistorial();
  }

  protected nombreTipo(tipo: TipoQueja): string {
    return etiquetaDe(TIPOS_QUEJA, tipo);
  }

  protected nombreCategoria(categoria: CategoriaQueja): string {
    return etiquetaDe(CATEGORIAS_QUEJA, categoria);
  }

  protected enviar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    this.enviando.set(true);
    this.cliente.crearQueja(this.formulario.getRawValue()).subscribe({
      next: () => {
        this.aviso.exito('Gracias por escribirnos. Revisaremos tu mensaje.');
        this.formulario.patchValue({ mensaje: '', numeroPedido: null });
        this.formulario.controls.mensaje.markAsUntouched();
        this.enviando.set(false);
        this.cargarHistorial();
      },
      error: (e) => {
        this.aviso.error(e);
        this.enviando.set(false);
      },
    });
  }

  private cargarHistorial(): void {
    this.cliente.misQuejas().subscribe({
      next: (pagina) => this.historial.set(pagina.contenido),
      error: (e) => this.aviso.error(e, 'No pudimos cargar tu historial'),
    });
  }
}
