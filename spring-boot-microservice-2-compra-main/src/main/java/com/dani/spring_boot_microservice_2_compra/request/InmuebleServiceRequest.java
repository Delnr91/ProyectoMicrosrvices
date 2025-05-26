package com.dani.spring_boot_microservice_2_compra.request;

import com.dani.spring_boot_microservice_2_compra.request.BasicAuthFeignConfiguration; // Importar la config
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity; // Para manejar la respuesta
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

// El nombre "inmueble-service" debe coincidir con el spring.application.name de inmueble-service en Eureka
// El path "/api/inmueble" es el @RequestMapping del InmuebleController en inmueble-service
@FeignClient(name = "inmueble-service", path = "/api/inmueble", configuration = BasicAuthFeignConfiguration.class)
public interface InmuebleServiceRequest {

    // El path aquí se concatena con el path del @FeignClient
    // y debe coincidir con el @PutMapping("/{inmuebleId}/estado") en InmuebleController de inmueble-service
    @PutMapping("/{inmuebleId}/estado")
    ResponseEntity<Void> updateInmuebleEstado(
            @PathVariable("inmuebleId") Long inmuebleId,
            @RequestParam("nuevoEstado") String nuevoEstado // Pasamos el estado como String
    );
}