## SURA – Proyecto GRL (Control Presupuestal de Prevención)

Monorepo propuesta para la plataforma de control presupuestal de prevención.

### Estructura
- `1000-arl-manager_ms/`: backend reactivo con Java 21, Spring Boot 3.3, WebFlux, R2DBC/PostgreSQL y RabbitMQ. Usa arquitectura hexagonal con los módulos `domain` (`model` y `usecase`), `infrastructure` (`driven-adapters` y `entry-points`) y `applications`.
- `1001-arl-manager_fe/`: frontend en Angular standalone con NgRx Signal Store.
- `1002-arl-manager_tf/`: infraestructura en Azure con Terraform y manifiestos de AKS.

### SDD (Spec-Driven Development) y entregables del diseño
Se encuentran consolidados en el directorio `docs/`, son la única fuente de verdad y se lee en orden:
1. [SDD: Contexto](docs/01-contexto.md): lenguaje ubicuo, reglas `RN-01` a `RN-10` y casos borde `CB-01` a `CB-05`.
2. [SDD: Requerimientos](docs/02-requerimientos.md): requerimientos.
3. [SDD: Diseño técnico](docs/03-diseno-tecnico.md): Diseño técnico.
4. [SDD: Tareas (Historias técnicas)](docs/04-tareas.md): Plan en 4 fases, con las historias técnicas HT-001 a HT-025.
5. [Historias de usuario](docs/historias/): HU-1 (reservar presupuesto), HU-2 (ejecutar/cancelar orden) y HU-3 (consultar saldos).
6. [OpenApi 3.X (Swagger)](docs/anexos/openapi.yaml): contrato API, obligatorio entre frontend y backend.
7. [Entregables](docs/entregables/): Documentos formato PDF relativos a la propuesta de solución y diseño técnico.

El flujo agéntico describe cómo los agentes interactúan con el sistema siguiendo las instrucciones definidas y aplicando el SDD. El cual se construyó de manera independiente enriquecido por la información del negocio y los requerimientos de la solución.

Los elementos relativos a entregables se encuentran en `docs/entregables/` y comprenden documentos en formato PDF que detallan el entendimiento del negocio, la disposición de la solución y los diagramas técnicos en formato C4 (Diagrama de Contexto, Contenedor, Componente) y Modelo DDD.

### Respecto al flujo agéntico y uso de Inteligencia Artificial
La inteligencia artificial para la elaboración del presente monorepo, fue usado de manera complementaria al flujo de trabajo y elaboración de la propuesta técnica. Se documenta con su uso desde las etapas iniciales del proceso para impulsar la generación de artefactos y cumplimiento del SDD. Acorde a ello, dado que se tiene todo el soporte configurado, se procede a utilizar las herramientas del Harness Copilot, para la ejecución de las Skills basados en buenas prácticas y lineamientos de equipos definidos, para conservar la calidad y coherencia en el desarrollo del proyecto.

- `copilot-instructions.md` define el contexto global y las reglas transversales.
- Hay instrucciones por capa en `.github/instructions/`: `backend`, `frontend` y `terraform`. Cada una se aplica según la carpeta que se edite.
- El skill `dev-agent` ejecuta paso a paso una historia técnica de `docs/04-tareas.md`. Aplica SDD y TDD estricto (JUnit 5/Mockito, patrón AAA, DataBuilders), tipado explícito y Spotless/Lint. Pausa en puntos de control con el humano (human-in-the-loop).