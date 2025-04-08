package com.igrowker.feature.parkify.features.parking.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateParkingRequest {
    private String nombre;
    private String direccion;
    private Double latitud;
    private Double longitud;
    private Double tarifaHora;
    private Boolean disponible;
    private String whatsapp;
    private Long ownerId;
}
