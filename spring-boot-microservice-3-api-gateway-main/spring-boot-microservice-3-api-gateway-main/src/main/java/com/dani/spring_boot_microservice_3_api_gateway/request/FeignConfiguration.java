package com.dani.spring_boot_microservice_3_api_gateway.request;

import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class FeignConfiguration {

    //crear conexion segura entre los microservicios, utilizado un atuh basic

    @Bean
    public BasicAuthRequestInterceptor basicAuthRequestInterceptor(
            @Value("${service.security.secure-key-username}")String username,
            @Value("${service.security.secure-key-password}")String password){

        return new BasicAuthRequestInterceptor(username, password);

    }
}

