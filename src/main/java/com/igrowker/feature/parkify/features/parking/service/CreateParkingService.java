package com.igrowker.feature.parkify.features.parking.service;

import com.igrowker.feature.parkify.features.parking.dto.request.CreateParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.response.CreateParkingResponse;
import com.igrowker.feature.parkify.features.parking.entities.Parking;
import com.igrowker.feature.parkify.features.parking.repository.ParkingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class CreateParkingService {
    private final ParkingRepository repository;

    public CreateParkingResponse createParking(CreateParkingRequest request) {
        Parking parking = new Parking();
        parking.setNombre(request.getNombre());
        parking.setDireccion(request.getDireccion());
        parking.setLatitud(request.getLatitud());
        parking.setLongitud(request.getLongitud());
        parking.setTarifaHora(request.getTarifaHora());
        parking.setDisponible(request.getDisponible());
        parking.setWhatsapp(request.getWhatsapp());
        parking.setOwnerId(request.getOwnerId());

        Parking saved = repository.save(parking);

        return CreateParkingResponse.builder()
                .id(saved.getId())
                .nombre(saved.getNombre())
                .direccion(saved.getDireccion())
                .latitud(saved.getLatitud())
                .longitud(saved.getLongitud())
                .tarifaHora(saved.getTarifaHora())
                .disponible(saved.getDisponible())
                .whatsapp(saved.getWhatsapp())
                .ownerId(saved.getOwnerId())
                .build();
    }
}
