import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { RatingModule } from 'primeng/rating';
import { TextareaModule } from 'primeng/textarea';
import { EvaluacionDiaria } from '../../../core/models/cliente.model';
import { AvisoService } from '../../../core/services/aviso.service';
import { ClienteService } from '../../../core/services/cliente.service';

@Component({
  selector: 'app-evaluacion',
  imports: [FormsModule, ButtonModule, RatingModule, TextareaModule],
  templateUrl: './evaluacion.html',
  styleUrl: './evaluacion.scss',
})
export class Evaluacion implements OnInit {
  private readonly cliente = inject(ClienteService);
  private readonly aviso = inject(AvisoService);

  protected readonly guardada = signal<EvaluacionDiaria | null>(null);
  protected readonly enviando = signal(false);

  protected estrellas = 0;
  protected comentario = '';

  ngOnInit(): void {
    this.cliente.evaluacionDeHoy().subscribe({
      next: (evaluacion) => {
        if (evaluacion) {
          this.guardada.set(evaluacion);
          this.estrellas = evaluacion.estrellas;
          this.comentario = evaluacion.comentario ?? '';
        }
      },
    });
  }

  protected enviar(): void {
    if (this.estrellas < 1) {
      this.aviso.info('Elige de 1 a 5 estrellas para calificar tu visita.');
      return;
    }
    this.enviando.set(true);
    this.cliente.evaluar(this.estrellas, this.comentario.trim() || null).subscribe({
      next: (evaluacion) => {
        this.guardada.set(evaluacion);
        this.aviso.exito('Gracias por calificar tu visita.');
        this.enviando.set(false);
      },
      error: (e) => {
        this.aviso.error(e);
        this.enviando.set(false);
      },
    });
  }
}
