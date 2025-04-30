package com.dani.spring_boot_microservice_3_api_gateway.request;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//mapa para el controler principal del microservice inmueble
@FeignClient(
        value = "inmueble-service",
        path = "/api/inmueble",
        //url = "${inmueble.service.url}",
        configuration = FeignConfiguration.class
)
public interface InmuebleServiceRequest {

    //mapeo de metodo para inmuble
    @PostMapping
    Object saveInmueble(@RequestBody Object requestBody);

    @DeleteMapping("{inmuebleId}")
    void deleteInmueble(@PathVariable("inmuebleId") Long inmuebleId);

    @GetMapping()
    List<Object> getAllInmuebles();


}
