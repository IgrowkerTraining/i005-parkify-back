package com.igrowker.feature.parkify.features.parking.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
//@Table(name="parking")
public class Parking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String direccion;
    private Double latitud;
    private Double longitud;
    private Double tarifaHora;
    private Boolean disponible;
    private String whatsapp;

    @Column(name = "owner_id")
    private Long ownerId;
}
