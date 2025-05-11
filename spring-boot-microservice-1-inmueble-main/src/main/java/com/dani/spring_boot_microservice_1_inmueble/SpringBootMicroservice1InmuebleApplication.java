package com.dani.spring_boot_microservice_1_inmueble;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.cloud.client.discovery.EnableDiscoveryClient; // Alternativa a @EnableEurekaClient si usas otras impl.

/**
 * Clase principal de la aplicación Spring Boot para el microservicio Inmueble.
 * Habilita la autoconfiguración de Spring Boot y el descubrimiento de servicios (Eureka).
 */
@SpringBootApplication
// @EnableDiscoveryClient // Opcional: Si usas Eureka, @SpringBootApplication puede ser suficiente en versiones recientes.
// Dejarlo no hace daño y es explícito. O puedes quitarlo si funciona sin él.
public class SpringBootMicroservice1InmuebleApplication {

	/**
	 * Punto de entrada principal para la aplicación Spring Boot.
	 * @param args Argumentos de línea de comandos (si los hubiera).
	 */
	public static void main(String[] args) {
		SpringApplication.run(SpringBootMicroservice1InmuebleApplication.class, args);
	}

}