package com.fotos.works;

import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cliente MinIO real, activo solo cuando storage.minio.enabled=true (ver
 * application.yml). Deshabilitado por defecto para que un `mvn test` local
 * sin Docker no intente resolver un endpoint MinIO inexistente.
 */
@Configuration
@EnableConfigurationProperties(MinioProperties.class)
@ConditionalOnProperty(prefix = "storage.minio", name = "enabled", havingValue = "true")
public class MinioConfig {

    @Bean
    public MinioClient minioClient(MinioProperties properties) {
        return MinioClient.builder()
                .endpoint(properties.endpoint())
                .credentials(properties.accessKey(), properties.secretKey())
                // Sin region explicita, el SDK hace un round-trip de red para
                // resolverla antes de firmar (rompe si endpoint es publico
                // pero no alcanzable desde dentro del contenedor). Fijarla
                // evita esa llamada; el presigning queda 100% local.
                .region("us-east-1")
                .build();
    }
}
