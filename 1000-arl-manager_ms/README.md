## 1000-arl-manager_ms
### Arquitectura Hexagonal del microservicio modular propuesta

La estructura separa el dominio de los detalles de entrega e integración: `domain/model` define los conceptos e invariantes del negocio; `domain/usecase` coordina sus reglas mediante puertos; `infrastructure/entry-points` adapta las solicitudes externas hacia los casos de uso; y `infrastructure/driven-adapters` implementa las integraciones de salida, como persistencia y mensajería.

```text
1000-arl-manager_ms/
├── build.gradle
├── settings.gradle
├── applications/
│   └── app-service/                          # Módulo ensamblador (Spring Boot 3.3 WebFlux)
│       ├── build.gradle
│       └── src/main/
│           ├── java/co/com/sura/arl/presupuesto/
│           │   ├── MainApplication.java
│           │   └── config/                   # Configuración de beans de UseCases y TransactionalOperator
│           └── resources/
│               ├── application.yaml
│               └── db/migration/             # Scripts SQL de Flyway (DDL, CHECK, auditoría, outbox)
│
├── domain/
│   ├── model/                                # Módulo 100% puro (Java 21 + Reactor Mono/Flux)
│   │   ├── build.gradle                      # Sin dependencias de Spring, R2DBC, Jackson ni Security
│   │   └── src/main/java/co/com/sura/arl/presupuesto/model/
│   │       ├── presupuesto/
│   │       │   ├── PresupuestoAnual.java
│   │       │   └── gateways/PresupuestoAnualRepository.java
│   │       ├── plantrabajo/
│   │       │   ├── PlanTrabajo.java
│   │       │   ├── EstadoPlanTrabajo.java    # EN_PLANEACION, EN_EJECUCION, CERRADO
│   │       │   └── gateways/PlanTrabajoRepository.java
│   │       ├── ordenservicio/
│   │       │   ├── OrdenServicio.java
│   │       │   ├── ServicioPrevencion.java
│   │       │   ├── EstadoOrdenServicio.java  # PLANEADA, EJECUTADA, CANCELADA
│   │       │   └── gateways/OrdenServicioRepository.java
│   │       ├── evaluacion/
│   │       │   ├── EvaluacionRiesgo.java
│   │       │   └── gateways/EvaluacionRiesgoRepository.java
│   │       ├── seguridad/
│   │       │   ├── ContextoUsuario.java      # usuarioId, rol, grupoPrevencionId, empresaAfiliadaId
│   │       │   └── RolUsuario.java
│   │       ├── eventos/
│   │       │   ├── EventoPresupuesto.java    # PresupuestoReservado, Consumido, Liberado
│   │       │   └── gateways/PublicadorEventosGateway.java
│   │       └── exception/                    # Excepciones de dominio (RN-01..RN-10, CB-01..CB-06)
│   │           ├── NegocioException.java
│   │           ├── SaldoInsuficienteException.java
│   │           ├── EstadoInvalidoException.java
│   │           └── AccesoDenegadoGrupoException.java
│   │
│   └── usecase/                              # Orquestación pura de reglas de negocio
│       ├── build.gradle                      # Depende únicamente de :domain:model
│       └── src/
│           ├── main/java/co/com/sura/arl/presupuesto/usecase/
│           │   ├── presupuesto/
│           │   │   ├── AsignarPresupuestoUseCase.java
│           │   │   └── ConsultarPresupuestoEmpresaUseCase.java
│           │   ├── plantrabajo/
│           │   │   ├── ConsultarPlanTrabajoUseCase.java
│           │   │   └── ReajustarPlanTrabajoUseCase.java
│           │   ├── ordenservicio/
│           │   │   ├── PlanearOrdenServicioUseCase.java
│           │   │   ├── EjecutarOrdenServicioUseCase.java
│           │   │   └── CancelarOrdenServicioUseCase.java
│           │   └── evaluacion/
│           │       └── RegistrarEvaluacionRiesgoUseCase.java
│           └── test/java/co/com/sura/arl/presupuesto/
│               ├── databuilder/              # Patrón DataBuilder obligatorio para pruebas
│               └── usecase/                  # Tests unitarios puros con JUnit 5 y StepVerifier
│
└── infrastructure/
    ├── entry-points/                         # Puertos de entrada (APIs y tareas programadas)
    │   ├── reactive-web/                     # Handlers/Routers WebFlux alineados a openapi.yaml
    │   │   └── src/main/java/co/com/sura/arl/presupuesto/api/
    │   │       ├── handler/                  # PresupuestoHandler, OrdenServicioHandler, etc.
    │   │       ├── dto/                      # Request/Response records (OpenAPI 3.1.0)
    │   │       ├── security/                 # Filtro JWT -> ContextoUsuario y RBAC
    │   │       ├── idempotency/              # WebFilter X-Idempotency-Key (CB-01)
    │   │       └── exception/                # GlobalExceptionHandler RFC 7807 (CB-02)
    │   └── scheduler/                        # Job cierre anual 31-Dic (RN-04)
    │
    └── driven-adapters/                      # Puertos de salida (Persistencia y Mensajería)
        ├── r2dbc-postgresql/                 # Adaptadores Spring Data R2DBC
        │   └── src/
        │       ├── main/java/co/com/sura/arl/presupuesto/r2dbc/
        │       │   ├── entity/               # Entidades R2DBC con @Version
        │       │   ├── repository/           # Queries con SELECT ... FOR UPDATE (CB-04)
        │       │   ├── audit/                # Persistencia inmutable de auditoría
        │       │   └── outbox/               # Persistencia de eventos en la misma transacción
        │       └── test/java/                # Tests con Testcontainers (postgres:16-alpine)
        └── async-event-bus/                  # Publicador RabbitMQ con Reactive Commons
            └── src/
                ├── main/java/                # Relay Outbox -> RabbitMQ
                └── test/java/                # Tests con Testcontainers (rabbitmq:3.13-alpine)
```

## Ejecución local

### Requisitos

- JDK 21 instalado y disponible en `PATH` (`java -version`).
- No es necesario instalar Gradle: el repositorio incluye Gradle Wrapper 8.7.
- Docker solo será necesario cuando se incorporen pruebas de integración que usen Testcontainers.

Ejecuta los comandos desde la raíz del repositorio. En Windows, usa PowerShell:

```powershell
cd .\1000-arl-manager_ms
.\gradlew.bat projects
```

En Linux o macOS, usa `./gradlew` en lugar de `.\gradlew.bat`.

### Compilar y ejecutar

Compila todos los módulos:

```powershell
.\gradlew.bat build
```

Inicia la aplicación Spring Boot localmente:

```powershell
.\gradlew.bat :applications:app-service:bootRun
```

La aplicación escucha en el puerto `8080` por defecto. Detén el proceso con `Ctrl+C`.
En este estado del proyecto, el servicio es todavía una base de arranque: no hay
configuración de aplicación (`application.yaml`), endpoints REST ni conexión de base de
datos configurados. Por eso, iniciar el proceso no implica que las operaciones de negocio
o una API estén disponibles.

Para generar el JAR ejecutable:

```powershell
.\gradlew.bat :applications:app-service:bootJar
```

El artefacto se genera en
`applications/app-service/build/libs/`. Ejecútalo con:

```powershell
java -jar .\applications\app-service\build\libs\app-service-0.1.0-SNAPSHOT.jar
```

### Pruebas y calidad

Ejecuta todas las pruebas unitarias:

```powershell
.\gradlew.bat test
```

Ejecuta una clase de prueba específica (reemplaza el nombre por una clase existente):

```powershell
.\gradlew.bat test --tests "*PresupuestoAnualTest"
```

Los reportes de pruebas se generan por módulo en
`<modulo>/build/reports/tests/test/index.html`. Para generar el reporte de cobertura:

```powershell
.\gradlew.bat jacocoTestReport
```

El reporte HTML queda en `<modulo>/build/reports/jacoco/test/html/index.html`.
La verificación de cobertura mínima del 80 % forma parte de `check`; por tanto,
`.\gradlew.bat check` también ejecuta las pruebas, Spotless y la verificación de cobertura,
y puede fallar si un módulo aún no alcanza ese umbral.

Las pruebas de integración con PostgreSQL/RabbitMQ y Testcontainers descritas en el mapa
de arquitectura son parte de la arquitectura objetivo; actualmente no hay pruebas de
integración configuradas en esos adaptadores.