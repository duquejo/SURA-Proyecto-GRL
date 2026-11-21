---
applyTo: "1001-arl-manager_fe/**"
---

# Reglas Frontend: Angular Standalone & Signals

## Arquitectura y Estado
- **Componentes:** Usa exclusivamente **Standalone Components** con `ChangeDetectionStrategy.OnPush`, `input()`, `output()` y control flow nativo (`@if`, `@for`).
- **Estado Reactivo:** Gestiona el estado por dominio mediante **NgRx Signal Store** (`signalStore`, `withState`, `withComputed`, `withMethods`), evitando suscripciones manuales innecesarias.
- **Integración API:** Tipa DTOs según `docs/anexos/openapi.yaml`. Envía `X-Idempotency-Key` (UUID v4) en toda operación de creación, ejecución o cancelación de órdenes y mapea errores RFC 7807 (`codigoRegla`) a mensajes claros de negocio.

## Pruebas con Vitest y Comandos
- **Framework:** Usa **Vitest** (`describe`, `it`, `expect`, `vi.fn()`) en modo no interactivo.
- **Estándar:** Estructura cada `it(...)` con comentarios `// Arrange`, `// Act`, `// Assert` y centraliza datos de prueba en archivos `*.builder.ts`.
- **Ejecución focalizada:** `npx --prefix 1001-arl-manager_fe vitest run ruta/al/archivo.spec.ts`
- **Suite completa:** `npx --prefix 1001-arl-manager_fe vitest run`