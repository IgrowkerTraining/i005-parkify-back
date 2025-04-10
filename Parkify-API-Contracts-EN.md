## English (Inglés)

**General Conventions:**

*   **Base Path:** `/api/v1`
*   **Data Format:** JSON
*   **DTO Field Naming:** `camelCase` (Java Records preferred for DTOs)
*   **Authentication:** JWT Bearer token in `Authorization: Bearer <token>` header for protected endpoints.
*   **Identifiers:**
    *   Resources in DB (Parking, User, Feature, Booking, etc.): `Long`.
    *   Features in API (path/query params, DTO): Unique `String` slug (e.g., `guarded`).
    *   Parkings/Users in path/DTO: `Long`.
*   **Date/Time:** ISO 8601 format (`String`).
*   **Pagination:** Query params `page` (int, 0-based), `size` (int, default 20), `sort` (string, `property,direction`). Response: `org.springframework.data.domain.Page<YourDto>`.
*   **Standard Error Response:** Format like RFC 7807 Problem Details (fields `type`, `title`, `status`, `detail`, `instance`, `timestamp`, `errors` [optional]).

---

**1. Resource: Authentication (`/auth`)**

*   **`POST /auth/register`**
    *   **Description:** Registers a new user (Driver or Owner).
    *   **Authentication:** Not required.
    *   **Request Body:** `RegisterRequest` (`name`, `email`, `password`, `role`['DRIVER' | 'OWNER'], `contactPhone`).
    *   **Response (201 Created):** `UserResponse` (`id`, `name`, `email`, `role`, `contactPhone`). `Location` header: `/api/v1/users/{userId}`.
    *   **Errors:** 400 (Validation failed, Email exists), 500.
*   **`POST /auth/login`**
    *   **Description:** Logs in a user, obtains a JWT token.
    *   **Authentication:** Not required.
    *   **Request Body:** `LoginRequest` (`email`, `password`).
    *   **Response (200 OK):** `LoginResponse` (`accessToken`, `tokenType`, `expiresIn`, `user`{`id`, `name`, `email`, `role`}).
    *   **Errors:** 400 (Validation failed), 401 (Invalid credentials), 500.
*   **`GET /auth/me`**
    *   **Description:** Gets information about the currently authenticated user.
    *   **Authentication:** Required (JWT).
    *   **Response (200 OK):** `UserResponse`.
    *   **Errors:** 401 (Unauthorized), 404 (User not found), 500.

---

**2. Resource: Configuration (`/config`)**

*   **`GET /config/initial`**
    *   **Description:** Gets initial UI/application configuration.
    *   **Authentication:** Required (JWT).
    *   **Response (200 OK):** `InitialConfigResponse` (`themeColors`, `featureFlags`).
    *   **Errors:** 401, 500.

---

**3. Resource: Content (`/content`)**

*   **`GET /content/home`**
    *   **Description:** Gets static content for the home page.
    *   **Authentication:** Not required.
    *   **Response (200 OK):** `HomeContentResponse`.
    *   **Errors:** 500.
*   **`GET /content/footer`**
    *   **Description:** Gets data for the footer.
    *   **Authentication:** Not required.
    *   **Response (200 OK):** `FooterContentResponse`.
    *   **Errors:** 500.

---

**4. Resource: Features (`/features`)**

*   **`GET /features`**
    *   **Description:** Gets the list of all available parking features.
    *   **Authentication:** Not required / `authenticated()`.
    *   **Query Params:** `Pageable` (optional).
    *   **Response (200 OK):** `List<FeatureDto>` (or `Page<FeatureDto>`).
    *   **Errors:** 500.
*   **`GET /features/{featureSlug}`**
    *   **Description:** Gets information about a specific feature by its unique slug.
    *   **Authentication:** Not required / `authenticated()`.
    *   **Path Params:** `featureSlug` (String).
    *   **Response (200 OK):** `FeatureDto`.
    *   **Errors:** 404 (Not Found), 500.
*   *(Optional: `POST`, `PUT`, `DELETE` for feature management - protected by ADMIN role)*

---

**5. Resource: Parkings (`/parkings`)**

*   **`GET /parkings`**
    *   **Description:** Searches and filters parkings.
    *   **Authentication:** Required (JWT) / `permitAll()`.
    *   **Query Params:** `Pageable`, `latitude`?, `longitude`?, `radius`?, `maxPrice`?, `minAvailability`?, `features` (List<String> of slugs)?.
    *   **Response (200 OK):** `Page<ParkingDto>`.
    *   **Errors:** 400 (Invalid params), 401, 500.
*   **`POST /parkings/my`**
    *   **Description:** Creates a parking by the currently authenticated owner.
    *   **Authentication:** Required (Role 'OWNER').
    *   **Request Body:** `CreateParkingRequestDto` (with `featureSlugs`).
    *   **Response (201 Created):** `ParkingDto`. `Location` header.
    *   **Errors:** 400 (Validation failed), 401, 403 (Forbidden), 404 (Feature slug not found?), 500.
*   **`GET /parkings/{parkingId}`**
    *   **Description:** Gets detailed information about a parking.
    *   **Authentication:** Required (JWT) / `permitAll()`.
    *   **Path Params:** `parkingId` (Long).
    *   **Response (200 OK):** `ParkingDto`.
    *   **Errors:** 401, 404 (Not Found), 500.
*   **`GET /parkings/my`**
    *   **Description:** Gets the parking belonging to the currently authenticated owner.
    *   **Authentication:** Required (Role 'OWNER').
    *   **Response (200 OK):** `ParkingDto`.
    *   **Errors:** 401, 403, 404 (Not Found), 500.
*   **`GET /parkings/{parkingId}/availability`**
    *   **Description:** Gets the current availability of a parking.
    *   **Authentication:** Required (JWT) / `permitAll()`.
    *   **Path Params:** `parkingId` (Long).
    *   **Response (200 OK):** `ParkingAvailabilityResponse` (`parkingId`, `availableSpots`).
    *   **Errors:** 401, 404, 500.
*   **`PATCH /parkings/my/availability`**
    *   **Description:** Updates the availability of the parking by the current owner.
    *   **Authentication:** Required (Role 'OWNER').
    *   **Request Body:** `UpdateAvailabilityRequest` (`{"availableSpots": number}`).
    *   **Response (200 OK):** `ParkingAvailabilityResponse`.
    *   **Errors:** 400 (Validation failed), 401, 403, 404 (Not Found), 500.
*   **`PUT /parkings/{parkingId}/features/{featureSlug}`**
    *   **Description:** Adds (associates) a feature to a parking. Idempotent.
    *   **Authentication:** Required (Role 'OWNER', owner of `parkingId`).
    *   **Path Params:** `parkingId` (Long), `featureSlug` (String).
    *   **Response (204 No Content):** Success.
    *   **Errors:** 401, 403, 404 (Parking or Feature Not Found), 500.
*   **`DELETE /parkings/{parkingId}/features/{featureSlug}`**
    *   **Description:** Removes (disassociates) a feature from a parking. Idempotent.
    *   **Authentication:** Required (Role 'OWNER', owner of `parkingId`).
    *   **Path Params:** `parkingId` (Long), `featureSlug` (String).
    *   **Response (204 No Content):** Success.
    *   **Errors:** 401, 403, 404 (Parking Not Found), 500.

---

**6. Resource: Users (`/users`)**

*   **`PUT /users/me/location`**
    *   **Description:** Updates the location of the current user.
    *   **Authentication:** Required (JWT).
    *   **Request Body:** `LocationUpdateRequest` (`latitude`, `longitude`).
    *   **Response (204 No Content):** Success.
    *   **Errors:** 400 (Validation failed), 401, 500.

---

**7. Resource: Bookings (`/bookings`)**

*   **`POST /bookings`**
    *   **Description:** Creates a booking request (records intent).
    *   **Authentication:** Required (Role 'DRIVER').
    *   **Request Body:** `BookingRequest` (`parkingId`, `bookingTime`?).
    *   **Response (201 Created):** `BookingResponse`. `Location` header.
    *   **Errors:** 400, 401, 403, 404 (Parking Not Found), 500.
*   **`PATCH /bookings/{bookingRequestId}`**
    *   **Description:** Updates the status of a booking request.
    *   **Authentication:** Required (JWT - Driver OR Owner).
    *   **Path Params:** `bookingRequestId` (Long).
    *   **Request Body:** `UpdateBookingStatusRequest` (`{"status": "user_confirmed | owner_approved | etc."}`).
    *   **Response (200 OK):** `BookingResponse`.
    *   **Errors:** 400 (Invalid status), 401, 403, 404 (Not Found), 500.

---

**8. Resource: Recommendations (`/recommendations`)**

*   **`GET /recommendations/zones`**
    *   **Description:** Gets recommended parking zones.
    *   **Authentication:** Required (JWT).
    *   **Query Params:** `latitude`, `longitude`, `limit`?.
    *   **Response (200 OK):** `RecommendedZonesResponse`.
    *   **Errores:** 400, 401, 500.
*   **`GET /recommendations/parkings`**
    *   **Описание:** Gets recommended parkings.
    *   **Аутентификация:** Required (JWT).
    *   **Query Params:** `Pageable`, `latitude`?, `longitude`?, `radius`?, `timeOfDay`?, `limit`?.
    *   **Response (200 OK):** `Page<ParkingDto>` (or specific DTO with score).
    *   **Ошибки:** 400, 401, 500.

---

**9. Resource: Operations (`/operations`)**

*   **`GET /operations/{operationId}/status`**
    *   **Описание:** Gets the status of an asynchronous operation (if applicable).
    *   **Аутентификация:** Required (JWT).
    *   **Path Params:** `operationId` (String/UUID).
    *   **Response (200 OK):** `OperationStatusResponse`.
    *   **Ошибки:** 401, 404, 500.

---