package com.dani.spring_boot_microservice_2_compra.service;

import com.dani.spring_boot_microservice_2_compra.model.Compra;
import com.dani.spring_boot_microservice_2_compra.repository.CompraRepository;
import com.dani.spring_boot_microservice_2_compra.request.InmuebleServiceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación concreta de la interfaz {@link CompraService}.
 * <p>
 * Contiene la lógica de negocio para gestionar las compras. Su responsabilidad principal
 * es guardar el registro de la compra y coordinar la actualización del estado del inmueble
 * correspondiente a través de una llamada Feign al {@code inmueble-service}.
 *
 * @author Daniel Núñez Rojas (danidev fullstack software)
 * @version 1.1
 * @since 2025-06-09 (Actualizado con JavaDoc completo)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CompraServiceImpl implements CompraService {

    private final CompraRepository compraRepository;
    private final InmuebleServiceRequest inmuebleServiceRequest;

    /**
     * El estado al que se actualizará un inmueble después de ser comprado.
     * El valor se inyecta desde la propiedad {@code estado.inmueble.vendido}.
     */
    @Value("${estado.inmueble.vendido}")
    private String ESTADO_VENDIDO;

    /**
     * {@inheritDoc}
     * <p>
     * Esta implementación primero asigna la fecha y hora actual a la compra,
     * luego la guarda en la base de datos a través del {@link CompraRepository}.
     * <p>
     * Después de guardar la compra exitosamente, realiza una llamada síncrona a través
     * del cliente Feign {@link InmuebleServiceRequest} para actualizar el estado del
     * inmueble correspondiente, marcándolo como vendido.
     */
    @Override
    public Compra saveCompra(Compra compra) {
        compra.setPurchaseDate(LocalDateTime.now());
        compraRepository.save(compra);
        log.info("Compra guardada en la base de datos con ID: {}", compra.getId());

        log.info("Procediendo a actualizar el estado del inmueble ID: {} a {}", compra.getInmuebleId(), ESTADO_VENDIDO);
        try {
            inmuebleServiceRequest.updateInmuebleEstado(compra.getInmuebleId(), ESTADO_VENDIDO);
            log.info("Estado del inmueble ID: {} actualizado exitosamente a {}", compra.getInmuebleId(), ESTADO_VENDIDO);
        } catch (Exception e) {
            // En un sistema de producción, aquí se debería manejar la excepción de forma más robusta.
            // Por ejemplo, usando un patrón de reintentos, publicando un evento en una cola de mensajes
            // para garantizar la consistencia, o marcando la compra para una revisión manual.
            log.error("Error al actualizar el estado del inmueble ID: {} a través de Feign. Causa: {}",
                    compra.getInmuebleId(), e.getMessage());
            // Por simplicidad, el error solo se registra, pero la compra ya fue guardada.
        }

        return compra;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Esta implementación simplemente delega la llamada al método {@code findAllByUserId}
     * del {@link CompraRepository}.
     */
    @Override
    public List<Compra> findAllByUserId(Long userId) {
        return compraRepository.findAllByUserId(userId);
    }
}