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
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
