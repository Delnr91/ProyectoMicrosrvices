package com.dani.spring_boot_microservice_3_api_gateway.repository;

import com.dani.spring_boot_microservice_3_api_gateway.model.Role;
import com.dani.spring_boot_microservice_3_api_gateway.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    //el metodo de busqueda empieza findBy  concatenado con el nombredecampo

    Optional<User> findByUsername(String username);

    //metodo para actualizacion de datos dentro de mis parametros

    @Modifying
    @Query(value = "update User set role=:role  where username = :username")
    void updateUserRole(@Param("username")String username, @Param("role")Role role);

}
