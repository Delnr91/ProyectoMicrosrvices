package com.dani.spring_boot_microservice_1_inmueble;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal y punto de entrada para el microservicio de Inmuebles.
 * <p>
 * Esta clase utiliza la anotación {@link SpringBootApplication}, que engloba
 * las anotaciones estándar de Spring Boot para la auto-configuración, el escaneo
 * de componentes y la configuración.
 * <p>
 * El microservicio se registra en Eureka (según la configuración en {@code application.properties})
 * para que otros servicios, como el API Gateway, puedan descubrirlo y comunicarse con él.
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.0
 * @since 2025-05-13
 */
@SpringBootApplication
public class SpringBootMicroservice1InmuebleApplication {

	/**
	 * Punto de entrada principal de la aplicación.
	 * Este método utiliza {@link SpringApplication#run(Class, String...)} para lanzar
	 * la aplicación Spring Boot.
	 *
	 * @param args Argumentos de línea de comandos pasados al iniciar la aplicación.
	 */
	public static void main(String[] args) {
		SpringApplication.run(SpringBootMicroservice1InmuebleApplication.class, args);
	}

}