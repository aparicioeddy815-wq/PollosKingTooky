import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ValidarAccesoService } from './service/validar-acceso';
import { AccesoModel } from './interface/acceso-model';

@Component({
  selector: 'app-validar-acceso',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './validar-acceso.html',
  styleUrl: './validar-acceso.css'
})
export class ValidarAccesoComponent {
  credenciales: AccesoModel = {
    usuario: '',
    contrasena: ''
  };

  constructor(private accesoService: ValidarAccesoService) {}

  onSubmit() {
    this.accesoService.login(this.credenciales).subscribe({
      next: (response: any) => {
        console.log('Login exitoso, token:', response.token);
        localStorage.setItem('token', response.token);
        alert('¡Bienvenido!');
      },
      error: (err: any) => {
        console.error('Error al iniciar sesión', err);
        alert('Usuario o contraseña incorrectos');
      }
    });
  }
}
