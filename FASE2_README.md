# FerrumLog - Fase 2 Completada

## 📋 Resumen de Implementación

Esta es la **Fase 2** del proyecto FerrumLog, implementando Spring Security 6, capa de servicios completa, validaciones Jakarta, controladores CRUD y configuración multi-perfil para bases de datos.

---

## 🔐 **1. Spring Security 6 con Lambda DSL**

### Componentes Implementados:

#### **SecurityConfig** (`config/SecurityConfig.java`)
- ✅ SecurityFilterChain con **Lambda DSL** (patrón moderno de Spring Security 6)
- ✅ BCryptPasswordEncoder para cifrado de contraseñas
- ✅ Rutas públicas: `/`, `/register`, `/css/**`, `/js/**`, `/images/**`
- ✅ Rutas protegidas: Todo el resto requiere autenticación
- ✅ Login personalizado en `/login`
- ✅ Logout con invalidación de sesión
- ✅ Consola H2 habilitada para desarrollo

#### **CustomUserDetails** (`security/CustomUserDetails.java`)
- ✅ Implementa `UserDetails` de Spring Security
- ✅ Conectado con la entidad `User`
- ✅ Proporciona autoridades (ROLE_USER)
- ✅ Métodos helper: `getUserId()`, `getUser()`

#### **CustomUserDetailsService** (`security/CustomUserDetailsService.java`)
- ✅ Implementa `UserDetailsService`
- ✅ Carga usuarios desde la base de datos via `UserRepository`
- ✅ Transaccional y optimizado

---

## 🏗️ **2. Capa de Servicios (Arquitectura en Capas)**

### Interfaces y Implementaciones:

#### **UserService / UserServiceImpl**
- ✅ Registro de usuarios con contraseña cifrada
- ✅ Validación de username/email duplicados
- ✅ Búsqueda por username

#### **ExerciseService / ExerciseServiceImpl**
- ✅ CRUD completo de ejercicios
- ✅ Búsqueda por nombre o grupo muscular
- ✅ Paginación de ejercicios

#### **RoutineService / RoutineServiceImpl**
- ✅ CRUD de rutinas con validación de propiedad
- ✅ Solo el usuario propietario puede ver/editar/eliminar sus rutinas
- ✅ Asignación automática del usuario autenticado

#### **WorkoutService / WorkoutServiceImpl**
- ✅ **MÉTODO CRÍTICO**: `getLastWorkoutSet(userId, exerciseId)`
  - Recupera el último peso/reps/RPE registrado
  - Implementa el principio de **sobrecarga progresiva**
  - Optimizado con query derivada de Spring Data JPA
- ✅ Registro de series de entrenamiento
- ✅ Creación automática de WorkoutLog diario

---

## ✅ **3. Jakarta Validation en DTOs**

Todos los DTOs tienen validaciones apropiadas:

- **UserRegistrationDto**: `@NotBlank`, `@Email`, `@Size`
- **ExerciseDto**: `@NotBlank`, `@Size`
- **RoutineDto**: `@NotBlank`, `@Size`
- **WorkoutSetDto**: `@NotNull`, `@Min`
- **WorkoutLogDto**: `@NotNull`, `@Valid`

Los controladores aplican `@Valid` y manejan `BindingResult` para mostrar errores en formularios.

---

## 🎮 **4. Controladores CRUD con Seguridad**

### **AuthController**
- ✅ `/register` (GET/POST) - Registro de usuarios
- ✅ `/login` (GET) - Formulario de login
- ✅ Validación de campos con `@Valid`
- ✅ Manejo de errores de negocio

### **ExerciseController**
- ✅ `/exercises` - Lista con paginación
- ✅ `/exercises/new` - Crear ejercicio
- ✅ `/exercises/{id}/edit` - Editar ejercicio
- ✅ `/exercises/{id}/delete` - Eliminar ejercicio
- ✅ `/exercises/search` - Búsqueda de ejercicios

### **RoutineController**
- ✅ `/routines` - Lista de rutinas del usuario
- ✅ `/routines/new` - Crear rutina
- ✅ `/routines/{id}/edit` - Editar rutina
- ✅ `/routines/{id}/delete` - Eliminar rutina
- ✅ **Asignación automática con SecurityContextHolder**
- ✅ Validación de propiedad (solo el dueño puede modificar)

### **WorkoutController**
- ✅ `/workouts/record` - Registrar serie
- ✅ **`/workouts/last-set?exerciseId=X`** - Endpoint JSON crítico
  - Devuelve el último WorkoutSet del usuario
  - Consumible desde AJAX/Fetch en frontend
- ✅ Asignación automática del usuario autenticado

### **Método Helper: getCurrentUserId()**
Implementado en `RoutineController` y `WorkoutController`:
```java
private Long getCurrentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth.getPrincipal() instanceof CustomUserDetails details) {
        return details.getUserId();
    }
    throw new RuntimeException("Usuario no autenticado");
}
```

---

## 🗄️ **5. Configuración Multi-Perfil (Bases de Datos)**

### Perfiles Disponibles:

#### **1. dev (H2 en memoria) - Por defecto**
```properties
spring.profiles.active=dev
```
- Base de datos en memoria
- Consola H2: `http://localhost:8080/h2-console`
- DDL: `create` (destruye datos cada reinicio)
- Ideal para desarrollo rápido

#### **2. mysql (MySQL)**
```properties
spring.profiles.active=mysql
```
- Conector MySQL 8+
- DDL: `update` (actualiza esquema sin perder datos)
- Pool de conexiones optimizado (HikariCP)
- Requiere crear base de datos:
```sql
CREATE DATABASE ferrumlog_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### **3. postgresql (PostgreSQL) - Recomendado para producción**
```properties
spring.profiles.active=postgresql
```
- Dialecto PostgreSQL
- DDL: `update`
- Optimizaciones de batch para PostgreSQL
- Requiere crear base de datos:
```sql
CREATE DATABASE ferrumlog_db
    WITH OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'es_ES.UTF-8'
    LC_CTYPE = 'es_ES.UTF-8';
```

### Cambiar de Base de Datos:

**Opción 1: Modificar `application.properties`**
```properties
spring.profiles.active=mysql
```

**Opción 2: Variable de entorno**
```bash
export SPRING_PROFILES_ACTIVE=postgresql
```

**Opción 3: Argumento de línea de comandos**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

**Opción 4: En IDE (IntelliJ/Eclipse)**
- Run Configuration → VM Options: `-Dspring.profiles.active=postgresql`

### Credenciales de Base de Datos:
Usar variables de entorno para seguridad:
```bash
export DB_USERNAME=ferrumlog
export DB_PASSWORD=tu_password_seguro
mvn spring-boot:run
```

---

## 📊 **Arquitectura Implementada**

```
┌─────────────────────────────────────────────────────┐
│                   FRONTEND                          │
│             (Thymeleaf Templates)                   │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│              CONTROLLERS                            │
│  AuthController │ ExerciseController │ RoutineController │ WorkoutController
│  - @Valid       │ - CRUD            │ - CRUD + Security │ - Last Set API   │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│               SERVICES                              │
│  UserService │ ExerciseService │ RoutineService │ WorkoutService           │
│  - Business Logic                                   │
│  - Transaction Management                           │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│             REPOSITORIES                            │
│  UserRepository │ ExerciseRepository │ RoutineRepository │ WorkoutSetRepo  │
│  - Spring Data JPA                                  │
│  - Query Methods / @Query                           │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│               DATABASE                              │
│   H2 / MySQL / PostgreSQL                           │
└─────────────────────────────────────────────────────┘
```

---

## 🚀 **Cómo Ejecutar**

### 1. Clonar y compilar
```bash
cd FERRUMLOG
mvn clean install
```

### 2. Ejecutar con H2 (desarrollo)
```bash
mvn spring-boot:run
```
Acceder a:
- App: `http://localhost:8080`
- H2 Console: `http://localhost:8080/h2-console`

### 3. Ejecutar con MySQL
```bash
# Crear base de datos primero
mysql -u root -p
CREATE DATABASE ferrumlog_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
exit;

# Ejecutar app
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

### 4. Ejecutar con PostgreSQL
```bash
# Crear base de datos primero
psql -U postgres
CREATE DATABASE ferrumlog_db ENCODING 'UTF8';
\q

# Ejecutar app
export SPRING_PROFILES_ACTIVE=postgresql
mvn spring-boot:run
```

---

## 🔑 **Endpoints Principales**

### Públicos:
- `GET /` - Home
- `GET /register` - Formulario de registro
- `POST /register` - Procesar registro
- `GET /login` - Formulario de login

### Protegidos (requieren autenticación):
- `GET /dashboard` - Dashboard del usuario
- `GET /exercises` - Lista de ejercicios
- `GET /routines` - Rutinas del usuario
- `POST /workouts/record` - Registrar serie
- **`GET /workouts/last-set?exerciseId=X`** - API JSON para último set

---

## 🎯 **Funcionalidades CRÍTICAS Implementadas**

### 1. **Sobrecarga Progresiva**
El método `WorkoutService.getLastWorkoutSet()` permite al usuario ver su último registro:
```java
Optional<WorkoutSetDto> lastSet = workoutService.getLastWorkoutSet(userId, exerciseId);
// Devuelve: peso, reps, RPE del último entrenamiento
```

### 2. **Seguridad por Usuario**
Todas las operaciones verifican propiedad:
```java
if (!routine.getUser().getId().equals(userId)) {
    throw new SecurityException("No autorizado");
}
```

### 3. **Validación en Todos los Niveles**
- DTO: `@NotBlank`, `@NotNull`, `@Min`, `@Size`
- Controller: `@Valid` + `BindingResult`
- Service: Validaciones de negocio
- Repository: Constraints de base de datos

---

## 📝 **Código Limpio y Mejores Prácticas**

✅ **Sin código obsoleto** - Solo APIs modernas de Spring Boot 3.2 y Spring Security 6  
✅ **Lambda DSL** - Patrón recomendado para SecurityFilterChain  
✅ **Separation of Concerns** - Capas bien definidas (Controller → Service → Repository)  
✅ **Transacciones** - `@Transactional` en servicios  
✅ **Logging** - SLF4J con Lombok `@Slf4j`  
✅ **Validaciones** - Jakarta Validation en DTOs  
✅ **Inmutabilidad** - Records para DTOs  
✅ **Seguridad** - BCrypt, SecurityContextHolder, validación de propiedad  

---

## 🧱 **Sesion Actual - Cambios Realizados**

### **Rediseño UI (tema gimnasio profesional)**
- Actualizada la landing con estetica oscura, acentos naranja y texto de alto contraste.
- Nuevo layout hero, botones principales/secondary, tarjetas y footer alineados al branding.
- Estilos consistentes con fondos oscuros, tipografia en mayusculas y mejor legibilidad.

Archivos:
- [src/main/resources/templates/index.html](src/main/resources/templates/index.html)

### **Login y Registro (tema gimnasio)**
- Formularios modernizados con contraste alto y acentos naranjas.
- Tipografia y botones alineados con el branding.
- Form actions y bindings Thymeleaf ajustados previamente para Spring Security.

Archivos:
- [src/main/resources/templates/login.html](src/main/resources/templates/login.html)
- [src/main/resources/templates/register.html](src/main/resources/templates/register.html)

### **Dashboard (tema gimnasio + contraste de tabla)**
- Fondo general y tarjetas con estilo oscuro y bordes con acento naranja.
- Sidebar rediseñada con estados hover/active visibles.
- Estadisticas con gradientes naranja y sombras.
- Tabla de "Ultimos Entrenamientos" con fondo opaco y texto blanco para contraste.

Archivos:
- [src/main/resources/templates/dashboard.html](src/main/resources/templates/dashboard.html)

---

## 🔧 **Próximos Pasos (Fase 3)**

1. **Frontend Completo** - Completar templates Thymeleaf
2. **REST API** - Crear controladores REST para SPA/Mobile
3. **Tests** - JUnit 5 + MockMvc + Testcontainers
4. **Migraciones** - Flyway o Liquibase para control de versiones de BD
5. **Estadísticas** - Gráficos de progreso con Chart.js
6. **Exportación** - Excel/PDF de entrenamientos

---

## 👨‍💻 **Autor**

Implementado siguiendo principios de Arquitectura de Software Senior con Spring Boot 3.4 y Spring Security 6.

**FerrumLog - Tu historial de entrenamientos, siempre a mano.** 💪
