# Microservicio de control presupuestal

Backend de la plataforma de control presupuestal de prevención. El proyecto está organizado como un monolito modular con separación hexagonal entre dominio,
puntos de entrada, adaptadores y aplicación ensambladora.

La arquitectura y las capacidades objetivo están descritas en
[`docs/03-diseno-tecnico.md`](../docs/03-diseno-tecnico.md), las historias en
[`docs/historias/`](../docs/historias/) y el contrato API en
[`docs/anexos/openapi.yaml`](../docs/anexos/openapi.yaml).

### Tecnologías configuradas

- Java 21 y Gradle Wrapper 8.7.
- Spring Boot 3.3 y Spring WebFlux.
- Project Reactor (`Mono`/`Flux`).
- Spring Data R2DBC y PostgreSQL.
- Flyway para la migración inicial del esquema.
- JUnit 5, AssertJ, Mockito, Reactor Test y Testcontainers para pruebas.
- Docker con el plugin de Docker Compose para iniciar PostgreSQL local.

RabbitMQ, los endpoints de negocio y la configuración de despliegue hacen parte del diseño objetivo; aún no están implementados en este microservicio.

## Módulos

```text
1000-arl-manager_ms/
├── applications/
│   └── app-service/                 # Ensamblador y aplicación Spring Boot
├── domain/
│   ├── model/                       # Modelo de dominio sin dependencias Spring
│   └── usecase/                     # Casos de uso y puertos
└── infrastructure/
    ├── entry-points/
    │   └── reactive-web/            # Módulo WebFlux
    └── driven-adapters/
        └── r2dbc-postgresql/        # Adaptador PostgreSQL/R2DBC y migraciones Flyway
```

`scheduler` y `async-event-bus` están en la arquitectura objetivo, pero aún no son módulos implementados. El módulo `reactive-web` tampoco contiene handlers
REST por ahora.

## Requisitos

- JDK 21 disponible en `PATH` (`java -version`).
- Docker en ejecución para PostgreSQL local y para las pruebas de integración
  que usan Testcontainers.
- No es necesario instalar Gradle: usa el wrapper incluido.

Ejecuta los comandos desde la carpeta `1000-arl-manager_ms`. En Windows,
PowerShell:

```powershell
cd .\1000-arl-manager_ms
.\gradlew.bat projects
```

En Linux o macOS, usa `./gradlew` en lugar de `.\gradlew.bat`.

## Compilar y ejecutar

Inicia PostgreSQL local desde esta carpeta:

```powershell
docker compose up -d postgres
```

Compose crea la base `arl_presupuesto` con el usuario y la contraseña locales
`postgres`, y conserva los datos en un volumen. La configuración de Flyway
ejecuta las migraciones al iniciar la aplicación. Para detener la base sin
borrar los datos:

```powershell
docker compose down
```

La configuración local de la aplicación usa estas conexiones:

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/arl_presupuesto
    username: postgres
    password: postgres
  flyway:
    url: jdbc:postgresql://localhost:5432/arl_presupuesto
    user: postgres
    password: postgres
```

Para conectarte desde un cliente SQL, usa host `localhost`, puerto `5432`, base de datos `arl_presupuesto` y esquema `public`. La base `postgres` no es la
base de la aplicación. Estas credenciales son solo para desarrollo local.

Compila todos los módulos:

```powershell
.\gradlew.bat build
```

Inicia la aplicación de arranque:

```powershell
.\gradlew.bat :app-service:bootRun
```

La aplicación escucha en el puerto `8080` por defecto. Detén el proceso con
`Ctrl+C`. El arranque no implica que la API o las operaciones de negocio estén
disponibles: todavía no hay endpoints REST.

Para generar y ejecutar el JAR:

```powershell
.\gradlew.bat :app-service:bootJar
java -jar .\applications\app-service\build\libs\app-service-0.1.0-SNAPSHOT.jar
```

## Pruebas y calidad

Ejecuta todas las pruebas:

```powershell
.\gradlew.bat test
```

Las pruebas de integración de `infrastructure:driven-adapters` usan
Testcontainers y requieren Docker. Para ejecutar solo las pruebas de ese módulo:

```powershell
.\gradlew.bat :r2dbc-postgresql:test
```

Para ejecutar una clase específica, reemplaza el patrón por el nombre de una
clase de prueba existente:

```powershell
.\gradlew.bat test --tests "*PresupuestoAnualR2dbcAdapterTest"
```

Los reportes de pruebas se generan en
`<modulo>/build/reports/tests/test/index.html`. Para generar los reportes de
cobertura JaCoCo:

```powershell
.\gradlew.bat jacocoTestReport
```

El reporte HTML queda en `<modulo>/build/reports/jacoco/test/html/index.html`. La tarea `check` ejecuta Spotless y verifica una cobertura mínima del 80 % por módulo; puede fallar mientras algún módulo no alcance ese umbral.
