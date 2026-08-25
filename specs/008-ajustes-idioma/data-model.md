# Data Model: Ajustes — idioma de la aplicación

**Feature**: `008-ajustes-idioma` | **Date**: 2026-08-25 | **Plan**: [plan.md](./plan.md)

Fase 1. Entidades del dominio, estado de interfaz y transiciones. Los tipos de `domain/` son Kotlin
puro: sin `android.*`, sin `androidx.*` y sin `data.*`.

---

## Dominio

### IdiomaApp *(enum)*

Los cinco idiomas de interfaz. Cada uno lleva su etiqueta BCP-47, que es a la vez el sufijo de su
carpeta de recursos, el valor persistido y el identificador de telemetría.

| Valor | `etiquetaBcp47` | Carpeta de recursos | Nombre mostrado (no traducible) |
|---|---|---|---|
| `ESPANOL` | `es` | `values/` (base) | Español |
| `INGLES` | `en` | `values-en/` | English |
| `FRANCES` | `fr` | `values-fr/` | Français |
| `ALEMAN` | `de` | `values-de/` | Deutsch |
| `ITALIANO` | `it` | `values-it/` | Italiano |

```kotlin
enum class IdiomaApp(val etiquetaBcp47: String) {
    ESPANOL("es"), INGLES("en"), FRANCES("fr"), ALEMAN("de"), ITALIANO("it");

    /** Identificador estable para telemetría: no se traduce y no cambia con el idioma. */
    val analyticsId: String get() = etiquetaBcp47

    companion object {
        /** El idioma de `values/`: lo que ve quien tiene el móvil en un idioma no soportado. */
        val PREDETERMINADO = ESPANOL

        /** `es`, `es-ES`, `es_MX`, `ES`, `es-419` → ESPANOL. `pt`, ``, null → null. */
        fun desdeEtiqueta(etiqueta: String?): IdiomaApp?
    }
}
```

**Reglas de `desdeEtiqueta`** (FR-010, FR-014; cada línea es un caso de `IdiomaAppTest`):

1. `null`, vacía o en blanco → `null`.
2. Se toma el primer subtag: se corta en el primer `-` o `_`.
3. Se compara en minúsculas: `ES`, `Es` y `es` son el mismo idioma.
4. Si no coincide con ninguna etiqueta → `null`. Nunca lanza.

El orden de declaración es el de la lista de Ajustes (español primero, el idioma base), y `entries` es
la única fuente de ese orden: la pantalla no mantiene una lista aparte.

### SeleccionIdioma

La regla de precedencia de FR-008 y FR-011 como función pura. Es el único sitio del proyecto donde se
decide qué idioma manda.

```kotlin
data class SeleccionIdioma(
    /** Lo que el joyero eligió en Ajustes; `null` = «Automático», sigue al dispositivo. */
    val elegido: IdiomaApp?,
    /** El idioma del dispositivo, ya reducido a uno de los cinco soportados. */
    val sistema: IdiomaApp,
) {
    /** Lo que la app muestra. Lo único que la interfaz necesita saber. */
    val efectivo: IdiomaApp get() = elegido ?: sistema

    /** `true` cuando manda el dispositivo: es lo que marca la fila «Automático». */
    val esAutomatico: Boolean get() = elegido == null
}
```

| `elegido` | `sistema` | `efectivo` | `esAutomatico` |
|---|---|---|---|
| `null` | `FRANCES` | `FRANCES` | `true` |
| `null` | `ESPANOL` (móvil en portugués) | `ESPANOL` | `true` |
| `ALEMAN` | `INGLES` | `ALEMAN` | `false` |
| `ALEMAN` | `ALEMAN` | `ALEMAN` | `false` |

### PreferenciasRepository *(interfaz de dominio)*

```kotlin
interface PreferenciasRepository {
    /** Emite la elección actual y cada cambio. `null` = «Automático». */
    val idioma: Flow<IdiomaApp?>

    /** Guarda la elección. `null` devuelve el control al dispositivo. */
    suspend fun guardarIdioma(idioma: IdiomaApp?)
}
```

`Flow` y no `suspend` —al contrario que `CotizacionesRepository`— porque aquí hay dos consumidores
que necesitan enterarse de los cambios: la raíz de la app y la pantalla de Ajustes. Es justo la
diferencia que justifica DataStore (R3).

Nombre en plural y genérico a propósito: cuando Ajustes crezca (tema, unidades por defecto), los
ajustes nuevos entran aquí sin renombrar nada. Hoy solo tiene idioma.

### IdiomaSistema *(interfaz, `core/util/`)*

```kotlin
interface IdiomaSistema {
    /** El idioma del dispositivo, ya reducido a los soportados (español si no hay coincidencia). */
    fun idioma(): IdiomaApp
}

class IdiomaSistemaJvm : IdiomaSistema {
    override fun idioma(): IdiomaApp =
        IdiomaApp.desdeEtiqueta(Locale.getDefault().language) ?: IdiomaApp.PREDETERMINADO
}
```

Mismo precedente que `Reloj`: consulta al entorno detrás de interfaz, JVM puro, sustituible por
`IdiomaSistemaFalso` en los tests (R5).

### Casos de uso

```kotlin
class ObservarIdiomaUseCase(
    private val preferencias: PreferenciasRepository,
    private val idiomaSistema: IdiomaSistema,
) {
    operator fun invoke(): Flow<SeleccionIdioma> =
        preferencias.idioma.map { SeleccionIdioma(elegido = it, sistema = idiomaSistema.idioma()) }
}

class GuardarIdiomaUseCase(private val preferencias: PreferenciasRepository) {
    suspend operator fun invoke(idioma: IdiomaApp?) = preferencias.guardarIdioma(idioma)
}
```

El idioma del sistema se lee **en cada emisión** y no una sola vez: así, si el joyero cambia el idioma
del móvil y vuelve a la app sin que el proceso muera, «Automático» sigue siendo verdad.

Un solo flujo sirve a los dos consumidores (la raíz solo mira `efectivo`; Ajustes mira las tres
propiedades), y con `factoryOf` cada uno recibe su instancia sin compartir estado.

---

## Estado de interfaz

Fuera del dominio, en `ui/`. Nada de `android.*` aquí tampoco: los ViewModels no conocen recursos.

### IdiomaAppUiState *(`ui/idioma/`)*

```kotlin
data class IdiomaAppUiState(
    /** `null` = todavía no se sabe qué idioma toca: la raíz no pinta nada (FR-013). */
    val idioma: IdiomaApp? = null,
)
```

### AjustesUiState *(`ui/ajustes/`)*

```kotlin
data class AjustesUiState(
    /** Lo elegido; `null` = «Automático» marcado. */
    val elegido: IdiomaApp? = null,
    /** Lo que se muestra junto a «Automático». */
    val sistema: IdiomaApp = IdiomaApp.PREDETERMINADO,
)
```

Dos campos y ninguna lista: las opciones son `IdiomaApp.entries` más la fila «Automático», y el orden
lo da el enum. `sistema` viaja como enum y no como texto porque su nombre lo pinta la vista con el
recurso correspondiente (`PresentacionAjustes.kt`), igual que el resto del proyecto.

### Presentación *(`ui/ajustes/PresentacionAjustes.kt`)*

El mapeo enum → recursos, que vive en `ui/` para que `domain/` siga libre de Android:

| `IdiomaApp` | `banderaRes` | `nombreRes` |
|---|---|---|
| `ESPANOL` | `R.drawable.ic_bandera_es` | `R.string.idioma_es` |
| `INGLES` | `R.drawable.ic_bandera_en` | `R.string.idioma_en` |
| `FRANCES` | `R.drawable.ic_bandera_fr` | `R.string.idioma_fr` |
| `ALEMAN` | `R.drawable.ic_bandera_de` | `R.string.idioma_de` |
| `ITALIANO` | `R.drawable.ic_bandera_it` | `R.string.idioma_it` |

---

## Transiciones de estado

### `IdiomaAppViewModel` (la raíz)

| Estado | Suceso | Estado siguiente | Efecto visible |
|---|---|---|---|
| `idioma = null` (arranque) | primera emisión de `ObservarIdiomaUseCase` | `idioma = seleccion.efectivo` | se compone el `NavHost` dentro de `ProveedorIdioma` |
| `idioma = X` | emisión con `efectivo = X` | sin cambios | nada se recompone (`StateFlow` no reemite valores iguales) |
| `idioma = X` | emisión con `efectivo = Y` | `idioma = Y` | el subárbol entero se repinta en `Y` |

No tiene acciones: es un observador. No emite telemetría (la pantalla de Ajustes ya registra el
cambio; contarlo dos veces falsearía la serie).

### `AjustesViewModel`

| Estado | Suceso | Estado siguiente | Efectos |
|---|---|---|---|
| inicial | `init` | `AjustesUiState()` | `logScreenView("ajustes")`, el mismo nombre que emitía el placeholder |
| cualquiera | emisión de `ObservarIdiomaUseCase` | `elegido` y `sistema` actualizados | — |
| `elegido = X` | `onIdiomaSeleccionado(X)` | sin cambios | **no** guarda ni registra evento: repetir la elección no es un cambio (escenario 5 de la historia 1) |
| `elegido = X` | `onIdiomaSeleccionado(Y)` | llega por la emisión del flujo, no se escribe a mano | `guardarIdioma(Y)` y `logEvent("ajustes_idioma", {"idioma": Y.analyticsId})` |
| `elegido = X` | `onAutomaticoSeleccionado()` | ídem con `null` | `guardarIdioma(null)` y `logEvent("ajustes_idioma", {"idioma": "automatico"})` |

**El estado no se escribe a mano al guardar**: se escribe cuando el flujo lo confirma. Una sola
dirección, y lo que se ve en pantalla es siempre lo que está guardado.

---

## Formato persistido

Contrato completo en [contracts/preferencia-idioma.md](./contracts/preferencia-idioma.md).

- Fichero: `files/datastore/ajustes.preferences_pb` (Preferences DataStore, nombre `ajustes`).
- Clave: `idioma` (cadena). Valor: `es` | `en` | `fr` | `de` | `it`.
- **Clave ausente = «Automático»**. Elegir «Automático» borra la clave; no se guarda un valor
  especial (R4).
- Valor desconocido (una copia de seguridad de una versión con un sexto idioma) → se trata como
  ausente y la app sigue al dispositivo (FR-014). No se borra al leer: si el joyero vuelve a esa
  versión, su elección sigue ahí.
- `IOException` al leer → se emite vacío y la app sigue al dispositivo, sin fallo visible.
- Entra en la copia de seguridad del sistema (FR-025): las reglas de `res/xml/` solo excluyen
  `cotizaciones.xml`, y el idioma elegido **debe** viajar al móvil nuevo.
