package com.dani.spring_boot_microservice_2_compra.model;


import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="compra")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id")
    private Long userId;

    @Column(name="inmueble_id")
    private Long inmuebleId;

    @Column(name="titulo")
    private String title;

    @Column(name="precio")
    private Double price;

    @Column(name="fecha_compra")
    private LocalDateTime purchaseDate;

}
