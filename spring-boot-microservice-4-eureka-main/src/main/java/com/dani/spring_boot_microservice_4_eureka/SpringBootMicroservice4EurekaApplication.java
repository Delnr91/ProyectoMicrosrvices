package com.dani.spring_boot_microservice_4_eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer; // Importar

/**
 * Clase principal para el Servidor de Descubrimiento Eureka.
 * Habilita la funcionalidad de Eureka Server para que otros microservicios
 * puedan registrarse y descubrirse entre sí.
 */
@SpringBootApplication
@EnableEurekaServer // Habilita esta aplicación como un Servidor Eureka
public class SpringBootMicroservice4EurekaApplication {

	/**
	 * Punto de entrada para lanzar el Servidor Eureka.
	 * @param args Argumentos de línea de comandos.
	 */
	public static void main(String[] args) {
		SpringApplication.run(SpringBootMicroservice4EurekaApplication.class, args);
	}

}