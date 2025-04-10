## Español (Spanish)

**Convenciones Generales:**

*   **Ruta Base:** `/api/v1`
*   **Formato de Datos:** JSON
*   **Nomenclatura de Campos DTO:** `camelCase` (Se prefieren Java Records para DTOs)
*   **Autenticación:** Token JWT Bearer en la cabecera `Authorization: Bearer <token>` para endpoints protegidos.
*   **Identificadores:**
    *   Recursos en BD (Parking, User, Feature, Booking, etc.): `Long`.
    *   Features en API (path/query params, DTO): `String` slug único (ej. `guarded`).
    *   Parkings/Users en path/DTO: `Long`.
*   **Fechas/Horas:** Formato ISO 8601 (`String`).
*   **Paginación:** Query params `page` (int, 0-based), `size` (int, default 20), `sort` (string, `propiedad,direccion`). Respuesta: `org.springframework.data.domain.Page<YourDto>`.
*   **Respuesta de Error Estándar:** Formato tipo RFC 7807 Problem Details (campos `type`, `title`, `status`, `detail`, `instance`, `timestamp`, `errors` [opcional]).

---

**1. Recurso: Autenticación (`/auth`)**

*   **`POST /auth/register`**
    *   **Descripción:** Registra un nuevo usuario (Conductor o Propietario).
    *   **Autenticación:** No requerida.
    *   **Cuerpo de Solicitud:** `RegisterRequest` (`name`, `email`, `password`, `role`['DRIVER' | 'OWNER'], `contactPhone`).
    *   **Respuesta (201 Creado):** `UserResponse` (`id`, `name`, `email`, `role`, `contactPhone`). Cabecera `Location: /api/v1/users/{userId}`.
    *   **Errores:** 400 (Falló la validación, Email existe), 500.
*   **`POST /auth/login`**
    *   **Descripción:** Inicia sesión del usuario, obtiene token JWT.
    *   **Autenticación:** No requerida.
    *   **Cuerpo de Solicitud:** `LoginRequest` (`email`, `password`).
    *   **Respuesta (200 OK):** `LoginResponse` (`accessToken`, `tokenType`, `expiresIn`, `user`{`id`, `name`, `email`, `role`}).
    *   **Errores:** 400 (Falló la validación), 401 (Credenciales inválidas), 500.
*   **`GET /auth/me`**
    *   **Descripción:** Obtiene información del usuario autenticado actualmente.
    *   **Autenticación:** Requerida (JWT).
    *   **Respuesta (200 OK):** `UserResponse`.
    *   **Errores:** 401 (No autorizado), 404 (Usuario no encontrado), 500.

---

**2. Recurso: Configuración (`/config`)**

*   **`GET /config/initial`**
    *   **Descripción:** Obtiene la configuración inicial de UI/aplicación.
    *   **Autenticación:** Requerida (JWT).
    *   **Respuesta (200 OK):** `InitialConfigResponse` (`themeColors`, `featureFlags`).
    *   **Errores:** 401, 500.

---

**3. Recurso: Contenido (`/content`)**

*   **`GET /content/home`**
    *   **Descripción:** Obtiene contenido estático para la página de inicio.
    *   **Autenticación:** No requerida.
    *   **Respuesta (200 OK):** `HomeContentResponse`.
    *   **Errores:** 500.
*   **`GET /content/footer`**
    *   **Descripción:** Obtiene datos para el pie de página.
    *   **Autenticación:** No requerida.
    *   **Respuesta (200 OK):** `FooterContentResponse`.
    *   **Errores:** 500.

---

**4. Recurso: Características (`/features`)**

*   **`GET /features`**
    *   **Descripción:** Obtiene la lista de todas las características (features) de parkings disponibles.
    *   **Autenticación:** No requerida / `authenticated()`.
    *   **Query Params:** `Pageable` (opcional).
    *   **Respuesta (200 OK):** `List<FeatureDto>` (o `Page<FeatureDto>`).
    *   **Errores:** 500.
*   **`GET /features/{featureSlug}`**
    *   **Descripción:** Obtiene información de una característica específica por su slug único.
    *   **Autenticación:** No requerida / `authenticated()`.
    *   **Path Params:** `featureSlug` (String).
    *   **Respuesta (200 OK):** `FeatureDto`.
    *   **Errores:** 404 (No Encontrado), 500.
*   *(Opcional: `POST`, `PUT`, `DELETE` para gestión de características - protegido con rol ADMIN)*

---

**5. Recurso: Parkings (`/parkings`)**

*   **`GET /parkings`**
    *   **Descripción:** Busca y filtra parkings.
    *   **Autenticación:** Requerida (JWT) / `permitAll()`.
    *   **Query Params:** `Pageable`, `latitude`?, `longitude`?, `radius`?, `maxPrice`?, `minAvailability`?, `features` (List<String> de slugs)?.
    *   **Respuesta (200 OK):** `Page<ParkingDto>`.
    *   **Errores:** 400 (Params inválidos), 401, 500.
*   **`POST /parkings/my`**
    *   **Descripción:** Crea un parking por el propietario autenticado actual.
    *   **Autenticación:** Requerida (Rol 'OWNER').
    *   **Cuerpo de Solicitud:** `CreateParkingRequestDto` (con `featureSlugs`).
    *   **Respuesta (201 Creado):** `ParkingDto`. Cabecera `Location`.
    *   **Errores:** 400 (Falló validación), 401, 403 (Prohibido), 404 (Feature slug no encontrado?), 500.
*   **`GET /parkings/{parkingId}`**
    *   **Descripción:** Obtiene información detallada de un parking.
    *   **Autenticación:** Requerida (JWT) / `permitAll()`.
    *   **Path Params:** `parkingId` (Long).
    *   **Respuesta (200 OK):** `ParkingDto`.
    *   **Errores:** 401, 404 (No Encontrado), 500.
*   **`GET /parkings/my`**
    *   **Descripción:** Obtiene el parking perteneciente al propietario autenticado actual.
    *   **Autenticación:** Requerida (Rol 'OWNER').
    *   **Respuesta (200 OK):** `ParkingDto`.
    *   **Errores:** 401, 403, 404 (No Encontrado), 500.
*   **`GET /parkings/{parkingId}/availability`**
    *   **Descripción:** Obtiene la disponibilidad actual de un parking.
    *   **Autenticación:** Requerida (JWT) / `permitAll()`.
    *   **Path Params:** `parkingId` (Long).
    *   **Respuesta (200 OK):** `ParkingAvailabilityResponse` (`parkingId`, `availableSpots`).
    *   **Errores:** 401, 404, 500.
*   **`PATCH /parkings/my/availability`**
    *   **Descripción:** Actualiza la disponibilidad del parking por el propietario actual.
    *   **Autenticación:** Requerida (Rol 'OWNER').
    *   **Cuerpo de Solicitud:** `UpdateAvailabilityRequest` (`{"availableSpots": number}`).
    *   **Respuesta (200 OK):** `ParkingAvailabilityResponse`.
    *   **Errores:** 400 (Falló validación), 401, 403, 404 (No Encontrado), 500.
*   **`PUT /parkings/{parkingId}/features/{featureSlug}`**
    *   **Descripción:** Añade (asocia) una característica a un parking. Idempotente.
    *   **Autenticación:** Requerida (Rol 'OWNER', propietario de `parkingId`).
    *   **Path Params:** `parkingId` (Long), `featureSlug` (String).
    *   **Respuesta (204 Sin Contenido):** Éxito.
    *   **Errores:** 401, 403, 404 (Parking o Feature No Encontrado), 500.
*   **`DELETE /parkings/{parkingId}/features/{featureSlug}`**
    *   **Descripción:** Elimina (desasocia) una característica de un parking. Idempotente.
    *   **Autenticación:** Requerida (Rol 'OWNER', propietario de `parkingId`).
    *   **Path Params:** `parkingId` (Long), `featureSlug` (String).
    *   **Respuesta (204 Sin Contenido):** Éxito.
    *   **Errores:** 401, 403, 404 (Parking No Encontrado), 500.

---

**6. Recurso: Usuarios (`/users`)**

*   **`PUT /users/me/location`**
    *   **Descripción:** Actualiza la ubicación del usuario actual.
    *   **Autenticación:** Requerida (JWT).
    *   **Cuerpo de Solicitud:** `LocationUpdateRequest` (`latitude`, `longitude`).
    *   **Respuesta (204 Sin Contenido):** Éxito.
    *   **Errores:** 400 (Falló validación), 401, 500.

---

**7. Recurso: Reservas (`/bookings`)**

*   **`POST /bookings`**
    *   **Descripción:** Crea una solicitud de reserva (registra intención).
    *   **Autenticación:** Requerida (Rol 'DRIVER').
    *   **Cuerpo de Solicitud:** `BookingRequest` (`parkingId`, `bookingTime`?).
    *   **Respuesta (201 Creado):** `BookingResponse`. Cabecera `Location`.
    *   **Errores:** 400, 401, 403, 404 (Parking No Encontrado), 500.
*   **`PATCH /bookings/{bookingRequestId}`**
    *   **Descripción:** Actualiza el estado de una solicitud de reserva.
    *   **Autenticación:** Requerida (JWT - Conductor O Propietario).
    *   **Path Params:** `bookingRequestId` (Long).
    *   **Cuerpo de Solicitud:** `UpdateBookingStatusRequest` (`{"status": "user_confirmed | owner_approved | etc."}`).
    *   **Respuesta (200 OK):** `BookingResponse`.
    *   **Errores:** 400 (Estado inválido), 401, 403, 404 (No Encontrado), 500.

---

**8. Recurso: Recomendaciones (`/recommendations`)**

*   **`GET /recommendations/zones`**
    *   **Descripción:** Obtiene zonas recomendadas para aparcar.
    *   **Autenticación:** Requerida (JWT).
    *   **Query Params:** `latitude`, `longitude`, `limit`?.
    *   **Respuesta (200 OK):** `RecommendedZonesResponse`.
    *   **Errores:** 400, 401, 500.
*   **`GET /recommendations/parkings`**
    *   **Descripción:** Obtiene parkings recomendados.
    *   **Autenticación:** Requerida (JWT).
    *   **Query Params:** `Pageable`, `latitude`?, `longitude`?, `radius`?, `timeOfDay`?, `limit`?.
    *   **Respuesta (200 OK):** `Page<ParkingDto>` (o DTO específico con score).
    *   **Errores:** 400, 401, 500.

---

**9. Recurso: Operaciones (`/operations`)**

*   **`GET /operations/{operationId}/status`**
    *   **Descripción:** Obtiene el estado de una operación asíncrona (si aplica).
    *   **Autenticación:** Requerida (JWT).
    *   **Path Params:** `operationId` (String/UUID).
    *   **Respuesta (200 OK):** `OperationStatusResponse`.
    *   **Errores:** 401, 404, 500.

---