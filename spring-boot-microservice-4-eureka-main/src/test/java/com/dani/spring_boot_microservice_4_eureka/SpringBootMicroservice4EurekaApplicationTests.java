package com.dani.spring_boot_microservice_4_eureka;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Clase de test básica para verificar que el contexto de la aplicación
 * del Servidor Eureka puede cargarse correctamente.
 */
@SpringBootTest // Indica que es un test de integración que carga el contexto de Spring Boot
class SpringBootMicroservice4EurekaApplicationTests {

	/**
	 * Test simple que verifica si el contexto de la aplicación se carga
	 * sin lanzar excepciones.
	 */
	@Test
	void contextLoads() {
		// Si el método se ejecuta sin errores, el contexto cargó correctamente.
	}

}