# Despliegue gratis: Render + Supabase

Esta guía explica cómo dejar FerrumLog publicado como web pública de forma
completamente gratuita, con una base de datos PostgreSQL compartida para todos
los usuarios.

---

## Qué vas a usar

| Servicio   | Qué hace                        | Coste  | Límite a tener en cuenta                          |
|------------|---------------------------------|--------|---------------------------------------------------|
| **GitHub** | Aloja el código fuente          | Gratis | —                                                 |
| **Supabase** | Base de datos PostgreSQL      | Gratis | Se pausa tras 1 semana sin actividad              |
| **Render** | Ejecuta la aplicación en la web | Gratis | La app duerme tras 15 min sin peticiones (arranque lento) |

> Si vas a mostrar el proyecto o usarlo activamente no notarás los límites.
> Para un uso real continuo 24/7 con usuarios, lo correcto es pasar al plan de pago.

---

## Paso 1 — Sube el código a GitHub

1. Ve a [github.com](https://github.com) y crea una cuenta si no tienes.
2. Crea un repositorio nuevo (puede ser privado).
3. En tu máquina, abre un terminal en la carpeta del proyecto y ejecuta:

```bash
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/TU_USUARIO/TU_REPO.git
git push -u origin main
```

---

## Paso 2 — Crea la base de datos en Supabase

1. Ve a [supabase.com](https://supabase.com) y crea una cuenta (gratis).
2. Haz clic en **New project**.
3. Rellena:
   - **Name**: `ferrumlog`
   - **Database Password**: elige una contraseña segura y **guárdala**
   - **Region**: elige la más cercana a Europa (p.ej. `Frankfurt`)
4. Espera a que el proyecto se cree (tarda 1-2 minutos).
5. Una vez creado, ve a **Project Settings → Database**.
6. En la sección **Connection string**, selecciona la pestaña **URI**.
7. Copia la cadena, que tendrá este aspecto:

```
postgresql://postgres:[TU_PASSWORD]@db.[PROJECT_REF].supabase.co:5432/postgres
```

Guarda esa URL porque la vas a necesitar en el Paso 3.

---

## Paso 3 — Despliega la app en Render

1. Ve a [render.com](https://render.com) y crea una cuenta (gratis).
2. En el dashboard haz clic en **New → Web Service**.
3. Conecta tu cuenta de GitHub y selecciona el repositorio de FerrumLog.
4. Rellena la configuración:

| Campo            | Valor                                    |
|------------------|------------------------------------------|
| **Name**         | `ferrumlog`                              |
| **Region**       | `Frankfurt (EU Central)`                 |
| **Branch**       | `main`                                   |
| **Runtime**      | `Docker`                                 |
| **Instance Type**| `Free`                                   |

5. Haz clic en **Advanced** y añade estas variables de entorno una a una:

| Variable de entorno         | Valor                                                  |
|-----------------------------|--------------------------------------------------------|
| `SPRING_PROFILES_ACTIVE`    | `postgresql`                                           |
| `SPRING_DATASOURCE_URL`     | `jdbc:postgresql://db.[PROJECT_REF].supabase.co:5432/postgres` |
| `SPRING_DATASOURCE_USERNAME`| `postgres`                                             |
| `SPRING_DATASOURCE_PASSWORD`| `[TU_PASSWORD de Supabase]`                            |
| `JAVA_OPTS`                 | `-Xms256m -Xmx512m`                                   |

> Sustituye `[PROJECT_REF]` y `[TU_PASSWORD]` con los valores reales de tu
> proyecto Supabase del Paso 2.

6. Haz clic en **Create Web Service**.

Render construirá la imagen Docker automáticamente (tarda unos 3-5 minutos la
primera vez).

---

## Paso 4 — Accede a la aplicación

Cuando el despliegue termine, Render te dará una URL pública con este formato:

```
https://ferrumlog.onrender.com
```

Entra a esa URL, regístrate y prueba la app.

---

## Cómo funciona el flujo completo

```
Navegador del usuario
       │
       ▼
https://ferrumlog.onrender.com   ← Render (gratis)
       │
       ▼
Spring Boot (Docker en Render)
       │
       ▼
PostgreSQL en Supabase (gratis) ← datos compartidos para todos
```

---

## Actualizaciones futuras

Cada vez que hagas cambios en el código y los subas a GitHub:

```bash
git add .
git commit -m "tu descripción del cambio"
git push
```

Render detectará el push y redesplegará la aplicación automáticamente.

---

## Preguntas frecuentes

**¿Por qué la app tarda en cargar la primera vez?**

El plan gratuito de Render apaga la instancia cuando no hay tráfico. El primer
acceso tras un periodo de inactividad puede tardar 30-60 segundos mientras
arranca. A partir de ahí responde con normalidad.

**¿Los datos se pierden si no uso la app una semana?**

Los datos de la base de datos en Supabase no se pierden, pero Supabase pausa
el proyecto si no hay actividad SQL durante 7 días. Para reactivarlo entra al
dashboard de Supabase y haz clic en **Restore project**. Los datos siguen ahí.

**¿Cómo cambio la contraseña de la base de datos?**

En el dashboard de Supabase ve a **Project Settings → Database → Reset database
password**. Después actualiza la variable `SPRING_DATASOURCE_PASSWORD` en
Render en **Environment → Edit** y redespliega.

**¿Puedo añadir un dominio propio?**

Sí. En Render ve a tu servicio → **Custom Domains** → añade tu dominio. Render
gestiona el certificado HTTPS automáticamente.
