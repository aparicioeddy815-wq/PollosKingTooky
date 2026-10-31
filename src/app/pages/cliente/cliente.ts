import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
  selector: 'app-cliente',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './cliente.html',
  styleUrls: ['./cliente.scss']
})
export class ClienteComponent implements OnInit {
  private http = inject(HttpClient);
  private router = inject(Router);

  ngOnInit(): void {
    // Inicialización del módulo de cliente
  }

  cerrarSesion(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    this.router.navigate(['/login']);
  }
}
