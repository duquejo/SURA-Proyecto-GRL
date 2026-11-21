---
name: sdd-phase-executor
description: Ejecuta fases y tareas de implementación desde especificaciones SDD (docs/04-tareas.md) bajo Clean Architecture Hexagonal, TDD estricto con patrón AAA/DataBuilder/Testcontainers y puntos de control Human-in-the-loop. Usar cuando se pida iniciar o ejecutar una fase de desarrollo, implementar tareas del plan o construir historias de usuario desde las specs.
---

# SDD Phase Executor

Orquesta la implementación controlada del plan definido en `docs/04-tareas.md`, respetando las reglas arquitectonicas definidas en `CLAUDE.md` y aplicando TDD estricto con puntos de validación humana (Human-in-the-loop).

## Paso 1: Seleccion de Fase y Alcance (Gate Inicial)

- Lee **unicamente** `docs/04-tareas.md` para identificar el estado actual.
- Si el usuario no especifico que fase o tarea ejecutar, **detente y pregunta primero** cual fase (`Fase 1`, `Fase 2`, `Fase 3` o `Fase 4`) o tarea puntual desea abordar.
- Lista las subtareas incluidas y confirma el alcance. Una vez confirmado, lee **sólo** las secciones especificas de `01-contexto.md`, `02-requerimientos.md`, `03-diseno-tecnico.md`, `historias/` y `anexos/openapi.yaml` estrictamente necesarias para esa tarea.

## Paso 2: Ciclo TDD Estricto (Red - Green - Refactor)

Ejecuta **una sola tarea o historia de usuario a la vez** siguiendo este orden:

1. **RED (Pruebas primero bajo patron AAA y DataBuilder):**
   - Escribe primero las pruebas en el módulo correspondiente:
     - **Backend (`domain`):** Unitarias puras con `JUnit 5`, `StepVerifier` y mocks/stubs (sin Spring ni Docker).
     - **Backend (`infrastructure`):** Integracion con `@SpringBootTest` y **Testcontainers** (`postgres:16-alpine`, `rabbitmq:3.13-alpine`) con `@ServiceConnection`. Prohibido H2.
     - **Frontend (`1001-arl-manager_fe`):** Unitarias con **Vitest** (`vi.fn()`) para Standalone Components, servicios y NgRx Signal Store.
     - **Cloud (`1002-arl-manager_tf`):** `terraform fmt -check` y `terraform validate`.
   - **Patron AAA obligatorio:** Estructura cada test en tres bloques comentados (`// Arrange`, `// Act`, `// Assert`).
   - **Patron DataBuilder obligatorio:** Reutiliza constructores en `src/test/java/.../databuilder/` y `*.builder.ts` para consolidar escenarios, mocks y stubs.
   - Ejecuta **sólo la prueba recién creada** en modo silencioso (`./1000-arl-manager_ms/gradlew -p 1000-arl-manager_ms test --tests "*NombreTest" -q` o `npx --prefix 1001-arl-manager_fe vitest run ruta/archivo.spec.ts`) y verifica que falle.

2. **GREEN (Implementación minima):**
   - Escribe el codigo mínimo necesario en la capa correspondiente para satisfacer las pruebas y `docs/anexos/openapi.yaml`.
   - Ejecuta **sólo la prueba focalizada** hasta que pase en verde.

3. **REFACTOR (Limpieza y verificación completa):**
   - Refina nombres, inmutabilidad y errores RFC 7807 (`CB-02` con `codigoRegla`), consolidando variantes en los `TestDataBuilder`.
   - Verifica ausencia de imports de infraestructura en `domain` y de `.block()` en codigo productivo.
   - Ejecuta ahora si la suite completa del módulo (`./1000-arl-manager_ms/gradlew -p 1000-arl-manager_ms test -q` o `npx --prefix 1001-arl-manager_fe vitest run`) para confirmar cero regresiones.

## Paso 3: Puntos de Control Human-in-the-loop (HITL)

**Detente obligatoriamente** y solicita revision y aprobación explicita del usuario en cada uno de estos hitos:

- **Gate de Esquema de Datos:** Al generar scripts SQL de Flyway (Tarea `1.3`) con restricciones `CHECK` e indices, antes de continuar con los adaptadores.
- **Gate por Tarea o Historia de Usuario:** Al finalizar el ciclo TDD completo de cada tarea o historia (`HU-1`, `HU-2`, `HU-3`), antes de iniciar la siguiente.
- **Gate de Infraestructura Cloud:** Al generar plantillas de Terraform o Helm en `1002-arl-manager_tf`, antes de ejecutar cualquier comando de aprovisionamiento (`terraform apply` o despliegues en AKS).

En cada pausa Human-in-the-loop entrega:
1. Lista de archivos creados o modificados (incluyendo los `TestDataBuilder` actualizados).
2. Evidencia de ejecucion de pruebas y reglas (`RN-*` / `CB-*`) validadas.
3. Propuesta de mensaje de `git commit` atomico para guardar el punto de restauracion limpio.

## Paso 4: Cierre de tarea

- Solo despues de recibir aprobación del usuario, actualiza el checkbox a `- [x]` en `docs/04-tareas.md` y pregunta si desea continuar con la siguiente tarea de la fase.