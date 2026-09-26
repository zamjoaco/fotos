import { Component, ElementRef, OnDestroy, effect, inject, signal, viewChild } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { PortfolioService, SectionDetail, WorkGalleryItem } from '../../core/portfolio.service';

@Component({
  imports: [RouterLink],
  selector: 'app-sections',
  styleUrl: './sections.css',
  templateUrl: './sections.html',
})
export class Sections implements OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly portfolio = inject(PortfolioService);

  protected readonly seccion = signal<SectionDetail | null>(null);
  protected readonly works = signal<WorkGalleryItem[]>([]);
  protected readonly cargando = signal(true);
  protected readonly cargandoMas = signal(false);
  protected readonly noEncontrada = signal(false);
  protected readonly hayMas = signal(false);
  protected readonly imagenesRotas = signal<ReadonlySet<string>>(new Set<string>());

  private readonly sentinel = viewChild<ElementRef<HTMLElement>>('sentinel');
  private observer?: IntersectionObserver;
  protected slug = '';
  private paginaActual = 0;

  constructor() {
    this.route.paramMap.subscribe((params) => {
      this.slug = params.get('slug') ?? '';
      this.works.set([]);
      this.paginaActual = 0;
      this.hayMas.set(false);
      this.noEncontrada.set(false);
      this.cargando.set(true);
      this.imagenesRotas.set(new Set<string>());

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
    });

    // El sentinel solo existe en el DOM cuando hayMas() es true (sections.html);
    // este effect (re)crea el observer cada vez que aparece/desaparece, en vez
    // de intentarlo una unica vez en ngAfterViewInit (que corre antes de que
    // el sentinel exista la primera vez).
    effect(() => {
      const el = this.sentinel()?.nativeElement;
      this.observer?.disconnect();
      if (!el || typeof IntersectionObserver === 'undefined') {
        return;
      }
      this.observer = new IntersectionObserver((entries) => {
        if (entries.some((entry) => entry.isIntersecting)) {
          this.cargarSiguiente();
        }
      });
      this.observer.observe(el);
    });
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

  protected imagenRota(work: WorkGalleryItem): boolean {
    return this.imagenesRotas().has(work.id);
  }

  // Las URLs de Minio son presignadas y expiran: si la foto falla al cargar
  // (404, token vencido, red) el <img> roto no aporta tamaño y la celda del
  // grid se desarma. Marcamos el work para pintar un placeholder en su lugar.
  protected alFallarImagen(work: WorkGalleryItem): void {
    this.imagenesRotas.update((actuales) => {
      const siguiente = new Set(actuales);
      siguiente.add(work.id);
      return siguiente;
    });
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
