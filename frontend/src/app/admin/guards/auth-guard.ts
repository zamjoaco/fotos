import { CanActivateFn } from '@angular/router';

export const authGuard: CanActivateFn = (route, state) => {
  // TODO(Epic 4): validar sesión de admin (JWT en cookie httpOnly) y
  // redirigir a /admin/login si no está autenticado. Por ahora siempre
  // permite el acceso porque el login real todavía no existe.
  return true;
};
