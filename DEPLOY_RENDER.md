# Despliegue recomendado: Render + PostgreSQL

Esta es la opción más equilibrada para FerrumLog si quieres una web pública con datos compartidos entre todos los usuarios.

## Qué resuelve

- La aplicación queda accesible desde internet.
- Los datos se guardan en una base PostgreSQL compartida.
- Cada despliegue recompila y publica automáticamente desde el repositorio.

## Archivos preparados

- `Dockerfile`: construye y empaqueta la app para Render.
- `docker-entrypoint.sh`: adapta la URL de PostgreSQL de Render al formato JDBC que espera Spring Boot.
- `render.yaml`: crea el servicio web y la base de datos PostgreSQL.

## Pasos en Render

1. Sube el proyecto a GitHub.
2. En Render, elige `New` -> `Blueprint`.
3. Conecta tu repositorio.
4. Render detectará `render.yaml` y te propondrá crear:
   - un servicio web `ferrumlog-web`
   - una base PostgreSQL `ferrumlog-db`
5. Acepta la creación y lanza el despliegue.

## Variables y perfiles

El despliegue ya queda configurado con:

- `SPRING_PROFILES_ACTIVE=postgresql`
- `DATABASE_URL` desde la base de datos de Render
- `SPRING_DATASOURCE_USERNAME` desde la base de datos de Render
- `SPRING_DATASOURCE_PASSWORD` desde la base de datos de Render

No hace falta hardcodear credenciales en el repositorio.

## Cómo arrancará la app

Render construye la imagen Docker y ejecuta la aplicación con:

- puerto dinámico usando `PORT`
- health check público en `/actuator/health`
- base PostgreSQL compartida

## Persistencia compartida

Los cambios de cualquier usuario se guardarán para todos porque ya no dependerán de H2 local, sino de la misma base remota PostgreSQL.

## Recomendación siguiente

Cuando el despliegue base funcione, el siguiente salto de calidad es introducir migraciones con Flyway para controlar los cambios de esquema de base de datos en producción.