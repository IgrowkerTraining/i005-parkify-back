package com.igrowker.feature.parkify.features.parking.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateParkingResponse {
    private Long id;
    private String nombre;
    private String direccion;
    private Double latitud;
    private Double longitud;
    private Double tarifaHora;
    private Boolean disponible;
    private String whatsapp;
    private Long ownerId;
}
