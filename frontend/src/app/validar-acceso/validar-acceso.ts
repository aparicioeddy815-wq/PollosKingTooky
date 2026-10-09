import { Component } from '@angular/core';
import { HttpClientModule, HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms'; // 1. Importar FormsModule
import { Router } from '@angular/router';

@Component({
  selector: 'app-validar-acceso',
  standalone: true, // Si tu componente es Standalone
  imports: [FormsModule, HttpClientModule], // 2. Agregarlo aquí
  templateUrl: './validar-acceso.html',
  styleUrls: ['./validar-acceso.css']
})
export class ValidarAccesoComponent {
  credentials = {
    nombreUsuario: '',
    contrasena: ''
  };

  private apiUrl = 'http://localhost:8080/api/auth/login';

  constructor(private http: HttpClient, private router: Router) {}

  onLogin() {
    console.log('Datos enviados:', this.credentials);

    this.http.post<any>(this.apiUrl, this.credentials).subscribe({
      next: (response) => {
        console.log('¡Inicio de sesión exitoso!', response);
        if (response && response.token) {
          localStorage.setItem('token', response.token);
        }
        alert('¡Bienvenido!');
      },
      error: (error) => {
        console.error('Error al iniciar sesión:', error);
        alert('Credenciales incorrectas o problema de servidor.');
      }
    });
  }
}
