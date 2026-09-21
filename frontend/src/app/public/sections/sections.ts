import { AfterViewInit, Component, ElementRef, OnDestroy, inject, signal, viewChild } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { PortfolioService, SectionDetail, WorkGalleryItem } from '../../core/portfolio.service';

@Component({
  imports: [RouterLink],
  selector: 'app-sections',
  styleUrl: './sections.css',
  templateUrl: './sections.html',
})
export class Sections implements AfterViewInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly portfolio = inject(PortfolioService);

  protected readonly seccion = signal<SectionDetail | null>(null);
  protected readonly works = signal<WorkGalleryItem[]>([]);
  protected readonly cargando = signal(true);
  protected readonly cargandoMas = signal(false);
  protected readonly noEncontrada = signal(false);
  protected readonly hayMas = signal(false);

  private readonly sentinel = viewChild<ElementRef<HTMLElement>>('sentinel');
  private observer?: IntersectionObserver;
  protected slug = '';
  private paginaActual = 0;

  constructor() {
    this.slug = this.route.snapshot.paramMap.get('slug') ?? '';
    this.portfolio.obtenerSeccion(this.slug).subscribe({
      next: (seccion) => {
        this.seccion.set(seccion);
        this.cargarPagina(0);
      },
      error: () => {
        this.noEncontrada.set(true);
        this.cargando.set(false);
      },
    });
  }

  ngAfterViewInit(): void {
    const el = this.sentinel()?.nativeElement;
    if (!el || typeof IntersectionObserver === 'undefined') {
      return;
    }
    this.observer = new IntersectionObserver((entries) => {
      if (entries.some((entry) => entry.isIntersecting)) {
        this.cargarSiguiente();
      }
    });
    this.observer.observe(el);
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }

  protected cargarSiguiente(): void {
    if (!this.hayMas() || this.cargandoMas()) {
      return;
    }
    this.cargandoMas.set(true);
    this.cargarPagina(this.paginaActual + 1);
  }

  private cargarPagina(page: number): void {
    this.portfolio.listarWorks(this.slug, page).subscribe({
      next: (resultado) => {
        this.works.update((actuales) => [...actuales, ...resultado.content]);
        this.paginaActual = resultado.page;
        this.hayMas.set(resultado.page + 1 < resultado.totalPages);
        this.cargando.set(false);
        this.cargandoMas.set(false);
      },
      error: () => {
        this.cargando.set(false);
        this.cargandoMas.set(false);
      },
    });
  }
}
