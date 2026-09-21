export const environment = {
  production: true,
  // Relativo: en prod el reverse proxy (ver Epic 7 / deploy) sirve el
  // backend bajo /api en el mismo origin.
  apiUrl: '/api',
};
