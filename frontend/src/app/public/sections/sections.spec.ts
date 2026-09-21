import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { Sections } from './sections';

describe('Sections', () => {
  let fixture: ComponentFixture<Sections>;
  let httpMock: HttpTestingController;

  const activatedRouteStub = {
    snapshot: { paramMap: convertToParamMap({ slug: 'bodas' }) },
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Sections],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: ActivatedRoute, useValue: activatedRouteStub },
      ],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(Sections);
  });

  afterEach(() => {
    httpMock.verify();
  });

  function flushSeccion() {
    httpMock
      .expectOne(`${environment.apiUrl}/sections/bodas`)
      .flush({ slug: 'bodas', nombre: 'Bodas', orden: 0 });
  }

  it('should create', () => {
    fixture.detectChanges();
    flushSeccion();
    httpMock
      .expectOne((req) => req.url === `${environment.apiUrl}/sections/bodas/works`)
      .flush({ content: [], page: 0, size: 24, totalElements: 0, totalPages: 0 });
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('muestra la galeria con las fotos de la primera pagina', () => {
    fixture.detectChanges();
    flushSeccion();
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
    fixture.detectChanges();
    flushSeccion();
    httpMock
      .expectOne((req) => req.url === `${environment.apiUrl}/sections/bodas/works`)
      .flush({ content: [], page: 0, size: 24, totalElements: 0, totalPages: 0 });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Todavia no hay fotos publicadas');
  });

  it('muestra "no encontrada" cuando la seccion no existe o no esta publicada', () => {
    fixture.detectChanges();
    httpMock.expectOne(`${environment.apiUrl}/sections/bodas`).flush(
      { error: 'not_found' },
      { status: 404, statusText: 'Not Found' },
    );
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('No encontramos esta seccion');
  });

  it('carga la siguiente pagina al hacer click en "Cargar mas"', () => {
    fixture.detectChanges();
    flushSeccion();
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
});
