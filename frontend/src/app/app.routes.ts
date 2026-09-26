import { Routes } from '@angular/router';
import { Home } from './public/home/home';
import { Sections } from './public/sections/sections';
import { SectionDetail } from './public/sections/section-detail/section-detail';
import { Dashboard } from './admin/dashboard/dashboard';
import { authGuard } from './admin/guards/auth-guard';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'secciones/:slug', component: Sections },
  { path: 'secciones/:slug/:workId', component: SectionDetail },
  {
    path: 'admin',
    canActivate: [authGuard],
    children: [{ path: '', component: Dashboard }],
  },
  { path: '**', redirectTo: '' },
];
