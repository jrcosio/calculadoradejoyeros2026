# Research: Ajustes — idioma de la aplicación

**Feature**: `008-ajustes-idioma` | **Date**: 2026-08-25 | **Plan**: [plan.md](./plan.md)

Fase 0. Cada punto responde a una incógnita del contexto técnico con una decisión, su porqué y lo
que se descartó. Lo que aquí se decide no se vuelve a discutir en `tasks.md`.

---

## R1 — Punto de partida: cómo están hoy los textos

**Hallazgo**: el proyecto está inusualmente bien preparado para la internacionalización, y eso
reduce la feature a lo esencial.

- 206 cadenas en un único `app/src/main/res/values/strings.xml`, agrupadas por pantalla con
  comentarios. Ninguna carpeta `values-xx`: se parte de cero, sin traducciones a medio hacer.
- **Cero textos cableados** en pantallas reales. El único `Text("…")` literal de `main` está en un
  parámetro de `@Preview` (`ui/herramientas/HerramientasScreen.kt`).
- Los `%1$s`…`%4$s` son **posicionales**, no `%s`: se pueden reordenar al traducir. Un solo `%1$d`
  (`chapas_ley_milesimas`) y un `%%` (`chapas_pureza_formato`).
- Ni un `<plurals>` ni un `<string-array>`: ningún texto pluraliza hoy, así que las reglas de plural
  de los cinco idiomas no entran en juego.
- **Ningún ViewModel importa `R` ni `Context`**: lo traducible viaja como enum en el `UiState` y se
  mapea a `R.string` en los `Presentacion*.kt` de cada paquete. No hay nada que refactorizar en la
  capa de estado para que el idioma cambie.
- Los identificadores estables (nombres de analytics, URLs, símbolos del API, `MONEDA = "EUR"`) ya
  viven en enums de Kotlin, fuera de `strings.xml`: ningún traductor puede romper la telemetría.
- `android:supportsRtl="true"` ya está puesto, aunque los cinco idiomas son de izquierda a derecha.
- Las fuentes (`Manrope`, `Playfair Display`) cubren latín extendido: `ß`, umlauts y acentos.

**Consecuencia**: el trabajo se reparte entre un mecanismo de aplicación (R2), una preferencia
persistida (R3–R7) y volumen de recursos (R8, R10).

---

## R2 — Cómo se aplica el idioma sin recrear la Activity

**Decisión**: un composable en la raíz, `ProveedorIdioma`, que crea un `Context` con la
configuración del idioma elegido (`createConfigurationContext`) y lo provee junto a su
`Configuration`:

```kotlin
CompositionLocalProvider(
    LocalContext provides contextoLocalizado,
    LocalConfiguration provides contextoLocalizado.resources.configuration,
    content = content,
)
```

**Rationale**: comprobado en las fuentes de `androidx.compose.ui:ui-android:1.12.0` (el artefacto que
resuelve el BoM 2026.08.00), que es el detalle del que depende toda la feature:

- `stringResource(id)` es `LocalResources.current.getString(id)` — ya no usa `LocalContext`
  directamente.
- `LocalResources` es un `compositionLocalWithComputedDefaultOf` cuyo cuerpo lee
  `LocalConfiguration.currentValue` y devuelve `LocalContext.currentValue.resources`. Al proveer los
  dos, el valor calculado se rehace y **todas** las llamadas a `stringResource` del subárbol se
  invalidan. No hay que proveer `LocalResources` a mano.
- `LocalContext` es `staticCompositionLocalOf`: proveerlo recompone el subárbol entero, que es
  exactamente lo que se busca en un cambio de idioma.

Alcance verificado sobre el código de la app: 184 llamadas a `stringResource` en 18 ficheros, más 6
usos de `LocalContext.current`, que **se benefician** del cambio en vez de sufrirlo:

| Uso de `LocalContext.current` | Efecto de proveerlo localizado |
|---|---|
| `Toast.makeText(context, R.string.aviso_proximamente, …)` en oro, plata, soldaduras, base y chapas | el aviso efímero sale traducido |
| `DateUtils.formatDateTime(contexto, …)` en `PresentacionPrecios.kt:105` | el nombre del mes sigue al idioma elegido (FR-019) |

Los enlaces externos de Info no usan `LocalContext`: usan `LocalUriHandler`, que se resuelve por
encima del proveedor y sigue atado a la Activity. Nada de la app llama a `startActivity` sobre
`LocalContext`, que es el único riesgo real de proveer un contexto que no es la Activity.

**Alternatives considered**:

- **`AppCompatDelegate.setApplicationLocales` (per-app language de AndroidX)**. Es la vía recomendada
  por Google, y aquí sale caro: obliga a añadir `androidx.appcompat`, a convertir `MainActivity` en
  `AppCompatActivity` y a cambiar el tema padre a uno de AppCompat — y el tema actual
  (`android:Theme.Material.NoActionBar` con `windowSplashScreenBackground`) está ajustado a mano para
  que el splash de Android 12+ no destelle en blanco. Además, por debajo de API 33 solo actúa sobre
  los delegates de AppCompat: con una `ComponentActivity` no haría nada, y minSdk es 24.
- **`LocaleManager` del framework**. API 33+. Con minSdk 24 dejaría fuera el rango que más lo
  necesita.
- **`attachBaseContext` + `recreate()`**. Funciona en todas las versiones y usa la resolución de
  recursos del sistema tal cual, pero exige leer la preferencia de forma bloqueante en el arranque de
  la Activity (`runBlocking` sobre DataStore en el hilo principal) y recrear la pantalla en cada
  cambio, lo que perdería lo que el joyero tenga escrito: FR-022 lo prohíbe.
- **Proveer solo `LocalResources`** dejando `LocalContext` intacto. Traduciría los
  `stringResource`, pero los cinco `Toast` y las fechas de `DateUtils` se quedarían en el idioma del
  sistema. Media feature.

**Riesgo conocido y aceptado**: `LocalImageVectorCache` y `LocalResourceIdCache` se proveen por
encima del proveedor y no se vacían en este cambio de configuración simulado. Es inocuo porque
ningún drawable de la app está cualificado por idioma; si algún día hubiera un
`drawable-en/`, habría que vaciar esas cachés.

---

## R3 — Dónde se guarda la elección

**Decisión**: Preferences DataStore (`androidx.datastore:datastore-preferences:1.2.1`), fichero
`ajustes`, una sola clave `idioma` con la etiqueta BCP-47.

**Rationale**: la preferencia **se observa**. FR-006 exige que un cambio del dato llegue a la raíz de
Compose como emisión, y FR-013 exige que la lectura del arranque no bloquee el hilo principal.
DataStore da las dos cosas de fábrica: `Flow` y corrutinas. La versión 1.2.1 es la última estable
publicada en `dl.google.com/dl/android/maven2` (la rama 1.3.0 solo tiene alphas), verificada contra
`maven-metadata.xml` y no contra la documentación.

**Alternatives considered**:

- **`SharedPreferences`**, como la caché de cotizaciones de la 007. Haría falta envolver
  `OnSharedPreferenceChangeListener` en un `callbackFlow` para obtener el flujo, y leer en el hilo
  principal o montar el salto de hilo a mano. Más código propio y sin transaccionalidad para el mismo
  resultado. El argumento de la 007 —«nadie la observa, así que no hace falta DataStore»— es
  justamente el que aquí no se cumple.
- **DataStore Proto**. Aporta esquema tipado que aquí no se necesita: el dato es una cadena de dos
  letras, y añadiría generación de código.
- **Guardar el `Locale` completo** (`es-ES`) en vez del idioma. Se descarta: la app solo tiene
  recursos por idioma y FR-010 dice explícitamente que la región se ignora.

---

## R4 — Cómo se modela «Automático»

**Decisión**: `IdiomaApp?` nulo. El enum `IdiomaApp` tiene cinco valores —los cinco idiomas que
existen de verdad— y la ausencia de elección se representa con `null`, tanto en el `Flow` del
repositorio como en la clave ausente del DataStore.

**Rationale**: «Automático» no es un idioma: es la decisión de no decidir. Si fuera un sexto valor
del enum, cualquier `when` exhaustivo del proyecto tendría que tratarlo, y un valor que nunca puede
llegar a `Locale.forLanguageTag` acabaría colándose en una conversión. Con `null`, el compilador
obliga a resolverlo antes de usarlo, y quien lo resuelve es `SeleccionIdioma.efectivo`. Además, la
ausencia de clave en el almacén y «Automático» elegido a mano son el mismo estado observable
(FR-012), así que no hay que distinguirlos ni migrar nada.

**Alternatives considered**: un `sealed interface PreferenciaIdioma { object Automatico; data class
Explicito(val idioma: IdiomaApp) }`. Es equivalente y más ceremonioso: tres tipos para lo que un
nulable ya expresa, y obligaría a mapear en cada frontera.

---

## R5 — De dónde sale el idioma del sistema

**Decisión**: `interface IdiomaSistema { fun idioma(): IdiomaApp }` en `core/util/`, con
`IdiomaSistemaJvm` resolviendo `Locale.getDefault().language` a través de `IdiomaApp.desdeEtiqueta`,
y `IdiomaApp.PREDETERMINADO` (español) cuando no hay coincidencia. Registrado en `coreModule`.

**Rationale**: es el mismo precedente que `Reloj`/`RelojSistema` de la 007: una consulta al entorno
detrás de interfaz, para que la regla se pueda probar en JVM con un falso en milisegundos. Queda en
`core/util/` y no en `data/` porque no hay nada que persistir ni ninguna fuente que confinar: es un
dato del entorno, y su implementación es **JVM puro** (`java.util.Locale`), sin `android.*`.
`Locale.getDefault()` en Android refleja el idioma del sistema, que es lo que FR-008 pide.

Convertir la etiqueta a idioma es un detalle con trampa y va en el dominio, con test propio:
`es`, `es-ES`, `es_MX`, `ES` y `es-419` deben dar todos `ESPANOL` (FR-010), y `pt`, `""` o `null`
deben dar `null` para que el llamante decida (FR-009).

**Alternatives considered**:

- **`context.resources.configuration.locales[0]`**. Es el idioma con el que la app está resolviendo
  recursos, no el del sistema: en cuanto el proveedor de R2 esté en marcha devolvería el idioma
  elegido, y «Automático» dejaría de saber a qué seguir. Descartado por incorrecto, no por estilo.
- **`Resources.getSystem().configuration.locales`**. Correcto pero ata `core/util/` a `android.*` sin
  ganar nada frente a `Locale.getDefault()`.
- **`LocaleList.getDefault()`** para recorrer las preferencias del usuario. `Locale.getDefault()` ya
  devuelve la primera; recorrer la lista completa (elegir el primer idioma soportado en vez del
  primero a secas) es una mejora futura si algún día se pide.

---

## R6 — El primer fotograma del arranque

**Decisión**: `IdiomaAppUiState.idioma` es nulable y significa «todavía no sé qué idioma toca».
Mientras es nulo, `MainActivity` **no compone el `NavHost`**.

**Rationale**: FR-013 prohíbe el destello de texto en el idioma equivocado, y la primera lectura de
DataStore es asíncrona. Con la raíz vacía durante ese instante lo que se ve es el
`android:windowBackground` del tema, que ya es `@color/brand_splash_background` —el mismo azul de la
portada—, así que el arranque se ve igual que hoy. No hace falta pantalla de carga ni retraso
artificial: la primera emisión de DataStore llega en el orden de milisegundos.

**Alternatives considered**: pintar directamente con el idioma del sistema y corregir cuando llegue
la preferencia (parpadeo visible, justo lo que FR-013 prohíbe); o bloquear el arranque con
`runBlocking` hasta tener el dato (lectura de disco en el hilo principal, que es lo que se quería
evitar al elegir DataStore).

---

## R7 — Koin: qué verifica `verify()` y cómo no romperlo

**Decisión**: el `DataStore<Preferences>` **no se registra** en el grafo. Lo crea `by lazy` el propio
`DataStoreAjustesLocalDataSource`, que se registra concreto y con `bind` a su interfaz:

```kotlin
single { DataStoreAjustesLocalDataSource(androidContext(), get()) } bind AjustesLocalDataSource::class
single<PreferenciasRepository> { PreferenciasRepositoryImpl(get()) }
```

**Rationale**: `KoinModulesTest` recorre el grafo con `verify()`, que solo inspecciona los
constructores del **tipo primario** de cada definición. Un `single<DataStore<Preferences>> { … }`
apuntaría a una interfaz construida por fábrica (`PreferenceDataStoreFactory.create`), el mismo caso
que obligó a meter los tipos de Firebase en `extraTypes`. Manteniendo el almacén dentro del data
source, el tipo primario es una clase con constructor real (`Context`, `DispatcherProvider`, ambos ya
en el grafo) y el test sigue comprobando algo de verdad. Es además el mismo patrón que
`SharedPreferencesCotizacionesLocalDataSource`, que se guarda sus `SharedPreferences` y su
`CodificadorInstantanea` en privado.

Un `single` garantiza la instancia única por fichero que DataStore exige por proceso.

**Alternatives considered**: registrar el `DataStore` y añadirlo a `extraTypes` (debilita el test para
todo el proyecto, no solo para este tipo); crear el almacén en `CalculadoraApp` (saca infraestructura
del grafo y la mete en la clase de arranque).

---

## R8 — Qué se traduce y qué no

**Decisión**: `translatable="false"` en 33 cadenas —28 existentes más los cinco nombres de idioma— y
traducción completa de las 184 restantes a los cuatro idiomas nuevos. La regla que decide es «no
queda ni una palabra dentro»: nombres propios, símbolos del SI, cifras con su quilate y plantillas
de formato; en cuanto hay una palabra, se traduce. Inventario completo en
[contracts/traducciones.md](./contracts/traducciones.md).

**Rationale**: `translatable="false"` no es cosmético: es lo que convierte a `lint` en la red
automática de FR-018. Con la marca marcada, `ExtraTranslation` avisa si alguien traduce
«Calculadora de Joyeros» en un `values-de`, y `MissingTranslation` avisa de lo contrario para todo lo
demás. Los grupos:

Casos que **sí** se traducen aunque lo parezcan: `plata_ley_925` y `plata_ley_800` llevan «(ley)»
entre paréntesis, que es la palabra española de la ley de contraste y cambia en cada idioma;
`nav_home` vale «Home» en español y coincide en inglés e italiano, pero es una etiqueta de
navegación, no una marca.

Dos cadenas mezclaban palabra y nombre propio y hubo que partirlas en `%1$s`: `precios_fuente`
(«Fuente: Metal Sentinel») y `welcome_developer` («Desarrollado por José Ramón Blanco»). La segunda
se descubrió mirando la portada en inglés en el emulador, con la app ya traducida: es la clase de
isla en español que un recuento de claves no detecta.

Los tres avisos legales (`oro_aviso_12k`, `plata_aviso_950`, `plata_aviso_900`) se traducen
manteniendo la referencia a España (FR-017): describen la ley española de contraste, no la del país
del lector. El aviso de humos de cadmio y zinc (`soldadura_aviso_seguridad`) es texto de seguridad y
se traduce literal, sin adornos ni resúmenes.

**Nota sobre `unidad_gramos`**: vale «gr», que no es el símbolo del SI («g»). Se deja como está: es un
cambio visible en todas las calculadoras y no pertenece a esta feature.

---

## R9 — `localeConfig` y filtros de idioma en el build

**Decisión**: no se declara `android:localeConfig` ni se crea `res/xml/locales_config.xml`. Tampoco
se añade `androidResources { localeFilters }`.

**Rationale**: `localeConfig` sirve para que el selector de idioma **del sistema** (Ajustes de Android,
API 33+) ofrezca la app. Sería una segunda fuente de verdad: el joyero podría fijar un idioma ahí que
nuestra preferencia de DataStore ignoraría, y las dos pantallas mostrarían cosas distintas. La app
gestiona su idioma dentro, y la spec no pide integración con el selector del sistema.

`localeFilters` es una optimización de tamaño del paquete (recorta los idiomas que traen AndroidX y
Firebase). Es un cambio de build independiente, medible y reversible: no entra aquí.

---

## R10 — Desbordes de texto en los cinco idiomas

**Decisión**: se endurecen dos componentes compartidos antes de traducir, no después.

| Componente | Hoy | Riesgo | Arreglo |
|---|---|---|---|
| `JewelryBottomBar` / `TabItem` | `Text(labelMedium)` sin `maxLines` en una pestaña de 96 dp dentro de una barra de 88 dp de alto | «Einstellungen» (13 caracteres) parte a dos líneas y desborda el alto | `BasicText` con `TextAutoSize.StepBased` y una línea, como `SelectorSegmentado` |
| `BotonDorado` | `Text(labelMedium 14 sp)` sin `maxLines`, dos botones a `weight(1f)` | «In Favoriten speichern» / «Enregistrer dans les favoris» junto a «Limpiar» | Ídem |

`SelectorSegmentado` ya autoajusta (`TextAutoSize.StepBased(6.sp, 14.sp)`) y `CampoMedida`,
`CampoCantidad` y `TarjetaTotal` muestran cifras y unidades cortas. `ModuleCard` tiene altura fija de
158 dp con título y descripción: no se cambia a ciegas, se revisa en el emulador en los cinco idiomas
(tarea de verificación) y solo se toca si algo se corta.

**Rationale**: es la diferencia entre «está traducido» y «se puede usar en alemán». FR-021 y SC-004 lo
piden explícitamente, y acortar la traducción para que quepa sería falsearla.

---

## R11 — Formato numérico: fuera de alcance, con motivo

**Decisión**: no se toca. `FormatoPrecios` y los cinco ViewModels que formatean cifras siguen con
coma decimal y punto de miles, sin `Locale`.

**Rationale**: de los cinco idiomas, cuatro (español, alemán, francés e italiano) usan coma decimal;
solo el inglés vería «148.000,50» donde esperaría «148,000.50». A cambio, localizar el formato obliga
a llevar el idioma hasta los cinco ViewModels que formatean, a reescribir sus tests —hoy
deterministas a propósito— y a decidir qué hace la coma en la **entrada** del joyero, que hoy acepta
coma y punto indistintamente (`parsearDecimalPositivo`). Es una feature aparte, y la spec lo declara
en Assumptions y en FR-020 para que no vuelva a colarse.

**Consecuencia documentada**: la moneda sigue siendo el euro en los cinco idiomas
(`MONEDA = "EUR"` en `MetalSentinelDataSource`, y `unidad_euro_*` en recursos). Esta feature no toca
precios.

---

## R12 — Las fechas: `DateUtils` no basta

**Decisión**: `fechaHoraLocal` (`ui/herramientas/precios/PresentacionPrecios.kt`) pasa de
`DateUtils.formatDateTime` a `android.text.format.DateFormat.getMediumDateFormat(contexto)` y
`getTimeFormat(contexto)`.

**Rationale**: descubierto en el emulador con la app en inglés y el dispositivo en portugués:
`DateUtils.formatDateTime` mostraba «25/08/2026 · 7:20 p.m.». La hora seguía al idioma elegido (sale
de los recursos del contexto) pero **la fecha no**: su orden y su formato numérico salen de
`Locale.getDefault()`, que es el del sistema. Con eso, FR-019 —«el nombre del mes en el idioma
elegido»— era falso en cuanto los dos idiomas no coincidían.

`DateFormat.getMediumDateFormat(context)` construye el formateador con **el locale de la
configuración del contexto**, que es el que `ProveedorIdioma` ya deja localizado, y `getTimeFormat`
hace lo mismo respetando además el ajuste de 12/24 horas del dispositivo. Las dos son API 3, igual
que `DateUtils`, así que no hay compromiso de minSdk. Verificado: «Updated Aug 25, 2026 · 7:20 PM»
con el dispositivo en portugués.

**Alternatives considered**: `java.time` con `DateTimeFormatter.ofLocalizedDate` (API 26, y el
proyecto es minSdk 24); pasar el `Locale` a mano desde el ViewModel (rompería la regla de que el
ViewModel no conoce recursos ni idioma).

---

## R13 — El paquete: sin splits por idioma

**Decisión**: `bundle { language { enableSplit = false } }` en `app/build.gradle.kts`.

**Rationale**: lo señaló `lint` con `AppBundleLocaleChanges` al ver el cambio de locale en tiempo de
ejecución. Los App Bundle reparten los recursos de idioma por splits **por defecto**: Play instala
solo los del idioma del dispositivo, así que en producción elegir una bandera distinta no cambiaría
nada —la app buscaría un `values-de` que no está instalado y caería a `values/`—. El bug sería
invisible en desarrollo, porque el APK de debug lleva todos los idiomas dentro.

Hoy el proyecto solo produce APK (no hay `signingConfig` de release todavía), pero la línea tiene
que estar antes de que exista el primer AAB, no después del primer informe de un joyero alemán.

**Alternatives considered**: la librería Play Feature Delivery para descargar idiomas a demanda
(complejidad y una dependencia nueva para cinco ficheros de 15 KB); dejarlo y documentarlo (un fallo
silencioso en producción no se documenta, se arregla).
