import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { SelectModule } from 'primeng/select';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { Usuario } from '../../../core/models/panel.model';
import { AuthService } from '../../../core/services/auth.service';
import { AvisoService } from '../../../core/services/aviso.service';
import { PanelService } from '../../../core/services/panel.service';

@Component({
  selector: 'app-usuarios',
  imports: [DatePipe, FormsModule, ButtonModule, InputTextModule, SelectModule, TableModule, TagModule],
  templateUrl: './usuarios.html',
  styleUrl: './usuarios.scss',
})
export class Usuarios implements OnInit {
  private readonly panel = inject(PanelService);
  private readonly aviso = inject(AvisoService);
  protected readonly auth = inject(AuthService);

  protected readonly opcionesEstado = [
    { etiqueta: 'Activos', valor: 1 },
    { etiqueta: 'Inactivos', valor: 0 },
  ];

  protected readonly usuarios = signal<Usuario[]>([]);
  protected readonly cargando = signal(true);
  protected busqueda = '';
  protected estado: number | null = null;

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.cargando.set(true);
    this.panel.usuarios(this.busqueda.trim(), this.estado).subscribe({
      next: (pagina) => {
        this.usuarios.set(pagina.contenido);
        this.cargando.set(false);
      },
      error: (e) => {
        this.aviso.error(e, 'No pudimos cargar los usuarios');
        this.cargando.set(false);
      },
    });
  }

  protected alternarEstado(usuario: Usuario): void {
    const activar = usuario.estado !== 1;
    this.panel.activarUsuario(usuario.idUsuario, activar).subscribe({
      next: () => {
        this.aviso.exito(`${usuario.nombreCompleto} ahora está ${activar ? 'activo' : 'inactivo'}.`);
        this.cargar();
      },
      error: (e) => this.aviso.error(e),
    });
  }
}
