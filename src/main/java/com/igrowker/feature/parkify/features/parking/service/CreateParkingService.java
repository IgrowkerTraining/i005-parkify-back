package com.igrowker.feature.parkify.features.parking.service;

import com.igrowker.feature.parkify.features.parking.dto.CreateParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.CreateParkingResponse;
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

        Parking savedParking = repository.save(parking);

        return CreateParkingResponse.builder()
                .id(savedParking.getId())
                .nombre(savedParking.getNombre())
                .direccion(savedParking.getDireccion())
                .latitud(savedParking.getLatitud())
                .longitud(savedParking.getLongitud())
                .tarifaHora(savedParking.getTarifaHora())
                .disponible(savedParking.getDisponible())
                .whatsapp(savedParking.getWhatsapp())
                .ownerId(savedParking.getOwnerId())
                .build();
    }
}
