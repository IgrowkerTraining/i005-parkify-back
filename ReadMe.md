# Parkify Backend

[![Java CI with Maven](https://github.com/IgrowkerTraining/i005-parkify-back/actions/workflows/maven.yml/badge.svg)](https://github.com/IgrowkerTraining/i005-parkify-back/actions/workflows/maven.yml)

Servicio backend para la aplicación Parkify, que proporciona una API para gestionar estacionamientos, usuarios, reservas, etc.

## 📋 Contenido

*   [Requisitos Previos](#-requisitos-previos)
*   [Inicio Rápido (Docker Compose)](#-inicio-rápido-docker-compose)
    *   [Configuración](#-configuración)
    *   [Ejecución](#-ejecución)
    *   [Detención](#-detención)
    *   [Visualización de Logs](#-visualización-de-logs)
    *   [Acceso a la Base de Datos](#-acceso-a-la-base-de-datos)
*   [Documentación de la API (Swagger)](#-documentación-de-la-api-swagger)
*   [Ejecución de Pruebas (Tests)](#-ejecución-de-pruebas-tests)
*   [Variables de Entorno (`.env`)](#-variables-de-entorno-env)
*   [Stack Tecnológico](#-stack-tecnológico)
*   [Licencia](#-licencia)

## 📦 Requisitos Previos

Antes de comenzar, asegúrate de tener instalado lo siguiente:

1.  **Git:** [Instalación de Git](https://git-scm.com/book/es/v2/Inicio---Sobre-el-Control-de-Versiones-Instalaci%C3%B3n-de-Git)
2.  **JDK 17:** (Amazon Corretto, OpenJDK, etc.) [Instalación de JDK](https://adoptium.net/) - *Aunque Docker incluye su propia Java, el JDK puede ser necesario para compilaciones/pruebas locales fuera de Docker.*
3.  **Maven:** [Instalación de Maven](https://maven.apache.org/install.html) - *Similar al JDK.*
4.  **Docker:** [Instalación de Docker](https://docs.docker.com/engine/install/)
5.  **Docker Compose:** Generalmente se instala junto con Docker Desktop. [Instrucciones](https://docs.docker.com/compose/install/)

## 🚀 Inicio Rápido (Docker Compose)

Este es el método recomendado para ejecutar el backend localmente para desarrollo y pruebas. Inicia tanto la aplicación Spring Boot como la base de datos PostgreSQL.

### ⚙️ Configuración

1.  **Clona el repositorio:**
    ```bash
    git clone https://github.com/IgrowkerTraining/i005-parkify-back.git
    cd i005-parkify-back
    ```

2.  **Crea el archivo `.env`:** En la raíz del proyecto, crea un archivo llamado `.env` (con el punto al inicio). Este archivo es utilizado por `docker-compose.yml` para configurar las variables de entorno.

3.  **Copia el siguiente contenido en `.env`:** Abre el archivo `.env` creado y pega el siguiente contenido:

    ```dotenv
    # Archivo .env para desarrollo local con docker-compose

    # --- Conexión a la Base de Datos ---
    # Estas variables son usadas por el servicio 'app' para conectar al servicio 'db'
    # Los valores deben coincidir con los esperados por el servicio 'db' (de su sección environment)
    SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/db_parkify
    # 'db' es el nombre del servicio postgres en docker-compose.yml
    # '5432' es el puerto estándar de postgres dentro de la red docker
    # 'db_parkify' es el nombre de la base de datos especificado en POSTGRES_DB para el servicio 'db'

    SPRING_DATASOURCE_USERNAME=parkify
    # Nombre de usuario especificado en POSTGRES_USER para el servicio 'db'

    SPRING_DATASOURCE_PASSWORD=1234
    # Contraseña especificada en POSTGRES_PASSWORD para el servicio 'db'

    # --- Perfil de Spring ---
    # Indica a Spring Boot que use el perfil 'dev'.
    # Si esta línea falta, se usará 'dev' por defecto (definido en docker-compose.yml).
    SPRING_PROFILES_ACTIVE=dev

    # --- Generación del Esquema JPA ---
    # El valor 'update' está establecido en docker-compose.yml, no es necesario sobrescribirlo aquí.
    # ¡ATENCIÓN!: Usa 'update' solo para desarrollo. Para QA/Prod usa 'validate' o 'none'.
    # SPRING_JPA_HIBERNATE_DDL_AUTO=update

    # --- Configuración JWT (Opcional, si necesitas sobrescribir los valores por defecto) ---
    # Si estas líneas están comentadas o ausentes, se usarán los valores de application.properties
    # JWT_SECRET=TXlUZXN0U2VjcmV0S2V5VGhhdElzTG9uZ0Vub3VnaDEyMw==
    # JWT_EXPIRATION=PT10H # Ejemplo: 10 horas
    ```

    **Importante:** ¡No añadas el archivo `.env` a Git! Ya está incluido en `.gitignore`.

### ▶️ Ejecución

1.  **Compilación y ejecución de contenedores (en segundo plano):**
    Abre una terminal en el directorio raíz del proyecto (donde está `docker-compose.yml`) y ejecuta:
    ```bash
    docker compose up -d --build
    ```
    *   `up`: Crea e inicia **todos** los servicios definidos en `docker-compose.yml` (`app` y `db`), ya que no se utilizan perfiles de Docker Compose.
    *   `-d`: Ejecuta los contenedores en modo *detached* (segundo plano). Regresarás inmediatamente a la línea de comandos.
    *   `--build`: Reconstruye forzosamente la imagen de la aplicación `app`. Recomendado en la primera ejecución o después de cambios en el código Java o `Dockerfile`.

2.  **Ejecución de contenedores (en modo interactivo):**
    Si prefieres ver los logs de la aplicación y la BD directamente en la terminal (útil para depuración), omite el flag `-d`:
    ```bash
    docker compose up --build
    ```
    *   Los logs de ambos servicios se mostrarán en tu terminal.
    *   Para detener los contenedores iniciados en este modo, presiona **`Ctrl+C`** en la misma terminal.

3.  **Verificación del estado:** (como antes)
    ```bash
    docker compose ps
    ```
    Deberías ver dos servicios con estado `running` o `up`. La base de datos puede tardar un poco en iniciarse la primera vez.

4.  **Acceso a la aplicación:** (como antes) La aplicación estará disponible en:
    `http://localhost:8080`

### ⏹️ Detención y Gestión de Contenedores

*   **`docker compose stop`:**
    *   **Acción:** Detiene los contenedores en ejecución, pero **no los elimina**, ni tampoco elimina las redes o volúmenes creados.
    *   **Cuándo usar:** Si deseas pausar temporalmente el entorno para liberar recursos, pero planeas volver a trabajar pronto (`docker compose start` iniciará los mismos contenedores detenidos).
    *   **Ejemplo:** `docker compose stop`

*   **`docker compose down`:**
    *   **Acción:** Detiene **y elimina** los contenedores, y también elimina la red creada por `docker-compose`. **Los volúmenes por defecto no se eliminan** para preservar los datos (ej. la base de datos).
    *   **Cuándo usar:** Es la forma estándar de "apagar" el entorno al finalizar el trabajo. La próxima vez que ejecutes `docker compose up`, se crearán contenedores nuevos (pero los datos en el volumen `postgres_data` persistirán).
    *   **Ejemplo:** `docker compose down`

*   **`docker compose down -v`:**
    *   **Acción:** Realiza lo mismo que `docker compose down`, pero **adicionalmente elimina los volúmenes nombrados** definidos en `docker-compose.yml` (en nuestro caso, `postgres_data`).
    *   **Cuándo usar:** Si necesitas restablecer completamente el estado del entorno, incluyendo la **eliminación de todos los datos** en la base de datos PostgreSQL. ¡Ten cuidado!
    *   **Ejemplo:** `docker compose down -v`

*   **Detención en modo interactivo:** Si iniciaste los contenedores con `docker compose up` (sin `-d`), simplemente presiona `Ctrl+C` en la terminal donde se están ejecutando. Esto equivale a `docker compose stop`. Para eliminar los contenedores posteriormente, deberás ejecutar `docker compose down`.

### 📄 Visualización de Logs

Para ver los logs de la aplicación en ejecución (útil para depuración):

```bash
# Mostrar los logs del servicio de la aplicación y seguirlos en tiempo real
docker compose logs -f app

# Mostrar los logs de la base de datos
docker compose logs -f db
```
Presiona `Ctrl+C` para dejar de seguir los logs.

### 🗄️ Acceso a la Base de Datos

Puedes conectarte a la base de datos PostgreSQL que se ejecuta en Docker utilizando cualquier cliente SQL (ej. DBeaver, pgAdmin, Herramientas de Base de Datos de IntelliJ IDEA):

*   **Host:** `localhost`
*   **Puerto:** `5432` (el puerto mapeado desde el contenedor)
*   **Base de datos:** `db_parkify`
*   **Usuario:** `parkify`
*   **Contraseña:** `1234`

## 📖 Documentación de la API (Swagger)

Una vez que la aplicación esté iniciada correctamente, la documentación interactiva de la API (Swagger UI) estará disponible en:

`http://localhost:8080/swagger-ui/index.html`

Utiliza Swagger UI para explorar los endpoints disponibles, sus parámetros, cuerpos de solicitud y respuestas, así como para enviar solicitudes de prueba directamente desde el navegador. Para los endpoints protegidos, utiliza el botón "Authorize" e introduce el token JWT (obtenido a través del endpoint de login) en el formato `Bearer <tu_token>`.

## ✅ Ejecución de Pruebas (Tests)

Puedes ejecutar las pruebas utilizando Maven. El plugin estándar `maven-surefire-plugin` ejecuta las pruebas unitarias (generalmente clases que terminan en `Test`). El plugin `maven-failsafe-plugin` (si está configurado en `pom.xml`) se utiliza normalmente para ejecutar pruebas de integración (clases que terminan en `IT` o `ITest`).

```bash
# Ejecutar TODAS las pruebas (unitarias + integración, si failsafe está configurado)
# Este comando ejecuta las fases 'test' (surefire) y 'integration-test'/'verify' (failsafe)
mvn verify

# Ejecutar solo las pruebas unitarias (Surefire plugin)
mvn test

# Compilar el proyecto omitiendo todas las pruebas
mvn clean package -DskipTests
```

**Verificación de `pom.xml`:**

Es necesario revisar el archivo `pom.xml` para confirmar la presencia y configuración de los plugins `maven-surefire-plugin` y `maven-failsafe-plugin`.

*   **Surefire (Pruebas Unitarias):** Generalmente incluido por defecto. Busca `*Test.java`, `*Tests.java`, `*TestCase.java`.
*   **Failsafe (Pruebas de Integración):** Debe ser añadido y configurado explícitamente para ejecutarse en las fases `integration-test` y `verify`. Usualmente busca `*IT.java`, `*ITest.java`.

**Si `maven-failsafe-plugin` está configurado**, el comando `mvn verify` ejecutará ambos tipos de pruebas. Si no lo está, `mvn test` o `mvn verify` solo ejecutarán las pruebas unitarias. ¡Revisa el `pom.xml`!

## ⚙️ Variables de Entorno (`.env`)

El archivo `.env` se utiliza para configurar el entorno local de Docker Compose. Variables clave:

*   `SPRING_DATASOURCE_URL`: URL JDBC para conectar a la base de datos PostgreSQL dentro de la red Docker.
*   `SPRING_DATASOURCE_USERNAME`: Nombre de usuario de la BD.
*   `SPRING_DATASOURCE_PASSWORD`: Contraseña del usuario de la BD.
*   `SPRING_PROFILES_ACTIVE`: Perfil activo de Spring Boot (ej. `dev`). Permite usar configuraciones específicas del entorno (ej. `application-dev.properties`).

## 🛠️ Stack Tecnológico

*   Java 17
*   Spring Boot 3.x
*   Spring Security (Autenticación JWT)
*   Spring Data JPA (Hibernate)
*   PostgreSQL 15
*   Maven
*   Docker / Docker Compose
*   Lombok
*   Swagger (OpenAPI 3)
*   JUnit 5 / Mockito

## 📄 Licencia

Distribuido bajo la Licencia MIT. Consulta `LICENSE` para más información.
