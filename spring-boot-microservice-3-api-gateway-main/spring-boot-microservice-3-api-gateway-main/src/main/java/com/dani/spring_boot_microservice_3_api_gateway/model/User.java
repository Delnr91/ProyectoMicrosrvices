package com.dani.spring_boot_microservice_3_api_gateway.model;


import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;


import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name= "users")
@Data
public class User {


    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column (name="username")
    private String username;

    @Column(name="password")
    private String password;

    @Column(name="nombre")
    private String nombre;

    @Column(name="fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Enumerated(EnumType.STRING)
    @Column(name="role")
    private Role role;

    @Transient
    private String token;



}
