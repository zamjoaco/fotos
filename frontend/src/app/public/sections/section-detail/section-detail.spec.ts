import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { SectionDetail } from './section-detail';

describe('SectionDetail', () => {
  let fixture: ComponentFixture<SectionDetail>;
  let httpMock: HttpTestingController;

  function configurar(paramMap: Record<string, string>) {
    return TestBed.configureTestingModule({
      imports: [SectionDetail],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: { paramMap: of(convertToParamMap(paramMap)) },
        },
      ],
    }).compileComponents();
  }

  afterEach(() => {
    httpMock?.verify();
  });

  it('muestra la foto con navegacion a la anterior y siguiente', async () => {
    await configurar({ slug: 'bodas', workId: '2' });
    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(SectionDetail);
    fixture.detectChanges();

    httpMock.expectOne(`${environment.apiUrl}/sections/bodas/works/2`).flush({
      id: '2',
      imageUrl: 'https://minio.test/2.jpg',
      imageUrlExpiraEn: '',
      orden: 1,
      anteriorId: '1',
      siguienteId: '3',
    });
    fixture.detectChanges();

    const links: NodeListOf<HTMLAnchorElement> = fixture.nativeElement.querySelectorAll('a');
    const textos = Array.from(links).map((a) => a.textContent?.trim());
    expect(textos.some((t) => t?.includes('Anterior'))).toBe(true);
    expect(textos.some((t) => t?.includes('Siguiente'))).toBe(true);
  });

  it('no muestra link a "anterior" en la primera foto de la seccion', async () => {
    await configurar({ slug: 'bodas', workId: '1' });
    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(SectionDetail);
    fixture.detectChanges();

    httpMock.expectOne(`${environment.apiUrl}/sections/bodas/works/1`).flush({
      id: '1',
      imageUrl: 'https://minio.test/1.jpg',
      imageUrlExpiraEn: '',
      orden: 0,
      anteriorId: null,
      siguienteId: '2',
    });
    fixture.detectChanges();

    const links: NodeListOf<HTMLAnchorElement> = fixture.nativeElement.querySelectorAll('a');
    const textos = Array.from(links).map((a) => a.textContent?.trim());
    expect(textos.some((t) => t?.includes('Anterior'))).toBe(false);
  });

  it('muestra "no encontrado" cuando la foto no existe o no pertenece a la seccion', async () => {
    await configurar({ slug: 'bodas', workId: 'x' });
    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(SectionDetail);
    fixture.detectChanges();

    httpMock
      .expectOne(`${environment.apiUrl}/sections/bodas/works/x`)
      .flush({ error: 'not_found' }, { status: 404, statusText: 'Not Found' });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('No encontramos esta foto');
  });
});
