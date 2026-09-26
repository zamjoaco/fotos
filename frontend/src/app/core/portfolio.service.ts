import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface SectionSummary {
  slug: string;
  nombre: string;
  orden: number;
  cantidadWorks: number;
}

export interface SectionDetail {
  slug: string;
  nombre: string;
  orden: number;
}

export interface WorkGalleryItem {
  id: string;
  imageUrl: string;
  imageUrlExpiraEn: string;
  orden: number;
}

export interface WorkPage {
  content: WorkGalleryItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface WorkDetail {
  id: string;
  imageUrl: string;
  imageUrlExpiraEn: string;
  orden: number;
  anteriorId: string | null;
  siguienteId: string | null;
}

@Injectable({ providedIn: 'root' })
export class PortfolioService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/sections`;

  listarSecciones(): Observable<SectionSummary[]> {
    return this.http.get<SectionSummary[]>(this.baseUrl);
  }

  obtenerSeccion(slug: string): Observable<SectionDetail> {
    return this.http.get<SectionDetail>(`${this.baseUrl}/${slug}`);
  }

  listarWorks(slug: string, page: number): Observable<WorkPage> {
    return this.http.get<WorkPage>(`${this.baseUrl}/${slug}/works`, { params: { page } });
  }

  obtenerWork(slug: string, workId: string): Observable<WorkDetail> {
    return this.http.get<WorkDetail>(`${this.baseUrl}/${slug}/works/${workId}`);
  }
}
