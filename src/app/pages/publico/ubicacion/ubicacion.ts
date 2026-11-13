import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { ButtonModule } from 'primeng/button';
import { Horario, Ubicacion as UbicacionLocal } from '../../../core/models/publico.model';
import { PublicoService } from '../../../core/services/publico.service';

@Component({
  selector: 'app-ubicacion',
  imports: [ButtonModule],
  templateUrl: './ubicacion.html',
  styleUrl: './ubicacion.scss',
})
export class Ubicacion implements OnInit {
  private readonly publico = inject(PublicoService);
  private readonly sanitizer = inject(DomSanitizer);

  protected readonly ubicacion = signal<UbicacionLocal | null>(null);
  protected readonly horarios = signal<Horario[]>([]);

  protected readonly mapa = computed(() => {
    const u = this.ubicacion();
    if (!u) return null;
    const margen = 0.004;
    const caja = [u.longitud - margen, u.latitud - margen, u.longitud + margen, u.latitud + margen].join('%2C');
    const url = `https://www.openstreetmap.org/export/embed.html?bbox=${caja}&layer=mapnik&marker=${u.latitud}%2C${u.longitud}`;
    return this.sanitizer.bypassSecurityTrustResourceUrl(url);
  });

  protected readonly enlaceMapa = computed(() => {
    const u = this.ubicacion();
    return u ? `https://www.google.com/maps?q=${u.latitud},${u.longitud}` : '#';
  });

  ngOnInit(): void {
    this.publico.ubicacion().subscribe({ next: (u) => this.ubicacion.set(u) });
    this.publico.horarios().subscribe({ next: (h) => this.horarios.set(h) });
  }
}
