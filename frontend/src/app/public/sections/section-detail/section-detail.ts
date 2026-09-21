import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { PortfolioService, WorkDetail } from '../../../core/portfolio.service';

@Component({
  imports: [RouterLink],
  selector: 'app-section-detail',
  styleUrl: './section-detail.css',
  templateUrl: './section-detail.html',
})
export class SectionDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly portfolio = inject(PortfolioService);

  protected readonly work = signal<WorkDetail | null>(null);
  protected readonly cargando = signal(true);
  protected readonly noEncontrado = signal(false);
  protected slug = '';

  constructor() {
    this.route.paramMap.subscribe((params) => {
      const slug = params.get('slug');
      const workId = params.get('workId');
      if (!slug || !workId) {
        this.noEncontrado.set(true);
        this.cargando.set(false);
        return;
      }

      this.slug = slug;
      this.cargando.set(true);
      this.noEncontrado.set(false);
      this.portfolio.obtenerWork(slug, workId).subscribe({
        next: (work) => {
          this.work.set(work);
          this.cargando.set(false);
        },
        error: () => {
          this.noEncontrado.set(true);
          this.cargando.set(false);
        },
      });
    });
  }
}
