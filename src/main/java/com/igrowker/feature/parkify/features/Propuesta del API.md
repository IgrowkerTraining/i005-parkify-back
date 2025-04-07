---

## Interfaces API Propuestas para Sprint 1

**Acuerdos Generales:**
*   **Ruta Base:** `/api/v1`
*   **Formato:** JSON
*   **Nombres de Campos:** `camelCase`
*   **Autenticación:** Token JWT (`Authorization: Bearer <token>`) donde se indique.
*   **Errores:** Formato estándar (ver ejemplo abajo).

**Ejemplo de Respuesta de Error Estándar:**

```json
{
  "timestamp": "2025-01-15T10:30:00Z",
  "status": 4xx / 5xx,
  "error": "Descripción Corta (Ej: Bad Request, Not Found)",
  "message": "Mensaje legible sobre el error",
  "path": "/api/v1/ruta/del/recurso"
}
```

---

### #10 Endpoint para Configuración Inicial

*   **Método:** `GET`
*   **Ruta:** `/api/v1/config/initial`
*   **Descripción:** Devuelve configuración básica de la aplicación.
*   **Autenticación:** **Requerida (JWT)** (Según la tarea, aunque podría ser pública).
*   **Query Params:** Ninguno.
*   **Request Body:** Ninguno.
*   **Response Body (200 OK):**

    ```json
    {
      "themeColors": { "primary": "#...", "secondary": "#..." },
      "featureFlags": { "recommendationsEnabled": true }
      // ...otros parámetros de configuración...
    }
    ```

---

### #11 Endpoint para Datos de la Pantalla de Inicio

*   **Método:** `GET`
*   **Ruta:** `/api/v1/content/home`
*   **Descripción:** Devuelve contenido estático para la pantalla de inicio.
*   **Autenticación:** No requerida.
*   **Query Params:** Ninguno.
*   **Request Body:** Ninguno.
*   **Response Body (200 OK):**

    ```json
    {
      "whoAreWe": { "title": "...", "text": "..." },
      "whatWeOffer": { "title": "...", "items": [ { "icon": "...", "text": "..." } ] }
      // ...otro contenido...
    }
    ```

---

### #12 Endpoint para Estado de Autenticación del Usuario

*   **Método:** `GET`
*   **Ruta:** `/api/v1/auth/me`
*   **Descripción:** Devuelve información del usuario autenticado actualmente (basado en JWT). Útil para saber si el usuario está logueado y cuál es su rol.
*   **Autenticación:** **Requerida (JWT)**.
*   **Query Params:** Ninguno.
*   **Request Body:** Ninguno.
*   **Response Body (200 OK - Autenticado):**

    ```json
    {
      "id": "string (uuid)",
      "name": "string",
      "email": "string",
      "role": "string (enum: ['driver', 'owner'])"
    }
    ```
*   **Response Body (401 Unauthorized - No autenticado o token inválido):** Respuesta de error estándar 401.

---

### #13 Endpoint para Datos del Footer

*   **Método:** `GET`
*   **Ruta:** `/api/v1/content/footer`
*   **Descripción:** Devuelve datos para el footer (enlaces).
*   **Autenticación:** No requerida.
*   **Query Params:** Ninguno.
*   **Request Body:** Ninguno.
*   **Response Body (200 OK):**

    ```json
    {
      "aboutUsLink": "/about",
      "contactLink": "/contact",
      "socialLinks": [ { "platform": "facebook", "url": "..." } ]
    }
    ```

---

### #14 Endpoint para Registro de Conductores

*   **Método:** `POST`
*   **Ruta:** `/api/v1/auth/register`
*   **Descripción:** Registra un nuevo usuario con el rol de 'conductor'. Debe usar **BCrypt** para la contraseña internamente.
*   **Autenticación:** No requerida.
*   **Query Params:** Ninguno.
*   **Request Body:**

    ```json
    {
      "name": "string (required)",
      "email": "string (required, format: email)",
      "password": "string (required, minLength: 8)",
      "role": "string (required, value: 'driver')" // Se especifica el rol
    }
    ```
*   **Response Body (201 Created):** Devuelve el usuario creado (sin contraseña).

    ```json
    {
      "id": "string (uuid)",
      "name": "string",
      "email": "string",
      "role": "driver"
    }
    ```
*   **Response Body (400 Bad Request):** Errores de validación (email inválido, contraseña corta, email ya existe). Respuesta de error estándar.
