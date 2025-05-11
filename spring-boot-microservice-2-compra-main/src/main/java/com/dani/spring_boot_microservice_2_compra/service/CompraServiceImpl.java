package com.dani.spring_boot_microservice_2_compra.service;

import com.dani.spring_boot_microservice_2_compra.model.Compra;
import com.dani.spring_boot_microservice_2_compra.repository.CompraRepository;
import lombok.RequiredArgsConstructor; // Importar
// import org.springframework.beans.factory.annotation.Autowired; // Ya no se usa
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importar

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación del servicio para la lógica de negocio de las compras.
 */
@Service // Marca como bean de servicio
@RequiredArgsConstructor // Inyección por constructor
public class CompraServiceImpl implements CompraService {

    // Inyección por constructor (final)
    private final CompraRepository compraRepository;

    /**
     * {@inheritDoc}
     * Establece la fecha de compra antes de guardar.
     */
    @Override
    @Transactional // Operación de escritura, requiere transacción
    public Compra saveCompra(Compra compra) {
        // Asigna la fecha actual a la compra antes de persistirla
        compra.setPurchaseDate(LocalDateTime.now());
        return compraRepository.save(compra);
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