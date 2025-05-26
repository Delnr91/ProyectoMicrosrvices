package com.dani.spring_boot_microservice_2_compra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
// import org.springframework.cloud.client.discovery.EnableDiscoveryClient; // Opcional con versiones recientes de Spring Cloud

/**
 * Clase principal de la aplicación Spring Boot para el microservicio Compra.
 * Punto de entrada que inicia la aplicación.
 */
@SpringBootApplication
@EnableFeignClients
// @EnableDiscoveryClient // Opcional si Eureka está en el classpath con SB recientes.
public class SpringBootMicroservice2CompraApplication {

	/**
	 * Método main, punto de entrada de la aplicación Java.
	 * Lanza la aplicación Spring Boot.
	 * @param args Argumentos de línea de comandos.
	 */
	public static void main(String[] args) {
		SpringApplication.run(SpringBootMicroservice2CompraApplication.class, args);
	}

}