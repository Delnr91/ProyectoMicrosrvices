package com.dani.spring_boot_microservice_2_compra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Clase principal y punto de entrada para el microservicio de Compras.
 * <p>
 * Esta clase utiliza la anotación {@link SpringBootApplication} para habilitar
 * la auto-configuración de Spring Boot, el escaneo de componentes y la configuración.
 * <p>
 * Funcionalidades clave habilitadas aquí:
 * <ul>
 * <li><b>{@link EnableFeignClients}:</b> Habilita el escaneo de interfaces anotadas
 * con {@code @FeignClient} (como {@link com.dani.spring_boot_microservice_2_compra.request.InmuebleServiceRequest}),
 * permitiendo la comunicación declarativa con otros microservicios como el {@code inmueble-service}.</li>
 * </ul>
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.1
 * @since 2025-06-09 (Actualizado con JavaDoc)
 */
@SpringBootApplication
@EnableFeignClients
public class SpringBootMicroservice2CompraApplication {

	/**
	 * Punto de entrada principal de la aplicación.
	 * Este método utiliza {@link SpringApplication#run(Class, String...)} para lanzar
	 * la aplicación Spring Boot.
	 *
	 * @param args Argumentos de línea de comandos pasados al iniciar la aplicación (si los hubiera).
	 */
	public static void main(String[] args) {
		SpringApplication.run(SpringBootMicroservice2CompraApplication.class, args);
	}

}