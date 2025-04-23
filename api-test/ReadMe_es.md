# README: Pruebas de API para Parkify Backend (test_api.sh)

## Propósito

Este documento describe cómo ejecutar e interpretar los resultados de las pruebas de API automatizadas para el backend de Parkify, utilizando el script de shell proporcionado `test_api.sh`. El script ejecuta una secuencia de peticiones `curl` contra la aplicación Parkify ejecutándose localmente (a través de Docker Compose) para verificar la funcionalidad principal del MVP y manejar algunos escenarios negativos.

**Incluso si no ejecutas el script**, la sección **"Cobertura de las Pruebas"** a continuación ofrece una visión clara de qué endpoints y escenarios de usuario específicos se verifican con esta prueba. Esto te ayudará a evaluar la preparación del backend y, si es necesario, replicar manualmente las peticiones deseadas (por ejemplo, en Postman).

## Prerrequisitos

Antes de ejecutar las pruebas, asegúrate de que los siguientes componentes estén instalados y configurados en tu sistema:

1.  **Docker y Docker Compose:** Necesarios para ejecutar la aplicación backend y la base de datos PostgreSQL en contenedores. Asegúrate de que el daemon de Docker (o Docker Desktop) esté en ejecución.
2.  **Shell de Línea de Comandos:**
    *   **Linux/macOS:** El `Terminal` estándar.
    *   **Windows:** Se recomienda usar **Git Bash** (se instala con Git) o **WSL** (Windows Subsystem for Linux). Es posible que `cmd` o `PowerShell` estándar no soporten completamente la sintaxis del script.
3.  **`curl`:** Utilidad de línea de comandos para realizar peticiones HTTP. Generalmente preinstalado en Linux/macOS e incluido con Git Bash.
4.  **`jq`:** Utilidad de línea de comandos para procesar JSON. Se usa en el script para extraer el token y formatear la salida. **Importante:** Sin `jq`, el script podría no funcionar correctamente o su salida será menos legible.
    *   **Verificar Instalación:** Abre tu terminal/Git Bash y escribe `jq --version`. Si ves un número de versión, `jq` está instalado. Si no, instálalo.
    *   **Instalación de `jq`:**
        *   Debian/Ubuntu: `sudo apt update && sudo apt install jq`
        *   Fedora/CentOS: `sudo yum install jq` o `sudo dnf install jq`
        *   macOS (Homebrew): `brew install jq`
        *   Windows (vía Git Bash): Puedes descargar el ejecutable `jq.exe` desde el [sitio web oficial](https://jqlang.github.io/jq/download/) y colocarlo en un directorio incluido en tu `PATH` (p.ej., el directorio `bin` dentro de tu instalación de Git), o simplemente poner `jq.exe` en la misma carpeta que `test_api.sh`.
        *   Windows (Chocolatey): `choco install jq`

## Preparación e Inicio del Entorno

1.  **Navega al directorio raíz del proyecto Parkify Backend**, donde se encuentra el archivo `docker-compose.yml` (y el `Dockerfile`).
2.  **Asegúrate de que Docker Desktop (o el daemon de Docker) esté en ejecución.**
3.  **Inicia la aplicación usando Docker Compose:**
    *   Abre un terminal (o Git Bash) **en este directorio raíz**.
    *   Ejecuta el comando:
        ```bash
        docker-compose up -d --build
        ```
    *   `--build`: Reconstruye la imagen Docker. Importante en la primera ejecución o después de cambios en el código Java/Spring o en el `Dockerfile`.
    *   `-d`: Ejecuta los contenedores en modo detached (en segundo plano).
4.  **Espera a que la aplicación Spring Boot se inicie completamente.** La aplicación no está lista para aceptar peticiones inmediatamente. Necesitas monitorizar los logs para ver la confirmación de inicio.
    *   Ejecuta el comando para ver los logs del contenedor `spring_boot_app`:
        ```bash
        docker-compose logs -f spring_boot_app
        ```
    *   Las pruebas pueden comenzar **solo después** de que aparezca una línea similar a esta (el tiempo de inicio puede variar):
        ```log
        spring_boot_app  | .... INFO ... --- [mini-project] [           main] c.i.feature.parkify.ParkifyApplication   : Started ParkifyApplication in ... seconds ...
        ```
    *   Una vez que veas esta línea, presiona `Ctrl+C` para dejar de seguir los logs (los contenedores seguirán ejecutándose en segundo plano).

## Ejecución de las Pruebas

1.  **Navega al directorio** donde se encuentra el archivo de script `test_api.sh`.
2.  **Haz que el script sea ejecutable (solo necesita hacerse una vez):**
    *   Los sistemas operativos Linux, macOS y Git Bash requieren permiso explícito para ejecutar archivos como programas.
    *   Abre un terminal (o Git Bash) **en el directorio que contiene `test_api.sh`**.
    *   Ejecuta el comando `chmod +x` (change mode, add executable permission):
        ```bash
        chmod +x test_api.sh
        ```
    *   Solo necesitas ejecutar este comando **una vez** para este archivo. Después, el sistema sabrá que puede ser ejecutado.
3.  **Ejecuta el script:**
    *   Estando en el mismo directorio, ejecuta:
        ```bash
        ./test_api.sh
        ```
    *   Los caracteres `./` le indican al sistema que ejecute el archivo desde el directorio actual.
    *   **Visualización y Guardado de la Salida:** La salida se mostrará en la consola. Si la salida es larga o quieres guardarla para analizarla, usa la redirección a un archivo:
        ```bash
        ./test_api.sh | tee test_output.log
        ```
        Este comando mostrará la salida en pantalla *y* la guardará en el archivo `test_output.log`.

## Interpretación de Resultados

El script muestra información sobre los pasos que se están ejecutando y sus resultados:

*   **`[OK] Nombre del paso: Estado esperado XXX recibido.`**: Indica que el paso de la prueba se completó con éxito y la API devolvió el estado HTTP esperado.
*   **`[ERROR] Nombre del paso: Se esperaba el estado XXX, se recibió 'YYY'.`**: Indica que el paso de la prueba falló. La API devolvió un estado HTTP inesperado. El script también imprimirá el cuerpo de la respuesta en caso de error para ayudar en el diagnóstico.
*   **`[INFO]`**: Mensajes informativos.
*   **`--- Pruebas Completadas ---`**: Mensaje que indica que el script ha finalizado su ejecución.

Revisa toda la salida en busca de mensajes `[ERROR]`. Si no hay errores, las pruebas de API se consideran superadas con éxito.

## Cobertura de las Pruebas

El script `test_api.sh` verifica los siguientes escenarios y endpoints principales:

*(Esta sección permite entender rápidamente qué se está probando, incluso sin ejecutar el script)*

1.  **Autenticación y Registro del Propietario (`/api/v1/auth`)**
    *   `POST /register`: Registro exitoso, error en registro duplicado (409), errores de validación (email inválido, contraseña corta - 400).
    *   `POST /login`: Login exitoso, obtención de token JWT, error con contraseña incorrecta (401), error con usuario inexistente (401), error de validación (contraseña vacía - 400).
2.  **Gestión de Parkings del Propietario (`/api/v1/parkings`)**
    *   `POST /my`: Creación de parking (éxito 201), errores de seguridad (sin token/token inválido - 403), error de validación (capacidad negativa - 400).
    *   `PATCH /{id}/availability`: Actualización de disponibilidad (éxito 200), errores de seguridad (sin token - 403), errores de lógica de negocio (actualizar parking inexistente - 404; actualizar con valores inválidos [negativo, > capacidad] - 400).
    *   `GET /my-list`: Obtención de la lista de parkings del propietario (éxito 200).
    *   `DELETE /my`: Eliminación del parking por el propietario (éxito 204), verificación de acceso después de eliminar (404).
3.  **Endpoints Públicos de Parkings (`/api/v1/parkings`)**
    *   `GET /availability?ids=...`: **Petición de Disponibilidad Múltiple (Batch).** Permite obtener el número actual de plazas disponibles para **varios parkings simultáneamente** en una sola petición. Esto es crucial para el Frontend, por ejemplo, para actualizar información en los marcadores del mapa o en una lista de parkings "Favoritos" del usuario sin necesidad de hacer múltiples peticiones individuales. La prueba verifica: obtención exitosa de datos, reflejo de actualizaciones hechas por el propietario, manejo de ID inexistente en la lista, manejo de lista de IDs vacía (error 400).
    *   `GET ?latitude=...&longitude=...`: Búsqueda de parkings cercanos (éxito 200), verificación de filtros (`radius`, `maxPrice`, `minAvailability`), verificación de paginación (`limit`, `offset`), errores por falta de parámetros obligatorios `latitude`/`longitude` (400).
    *   `GET /{id}`: Obtener detalles de un parking (éxito 200), error con ID inexistente (404).
    *   `GET /{id}/availability`: Obtener disponibilidad de un parking (éxito 200), error con ID inexistente (404).
4.  **Contenido y Configuración (`/api/v1/content`, `/api/v1/config`)**
    *   `GET /content/footer`: Obtención exitosa de datos (200).
    *   `GET /content/home`: Obtención exitosa de datos (200).
    *   `GET /config/initial`: Obtención exitosa de datos (200).
5.  **Seguridad (Verificaciones Generales)**
    *   Verificación de acceso a endpoints protegidos (`/my-list`, `PATCH /{id}/availability`, `DELETE /my`) sin token (espera 403).
6.  **Limpieza (Cleanup)**
    *   Verificación de acceso a recursos eliminados (espera 404).

## Posibles Problemas y Soluciones

*   **Error "Connection refused":** Asegúrate de que la pila de Docker Compose esté iniciada (`docker-compose ps`) y que el contenedor `spring_boot_app` esté funcionando y accesible en `localhost:8080`. Verifica si el puerto 8080 está ocupado por otro proceso.
*   **La aplicación no se inicia:** Verifica los logs del contenedor `spring_boot_app` (`docker-compose logs -f spring_boot_app`) en busca de errores durante el inicio de Spring Boot o la conexión a la BD.
*   **Error "jq: command not found":** Instala `jq` según las instrucciones en la sección "Prerrequisitos".
*   **Error "Permission denied" al ejecutar `./test_api.sh`:** No has otorgado permisos de ejecución al script. Vuelve a la sección "Ejecución de las Pruebas", paso 2, y ejecuta el comando `chmod +x test_api.sh`.
*   **Error "./test_api.sh: No such file or directory":** No estás en el directorio donde se encuentra el script. Usa el comando `cd <ruta_a_la_carpeta_del_script>` para navegar al directorio correcto.
*   **Las pruebas fallan inesperadamente:** Revisa la salida del script y el cuerpo de la respuesta en el paso fallido. La lógica de la API podría haber cambiado, puede haber un bug en el backend, o la aplicación/BD no se iniciaron correctamente.

## Notas y Limitaciones

*   Las pruebas asumen que se ejecutan contra una base de datos "limpia" (tal como se crea con `docker-compose up`). Las ejecuciones posteriores del script usan emails únicos para el registro para evitar conflictos.
*   Estas pruebas **no miden el rendimiento** de la API.
*   Las pruebas cubren los escenarios principales del MVP pero **no garantizan** la verificación de todos los posibles casos límite o combinaciones de parámetros.
*   El script depende de la estructura actual de la API. Cambios en las rutas, parámetros o formatos de respuesta pueden requerir la actualización del script.