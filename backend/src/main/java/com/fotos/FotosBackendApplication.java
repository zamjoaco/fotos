package com.fotos;

import com.fotos.works.MinioProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * MinioProperties se registra aca, sin condicion, porque
 * MinioPresignedUrlService la pide por constructor de forma incondicional
 * (ver MinioConfig): si quedara solo en el @Configuration condicional de
 * MinIO, con storage.minio.enabled=false (el default) nunca existiria como
 * bean y el contexto no arrancaria.
 */
@SpringBootApplication
@EnableConfigurationProperties(MinioProperties.class)
public class FotosBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(FotosBackendApplication.class, args);
	}

}
