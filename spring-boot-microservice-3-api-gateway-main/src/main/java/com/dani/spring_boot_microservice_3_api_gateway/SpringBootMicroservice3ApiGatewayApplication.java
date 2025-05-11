package com.dani.spring_boot_microservice_3_api_gateway;

// Imports OpenAPI (si añades @OpenAPIDefinition aquí)
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients; // Habilita clientes Feign
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Clase principal para el microservicio API Gateway.
 * Habilita la autoconfiguración de Spring Boot, clientes Feign,
 * y define beans globales como PasswordEncoder.
 * También define la configuración global de OpenAPI/Swagger UI.
 */
@SpringBootApplication
@EnableFeignClients // Habilita el escaneo de interfaces @FeignClient
// --- Definición Global OpenAPI ---
@OpenAPIDefinition(
		info = @Info(
				title = "API Gateway - Gestión Inmobiliaria",
				version = "v1.0",
				description = "Punto de entrada principal que gestiona autenticación y enrutamiento a microservicios de Inmuebles y Compras.",
				contact = @Contact(name = "Tu Nombre/Equipo", email = "tu@email.com", url = "http://tuweb.com"),
				license = @License(name = "Tu Licencia (ej. Apache 2.0)", url = "http://licencia.url")
		)
		// servers = { @Server(url = "http://localhost:5555", description = "Servidor Local Desarrollo") } // Opcional: Definir servidores
)
// Define el esquema de seguridad JWT Bearer para usar con @SecurityRequirement en los controllers
@SecurityScheme(
		name = "bearerAuth", // Nombre referenciado en @SecurityRequirement
		description = "JWT Authentication Bearer Token",
		scheme = "bearer",
		type = SecuritySchemeType.HTTP, // Tipo HTTP
		bearerFormat = "JWT", // Formato del token
		in = SecuritySchemeIn.HEADER // El token se espera en el header Authorization
)
// --- Fin Definición Global OpenAPI ---
public class SpringBootMicroservice3ApiGatewayApplication {

	/**
	 * Define el bean global para la codificación de contraseñas.
	 * @return una instancia de BCryptPasswordEncoder.
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * Punto de entrada principal de la aplicación.
	 * @param args argumentos de línea de comandos.
	 */
	public static void main(String[] args) {
		SpringApplication.run(SpringBootMicroservice3ApiGatewayApplication.class, args);
	}
}