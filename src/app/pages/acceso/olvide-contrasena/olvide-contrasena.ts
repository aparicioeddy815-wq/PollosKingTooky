import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { AuthService } from '../../../core/services/auth.service';
import { mensajeDeError } from '../../../core/utils/errores';
import { AccesoTarjeta } from '../../../shared/components/acceso-tarjeta/acceso-tarjeta';

@Component({
  selector: 'app-olvide-contrasena',
  imports: [ReactiveFormsModule, RouterLink, ButtonModule, InputTextModule, AccesoTarjeta],
  templateUrl: './olvide-contrasena.html',
  styleUrl: './olvide-contrasena.scss',
})
export class OlvideContrasena {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);

  protected readonly enviando = signal(false);
  protected readonly error = signal('');
  protected readonly confirmacion = signal('');

  protected readonly formulario = this.fb.nonNullable.group({
    correo: ['', [Validators.required, Validators.email]],
  });

  protected enviar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    this.enviando.set(true);
    this.error.set('');

    this.auth.olvideContrasena(this.formulario.controls.correo.value).subscribe({
      next: (respuesta) => {
        this.confirmacion.set(respuesta.mensaje);
        this.enviando.set(false);
      },
      error: (e) => {
        this.error.set(mensajeDeError(e));
        this.enviando.set(false);
      },
    });
  }
}
