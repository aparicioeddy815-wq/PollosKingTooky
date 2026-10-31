import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-gerente',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './gerente.html',
  styleUrls: ['./gerente.scss']
})
export class GerenteComponent implements OnInit {
  private http = inject(HttpClient);
  private router = inject(Router);

  ngOnInit(): void {
    // Inicialización del componente del gerente
  }
  // Método para cerrar sesión de forma segura
  cerrarSesion(): void {
    // Si guardas tokens o datos en el localStorage, puedes limpiarlos aquí:
    localStorage.removeItem('token');
    localStorage.removeItem('role');

    // Redirigimos al usuario a la pantalla de login
    this.router.navigate(['/login']);
  }
}
