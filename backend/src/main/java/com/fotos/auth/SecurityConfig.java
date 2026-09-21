package com.fotos.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Placeholder de seguridad para Epic 0: sin login todavia (eso llega en
 * Epic 4), pero al tener spring-boot-starter-security en el classpath,
 * Spring Security bloquea todo por defecto (incluido /actuator/health,
 * usado por el healthcheck de docker-compose) con una password generada.
 * Se abre todo explicitamente hasta que exista auth real.
 *
 * TODO(Epic 4): reemplazar el permitAll por reglas reales (JWT en cookie
 * httpOnly para /admin/**, resto publico) y volver a habilitar CSRF donde
 * corresponda. Ese dia, revisar tambien el healthcheck de docker-compose.yml
 * (hoy depende de que /actuator/health quede sin auth).
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
