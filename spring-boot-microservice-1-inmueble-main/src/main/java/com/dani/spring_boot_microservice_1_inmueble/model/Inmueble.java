package com.dani.spring_boot_microservice_1_inmueble.model;



import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "inmueble")
public class Inmueble {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="nombre")
    private String name;

    @Column(name = "direccion")
    private String address;

    @Column(name= "foto")
    private String picture;

    @Column(name="precio")
    private Double price;

    @Column(name= "fecha")
    private LocalDateTime creationDate;

}
