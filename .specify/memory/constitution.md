# Calculadora de Joyeros — Constitución

Principios no negociables del proyecto. Toda especificación, plan, tarea e
implementación se valida contra este documento.

## Core Principles

### I. Spec-Driven Development (NO NEGOCIABLE)

Ninguna feature se implementa sin recorrer antes el ciclo completo:
`/speckit-specify` → `/speckit-plan` → `/speckit-tasks` → `/speckit-implement`.

- No se escribe código de producto sin un `tasks.md` aprobado en `specs/<NNN>-<slug>/`.
- La especificación describe **qué** y **por qué**, nunca **cómo**: sin nombres de
  clase, de librería ni de API en `spec.md`.
- Si durante la implementación aparece un requisito que la spec no cubre, se
  detiene y se actualiza la spec. No se improvisa.
- **Exentos** del ciclo: arreglos de build, subidas de versión de dependencias,
  correcciones de typos y cambios de documentación.

### II. MVVM con capas estancas

El flujo de dependencias es siempre `ui → domain ← data`. No hay atajos.

- `domain/` no importa nada de `android.*`, `androidx.*`, `com.google.firebase.*`
  ni de `data/`. Es Kotlin puro y testeable en JVM.
- `data/` implementa las interfaces de `domain/repository/`. Los SDK externos
  viven confinados en `data/source/`.
- `ui/` solo habla con `domain/`. Un Composable jamás toca un repositorio de
  `data/` ni un SDK.
- Todo ViewModel expone **un único** `StateFlow` inmutable de un tipo de estado
  propio de la pantalla. Nada de `LiveData` ni de estado mutable público.
- Un ViewModel no importa `androidx.compose.*`: no conoce a su vista.
- Todo Composable de pantalla se parte en dos: el que resuelve el ViewModel y el
  que pinta, sin estado y con `@Preview`.

### III. Inyección de dependencias solo por Koin

- Koin con DSL manual. Nada de `koin-annotations`.
- KSP se usa **sólo** como procesador de anotaciones de Room. Jamás para
  inyección de dependencias.
- Toda dependencia entra por constructor. Prohibido `get()` dentro de una clase,
  y prohibido instanciar dependencias a mano dentro de un Composable.
- Los repositorios se registran siempre por su interfaz de dominio:
  `single<InterfazDeDominio> { Implementacion(get()) }`.
- Todo módulo nuevo se añade a `featureModules` en `core/di/AppModule.kt`, con lo
  que queda cubierto automáticamente por `KoinModulesTest`.

### IV. Test obligatorio (NO NEGOCIABLE)

- Todo ViewModel y todo caso de uso llevan test unitario. Sin excepción.
- El grafo de Koin se verifica en `KoinModulesTest`; si un módulo nuevo lo rompe,
  el fallo sale en el test, no en el móvil.
- Las corrutinas se testean con `DispatcherProvider` inyectado y `TestDispatcher`.
  Prohibido usar `Dispatchers.IO` directamente en domain o data.
- Un `tasks.md` no se da por terminado con tests en rojo.

### V. Una sola fuente de verdad para las versiones

- Toda dependencia y todo plugin pasan por `gradle/libs.versions.toml`. Prohibido
  escribir coordenadas o versiones sueltas en un `build.gradle.kts`.
- Cuando existe un BoM (Firebase, Compose, Koin) se usa, y sus artefactos se
  declaran **sin versión**.
- La versión de Kotlin la fija el Kotlin integrado de AGP. No se sube por libre.

## Restricciones técnicas

- **Stack**: Kotlin + Jetpack Compose + Material 3, Navigation Compose con rutas
  type-safe (`@Serializable`), corrutinas y `StateFlow`.
- **Firebase**: Analytics y Crashlytics. Cualquier producto nuevo de Firebase
  entra por el BoM y queda detrás de una interfaz de `domain/repository/`.
- **minSdk 24**. Toda API superior va protegida por comprobación de versión.
- **Idioma**: código, nombres y comentarios en español cuando describen dominio de
  joyería; las convenciones de Kotlin y Android se respetan tal cual.
- **Persistencia**: preferencias del usuario en DataStore, caché derivada en
  `SharedPreferences`, y datos que el joyero crea a mano en Room. Prohibido
  `fallbackToDestructiveMigration`; el esquema exportado se commitea.
- El `google-services.json` está en `.gitignore` y **no se commitea nunca**.

## Flujo de trabajo

1. `/speckit-specify` crea la rama de feature (`NNN-slug`) y `spec.md`.
2. `/speckit-clarify` cuando la spec tenga zonas ambiguas. Recomendado.
3. `/speckit-plan` produce el plan técnico contra esta constitución.
4. `/speckit-tasks` desglosa en tareas ejecutables.
5. `/speckit-analyze` antes de implementar, para cazar incoherencias entre
   artefactos. Recomendado.
6. `/speckit-implement` ejecuta las tareas.
7. Los commits siguen Conventional Commits (`feat:`, `fix:`, `build:`, `test:`,
   `docs:`, `chore:`, `refactor:`).

Puertas de calidad antes de dar una feature por cerrada:

```
./gradlew :app:testDebugUnitTest   # en verde
./gradlew :app:assembleDebug       # en verde
```

## Governance

Esta constitución prevalece sobre cualquier otra práctica o preferencia.

- Toda revisión verifica el cumplimiento de los principios anteriores.
- Cualquier desviación se justifica por escrito en el `plan.md` de la feature, en
  su sección de complejidad. Sin justificación, no entra.
- Enmendar esta constitución exige actualizar `CLAUDE.md` en el mismo cambio, para
  que las reglas en tiempo de ejecución no se desincronicen.
- `CLAUDE.md` es la guía operativa del día a día; este documento es la norma.

**Version**: 1.1.0 | **Ratified**: 2026-08-22 | **Last Amended**: 2026-08-26
