package com.dani.spring_boot_microservice_3_api_gateway.service;

import com.dani.spring_boot_microservice_3_api_gateway.model.Role;
import com.dani.spring_boot_microservice_3_api_gateway.model.User;

import java.util.Optional;


public interface UserService {
    User saveUser(User user);


    /*mETODO PARA USUARIO POR USERNAME
     */
    Optional<User> findByUsername(String username);

    void changeRole(Role newRole, String username);

    //metodo de busqueda por username, que retorne el usuario incluyendo el token
    User findByUserameReturnToken(String username);
}
