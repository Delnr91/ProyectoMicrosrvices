package com.dani.spring_boot_microservice_1_inmueble.service;

import com.dani.spring_boot_microservice_1_inmueble.model.Inmueble;
import com.dani.spring_boot_microservice_1_inmueble.repository.InmuebleRepository;
import lombok.RequiredArgsConstructor; // Importar para inyección por constructor
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importar para gestión transaccional

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación concreta de la interfaz InmuebleService.
 * Contiene la lógica de negocio para la gestión de inmuebles.
 * Interactúa con InmuebleRepository para la persistencia de datos.
 */
@Service // Marca esta clase como un bean de servicio de Spring.
@RequiredArgsConstructor // Lombok: Genera constructor para inyección de dependencias final.
public class InmuebleServiceImpl implements InmuebleService {

    // Inyección por constructor de la dependencia del repositorio. Es final.
    private final InmuebleRepository inmuebleRepository;

    /**
     * {@inheritDoc}
     * Antes de guardar, establece la fecha de creación del inmueble a la fecha y hora actuales.
     */
    @Override
    @Transactional // Asegura que la operación se ejecute dentro de una transacción.
    public Inmueble saveInmueble(Inmueble inmueble) {
        // Establece la fecha y hora de creación antes de guardar.
        inmueble.setCreationDate(LocalDateTime.now());
        return inmuebleRepository.save(inmueble);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional // Asegura que la operación se ejecute dentro de una transacción.
    public void deleteInmueble(Long inmuebleId) {
        inmuebleRepository.deleteById(inmuebleId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true) // Indica que esta transacción es de solo lectura (optimización).
    public List<Inmueble> findAllInmuebles() {
        return inmuebleRepository.findAll();
    }
}