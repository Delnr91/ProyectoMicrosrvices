package com.dani.spring_boot_microservice_2_compra.request;

import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BasicAuthFeignConfiguration {

    // Estas propiedades deben existir en application.properties de compra-service
    @Value("${service.security.internal-call.username}")
    private String username;

    @Value("${service.security.internal-call.password}")
    private String password;

    @Bean
    public BasicAuthRequestInterceptor basicAuthRequestInterceptor() {
        return new BasicAuthRequestInterceptor(username, password);
    }
}
