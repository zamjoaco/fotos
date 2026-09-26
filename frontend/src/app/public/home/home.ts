import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PortfolioService, SectionSummary } from '../../core/portfolio.service';

@Component({
  imports: [RouterLink],
  selector: 'app-home',
  styleUrl: './home.css',
  templateUrl: './home.html',
})
export class Home {
  private readonly portfolio = inject(PortfolioService);

  protected readonly secciones = signal<SectionSummary[]>([]);
  protected readonly cargando = signal(true);

  constructor() {
    this.portfolio.listarSecciones().subscribe({
      next: (secciones) => {
        this.secciones.set(secciones);
        this.cargando.set(false);
      },
      error: () => {
        this.secciones.set([]);
        this.cargando.set(false);
      },
    });
  }
}
