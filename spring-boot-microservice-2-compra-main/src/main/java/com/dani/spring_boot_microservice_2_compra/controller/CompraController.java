package com.dani.spring_boot_microservice_2_compra.controller;

import com.dani.spring_boot_microservice_2_compra.model.Compra;
import com.dani.spring_boot_microservice_2_compra.service.CompraService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar las operaciones CRUD de la entidad {@link Compra}.
 * <p>
 * Expone endpoints HTTP que son consumidos principalmente por el API Gateway.
 * Este controlador delega toda la lógica de negocio al {@link CompraService}.
 * Los endpoints están protegidos por la configuración de seguridad del servicio,
 * que espera una autenticación básica para la comunicación entre servicios.
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.1
 * @since 2025-06-09 (Actualizado con JavaDoc completo)
 */
@RestController
@RequestMapping("api/compra")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;

    /**
     * Endpoint para guardar una nueva transacción de compra.
     *
     * @param compra El objeto {@link Compra} con los datos de la nueva compra,
     * recibido en el cuerpo de la petición.
     * @return Un {@link ResponseEntity} con la compra guardada y el estado HTTP 201 (Created).
     */
    @PostMapping
    public ResponseEntity<Compra> saveCompra(@RequestBody Compra compra) {
        return new ResponseEntity<>(compraService.saveCompra(compra), HttpStatus.CREATED);
    }

    /**
     * Endpoint para obtener todas las compras realizadas por un usuario específico.
     *
     * @param userId El ID del usuario cuyas compras se desean obtener, extraído de la ruta.
     * @return Un {@link ResponseEntity} con una lista de las compras del usuario y estado HTTP 200 (OK).
     */
    @GetMapping("{userId}")
    public ResponseEntity<List<Compra>> getAllComprasOfUser(@PathVariable Long userId) {
        return ResponseEntity.ok(compraService.findAllByUserId(userId));
    }
}