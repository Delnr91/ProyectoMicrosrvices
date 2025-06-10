package com.dani.spring_boot_microservice_3_api_gateway;

// Ya no se necesitan imports de io.swagger.v3.oas.annotations...

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Clase principal y punto de entrada para el microservicio API Gateway.
 * <p>
 * Esta clase utiliza la anotación {@link SpringBootApplication}, que engloba
 * {@code @Configuration}, {@code @EnableAutoConfiguration}, y {@code @ComponentScan},
 * para iniciar el contexto de la aplicación Spring y arrancar el servidor web embebido.
 * <p>
 * Funcionalidades clave habilitadas aquí:
 * <ul>
 * <li><b>{@link EnableFeignClients}:</b> Habilita el escaneo de interfaces anotadas
 * con {@code @FeignClient} (como {@link com.dani.spring_boot_microservice_3_api_gateway.request.InmuebleServiceRequest}),
 * permitiendo la comunicación declarativa con otros microservicios.</li>
 * <li><b>Definición de Beans Globales:</b> Configura beans que estarán disponibles
 * en todo el contexto de la aplicación, como el bean {@link PasswordEncoder}.</li>
 * </ul>
 * Sirve como la raíz de la configuración para toda la aplicación del API Gateway.
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.2
 * @since 2025-05-13 (Actualizado para eliminar OpenAPI y añadir JavaDoc)
 */
@SpringBootApplication
@EnableFeignClients // Habilita el escaneo de interfaces @FeignClient
public class SpringBootMicroservice3ApiGatewayApplication {

	/**
	 * Define el bean global para la codificación de contraseñas.
	 * <p>
	 * Al declarar {@link PasswordEncoder} como un bean, puede ser inyectado y utilizado
	 * en cualquier parte de la aplicación (por ejemplo, en {@link com.dani.spring_boot_microservice_3_api_gateway.service.UserServiceImpl}
	 * para codificar contraseñas de nuevos usuarios y en {@link com.dani.spring_boot_microservice_3_api_gateway.security.SecurityConfig}
	 * para la autenticación).
	 * <p>
	 * Se utiliza la implementación {@link BCryptPasswordEncoder}, que es un estándar fuerte
	 * y recomendado para el hashing de contraseñas.
	 *
	 * @return una instancia de {@link BCryptPasswordEncoder}.
	 */
	@Bean
	public PasswordEncoder passwordEncoder() { //
		return new BCryptPasswordEncoder();
	}

	/**
	 * Punto de entrada principal de la aplicación.
	 * Este método utiliza {@link SpringApplication#run(Class, String...)} para lanzar
	 * la aplicación Spring Boot.
	 *
	 * @param args Argumentos de línea de comandos pasados al iniciar la aplicación (si los hubiera).
	 */
	public static void main(String[] args) { //
		SpringApplication.run(SpringBootMicroservice3ApiGatewayApplication.class, args);
	}
}