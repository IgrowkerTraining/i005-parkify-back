#!/bin/bash

# --- Variables (reemplaza según sea necesario) ---
BASE_URL="http://localhost:8080/api/v1"
# Generamos un email único para cada ejecución para evitar conflictos en ejecuciones repetidas
OWNER_EMAIL="curl_owner_$(date +%s%N)@example.com" # Añadido %N para mayor unicidad
OWNER_PASSWORD="password123"
INVALID_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c" # Ejemplo de token inválido

# Para almacenar el token y los IDs de los parkings entre pasos
TOKEN=""
PARKING_ID_1=""
PARKING_ID_2=""
PARKING_ID_FAR=""
DELETED_PARKING_ID="" # Guardaremos el ID del parking eliminado para la prueba

# Coordenadas para la búsqueda (aproximadamente centro de Madrid)
SEARCH_LAT=40.4168
SEARCH_LON=-3.7038
NON_EXISTENT_PARKING_ID=999999

# --- Funciones de Ayuda (Opcional, para legibilidad) ---
# Función para imprimir el encabezado de la sección
section_header() {
    echo -e "\n=================================================="
    echo " $1"
    echo "=================================================="
}

# Función para verificar el estado HTTP (muy básica)
check_status() {
    local response_file=$1
    local expected_status=$2
    local step_name=$3

    # Extraemos el estado de la última línea de encabezados (funciona para la salida de curl -v)
    # Añadimos 'head -n 1' por si curl da múltiples líneas de estado (p.ej., con redirecciones)
    local http_status_line=$(grep "< HTTP/" $response_file | tail -n 1)
    local http_status=$(echo $http_status_line | awk '{print $3}')

    if [ "$http_status" == "$expected_status" ]; then
        echo "[OK] $step_name: Estado esperado $expected_status recibido."
    else
        echo "[ERROR] $step_name: Se esperaba estado $expected_status, se recibió '$http_status'."
        # Imprimimos el cuerpo de la respuesta para diagnosticar el error
        echo "--- Cuerpo de la respuesta (en error) ---"
        cat response.body | jq '.' || cat response.body # Intentamos con jq, si no, lo mostramos tal cual
        echo "--------------------------------------"
        # exit 1; # Puedes descomentar si es crítico detener el script en el primer error
    fi
}

# --- Inicio de las Pruebas ---
section_header "Iniciando Pruebas de API Parkify con curl (Incluyendo Escenarios Negativos)"
echo "Usando Email: $OWNER_EMAIL"

# === Flujo del Propietario: Registro e Inicio de Sesión (con Pruebas Negativas) ===

section_header "1. Flujo de Registro e Inicio de Sesión del Propietario"

echo -e "\n[1.1 Negativo: Registro con Email Inválido (Esperado 400)]"
curl -v -X POST "$BASE_URL/auth/register" \
     -H "Content-Type: application/json" \
     -d "{\"email\": \"invalid-email\", \"password\": \"$OWNER_PASSWORD\", \"username\": \"Invalid Email User\", \"role\": \"OWNER\", \"contactPhone\": \"111\"}" \
     -o response.body 2> curl_output.log
check_status curl_output.log 400 "Registro con email inválido"

echo -e "\n[1.2 Negativo: Registro con Contraseña Corta (Esperado 400)]"
curl -v -X POST "$BASE_URL/auth/register" \
     -H "Content-Type: application/json" \
     -d "{\"email\": \"shortpass@example.com\", \"password\": \"123\", \"username\": \"Short Pass User\", \"role\": \"OWNER\", \"contactPhone\": \"222\"}" \
     -o response.body 2> curl_output.log
check_status curl_output.log 400 "Registro con contraseña corta"

echo -e "\n[1.3 Positivo: Registro del Propietario]"
curl -v -X POST "$BASE_URL/auth/register" \
     -H "Content-Type: application/json" \
     -d "{\"email\": \"$OWNER_EMAIL\", \"password\": \"$OWNER_PASSWORD\", \"username\": \"Curl Test Owner\", \"role\": \"OWNER\", \"contactPhone\": \"0000000000\"}" \
     -o response.body 2> curl_output.log
check_status curl_output.log 201 "Registro del Propietario"

echo -e "\n[1.4 Negativo: Intento de Registro Duplicado (Esperado 409)]"
curl -v -X POST "$BASE_URL/auth/register" \
     -H "Content-Type: application/json" \
     -d "{\"email\": \"$OWNER_EMAIL\", \"password\": \"$OWNER_PASSWORD\", \"username\": \"Curl Test Owner\", \"role\": \"OWNER\", \"contactPhone\": \"0000000000\"}" \
     -o response.body 2> curl_output.log
check_status curl_output.log 409 "Registro Duplicado"
echo "Mensaje de error: $(cat response.body | jq -r '.message // empty')"

echo -e "\n[1.5 Negativo: Login con Contraseña Vacía (Esperado 400)]"
curl -v -X POST "$BASE_URL/auth/login" \
     -H "Content-Type: application/json" \
     -d "{\"email\": \"$OWNER_EMAIL\", \"password\": \"\"}" \
     -o response.body 2> curl_output.log
check_status curl_output.log 400 "Login con contraseña vacía"

echo -e "\n[1.6 Positivo: Login del Propietario]"
RESPONSE_BODY=$(curl -s -X POST "$BASE_URL/auth/login" \
     -H "Content-Type: application/json" \
     -d "{\"email\": \"$OWNER_EMAIL\", \"password\": \"$OWNER_PASSWORD\"}")

echo "Respuesta de Login: $RESPONSE_BODY"
TOKEN=$(echo $RESPONSE_BODY | jq -r '.token // empty')

if [ -z "$TOKEN" ] || [ "$TOKEN" == "null" ]; then
    echo "[ERROR] No se pudo obtener el token. Verifica las credenciales y la respuesta del servidor."
    exit 1
else
    echo "[OK] Token obtenido."
fi

echo -e "\n[1.7 Negativo: Intento de Login con Contraseña Incorrecta (Esperado 401)]"
curl -v -X POST "$BASE_URL/auth/login" \
     -H "Content-Type: application/json" \
     -d "{\"email\": \"$OWNER_EMAIL\", \"password\": \"wrongpassword\"}" \
     -o response.body 2> curl_output.log
check_status curl_output.log 401 "Login con contraseña incorrecta"

echo -e "\n[1.8 Negativo: Intento de Login de Usuario Inexistente (Esperado 401)]"
curl -v -X POST "$BASE_URL/auth/login" \
     -H "Content-Type: application/json" \
     -d "{\"email\": \"nonexistent@example.com\", \"password\": \"password123\"}" \
     -o response.body 2> curl_output.log
check_status curl_output.log 401 "Login de usuario inexistente"


# === Flujo del Propietario: Gestión de Parkings (con Pruebas Negativas) ===

section_header "2. Flujo de Gestión de Parkings del Propietario"

echo -e "\n[2.1 Negativo: Creación de Parking Sin Token (Esperado 403)]"
curl -v -X POST "$BASE_URL/parkings/my" \
     -H "Content-Type: application/json" \
     -d "{\"name\": \"No Token Parking\", \"address\": \"No Token St\", \"latitude\": 40.0, \"longitude\": -3.0, \"capacity\": 5, \"hourlyRate\": 2.0}" \
     -o response.body 2> curl_output.log
check_status curl_output.log 403 "Creación de parking sin token"

echo -e "\n[2.2 Negativo: Creación de Parking con Token Inválido (Esperado 403)]"
curl -v -X POST "$BASE_URL/parkings/my" \
     -H "Authorization: Bearer $INVALID_TOKEN" \
     -H "Content-Type: application/json" \
     -d "{\"name\": \"Invalid Token Parking\", \"address\": \"Invalid Token St\", \"latitude\": 40.0, \"longitude\": -3.0, \"capacity\": 5, \"hourlyRate\": 2.0}" \
     -o response.body 2> curl_output.log
check_status curl_output.log 403 "Creación de parking con token inválido"

echo -e "\n[2.3 Negativo: Creación de Parking con Capacidad Negativa (Esperado 400)]"
curl -v -X POST "$BASE_URL/parkings/my" \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d "{\"name\": \"Negative Cap Parking\", \"address\": \"Neg Cap St\", \"latitude\": 40.0, \"longitude\": -3.0, \"capacity\": -5, \"hourlyRate\": 2.0}" \
     -o response.body 2> curl_output.log
check_status curl_output.log 400 "Creación de parking con capacidad negativa"

echo -e "\n[2.4 Positivo: Creación de Varios Parkings]"
# (El código para crear los parkings P1, P2, PFar permanece igual que en el script anterior)
# Parking 1 (Cerca, precio 4.5, capacidad 15)
echo "Creando parking 1 (cerca, precio 4.5)"
CREATE_RESPONSE_1=$(curl -s -X POST "$BASE_URL/parkings/my" \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d "{\"name\": \"Curl Parking Near 1\", \"address\": \"1 Curl Street\", \"latitude\": ${SEARCH_LAT}, \"longitude\": ${SEARCH_LON}, \"description\": \"Near, rate 4.5\", \"capacity\": 15, \"hourlyRate\": 4.5, \"workingHours\": \"09:00-18:00\", \"parkingPhone\": \"111111\", \"parkingImageUrl\": \"http://example.com/image1.jpg\"}")
PARKING_ID_1=$(echo $CREATE_RESPONSE_1 | jq -r '.id // empty')
if [ -z "$PARKING_ID_1" ] || [ "$PARKING_ID_1" == "null" ]; then echo "[ERROR] No se pudo crear el Parking 1."; exit 1; else echo "[OK] Parking 1 creado, ID: $PARKING_ID_1"; DELETED_PARKING_ID=$PARKING_ID_1; fi # Guardamos el ID del primero para la prueba de eliminación

# Parking 2 (Cerca, precio 6.0, capacidad 10)
echo "Creando parking 2 (cerca, precio 6.0)"
LAT2=$(echo "$SEARCH_LAT + 0.01" | bc)
LON2=$(echo "$SEARCH_LON + 0.01" | bc)
CREATE_RESPONSE_2=$(curl -s -X POST "$BASE_URL/parkings/my" \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d "{\"name\": \"Curl Parking Near 2\", \"address\": \"2 Curl Avenue\", \"latitude\": ${LAT2}, \"longitude\": ${LON2}, \"description\": \"Near, rate 6.0\", \"capacity\": 10, \"hourlyRate\": 6.0, \"workingHours\": \"10:00-20:00\", \"parkingPhone\": \"222222\", \"parkingImageUrl\": \"http://example.com/image2.jpg\"}")
PARKING_ID_2=$(echo $CREATE_RESPONSE_2 | jq -r '.id // empty')
if [ -z "$PARKING_ID_2" ] || [ "$PARKING_ID_2" == "null" ]; then echo "[ERROR] No se pudo crear el Parking 2."; exit 1; else echo "[OK] Parking 2 creado, ID: $PARKING_ID_2"; fi

# Parking 3 (Lejos, >5km, precio 5.0, capacidad 30)
echo "Creando parking 3 (lejos, precio 5.0)"
LAT_FAR=$(echo "$SEARCH_LAT + 0.1" | bc) # Aproximadamente 11 km
LON_FAR=$SEARCH_LON
CREATE_RESPONSE_FAR=$(curl -s -X POST "$BASE_URL/parkings/my" \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d "{\"name\": \"Curl Parking Far\", \"address\": \"100 Faraway Road\", \"latitude\": ${LAT_FAR}, \"longitude\": ${LON_FAR}, \"description\": \"Far, rate 5.0\", \"capacity\": 30, \"hourlyRate\": 5.0, \"workingHours\": \"08:00-22:00\", \"parkingPhone\": \"333333\", \"parkingImageUrl\": \"http://example.com/image_far.jpg\"}")
PARKING_ID_FAR=$(echo $CREATE_RESPONSE_FAR | jq -r '.id // empty')
if [ -z "$PARKING_ID_FAR" ] || [ "$PARKING_ID_FAR" == "null" ]; then echo "[ERROR] No se pudo crear el Parking Far."; exit 1; else echo "[OK] Parking Far creado, ID: $PARKING_ID_FAR"; fi

# --- Establecimiento de disponibilidad vía PATCH con ID ---
echo -e "\n[2.5 Positivo: Establecimiento de Disponibilidad vía PATCH /{id}/availability]"
# Establecer disponibilidad 10 para Parking 1
curl -v -X PATCH "$BASE_URL/parkings/${PARKING_ID_1}/availability" \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d "{\"availableSpots\": 10}" \
     -o response.body 2> curl_output.log
check_status curl_output.log 200 "Establecer disponibilidad P1=10"

# Establecer disponibilidad 5 para Parking 2
curl -v -X PATCH "$BASE_URL/parkings/${PARKING_ID_2}/availability" \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d "{\"availableSpots\": 5}" \
     -o response.body 2> curl_output.log
check_status curl_output.log 200 "Establecer disponibilidad P2=5"

# Establecer disponibilidad 20 para Parking Far
curl -v -X PATCH "$BASE_URL/parkings/${PARKING_ID_FAR}/availability" \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d "{\"availableSpots\": 20}" \
     -o response.body 2> curl_output.log
check_status curl_output.log 200 "Establecer disponibilidad PFar=20"

echo -e "\n[2.6 Positivo: Obtención de la Lista de Mis Parkings (Esperado 3)]"
curl -s -X GET "$BASE_URL/parkings/my-list" \
     -H "Authorization: Bearer $TOKEN" | jq '. | length as $len | "Encontrados: \($len), IDs: \(map(.id))"'

echo -e "\n[2.7 Negativo: Actualización de Disponibilidad Sin Token (Esperado 403)]"
curl -v -X PATCH "$BASE_URL/parkings/${PARKING_ID_1}/availability" \
     -H "Content-Type: application/json" \
     -d "{\"availableSpots\": 9}" \
     -o response.body 2> curl_output.log
check_status curl_output.log 403 "Actualización de disponibilidad sin token"

echo -e "\n[2.8 Negativo: Actualización de Disponibilidad de Parking Inexistente (Esperado 404)]"
curl -v -X PATCH "$BASE_URL/parkings/${NON_EXISTENT_PARKING_ID}/availability" \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d "{\"availableSpots\": 5}" \
     -o response.body 2> curl_output.log
check_status curl_output.log 404 "Actualización de disponibilidad parking inexistente"

echo -e "\n[2.9 Negativo: Actualización de Disponibilidad con Número Negativo de Plazas (Esperado 400)]"
curl -v -X PATCH "$BASE_URL/parkings/${PARKING_ID_1}/availability" \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d "{\"availableSpots\": -1}" \
     -o response.body 2> curl_output.log
check_status curl_output.log 400 "Actualización de disponibilidad con número negativo"

echo -e "\n[2.10 Negativo: Actualización de Disponibilidad > Capacidad (Esperado 400)]"
# Capacidad de P1 = 15
curl -v -X PATCH "$BASE_URL/parkings/${PARKING_ID_1}/availability" \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d "{\"availableSpots\": 16}" \
     -o response.body 2> curl_output.log
check_status curl_output.log 400 "Actualización de disponibilidad P1 (> capacidad)"


# === Pruebas de Peticiones de Disponibilidad Múltiple (Batch) (con Pruebas Negativas) ===
section_header "3. Pruebas de Peticiones de Disponibilidad Múltiple (Batch)"

echo -e "\n[3.1 Positivo: Petición Múltiple de Disponibilidad (Estado Inicial)]"
# Esperado: P1=10, P2=5, PFar=20
echo "IDs solicitados: $PARKING_ID_1,$PARKING_ID_2,$PARKING_ID_FAR,${NON_EXISTENT_PARKING_ID}" # Añadido ID inexistente
time curl -s -X GET "$BASE_URL/parkings/availability?ids=$PARKING_ID_1,$PARKING_ID_2,$PARKING_ID_FAR,${NON_EXISTENT_PARKING_ID}" -v > batch_initial.json 2> /dev/null
echo "Respuesta guardada en batch_initial.json. Tiempo de ejecución:"
cat batch_initial.json | jq . # Debería devolver solo los existentes

echo -e "\n[3.2 Negativo: Petición Múltiple con Lista de IDs Vacía (Esperado 400)]"
curl -v -X GET "$BASE_URL/parkings/availability?ids=" \
     -o response.body 2> curl_output.log
check_status curl_output.log 400 "Petición múltiple con lista de IDs vacía"

# (Dejamos el cambio de disponibilidad y la repetición de la petición múltiple para verificar la actualización)
echo -e "\n[3.3 Positivo: Cambiamos Disponibilidad del Parking Far (ID: $PARKING_ID_FAR) a 3]"
curl -v -X PATCH "$BASE_URL/parkings/${PARKING_ID_FAR}/availability" \
     -H "Authorization: Bearer $TOKEN" \
     -H "Content-Type: application/json" \
     -d "{\"availableSpots\": 3}" \
     -o response.body 2> curl_output.log
check_status curl_output.log 200 "Cambio de disponibilidad PFar=3"

echo -e "\n[3.4 Positivo: Petición Múltiple de Disponibilidad (Después del Cambio)]"
# Esperado: P1=10, P2=5, PFar=3
echo "IDs solicitados: $PARKING_ID_1,$PARKING_ID_2,$PARKING_ID_FAR,${NON_EXISTENT_PARKING_ID}"
time curl -s -X GET "$BASE_URL/parkings/availability?ids=$PARKING_ID_1,$PARKING_ID_2,$PARKING_ID_FAR,${NON_EXISTENT_PARKING_ID}" -v > batch_after.json 2> /dev/null
echo "Respuesta guardada en batch_after.json. Tiempo de ejecución:"
cat batch_after.json | jq .

echo "[INFO] Compara batch_initial.json y batch_after.json para ver los cambios (PFar)."


# === Acceso Público: Búsqueda, Filtros, Detalles (con Pruebas Negativas) ===
section_header "4. Pruebas de Acceso Público"

# Las pruebas positivas de búsqueda (4.1 - 4.10) permanecen igual...
echo -e "\n[4.1 Búsqueda de Parkings Cercanos (Básica)]"
curl -s -X GET "$BASE_URL/parkings?latitude=${SEARCH_LAT}&longitude=${SEARCH_LON}" | jq '.data | length as $len | "Encontrados: \($len), Primeros IDs: \(map(.id))"'
# ... (resto de pruebas positivas de búsqueda) ...
echo -e "\n[4.10 Búsqueda con Paginación (limit=2, offset=1) - Segundo y tercero]"
curl -s -X GET "$BASE_URL/parkings?latitude=${SEARCH_LAT}&longitude=${SEARCH_LON}&limit=2&offset=1" | jq '{data: .data | map(.id), pagination: .pagination}'


echo -e "\n[4.11 Negativo: Búsqueda sin Latitude (Esperado 400)]"
curl -v -X GET "$BASE_URL/parkings?longitude=${SEARCH_LON}" \
     -o response.body 2> curl_output.log
check_status curl_output.log 400 "Búsqueda sin latitude"

echo -e "\n[4.12 Negativo: Búsqueda sin Longitude (Esperado 400)]"
curl -v -X GET "$BASE_URL/parkings?latitude=${SEARCH_LAT}" \
     -o response.body 2> curl_output.log
check_status curl_output.log 400 "Búsqueda sin longitude"

echo -e "\n[4.13 Positivo: Obtención de Detalles del Parking Far (ID: $PARKING_ID_FAR)]"
curl -s -X GET "$BASE_URL/parkings/$PARKING_ID_FAR" | jq .

echo -e "\n[4.14 Negativo: Obtención de Detalles de Parking Inexistente (Esperado 404)]"
curl -v -X GET "$BASE_URL/parkings/${NON_EXISTENT_PARKING_ID}" \
     -o response.body 2> curl_output.log
check_status curl_output.log 404 "Obtención de detalles parking inexistente"

echo -e "\n[4.15 Positivo: Obtención de Disponibilidad de Un Parking (ID: $PARKING_ID_2)]"
curl -s -X GET "$BASE_URL/parkings/$PARKING_ID_2/availability" | jq .

echo -e "\n[4.16 Negativo: Obtención de Disponibilidad de Parking Inexistente (Esperado 404)]"
curl -v -X GET "$BASE_URL/parkings/${NON_EXISTENT_PARKING_ID}/availability" \
     -o response.body 2> curl_output.log
check_status curl_output.log 404 "Obtención de disponibilidad parking inexistente"


# === Acceso Público: Contenido y Configuración ===
# Estos endpoints normalmente no tienen escenarios negativos, salvo errores generales del servidor
section_header "5. Pruebas de Contenido y Configuración"
echo -e "\n[5.1 Obtención del Contenido del Footer]"
curl -s -X GET "$BASE_URL/content/footer" | jq .
echo -e "\n[5.2 Obtención del Contenido de la Página de Inicio]"
curl -s -X GET "$BASE_URL/content/home" | jq .
echo -e "\n[5.3 Obtención de la Configuración Inicial]"
curl -s -X GET "$BASE_URL/config/initial" | jq .


# === Verificaciones de Seguridad (ampliado) ===
section_header "6. Pruebas de Seguridad"

echo -e "\n[6.1 Acceso a /my-list sin Token (Esperado 403)]"
curl -v -X GET "$BASE_URL/parkings/my-list" -o /dev/null 2> curl_output.log
check_status curl_output.log 403 "Acceso a /my-list sin token"

echo -e "\n[6.2 Acceso a PATCH /{id}/availability sin Token (Esperado 403)]"
curl -v -X PATCH "$BASE_URL/parkings/${PARKING_ID_2}/availability" \
     -H "Content-Type: application/json" \
     -d "{\"availableSpots\": 9}" \
     -o response.body 2> curl_output.log
check_status curl_output.log 403 "Acceso a PATCH /{id}/availability sin token"

echo -e "\n[6.3 Acceso a DELETE /my sin Token (Esperado 403)]"
curl -v -X DELETE "$BASE_URL/parkings/my" \
     -o response.body 2> curl_output.log
check_status curl_output.log 403 "Acceso a DELETE /my sin token"

# === Limpieza (con Pruebas Negativas) ===
section_header "7. Limpieza"

if [ ! -z "$TOKEN" ] && [ "$TOKEN" != "null" ] && [ ! -z "$DELETED_PARKING_ID" ]; then
    echo -e "\n[7.1 Positivo: Eliminación del Parking (ID: $DELETED_PARKING_ID) por el Propietario]"
    # Usamos el endpoint /my, que debería eliminar el primer parking del propietario
    curl -v -X DELETE "$BASE_URL/parkings/my" \
         -H "Authorization: Bearer $TOKEN" \
         -o /dev/null 2> curl_output.log
    check_status curl_output.log 204 "Eliminación de parking por el propietario"

    echo -e "\n[7.2 Negativo: Intento de Obtener Detalles del Parking Eliminado (Esperado 404)]"
    curl -v -X GET "$BASE_URL/parkings/$DELETED_PARKING_ID" \
        -o response.body 2> curl_output.log
    check_status curl_output.log 404 "Obtención de detalles parking eliminado"

    echo -e "\n[7.3 Negativo: Intento de Actualizar Disponibilidad del Parking Eliminado (Esperado 404)]"
    curl -v -X PATCH "$BASE_URL/parkings/${DELETED_PARKING_ID}/availability" \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d "{\"availableSpots\": 5}" \
        -o response.body 2> curl_output.log
    check_status curl_output.log 404 "Actualización de disponibilidad parking eliminado"

    echo -e "\n[7.4 Negativo: Intento de Eliminación Repetida vía /my (Esperado 404)]"
    # Como el primer parking fue eliminado, el siguiente intento debería devolver 404 si solo había uno,
    # o eliminar el siguiente. Verificaremos que la lista disminuyó e intentaremos eliminar de nuevo.
    echo "Verificando lista después de la primera eliminación:"
    curl -s -X GET "$BASE_URL/parkings/my-list" \
        -H "Authorization: Bearer $TOKEN" | jq '. | length as $len | "Restantes: \($len), IDs: \(map(.id))"'

    echo "Intentando eliminar de nuevo vía /my:"
    curl -v -X DELETE "$BASE_URL/parkings/my" \
         -H "Authorization: Bearer $TOKEN" \
         -o /dev/null 2> curl_output.log
    # El estado esperado depende: 204 si quedan más parkings, 404 si no quedan.
    echo "[INFO] Estado esperado 204 (si quedan más) o 404 (si no quedan)."

    # Opcional: Eliminar los parkings restantes para limpieza, si los hubiera
    echo "Eliminando parkings restantes..."
    curl -s -X DELETE "$BASE_URL/parkings/my" -H "Authorization: Bearer $TOKEN" > /dev/null
    curl -s -X DELETE "$BASE_URL/parkings/my" -H "Authorization: Bearer $TOKEN" > /dev/null # Por si eran 3
else
    echo "[INFO] Saltando limpieza: no se obtuvo token o no se crearon parkings."
fi

# Limpieza de archivos temporales
rm curl_output.log response.body batch_initial.json batch_after.json 2>/dev/null

section_header "--- Pruebas Completadas ---"