package com.fotos.works;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code endpoint} debe ser el endpoint publico/reachable por el browser,
 * no un hostname interno de la red de Docker: queda embebido tal cual en
 * la URL presignada que termina recibiendo el visitante. Por eso el
 * MinioClient fija una region explicita (ver MinioConfig) — sin eso, el
 * SDK intenta resolver la region contra ese mismo endpoint antes de
 * firmar, y falla si no es alcanzable desde dentro del contenedor.
 */
@ConfigurationProperties(prefix = "storage.minio")
public record MinioProperties(
        boolean enabled,
        String endpoint,
        String accessKey,
        String secretKey,
        String bucket) {
}
