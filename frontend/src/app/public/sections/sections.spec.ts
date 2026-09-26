import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { Subject } from 'rxjs';
import { vi } from 'vitest';
import { environment } from '../../../environments/environment';
import { Sections } from './sections';

describe('Sections', () => {
  let fixture: ComponentFixture<Sections>;
  let httpMock: HttpTestingController;
  let paramMap$: Subject<ReturnType<typeof convertToParamMap>>;

  beforeEach(async () => {
    paramMap$ = new Subject();
    await TestBed.configureTestingModule({
      imports: [Sections],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { paramMap: paramMap$ } },
      ],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(Sections);
  });

  const originalIntersectionObserver = (globalThis as unknown as { IntersectionObserver?: unknown })
    .IntersectionObserver;

  afterEach(() => {
    httpMock.verify();
    (globalThis as unknown as { IntersectionObserver?: unknown }).IntersectionObserver =
      originalIntersectionObserver;
  });

  function irASeccion(slug: string) {
    fixture.detectChanges();
    paramMap$.next(convertToParamMap({ slug }));
  }

  function flushSeccion(slug: string) {
    httpMock.expectOne(`${environment.apiUrl}/sections/${slug}`).flush({ slug, nombre: slug, orden: 0 });
  }

  it('should create', () => {
    irASeccion('bodas');
    flushSeccion('bodas');
    httpMock
      .expectOne((req) => req.url === `${environment.apiUrl}/sections/bodas/works`)
      .flush({ content: [], page: 0, size: 24, totalElements: 0, totalPages: 0 });
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('muestra la galeria con las fotos de la primera pagina', () => {
    irASeccion('bodas');
    flushSeccion('bodas');
    httpMock.expectOne((req) => req.url === `${environment.apiUrl}/sections/bodas/works`).flush({
      content: [{ id: '1', imageUrl: 'https://minio.test/1.jpg', imageUrlExpiraEn: '', orden: 0 }],
      page: 0,
      size: 24,
      totalElements: 1,
      totalPages: 1,
    });
    fixture.detectChanges();

    const imgs = fixture.nativeElement.querySelectorAll('img');
    expect(imgs.length).toBe(1);
  });

  it('muestra estado vacio cuando la seccion no tiene fotos publicadas', () => {
    irASeccion('bodas');
    flushSeccion('bodas');
    httpMock
      .expectOne((req) => req.url === `${environment.apiUrl}/sections/bodas/works`)
      .flush({ content: [], page: 0, size: 24, totalElements: 0, totalPages: 0 });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Todavia no hay fotos publicadas');
  });

  it('muestra "no encontrada" cuando la seccion no existe o no esta publicada', () => {
    irASeccion('bodas');
    httpMock.expectOne(`${environment.apiUrl}/sections/bodas`).flush(
      { error: 'not_found' },
      { status: 404, statusText: 'Not Found' },
    );
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('No encontramos esta seccion');
  });

  it('carga la siguiente pagina al hacer click en "Cargar mas"', () => {
    irASeccion('bodas');
    flushSeccion('bodas');
    httpMock.expectOne((req) => req.url === `${environment.apiUrl}/sections/bodas/works`).flush({
      content: [{ id: '1', imageUrl: 'https://minio.test/1.jpg', imageUrlExpiraEn: '', orden: 0 }],
      page: 0,
      size: 1,
      totalElements: 2,
      totalPages: 2,
    });
    fixture.detectChanges();

    const boton: HTMLButtonElement = fixture.nativeElement.querySelector('button');
    expect(boton).toBeTruthy();
    boton.click();

    httpMock
      .expectOne((req) => req.url === `${environment.apiUrl}/sections/bodas/works` && req.params.get('page') === '1')
      .flush({
        content: [{ id: '2', imageUrl: 'https://minio.test/2.jpg', imageUrlExpiraEn: '', orden: 1 }],
        page: 1,
        size: 1,
        totalElements: 2,
        totalPages: 2,
      });
    fixture.detectChanges();

    const imgs = fixture.nativeElement.querySelectorAll('img');
    expect(imgs.length).toBe(2);
  });

  it('crea el IntersectionObserver sobre el sentinel apenas hay mas paginas para cargar', () => {
    const observeSpy = vi.fn();
    const disconnectSpy = vi.fn();
    (globalThis as unknown as { IntersectionObserver: unknown }).IntersectionObserver = class {
      observe = observeSpy;
      disconnect = disconnectSpy;
    };

    irASeccion('bodas');
    flushSeccion('bodas');
    httpMock.expectOne((req) => req.url === `${environment.apiUrl}/sections/bodas/works`).flush({
      content: [{ id: '1', imageUrl: 'https://minio.test/1.jpg', imageUrlExpiraEn: '', orden: 0 }],
      page: 0,
      size: 1,
      totalElements: 2,
      totalPages: 2,
    });
    fixture.detectChanges();

    // Regresion del bug donde el observer se creaba en ngAfterViewInit, antes
    // de que el sentinel existiera en el DOM (solo aparece cuando hayMas()).
    expect(observeSpy).toHaveBeenCalled();
  });

  it('reemplaza solo la foto que falla por un placeholder y conserva la celda del grid', () => {
    irASeccion('bodas');
    flushSeccion('bodas');
    httpMock.expectOne((req) => req.url === `${environment.apiUrl}/sections/bodas/works`).flush({
      content: [
        { id: '1', imageUrl: 'https://minio.test/1.jpg', imageUrlExpiraEn: '', orden: 0 },
        { id: '2', imageUrl: 'https://minio.test/2.jpg', imageUrlExpiraEn: '', orden: 1 },
      ],
      page: 0,
      size: 24,
      totalElements: 2,
      totalPages: 1,
    });
    fixture.detectChanges();

    const imgs: NodeListOf<HTMLImageElement> = fixture.nativeElement.querySelectorAll('img');
    expect(imgs.length).toBe(2);
    imgs[0].dispatchEvent(new Event('error'));
    fixture.detectChanges();

    // La foto rota deja de renderizar <img>, pero la celda sigue reservando el
    // hueco: el aspect-ratio vive en el <a>, no en la imagen.
    expect(fixture.nativeElement.querySelectorAll('img').length).toBe(1);
    const celdas: NodeListOf<HTMLAnchorElement> = fixture.nativeElement.querySelectorAll('ul.grid a');
    expect(celdas.length).toBe(2);
    expect(celdas[0].className).toContain('aspect-square');
    expect(fixture.nativeElement.textContent).toContain('Foto no disponible');
  });

  it('limpia las fotos rotas al cambiar de seccion', () => {
    irASeccion('bodas');
    flushSeccion('bodas');
    httpMock.expectOne((req) => req.url === `${environment.apiUrl}/sections/bodas/works`).flush({
      content: [{ id: '1', imageUrl: 'https://minio.test/1.jpg', imageUrlExpiraEn: '', orden: 0 }],
      page: 0,
      size: 24,
      totalElements: 1,
      totalPages: 1,
    });
    fixture.detectChanges();
    fixture.nativeElement.querySelector('img').dispatchEvent(new Event('error'));
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Foto no disponible');

    paramMap$.next(convertToParamMap({ slug: 'retratos' }));
    flushSeccion('retratos');
    httpMock
      .expectOne((req) => req.url === `${environment.apiUrl}/sections/retratos/works`)
      .flush({
        content: [{ id: '9', imageUrl: 'https://minio.test/9.jpg', imageUrlExpiraEn: '', orden: 0 }],
        page: 0,
        size: 24,
        totalElements: 1,
        totalPages: 1,
      });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).not.toContain('Foto no disponible');
    expect(fixture.nativeElement.querySelectorAll('img').length).toBe(1);
  });

  it('recarga la galeria al navegar de una seccion a otra sin remontar el componente', () => {
    irASeccion('bodas');
    flushSeccion('bodas');
    httpMock.expectOne((req) => req.url === `${environment.apiUrl}/sections/bodas/works`).flush({
      content: [{ id: '1', imageUrl: 'https://minio.test/1.jpg', imageUrlExpiraEn: '', orden: 0 }],
      page: 0,
      size: 24,
      totalElements: 1,
      totalPages: 1,
    });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('img').length).toBe(1);

    paramMap$.next(convertToParamMap({ slug: 'retratos' }));
    flushSeccion('retratos');
    httpMock
      .expectOne((req) => req.url === `${environment.apiUrl}/sections/retratos/works`)
      .flush({ content: [], page: 0, size: 24, totalElements: 0, totalPages: 0 });
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelectorAll('img').length).toBe(0);
    expect(fixture.nativeElement.textContent).toContain('Todavia no hay fotos publicadas');
  });
});
