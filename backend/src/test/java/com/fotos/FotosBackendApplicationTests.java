package com.fotos;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Smoke test de arranque de contexto contra un Postgres real (Testcontainers),
 * no contra el datasource local por defecto. Corre con `mvn verify`; requiere
 * Docker disponible (por eso el CI de Epic 0 sigue usando -DskipTests, ver
 * .github/workflows/ci.yml). Redis/mail/MinIO no tienen contenedor propio
 * todavia (llegan con sus features en Epic 1+): el contexto arranca igual
 * porque esos clientes no conectan de forma eager.
 */
@Testcontainers
@SpringBootTest
class FotosBackendApplicationTests {

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@Test
	void contextLoads() {
	}

}
