🚀 Guía Rápida para Ejecutar el Proyecto Backend en Docker
Este documento proporciona instrucciones paso a paso para ejecutar el entorno de desarrollo de la aplicación Spring Boot con MySQL utilizando Docker. Ideal para programadores backend.

📦 Requisitos Previos
Antes de ejecutar el proyecto, asegúrate de tener lo siguiente instalado en tu máquina:

Docker: Descargar Docker

Docker Compose: Viene incluido con Docker Desktop.

Git: Si aún no tienes el proyecto, puedes clonarlo desde GitHub.

🧑‍💻 Instalación y Configuración
Clonar el Repositorio (si aún no lo has hecho):

bash
git clone https://github.com/usuario/proyecto.git
cd proyecto

Asegurarte de tener la última versión de dependencias:

Si no tienes el archivo target/*.jar, primero debes construir el proyecto localmente. Esto se hace automáticamente al ejecutar docker-compose por primera vez. Solo en caso de que lo necesites, puedes usar Maven manualmente:

bash
mvn clean install

🔧 Levantar el Entorno de Desarrollo
Ejecutar el proyecto con Docker Compose:

En la raíz del proyecto, donde se encuentra el archivo docker-compose.yml, ejecuta el siguiente comando para construir las imágenes de Docker y levantar los contenedores:

bash
docker compose up -d --build

Esto descargará las imágenes necesarias, construirá la imagen de la aplicación Spring Boot y levantará tanto el contenedor de MySQL como el de la app.

Opción -d: Ejecuta todo en segundo plano.

Verificar que los contenedores están corriendo:

Puedes verificar el estado de los contenedores con el siguiente comando:

bash
docker ps

Este comando te mostrará los contenedores activos, incluyendo el contenedor de la base de datos MySQL y la app Spring Boot.

🌐 Acceder a la Aplicación
La aplicación Spring Boot estará disponible en el puerto 8080. Para verificar que está funcionando:

Abre tu navegador y visita:
http://localhost:8080

Deberías ver la respuesta de tu aplicación Spring Boot. Si todo está bien, ¡la app está corriendo!

🛠️ Ver Logs de los Contenedores
Si necesitas ver más detalles o investigar posibles errores en los contenedores, puedes acceder a los logs con:

bash
docker compose logs -f

Para solo ver los logs de la aplicación Spring Boot:

bash
docker compose logs -f app

🧯 Solucionar Problemas Comunes
Error de conexión a la base de datos:
Si ves errores relacionados con la base de datos (por ejemplo, conexión rechazada), asegúrate de que MySQL se haya iniciado correctamente antes de que Spring Boot intente conectarse. Puedes esperar unos segundos más y volver a intentar.

Contenedor de la aplicación no se inicia:
Verifica los logs para más detalles:

bash
docker compose logs -f spring_boot_app

Interrumpir la ejecución:
Para detener los contenedores sin borrar los volúmenes persistentes:

bash
docker compose down

Limpiar contenedores y volúmenes (si es necesario):
Si necesitas limpiar todos los contenedores y volúmenes:

bash
docker compose down -v

📚 Notas Importantes
Variables de entorno:

SPRING_DATASOURCE_URL: URL para conectarse a MySQL (utiliza el contenedor mysql_db).

SPRING_DATASOURCE_USERNAME: Usuario para acceder a la base de datos (definido como parkify).

SPRING_DATASOURCE_PASSWORD: Contraseña para el usuario parkify.

SPRING_JPA_HIBERNATE_DDL_AUTO: Configuración de JPA (en este caso, update).

Base de datos:
El contenedor de MySQL se configura para crear una base de datos llamada db_parkify y un usuario parkify con la contraseña 1234.

📝 Conclusión
¡Listo! Ahora tienes un entorno Dockerizado corriendo tu aplicación Spring Boot y la base de datos MySQL. Cada vez que quieras levantar el proyecto, solo tienes que ejecutar docker compose up -d y la app estará funcionando en minutos.