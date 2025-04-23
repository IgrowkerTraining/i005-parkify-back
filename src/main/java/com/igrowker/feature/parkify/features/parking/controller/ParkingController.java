package com.igrowker.feature.parkify.features.parking.controller;

import com.igrowker.feature.parkify.exception.GlobalExceptionHandler;
import com.igrowker.feature.parkify.features.parking.dto.request.CreateMyParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.request.ParkingRequest;
import com.igrowker.feature.parkify.features.parking.dto.request.UpdateAvailabilityRequest;
import com.igrowker.feature.parkify.features.parking.dto.response.OwnerParkingDetailsResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.PaginatedParkingResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingAvailabilityResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingDetailsResponse;
import com.igrowker.feature.parkify.features.parking.dto.response.ParkingResponse;
import com.igrowker.feature.parkify.features.parking.service.ParkingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/parkings")
@Tag(name = "Parking", description = "Operations related to parking services")
public class ParkingController {
    private final ParkingService parkingService;

    //#18
    @Operation(
            summary = "Get owner and parking information",
            description = "Returns details of the authenticated owner and their associated parking. The owner's email is extracted from the JWT token."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Owner and parking details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = OwnerParkingDetailsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Owner or parking not found", content = @Content)
    })
    @GetMapping("/owner/parking")
    public ResponseEntity<OwnerParkingDetailsResponse> getOwnerWithParking(Authentication authentication) {
        String ownerEmail = authentication.getName();
        OwnerParkingDetailsResponse response = parkingService.getOwnerWithParking(ownerEmail);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //21
    @Operation(
            summary = "Get nearby parkings",
            description = "Returns a paginated list of parkings near a specific geographic location."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of parkings found"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/nearby")
    public ResponseEntity<PaginatedParkingResponse> getNearbyParkings(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(required = false) Integer radius,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minAvailability,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit
    ) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        PaginatedParkingResponse response = parkingService.findNearbyParkings(
                lat, lon, radius, maxPrice, minAvailability, limit, offset, pageable
        );
        return ResponseEntity.ok(response);
    }


    // #20, #22
    @Operation(
            summary = "Find Nearby Parkings (#20, #22)",
            description = "Searches for parking facilities near a given location, allowing filtering by radius, price, availability. Returns a paginated list sorted by distance."
    )
    @Parameter(name = "latitude", in = ParameterIn.QUERY, required = true, description = "Latitude of the search center", example = "40.7128")
    @Parameter(name = "longitude", in = ParameterIn.QUERY, required = true, description = "Longitude of the search center", example = "-74.0060")
    @Parameter(name = "radius", in = ParameterIn.QUERY, description = "Maximum distance in kilometers", example = "5")
    @Parameter(name = "maxPrice", in = ParameterIn.QUERY, description = "Maximum hourly rate", example = "10.50")
    @Parameter(name = "minAvailability", in = ParameterIn.QUERY, description = "Minimum number of available spots", example = "1")
    @Parameter(name = "limit", in = ParameterIn.QUERY, description = "Number of results per page", example = "10")
    @Parameter(name = "offset", in = ParameterIn.QUERY, description = "Offset for pagination", example = "0")
    @ApiResponse(responseCode = "200", description = "List of nearby parkings retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PaginatedParkingResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid or missing required parameters (latitude, longitude)",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    @GetMapping
    public ResponseEntity<PaginatedParkingResponse> findParkings(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false, defaultValue = "1000") Integer radius,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minAvailability,
            @RequestParam(required = false, defaultValue = "20") int limit,
            @RequestParam(required = false, defaultValue = "0") int offset,
            Pageable pageable
    ) {
        final PaginatedParkingResponse response = parkingService.findNearbyParkings(
                latitude, longitude, radius, maxPrice, minAvailability,
                limit, offset, pageable);
        return ResponseEntity.ok(response);
    }

    // #25
    @Operation(
            summary = "Get Parking Availability (#25)",
            description = "Retrieves the current number of available spots for a specific parking facility. Publicly accessible."
    )
    @ApiResponse(responseCode = "200", description = "Availability retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ParkingAvailabilityResponse.class)))
    @ApiResponse(responseCode = "404", description = "Parking facility not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    @GetMapping("/{parkingId}/availability")
    public ResponseEntity<ParkingAvailabilityResponse> getParkingAvailability
    (@PathVariable Long parkingId
    ) {
        ParkingAvailabilityResponse response = parkingService.getParkingAvailability(parkingId);
        return ResponseEntity.ok(response);
    }

    @Deprecated(since = "2025.04.10")
    @PostMapping("/create")
    public ResponseEntity<ParkingResponse> createParking(@RequestBody ParkingRequest request) {
        ParkingResponse response = parkingService.createParking(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update Parking Availability (Legacy Endpoint)",
            description = "Updates the number of available spots for a specific parking facility using PUT. " +
                    "Requires `parkingId` and `availableSpots` within the request body (`ParkingRequest`). " +
                    "Note: This endpoint uses PUT and a broad request object. Prefer newer PATCH endpoints if available."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Parking details including the ID of the parking to update (`parkingId`) " +
            "and the new number of available spots (`availableSpots`). Other fields may be ignored.",
            required = true,
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ParkingRequest.class)))
    @ApiResponse(responseCode = "200", description = "Availability updated successfully. Returns the full updated parking details.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ParkingResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request data (e.g., missing parkingId, negative availableSpots, validation errors in ParkingRequest)",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Parking facility not found with the ID provided in the request body (`parkingId`).",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
//    @Deprecated(since = "2025.04.10")
    @PutMapping("/update-availability")
    public ResponseEntity<ParkingResponse> updateAvailability(
            @RequestBody ParkingRequest request) {
        return ResponseEntity.ok(parkingService.updateAvailability(request));
    }

    // #23
    @Operation(
            summary = "Get Parking Details (#23)",
            description = "Retrieves detailed information about a specific parking facility, including owner ID. Publicly accessible."
    )
    @ApiResponse(responseCode = "200", description = "Parking details retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ParkingDetailsResponse.class)))
    @ApiResponse(responseCode = "404", description = "Parking facility or its associated owner not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    @GetMapping("/{parkingId}")
    public ResponseEntity<ParkingDetailsResponse> getParkingDetails(@PathVariable Long parkingId) {
        final ParkingDetailsResponse response = parkingService.getParkingDetails(parkingId);
        return ResponseEntity.ok(response);
    }

    // #15 (step 2)
    @Operation(summary = "Create My Parking (for Owner)", description = "Allows an authenticated OWNER to create their parking facility. Use this instead of the deprecated /create. Relates to #15.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Details of the parking to create", required = true,
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CreateMyParkingRequest.class)))
    @ApiResponse(responseCode = "201", description = "Parking created successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ParkingResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request data (validation error)",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden (Not an OWNER)")
    @ApiResponse(responseCode = "404", description = "Owner not found")
    @PostMapping("/my")
    public ResponseEntity<ParkingResponse> createMyParking(
            @Valid @RequestBody CreateMyParkingRequest request,
            Authentication authentication
    ) {
        final String ownerEmail = authentication.getName();
        final ParkingResponse createdParking = parkingService.createMyParking(request, ownerEmail);
        final URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/api/v1/parkings/{id}")
                .buildAndExpand(createdParking.getId()).toUri();
        return ResponseEntity.created(location).body(createdParking);
    }

    // #26
    @Operation(
            summary = "Get My Parking Details (#26)",
            description = "Retrieves the details of the parking facility associated with the currently authenticated OWNER."
    )
    @ApiResponse(responseCode = "200", description = "Parking details retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ParkingDetailsResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized (Missing or invalid JWT token)",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden (User is not an OWNER)",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Owner not found or has no associated parking",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GlobalExceptionHandler.ErrorResponse.class)))
    @GetMapping("/my")
    public ResponseEntity<ParkingDetailsResponse> getMyParking(Authentication authentication) {
        final String ownerEmail = authentication.getName();
        final ParkingDetailsResponse response = parkingService.getMyParkingDetails(ownerEmail);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete My Parking",
            description = "Allows the authenticated owner to delete their own parking facility."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Parking deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden (Not an OWNER)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Owner or their parking not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = GlobalExceptionHandler.ErrorResponse.class)
                    )
            )
    })
    @DeleteMapping("/my")
    public ResponseEntity<Void> deleteMyParking(Authentication authentication) {
        String ownerEmail = authentication.getName();
        parkingService.deleteMyParking(ownerEmail);
        return ResponseEntity.noContent().build();
    }

    // #27
    @Operation(
            summary = "Update parking availability",
            description = "Allows the authenticated owner to update the number of available spots in their parking."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Availability updated successfully",
                    content = @Content(schema = @Schema(implementation = ParkingAvailabilityResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized or invalid JWT token", content = @Content),
            @ApiResponse(responseCode = "404", description = "Owner or parking not found", content = @Content)
    })
    @PatchMapping("/my/availability")
    public ResponseEntity<ParkingAvailabilityResponse> updateMyParkingAvailability(
            @Valid @RequestBody UpdateAvailabilityRequest request,
            Authentication authentication) {
        final String ownerEmail = authentication.getName();
        final ParkingAvailabilityResponse updatedAvailability = parkingService
                .updateMyParkingAvailability(ownerEmail, request.availableSpots());
        return ResponseEntity.ok(updatedAvailability);
    }

}
