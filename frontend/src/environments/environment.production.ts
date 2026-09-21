export const environment = {
  production: true,
  // Relativo: en prod el reverse proxy (ver Epic 7 / deploy) sirve el
  // backend bajo /api en el mismo origin. Ese proxy debe hacer el mismo
  // strip de "/api" que frontend/proxy.conf.json hace en dev (pathRewrite),
  // ya que el backend no tiene context-path propio.
  apiUrl: '/api',
};
