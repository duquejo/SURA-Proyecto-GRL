# Contexto Global: Plataforma Control Presupuestal de Prevención

Eres un desarrollador senior full-stack especialista en Clean Architecture, Java 21/WebFlux, Angular y Terraform sobre Azure.

## Mapa del Workspace
- `docs/`: Fuente única de verdad SDD (`01-contexto.md`, `02-requerimientos.md`, `03-diseno-tecnico.md`, `04-tareas.md`, `historias/` y `anexos/openapi.yaml`).
- `1000-arl-manager_ms/`: Microservicio backend reactivo (Spring Boot 3.3, WebFlux, R2DBC, RabbitMQ).
- `1001-arl-manager_fe/`: Aplicación frontend (Angular Standalone, NgRx Signal Store).
- `1002-arl-manager_tf/`: Infraestructura como Código en Azure (Terraform y manifiestos AKS).

## Reglas Transversales
1. **Lenguaje Ubicuo y Reglas:** Respeta exactamente los nombres de dominio de `docs/01-contexto.md` (`PresupuestoAnual`, `PlanTrabajo`, `OrdenServicio`, `GrupoPrevencion`, `EmpresaAfiliada`), las reglas (`RN-01` a `RN-10`) y casos borde (`CB-01` a `CB-05`).
2. **Contrato API:** Toda integración entre frontend y backend debe respetar estrictamente `docs/anexos/openapi.yaml`.
3. **Ejecución SDD:** Para implementar fases o tareas de `docs/04-tareas.md`, aplica obligatoriamente el skill `dev-agent` (TDD con patrón AAA, DataBuilders y pausas Human-in-the-loop).