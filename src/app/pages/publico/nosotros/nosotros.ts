import { Component, OnInit, inject, signal } from '@angular/core';
import { SobreNosotros } from '../../../core/models/publico.model';
import { PublicoService } from '../../../core/services/publico.service';

@Component({
  selector: 'app-nosotros',
  templateUrl: './nosotros.html',
  styleUrl: './nosotros.scss',
})
export class Nosotros implements OnInit {
  private readonly publico = inject(PublicoService);

  protected readonly info = signal<SobreNosotros | null>(null);

  ngOnInit(): void {
    this.publico.sobreNosotros().subscribe({ next: (info) => this.info.set(info) });
  }
}
