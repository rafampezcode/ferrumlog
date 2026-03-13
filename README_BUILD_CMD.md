# Build y ejecución rápida (CMD)

En esta máquina, el comando que funciona directo es con Maven global (`mvn`).

## Comando único (copiar y pegar en CMD)

> Reemplaza la ruta de `JAVA_HOME` por la de tu JDK si es distinta.

```bat
mvn clean package spring-boot:run "-Dspring-boot.run.arguments=--server.port=3000"
```

Qué hace este comando:
- Compila el proyecto (`clean package`).
- Levanta la app en el puerto `3000`.

URL final:
- `http://localhost:3000`
