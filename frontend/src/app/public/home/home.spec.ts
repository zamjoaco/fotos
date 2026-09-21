import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { Home } from './home';

describe('Home', () => {
  let fixture: ComponentFixture<Home>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Home],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(Home);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    fixture.detectChanges();
    httpMock.expectOne(`${environment.apiUrl}/sections`).flush([]);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('muestra las secciones que devuelve la API', () => {
    fixture.detectChanges();
    httpMock.expectOne(`${environment.apiUrl}/sections`).flush([
      { slug: 'bodas', nombre: 'Bodas', orden: 0, cantidadWorks: 3 },
    ]);
    fixture.detectChanges();

    const links = fixture.nativeElement.querySelectorAll('a');
    expect(links.length).toBe(1);
    expect(links[0].textContent).toContain('Bodas');
  });

  it('muestra un estado vacio si no hay secciones publicadas', () => {
    fixture.detectChanges();
    httpMock.expectOne(`${environment.apiUrl}/sections`).flush([]);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Todavia no hay secciones publicadas');
  });
});
