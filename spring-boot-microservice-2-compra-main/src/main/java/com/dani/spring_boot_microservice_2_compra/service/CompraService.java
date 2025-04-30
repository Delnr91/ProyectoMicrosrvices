package com.dani.spring_boot_microservice_2_compra.service;

import com.dani.spring_boot_microservice_2_compra.model.Compra;

import java.util.List;

public interface CompraService {


    Compra saveCompra(Compra compra);

    List<Compra> findAllComprasOfUser(Long userId);


}
