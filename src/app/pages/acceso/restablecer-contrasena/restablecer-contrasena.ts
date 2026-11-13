import { Component, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { PasswordModule } from 'primeng/password';
import { AuthService } from '../../../core/services/auth.service';
import { AvisoService } from '../../../core/services/aviso.service';
import { mensajeDeError } from '../../../core/utils/errores';
import { AccesoTarjeta } from '../../../shared/components/acceso-tarjeta/acceso-tarjeta';

@Component({
  selector: 'app-restablecer-contrasena',
  imports: [ReactiveFormsModule, RouterLink, ButtonModule, PasswordModule, AccesoTarjeta],
  templateUrl: './restablecer-contrasena.html',
  styleUrl: './restablecer-contrasena.scss',
})
export class RestablecerContrasena {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly aviso = inject(AvisoService);
  private readonly router = inject(Router);

  /** El token llega en el enlace del correo: /restablecer-contrasena?token=... */
  readonly token = input<string>();

  protected readonly enviando = signal(false);
  protected readonly error = signal('');

  protected readonly formulario = this.fb.nonNullable.group({
    nuevaContrasena: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(100)]],
  });

  protected guardar(): void {
    const token = this.token();
    if (!token || this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    this.enviando.set(true);
    this.error.set('');

    this.auth.restablecerContrasena(token, this.formulario.controls.nuevaContrasena.value).subscribe({
      next: () => {
        this.aviso.exito('Ya puedes ingresar con tu nueva contraseña.', 'Contraseña actualizada');
        this.router.navigate(['/ingresar']);
      },
      error: (e) => {
        this.error.set(mensajeDeError(e));
        this.enviando.set(false);
      },
    });
  }
}
