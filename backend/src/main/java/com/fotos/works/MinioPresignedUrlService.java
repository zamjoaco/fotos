package com.fotos.works;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * Genera URLs presignadas de corta duracion contra MinIO (research.md #1,
 * feature 001-portfolio-publico). El MinioClient es opcional a nivel de
 * Spring (bean condicional, ver MinioConfig) para que el contexto siga
 * arrancando cuando storage.minio.enabled=false (default de Epic 0, usado
 * por ejemplo en el smoke test de arranque); si se llama a un metodo de
 * esta clase sin MinIO configurado, falla explicitamente en ese momento.
 */
@Service
public class MinioPresignedUrlService {

    private static final Duration EXPIRACION = Duration.ofMinutes(15);

    private final ObjectProvider<MinioClient> minioClientProvider;
    private final MinioProperties properties;

    public MinioPresignedUrlService(ObjectProvider<MinioClient> minioClientProvider, MinioProperties properties) {
        this.minioClientProvider = minioClientProvider;
        this.properties = properties;
    }

    public record PresignedUrl(String url, Instant expiraEn) {
    }

    public PresignedUrl generar(String objectKey) {
        MinioClient client = minioClientProvider.getIfAvailable();
        if (client == null) {
            throw new IllegalStateException(
                    "MinIO no esta habilitado (storage.minio.enabled=false); no se pueden generar URLs de imagenes.");
        }
        try {
            String url = client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(properties.bucket())
                    .object(objectKey)
                    .expiry((int) EXPIRACION.toSeconds())
                    .build());
            return new PresignedUrl(url, Instant.now().plus(EXPIRACION));
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar la URL presignada para " + objectKey, e);
        }
    }
}
