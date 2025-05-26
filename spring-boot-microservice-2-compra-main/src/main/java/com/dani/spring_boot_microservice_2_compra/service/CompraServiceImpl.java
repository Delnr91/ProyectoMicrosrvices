package com.dani.spring_boot_microservice_2_compra.service;

import com.dani.spring_boot_microservice_2_compra.model.Compra;
import com.dani.spring_boot_microservice_2_compra.repository.CompraRepository;
import com.dani.spring_boot_microservice_2_compra.request.InmuebleServiceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación del servicio para la lógica de negocio de las compras.
 */
@Service // Marca como bean de servicio
@RequiredArgsConstructor // Inyección por constructor
@Slf4j
public class CompraServiceImpl implements CompraService {

    // Inyección por constructor (final)
    private final CompraRepository compraRepository;
    private final InmuebleServiceRequest inmuebleServiceRequest;

    /**
     * {@inheritDoc}
     * Establece la fecha de compra antes de guardar.
     */
    @Override
    @Transactional // Operación de escritura, requiere transacción
    public Compra saveCompra(Compra compra) {
        compra.setPurchaseDate(LocalDateTime.now());
        Compra savedCompra = compraRepository.save(compra);
        log.info("Compra registrada en BD local: Compra ID {}, Inmueble ID {}, User ID {}",
                savedCompra.getId(), savedCompra.getInmuebleId(), savedCompra.getUserId());

        // Después de guardar la compra, intentar actualizar el estado del inmueble a VENDIDO
        if (savedCompra.getInmuebleId() != null) {
            try {
                log.info("Intentando actualizar estado del inmueble ID: {} a VENDIDO vía Feign.", savedCompra.getInmuebleId());
                // El estado "VENDIDO" debe coincidir con el valor del enum EstadoInmueble en inmueble-service
                ResponseEntity<Void> response = inmuebleServiceRequest.updateInmuebleEstado(savedCompra.getInmuebleId(), "VENDIDO");

                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("Solicitud de actualización de estado para inmueble ID: {} enviada y procesada exitosamente por inmueble-service (status: {}).",
                            savedCompra.getInmuebleId(), response.getStatusCode());
                } else {
                    log.warn("Solicitud de actualización de estado para inmueble ID: {} enviada, pero inmueble-service respondió con status: {}.",
                            savedCompra.getInmuebleId(), response.getStatusCode());
                    // Considerar qué hacer si el servicio destino no lo procesa como éxito (ej. 404 si el inmueble ya no existe allí)
                }
            } catch (Exception e) {
                // ¿Qué hacer si la llamada a inmueble-service falla (ej. por red, o si el servicio está caído)?
                // Por ahora, solo logueamos. En un sistema real, se necesitaría una estrategia de compensación o reintento.
                log.error("Error CRÍTICO al intentar actualizar el estado del inmueble ID: {} a VENDIDO después de la compra ID: {}. Causa: {}",
                        savedCompra.getInmuebleId(), savedCompra.getId(), e.getMessage(), e);
                // Importante: La compra YA está guardada. Esto es una llamada de consistencia eventual.
                // No se debería relanzar la excepción para revertir la compra, a menos que se implemente una Saga o transacción distribuida.
            }
        } else {
            log.warn("La compra ID: {} no tiene un inmuebleId asociado, no se puede actualizar el estado del inmueble.", savedCompra.getId());
        }
        return savedCompra;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true) // Operación de solo lectura, optimización
    public List<Compra> findAllComprasOfUser(Long userId) {
        return compraRepository.findAllByUserId(userId);
    }
}