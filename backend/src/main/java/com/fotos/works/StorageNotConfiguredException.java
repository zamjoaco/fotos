package com.fotos.works;

/**
 * MinIO no esta habilitado (storage.minio.enabled=false) pero se intento
 * generar una URL presignada. Ver MinioConfig / MinioPresignedUrlService.
 */
public class StorageNotConfiguredException extends RuntimeException {

    public StorageNotConfiguredException(String message) {
        super(message);
    }
}
