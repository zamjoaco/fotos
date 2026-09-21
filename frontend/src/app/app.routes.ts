import { Routes } from '@angular/router';
import { Home } from './public/home/home';
import { Sections } from './public/sections/sections';
import { Dashboard } from './admin/dashboard/dashboard';
import { authGuard } from './admin/guards/auth-guard';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'secciones', component: Sections },
  {
    path: 'admin',
    canActivate: [authGuard],
    children: [{ path: '', component: Dashboard }],
  },
  { path: '**', redirectTo: '' },
];
