# Tasks: Herramientas — precio de metales y peso de chapas

**Feature**: `007-herramientas` | **Spec**: [spec.md](./spec.md) | **Plan**: [plan.md](./plan.md) | **Modelo**: [data-model.md](./data-model.md) | **Contrato**: [contracts/metal-quote.md](./contracts/metal-quote.md) | **Validación**: [quickstart.md](./quickstart.md)

## Formato: `[ID] [P?] [Story] Descripción`

- **[P]**: paralelizable — ficheros distintos, sin dependencia con tareas pendientes.
- **[Story]**: historia de usuario a la que sirve (US1…US5). Las fases de Setup, Fundacional y
  Pulido no llevan etiqueta.
- Rutas de fichero relativas a la raíz del repositorio. `$SRC` =
  `app/src/main/java/com/jrblanco/calculadoradejoyeros2021`, `$TEST` =
  `app/src/test/java/com/jrblanco/calculadoradejoyeros2021`, `$ATEST` =
  `app/src/androidTest/java/com/jrblanco/calculadoradejoyeros2021`.

**Tests**: sí. La constitución (principio IV) exige test unitario por ViewModel **y por caso de
uso**, las corrutinas se prueban con `DispatcherProvider` inyectado y `TestDispatcher`, y hay
precedente de test instrumentado por pantalla desde la 002. Es la **primera feature con red,
corrutinas y persistencia**: crea los primeros `TestDispatcherProvider`, `RelojFalso` y fakes del
proyecto.

**Fuentes de verdad numérica**: el contrato real del proveedor
([contracts/metal-quote.md](./contracts/metal-quote.md)) y
`UI_Plantillas/Feature_Herramientas/Especificacion_Calculadora_Peso_Chapas_Oro_Plata.md` (§
citados), que **prevalece sobre el mockup de chapas** (10 × 20 × 0,80 mm de 18K pesan 2,49 g,
no 3,13; densidad 15,58, no 15,55). Los valores esperados están en
[data-model.md](./data-model.md).

**Aviso de alcance**: esta feature toca dos componentes compartidos —`SelectorSegmentado`
(`OpcionSegmento.iconRes`) y `Formularios.kt` (`CampoMedida` + `MarcoCampo`)— consumidos por
oro, plata y soldaduras, y añade un `buildConfigField`, un permiso y reglas de backup. T027 es
la puerta que verifica la regresión antes de que empiece cualquier historia. **Sin dependencias
nuevas**: `libs.versions.toml` no se toca.

**Bloqueo externo**: T001 necesita la credencial del autor en `local.properties`. Sin ella se
puede avanzar todo lo demás; solo `MetalSentinelDataSource` (T032) queda pendiente de confirmar
`PARAMETRO_METAL` y la unidad de cada metal.

---

## Fase 1 — Setup: contrato, recursos y build

**Propósito**: confirmar el contrato con la credencial real, dejar en `res/` la imagen, los
iconos y el vocabulario de la feature, y preparar la build (credencial, permiso, backup).

- [ ] T001 Confirmar el contrato del proveedor con la credencial real (Paso 0 del plan): el
  autor añade `RAPIDAPI_KEY=…` a `local.properties` (ya en `.gitignore`); ejecutar el bloque
  «Verificación del contrato» de `specs/007-herramientas/contracts/metal-quote.md` **sin
  imprimir la clave**, para `AU AG CU PD RH` con `?metal=` y, si 4xx, con `?symbol=`; anotar en
  el contrato el parámetro que responde 200 y la `unit` y `currency` de cada metal; guardar la
  respuesta de `AU` anonimizada (sin `ID` ni `extra`) en
  `UI_Plantillas/Feature_Herramientas/respuesta_ejemplo_metal_quote.json`. Si el parámetro no
  es `metal` o alguna unidad no es `OUNCE`, actualizar `contracts/metal-quote.md` y
  `data-model.md` **antes** de T032. Si la clave aún no está, dejar la tarea abierta y seguir:
  no bloquea nada más.
- [X] T002 [P] Copiar `UI_Plantillas/Feature_Herramientas/rodio.png` a
  `app/src/main/res/drawable-nodpi/rodio.png` redimensionado a 512 px de lado mayor
  (`sips -Z 512`, el tamaño de `cobre.png` y `paladio.png`); comprobar que el nombre no colisiona
  y que AAPT lo acepta. `chapa.png` **no se importa** (último recurso, ver US4).
- [X] T003 [P] Dibujar cinco vectores nuevos en `app/src/main/res/drawable/` con el estilo de
  `ic_lingotes.xml` (viewport 24, trazo 1.8, extremos y uniones redondeados, color blanco para
  tintar en tiempo de ejecución, misma cabecera de comentario): `ic_grafica.xml` (marco
  `M3.5,5 L20.5,5 L20.5,19 L3.5,19 Z` + línea `M6.5,15 L10.5,10.5 L13.5,13 L17.5,8` + punta
  `M15,8 L17.5,8 L17.5,10.5`), `ic_capas.xml` (rombo `M12,4.5 L20,8.5 L12,12.5 L4,8.5 Z` +
  `M4,12.5 L12,16.5 L20,12.5` + `M4,16 L12,20 L20,16`), `ic_ancho.xml` (topes
  `M3.5,7 L3.5,17 M20.5,7 L20.5,17` + eje `M5.5,12 L18.5,12` + flechas
  `M8.5,9 L5.5,12 L8.5,15 M15.5,9 L18.5,12 L15.5,15`), `ic_espesor.xml` (topes
  `M7,3.5 L17,3.5 M7,20.5 L17,20.5` + eje `M12,5.5 L12,18.5` + flechas
  `M9,8.5 L12,5.5 L15,8.5 M9,15.5 L12,18.5 L15,15.5`), `ic_regla.xml` (cuerpo
  `M3,16.5 L15.5,4 L20,8.5 L7.5,21 Z` + marcas `M7,12.5 L9,14.5 M10,9.5 L12,11.5 M13,6.5 L15,8.5`).
  No se añade `material-icons`.
- [X] T004 [P] Añadir en `app/src/main/res/values/strings.xml`: al bloque
  `<!-- Compartido: unidades, metales y acciones -->` → `metal_oro` («Oro»), `metal_oro_imagen`
  («Lingotes de oro»), `metal_plata` («Plata»), `metal_plata_imagen` («Lingotes de plata»),
  `metal_rodio` («Rodio»), `metal_rodio_imagen` («Montón de granalla de rodio»), `metal_oro_fino`
  («Oro fino»), `unidad_milimetros` («mm»), `unidad_cm3` («cm³»), `unidad_g_cm3` («g/cm³»),
  `unidad_euro_gramo` («€/g»), `unidad_euro_kilo` («€/kg»), `unidad_euro_onza` («€/oz»); bloque
  nuevo `<!-- Herramientas -->` → `herramientas_subherramienta_precios` («PRECIO METALES»),
  `herramientas_subherramienta_chapas` («PESO DE CHAPAS»), `herramientas_primera_visita_titulo`
  («Elige una herramienta»), `herramientas_primera_visita_texto` («Cotizaciones de metales al día
  y el peso de tus chapas por medidas.»); bloque `<!-- Herramientas: precio de metales -->` →
  `precios_titulo` («Precio de metales hoy»), `precios_actualizado` («Actualizado %1$s»),
  `precios_origen_cache` («Datos guardados de la última consulta»), `precios_seccion_unidad`
  («Unidad»), `precios_unidad_gramo` («Gramo»), `precios_unidad_kilo` («Kilo»),
  `precios_unidad_onza` («Onza troy»), `precios_mercado_titulo` («Información del mercado»),
  `precios_mercado_subtitulo` («Datos del proveedor para el metal elegido»),
  `precios_mercado_metal` («%1$s (%2$s)»), `precios_detalle_ask` («Ask»), `precios_detalle_bid`
  («Bid»), `precios_detalle_maximo` («Máximo»), `precios_detalle_minimo` («Mínimo»),
  `precios_detalle_variacion` («Variación»), `precios_detalle_variacion_pct` («Variación %»),
  `precios_detalle_unidad` («Unidad»), `precios_detalle_actualizacion` («Actualización»),
  `precios_nota_orientativos` («Precios orientativos. Pueden variar según el mercado.»),
  `precios_fuente` («Fuente: Metal Sentinel»), `precios_cargando` («Consultando cotizaciones…»),
  `precios_accion_reintentar` («Reintentar»), `precios_desactualizado` («Desactualizado»),
  `precios_aviso_espera` («Acabas de reintentar. Espera un momento antes de volver a consultar.»),
  `precios_aviso_parcial` («No se han podido actualizar todos los metales.»),
  `precios_aviso_desactualizado` («Sin conexión con el proveedor. Se muestran los últimos
  precios conocidos.»), `precios_error_sin_credencial` («El servicio de cotizaciones no está
  configurado en esta versión.»), `precios_error_credencial` («El proveedor ha rechazado la
  credencial o la suscripción.»), `precios_error_no_encontrado` («El servicio de cotizaciones no
  está disponible.»), `precios_error_limite` («Se ha alcanzado el límite de consultas al
  proveedor.»), `precios_error_servidor` («El proveedor no responde en este momento.»),
  `precios_error_sin_conexion` («No se ha podido conectar con el proveedor.»),
  `precios_error_respuesta` («El proveedor ha devuelto una respuesta no válida.»),
  `precios_error_desconocido` («No se ha podido obtener la cotización.»),
  `precios_tendencia_sube` («Sube»), `precios_tendencia_baja` («Baja»),
  `precios_tendencia_plana` («Sin cambio»); bloque `<!-- Herramientas: peso de chapas -->` →
  `chapas_titulo` («Peso de chapas»), `chapas_subtitulo` («Calcula el peso de tu chapa según sus
  medidas»), `chapas_seccion_material` («Material»), `chapas_familia_oro` («ORO»),
  `chapas_familia_plata` («PLATA»), `chapas_seccion_medidas` («Medidas»), `chapas_medida_ancho`
  («Ancho»), `chapas_medida_espesor` («Espesor»), `chapas_medida_largo` («Largo»),
  `chapas_material_oro` («Oro %1$s»), `chapas_material_plata` («Plata %1$s»),
  `chapas_resultado_titulo` («Peso de la chapa»), `chapas_resultado_para` («Calculado para
  %1$s»), `chapas_detalle_volumen` («Volumen»), `chapas_detalle_densidad` («Densidad»),
  `chapas_detalle_pureza` («Pureza»), `chapas_pureza_formato` («%1$s %% (%2$s)»),
  `chapas_dibujo_descripcion` («Chapa de %1$s: %2$s de ancho, %3$s de largo y %4$s de espesor»),
  `chapas_dibujo_medida` («%1$s mm»), `chapas_dibujo_sin_medida` («sin medida»),
  `chapas_nota_aproximado` («Valores aproximados. Considera merma según tu proceso.»),
  `chapas_aviso_rango` («Medida fuera del rango operativo: ancho y largo hasta 10 000 mm,
  espesor hasta 1 000 mm.»). Se reutilizan sin duplicar: `modulo_herramientas_titulo`,
  `modulo_herramientas_imagen`, `metal_cobre`, `metal_paladio`, `metal_plata_fina` (+`_imagen`),
  `oro_ley_18k/14k/12k/9k`, `plata_ley_950/925/900/800`, `oro_aviso_12k`, `plata_aviso_950/900`,
  `unidad_gramos`, `accion_limpiar`, `accion_guardar_favoritos`, `aviso_proximamente` (FR-030).
- [X] T005 [P] En `app/build.gradle.kts` leer la credencial de forma compatible con la caché de
  configuración y volcarla a `BuildConfig`: antes de `android {}`,
  `val rapidApiKey: String = providers.environmentVariable("RAPIDAPI_KEY").orElse(providers.gradleProperty("RAPIDAPI_KEY")).orElse(providers.fileContents(isolated.rootProject.projectDirectory.file("local.properties")).asText.map { texto -> texto.lineSequence().map(String::trim).firstOrNull { it.startsWith("RAPIDAPI_KEY=") }?.substringAfter('=')?.trim().orEmpty() }).getOrElse("")`
  (si `isolated` no compilara con la versión de Gradle del wrapper, usar
  `rootProject.layout.projectDirectory`); en `defaultConfig`,
  `buildConfigField("String", "RAPIDAPI_KEY", "\"${rapidApiKey.replace("\\", "\\\\").replace("\"", "\\\"")}\"")`;
  `if (rapidApiKey.isBlank()) logger.warn("RAPIDAPI_KEY no configurada: la pantalla de precios mostrará «servicio no configurado»")`.
  **Nunca** imprimir el valor. Comentario: la clave queda extraíble del APK (prototipo, R6).
  `buildFeatures.buildConfig` ya está a `true`.
- [X] T006 [P] Añadir `<uses-permission android:name="android.permission.INTERNET" />` en
  `app/src/main/AndroidManifest.xml`, antes de `<application>`. Sin `usesCleartextTraffic`
  (el valor por defecto ya es `false` y solo se usa HTTPS).
- [X] T007 [P] Sustituir las plantillas comentadas de `app/src/main/res/xml/backup_rules.xml`
  por `<full-backup-content><exclude domain="sharedpref" path="cotizaciones.xml"/></full-backup-content>`
  y de `app/src/main/res/xml/data_extraction_rules.xml` por
  `<data-extraction-rules><cloud-backup><exclude domain="sharedpref" path="cotizaciones.xml"/></cloud-backup><device-transfer><exclude domain="sharedpref" path="cotizaciones.xml"/></device-transfer></data-extraction-rules>`,
  con un comentario: la caché de cotizaciones no debe restaurarse en otro dispositivo (R4).

---

## Fase 2 — Fundacional: core, dominio y componentes compartidos

**⚠️ CRÍTICO**: ninguna historia puede empezar hasta que esta fase esté completa. Son cuatro
bloques independientes —core (T008–T010), dominio de cotizaciones (T011–T017 y T023), motor de
chapas (T018–T022), componentes (T024–T025) y el estado del armazón (T026)— que confluyen en T027.

Todo `domain/` es Kotlin puro: sin `android.*`, sin `androidx.*`, sin `R`; `BigDecimal` desde
literales `String`; instantes como `Long` epoch-millis (sin `java.time`: minSdk 24).

- [X] T008 [P] Crear `$SRC/core/util/Reloj.kt`: `interface Reloj { fun ahoraMillis(): Long }` y
  `class RelojSistema : Reloj { override fun ahoraMillis() = System.currentTimeMillis() }`, KDoc
  en español (hermano de `DispatcherProvider`: permite congelar la hora en los tests y decidir
  la caché sin esperar). Registrar en `$SRC/core/di/CoreModule.kt`
  `single<Reloj> { RelojSistema() }` (tipo del grafo: vale para `verify()` y para `get()`, R8).
- [X] T009 [P] Crear `$SRC/core/util/Decimales.kt` con
  `fun parsearDecimalPositivo(texto: String): BigDecimal? = texto.trim().replace(',', '.').toBigDecimalOrNull()?.takeIf { it > BigDecimal.ZERO }`
  (KDoc: coma y punto valen; vacío, no numérico o ≤ 0 → null; es la promoción del
  `parsearCantidad` privado de los cuatro ViewModels, regla del segundo consumidor) y
  `$TEST/core/util/DecimalesTest.kt` (JUnit4): `"0,5"` y `"0.5"` → `BigDecimal("0.5")`; `"10"`;
  `" 7,25 "`; rechaza `""`, `"  "`, `"0"`, `"-1"`, `"abc"`, `"1,2,3"`, `"1.2.3"`.
- [X] T010 [P] Crear en `$TEST/core/util/` la infraestructura de test de corrutinas:
  `TestDispatcherProvider.kt` —
  `class TestDispatcherProvider(dispatcher: TestDispatcher = UnconfinedTestDispatcher()) : DispatcherProvider`
  con `main = io = default = dispatcher` (KDoc: los ViewModels lanzan siempre con
  `dispatchers.main`, así el test no necesita `Dispatchers.setMain`, R9)— y `RelojFalso.kt` —
  `class RelojFalso(var ahoraMillis: Long = 0L) : Reloj { override fun ahoraMillis() = ahoraMillis; fun avanzar(millis: Long) { ahoraMillis += millis } }`.
  **Depende de T008**.
- [X] T011 [P] Crear en `$SRC/domain/model/` los enums de cotizaciones: `MetalCotizado.kt` —
  `enum class MetalCotizado(val simboloApi: String) { ORO("AU"), PLATA("AG"), COBRE("CU"), PALADIO("PD"), RODIO("RH") }`
  con `val analyticsId get() = name.lowercase()` (KDoc: el orden del enum **es** el orden de
  pintado del mockup; el símbolo es el que espera el proveedor, en mayúsculas)—; `UnidadPrecio.kt`
  — `enum class UnidadPrecio { GRAMO, KILO, ONZA_TROY }` + `analyticsId` (KDoc: sin valor
  «desconocida» a propósito: la unidad de origen no confirmada es `unidadOrigen: UnidadPrecio?`
  en la cotización)—; `Tendencia.kt` — `enum class Tendencia { SUBE, BAJA, PLANA; companion object { fun de(variacion: BigDecimal) = when (variacion.signum()) { 1 -> SUBE; -1 -> BAJA; else -> PLANA } } }`—;
  `OrigenDatos.kt` — `enum class OrigenDatos { RED, CACHE, CACHE_EN_ESPERA }`—;
  `MotivoErrorCotizacion.kt` — `enum class MotivoErrorCotizacion { SIN_CREDENCIAL, CREDENCIAL_RECHAZADA, NO_ENCONTRADO, LIMITE_ALCANZADO, SERVIDOR, SIN_CONEXION, RESPUESTA_INVALIDA, DESCONOCIDO }`
  + `analyticsId`, KDoc con la causa de cada uno (tabla de `contracts/metal-quote.md`).
- [X] T012 Crear `$SRC/domain/model/CotizacionMetal.kt` —
  `data class CotizacionMetal(val metal: MetalCotizado, val moneda: String, val ask: BigDecimal, val bid: BigDecimal, val mid: BigDecimal, val maximo: BigDecimal, val minimo: BigDecimal, val variacion: BigDecimal, val variacionPorcentaje: BigDecimal, val unidadOrigen: UnidadPrecio?, val etiquetaUnidadOrigen: String, val instanteMercadoEpochMillis: Long, val obtenidoEnEpochMillis: Long)`
  con `val precioPrincipal: BigDecimal? get() = listOf(mid, ask, bid).firstOrNull { it.signum() > 0 }`
  (KDoc: decisión del autor mid → ask → bid; `open`/`close` llegan a 0 y no se modelan) y
  `val tendencia get() = Tendencia.de(variacion)`— y `$SRC/domain/model/ResultadoCotizacion.kt`
  — `sealed interface ResultadoCotizacion { val metal: MetalCotizado; data class Exito(val cotizacion: CotizacionMetal) : ResultadoCotizacion { override val metal get() = cotizacion.metal }; data class Error(override val metal: MetalCotizado, val motivo: MotivoErrorCotizacion, val ultimaConocida: CotizacionMetal? = null, val causa: Throwable? = null) : ResultadoCotizacion }`
  (KDoc: `ultimaConocida` permite «desactualizado» por fila; `causa` solo para `recordError`, no
  se persiste). **Depende de T011**.
- [X] T013 Crear `$SRC/domain/model/InstantaneaCotizaciones.kt`:
  `data class InstantaneaCotizaciones(val resultados: Map<MetalCotizado, ResultadoCotizacion> = emptyMap(), val instanteIntentoEpochMillis: Long? = null, val origen: OrigenDatos = OrigenDatos.CACHE)`
  con `val estaCompleta: Boolean` (los 5 metales con `Exito`), `val hayErrores: Boolean`,
  `fun esVigente(metal, ahoraMillis, vigenciaMillis): Boolean` (`Exito` con
  `0 <= ahora - obtenidoEn < vigencia`; delta negativo = reloj atrasado = **no** vigente),
  `fun ultimaCotizacionConocida(metal): CotizacionMetal?` (`Exito.cotizacion` o
  `Error.ultimaConocida`), `fun fusionarCon(nuevos: Map<MetalCotizado, ResultadoCotizacion>, instanteIntentoEpochMillis: Long): InstantaneaCotizaciones`
  (un `Error` nuevo hereda `ultimaConocida` del resultado anterior si no trae una; un `Exito`
  sustituye), `val instanteMasRecienteEpochMillis: Long?` (máximo `obtenidoEn` de los éxitos) y
  `companion object { val VACIA = InstantaneaCotizaciones() }`. KDoc: un solo instante global de
  intento + `obtenidoEn` por metal; el instante de éxito no se almacena, se deriva. **Depende de T012**.
- [X] T014 Crear `$SRC/domain/model/PoliticaCacheCotizaciones.kt`:
  `sealed interface DecisionCache { data object Servir : DecisionCache; data object Esperar : DecisionCache; data class Actualizar(val pendientes: Set<MetalCotizado>) : DecisionCache }`
  y `class PoliticaCacheCotizaciones(val vigenciaMillis: Long = 3_600_000L, val esperaReintentoMillis: Long = 60_000L, val esperaTrasLimiteMillis: Long = 300_000L) { fun decidir(guardada: InstantaneaCotizaciones, ahoraMillis: Long): DecisionCache }`
  con el algoritmo de `data-model.md`: pendientes = metales sin `esVigente`; vacío → `Servir`;
  `instanteIntento != null && 0 <= ahora - instanteIntento < espera` → `Esperar`, con `espera`
  = `esperaTrasLimiteMillis` si algún `Error` de la instantánea es `LIMITE_ALCANZADO`, si no
  `esperaReintentoMillis`; en otro caso `Actualizar(pendientes)`. Función pura, sin corrutinas
  (R5). **Depende de T013**.
- [X] T015 [P] Crear `$SRC/domain/model/ConversorUnidadesPrecio.kt`: `object` con
  `val GRAMOS_POR_ONZA_TROY = BigDecimal("31.1034768")`, `val GRAMOS_POR_KILO = BigDecimal("1000")`,
  `const val ESCALA = 10`, `fun gramosPor(unidad: UnidadPrecio): BigDecimal` (GRAMO → ONE) y
  `fun convertir(importe: BigDecimal, desde: UnidadPrecio, hacia: UnidadPrecio): BigDecimal`
  = `importe` si iguales; si no `importe.multiply(gramosPor(hacia)).divide(gramosPor(desde), ESCALA, RoundingMode.HALF_UP)`.
  KDoc: constantes propias (no se importan de otros motores); multiplicación exacta primero y
  **una única división**, criterio de los tres motores existentes. **Depende de T011**.
- [X] T016 Crear `$SRC/domain/repository/CotizacionesRepository.kt` —
  `interface CotizacionesRepository { suspend fun obtenerCotizaciones(): InstantaneaCotizaciones }`
  (KDoc: `suspend`, no `Flow`; sin `forzar`: la política de caché decide, R5)— y en
  `$SRC/domain/usecase/`: `ObtenerCotizacionesUseCase.kt` —
  `class ObtenerCotizacionesUseCase(private val repositorio: CotizacionesRepository) { suspend operator fun invoke(): InstantaneaCotizaciones = repositorio.obtenerCotizaciones() }`—
  y `ConvertirCotizacionUseCase.kt` —
  `class ConvertirCotizacionUseCase { operator fun invoke(cotizacion: CotizacionMetal, hacia: UnidadPrecio): CotizacionMetal? }`:
  `null` si `unidadOrigen == null`; si no, copia con ask/bid/mid/máximo/mínimo/variación
  convertidos con `ConversorUnidadesPrecio.convertir(valor, unidadOrigen, hacia)`,
  `variacionPorcentaje` intacta y `unidadOrigen = hacia`. **Depende de T013 y T015**.
- [X] T017 [P] Crear los tests JVM del dominio de cotizaciones (JUnit4, nombres en backticks
  en español sin tildes, sin mocks, helper privado `assertCerca(esperado: String, real: BigDecimal)`
  con tolerancia 1E-6 como en oro y plata): `$TEST/domain/model/ConversorUnidadesPrecioTest.kt`
  (`31.1034768` oz → `1.0000000000` g exacto con `compareTo`; `1000` g → 1 kg; 1 kg →
  `32.1507465686` oz; identidad; cobre `2.49` oz → `0.0800553…` g; escala 10 `HALF_UP`),
  `$TEST/domain/model/CotizacionMetalTest.kt` (`precioPrincipal` mid → ask → bid → null;
  `Tendencia.de` por signo), `$TEST/domain/model/InstantaneaCotizacionesTest.kt`
  (`estaCompleta`, `esVigente` en 0, 59:59, 60:00 y delta negativo, `fusionarCon` hereda
  `ultimaConocida`, `instanteMasReciente`), `$TEST/domain/model/PoliticaCacheCotizacionesTest.kt`
  (vacía → `Actualizar(5)`; completa a 59:59 → `Servir`; a 60:00 → `Actualizar(5)`; parcial +
  intento hace 30 s → `Esperar`; hace 61 s → `Actualizar(solo fallidos)`; éxito caducado + error
  → `Actualizar(ambos)`; error `LIMITE_ALCANZADO` hace 4 min → `Esperar`, hace 6 → `Actualizar`;
  reloj atrasado → `Actualizar`; TTLs cortos por constructor) y
  `$TEST/domain/usecase/ConvertirCotizacionUseCaseTest.kt` (todos los lineales convertidos, `%`
  intacto, `unidadOrigen == null` → `null`, `unidadOrigen = hacia`, identidad) y
  `$TEST/domain/usecase/ObtenerCotizacionesUseCaseTest.kt` (`runTest` con el
  `FakeCotizacionesRepository` de T023: devuelve exactamente la instantánea del repositorio;
  llama una sola vez; una excepción del repositorio se propaga sin envolver). **Depende de
  T016 y T023**.
- [X] T018 [P] Crear `$SRC/domain/model/MaterialChapa.kt`:
  `enum class FamiliaChapa { ORO, PLATA; val analyticsId get() = name.lowercase() }` y
  `enum class MaterialChapa(val familia: FamiliaChapa, val milesimas: Int, val densidad: BigDecimal, val esSoloTecnica: Boolean = false)`
  con, en este orden, `ORO_18K(ORO, 750, BigDecimal("15.58"))`, `ORO_14K(ORO, 585, BigDecimal("13.07"))`,
  `ORO_12K(ORO, 500, BigDecimal("12.75"), esSoloTecnica = true)`, `ORO_9K(ORO, 375, BigDecimal("11.20"))`,
  `PLATA_950(PLATA, 950, BigDecimal("10.40"), esSoloTecnica = true)`, `PLATA_925(PLATA, 925, BigDecimal("10.36"))`,
  `PLATA_900(PLATA, 900, BigDecimal("10.31"), esSoloTecnica = true)`, `PLATA_800(PLATA, 800, BigDecimal("10.14"))`;
  `val finura: BigDecimal get() = BigDecimal(milesimas).movePointLeft(3)`,
  `val analyticsId: String get() = name.substringAfter('_').lowercase()`, y en `companion object`
  `fun deFamilia(familia): List<MaterialChapa>` y `fun porDefecto(familia)` (ORO → 18K, PLATA →
  925). KDoc citando §2, §3.1 (14K es 585, nunca 14/24), §5.1 (densidades **orientativas**) y
  §19 (por qué un enum propio y no `LeyOro`/`LeyPlata`; un test de paridad vigila la coherencia).
- [X] T019 Crear `$SRC/domain/model/CalculoChapa.kt`:
  `data class CalculoChapa(val material: MaterialChapa, val ancho: BigDecimal, val largo: BigDecimal, val espesor: BigDecimal, val areaMm2: BigDecimal, val volumenMm3: BigDecimal, val volumenCm3: BigDecimal, val densidad: BigDecimal, val peso: BigDecimal, val metalFino: BigDecimal, val liga: BigDecimal)`
  con `companion object { val MM3_POR_CM3 = BigDecimal("1000"); internal fun de(ancho, largo, espesor, material): CalculoChapa }`:
  `areaMm2 = ancho × largo`, `volumenMm3 = areaMm2 × espesor`, `volumenCm3 = volumenMm3.movePointLeft(3)`
  (÷ `MM3_POR_CM3` exacto), `peso = volumenCm3 × material.densidad`,
  `metalFino = peso × material.finura`, `liga = peso − metalFino`, `check(liga.signum() >= 0)`.
  KDoc: **sin ningún redondeo** (§10.1); no hay `ESCALA` porque no hay división (llegará con los
  inversos §8.3, fuera de alcance); constante propia como `FINURA_ORIGEN` en los otros motores.
  **Depende de T018**.
- [X] T020 Crear `$SRC/domain/usecase/CalcularPesoChapaUseCase.kt`:
  `class CalcularPesoChapaUseCase { operator fun invoke(ancho: BigDecimal, largo: BigDecimal, espesor: BigDecimal, material: MaterialChapa): CalculoChapa }`
  con `require(ancho > ZERO) { "El ancho debe ser mayor que cero: $ancho" }` (ídem largo,
  espesor, §11.1) y `return CalculoChapa.de(...)`. KDoc: los límites operativos de §11.4 son
  control de interfaz (ViewModel), no del motor. **Depende de T019**.
- [X] T021 [P] Crear `$TEST/domain/usecase/CalcularPesoChapaUseCaseTest.kt` (motor real, sin
  mocks, `compareTo == 0` porque todo es exacto, más `assertCerca`): chapa de referencia 10 × 20
  × 0,5 mm (§6, §7) en 18K → `peso 1.558`, `metalFino 1.1685`, `liga 0.3895`, `areaMm2 200`,
  `volumenMm3 100`, `volumenCm3 0.1`; tabla §7 completa: 14K `1.307`/`0.764595`/`0.542405`,
  12K `1.275`/`0.6375`/`0.6375`, 9K `1.120`/`0.4200`/`0.7000`, 950 `1.040`/`0.9880`/`0.0520`,
  925 `1.036`/`0.9583`/`0.0777`, 900 `1.031`/`0.9279`/`0.1031`, 800 `1.014`/`0.8112`/`0.2028`;
  `setScale(2, HALF_UP)` de los 8 pesos = «Mostrar» de §7 (1.56/1.31/1.28/1.12/1.04/1.04/1.03/1.01);
  14K usa 585 (`0.764595`, no `0.762416…`); invariantes sobre `["0.1","0.5","10","123.45","10000"]`
  × 8 materiales: `peso == volumenCm3 × densidad`, `metalFino + liga == peso`,
  `metalFino < peso`, simetría ancho↔largo, `peso == ancho×largo×espesor×densidad ÷ 1000`
  calculado aparte; `assertThrows(IllegalArgumentException)` para cero y negativo en cada medida;
  densidades iguales a los literales de §5.1; `esSoloTecnica` exactamente {12K, 950, 900};
  **paridad**: cada material ORO tiene una `LeyOro` con iguales `milesimas` y `esSoloTecnica`,
  y cada PLATA una `LeyPlata`. **Depende de T020**.
- [X] T022 Registrar en `$SRC/core/di/DomainModule.kt` con `factoryOf`:
  `ObtenerCotizacionesUseCase`, `ConvertirCotizacionUseCase` y `CalcularPesoChapaUseCase` (los
  «trece» casos de uso pasan a dieciséis). `ObtenerCotizacionesUseCase` necesita
  `CotizacionesRepository`, que se registra en T037: hasta entonces `KoinModulesTest` fallará →
  ejecutar T022 y T037 en la misma sesión o dejar el `factoryOf` comentado hasta T037.
  **Depende de T016 y T020**.
- [X] T023 [P] Crear los fakes y la muestra real en `$TEST/data/`:
  `source/remote/FakeClienteHttp.kt` (cola de `RespuestaHttp`/excepciones y registro de la
  última URL y cabeceras), `source/remote/FakeCotizacionesRemoteDataSource.kt` (mapa
  `MetalCotizado → CotizacionMetal | MetalSentinelException`, contador de llamadas por metal,
  `CompletableDeferred` opcional para simular concurrencia), `source/local/FakeCotizacionesLocalDataSource.kt`
  (una `var guardada: InstantaneaCotizaciones?`), `repository/FakeCotizacionesRepository.kt`
  (respuesta programable, contador, excepción opcional), y
  `source/remote/MuestrasMetalSentinel.kt` — `object` con `val AU_USD` = el JSON literal del
  contrato y, tras T001, `val AU_EUR` = la respuesta real anonimizada. Estas clases dependen de
  interfaces creadas en US1 (T028–T030): crear ahora las que solo dependen del dominio
  (`FakeCotizacionesRepository`, `MuestrasMetalSentinel`) y el resto en T033. **Depende de T016**.
- [X] T024 [P] Añadir a `$SRC/ui/components/SelectorSegmentado.kt` el parámetro
  `val iconRes: Int? = null` en `OpcionSegmento`; en `Segmento`, si `iconRes != null` pintar
  `Icon(painterResource(iconRes), contentDescription = null, tint = if (activa) JewelryColors.Background else JewelryColors.TextSecondary, modifier = Modifier.size(18.dp))`
  + `Spacer(Modifier.width(JewelrySpacing.Sm))` en el hueco del check (el check no se pinta con
  icono: la píldora y la semántica `selected` ya transmiten el estado). KDoc del porqué (el
  selector de sub-herramientas del mockup lleva icono) y de que el valor por defecto deja oro,
  plata y soldaduras byte a byte iguales.
- [X] T025 [P] Añadir a `$SRC/ui/components/Formularios.kt`: un `private @Composable fun MarcoCampo(borde: Color, modifier: Modifier = Modifier, contenido: @Composable RowScope.() -> Unit)`
  (fondo `JewelryColors.Background`, `RoundedCornerShape(JewelryRadius.Medium)`, borde 1 dp,
  padding horizontal `Md` / vertical `Sm`, `verticalAlignment = CenterVertically`) que
  `CampoCantidad` pasa a usar **sin cambiar su firma ni su aspecto** (mantiene `heightIn(min = 64.dp)`,
  `CifraGrande` centrada y el sufijo `unidad_gramos`), y
  `@Composable fun CampoMedida(etiqueta: String, valor: String, onCambio: (String) -> Unit, iconRes: Int, unidad: String, modifier: Modifier = Modifier, acento: Color = JewelryColors.GoldPrimary, borde: Color = JewelryColors.Border, error: Boolean = false, imeAction: ImeAction = ImeAction.Next)`:
  `MarcoCampo(borde = if (error) JewelryColors.Danger else borde)` con `heightIn(min = 56.dp)`,
  `Icon(iconRes, 20.dp, tint = acento)`, `Column(weight 1f) { Text(etiqueta, labelMedium, TextMuted); BasicTextField(valor, onCambio, textStyle = bodyLarge.copy(fontSize = 20.sp, fontWeight = Bold, color = TextPrimary), keyboardOptions = KeyboardOptions(keyboardType = Decimal, imeAction = imeAction), singleLine = true, cursorBrush = SolidColor(acento)) }`,
  `Text(unidad, titleMedium, color = acento)`; `semantics(mergeDescendants = true)` en el marco.
  KDoc: tres campos de 34 sp sin etiqueta (el `CampoCantidad`) son demasiado pesados para
  ancho/espesor/largo; regla del segundo consumidor.
- [X] T026 [P] Crear `$SRC/ui/herramientas/HerramientasUiState.kt` con el enum
  `enum class Subherramienta { PRECIOS, CHAPAS; val analyticsId: String get() = name.lowercase() }`
  y `data class HerramientasUiState(val subherramienta: Subherramienta? = null)` (KDoc: el
  constructor sin argumentos **es** la primera visita: solo el selector, FR-002; conceptos de
  UI, como `HomeModule`), y `$SRC/ui/herramientas/PresentacionHerramientas.kt` (interno al
  paquete) con `internal val Subherramienta.etiquetaRes: Int` (`herramientas_subherramienta_precios`
  / `herramientas_subherramienta_chapas`) e `internal val Subherramienta.iconRes: Int`
  (`ic_grafica` / `ic_capas`). **Depende de T003 y T004**.
- [X] T027 Puerta de la fase fundacional y de la regresión:
  `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"` y comprobar
  que `./gradlew :app:testDebugUnitTest` pasa (tests nuevos de T009, T017 y T021 en verde;
  `KoinModulesTest` en verde con T022 completo o con el `factoryOf` de cotizaciones diferido),
  `./gradlew :app:assembleDebug` pasa con `BuildConfig.RAPIDAPI_KEY` generado y el permiso en el
  manifest fusionado, y las `@Preview` de oro, plata y soldaduras renderizan igual que antes de
  T024–T025. **Depende de T001–T026** (T001 puede seguir abierto si falta la credencial).

**Checkpoint**: dominio de cotizaciones y motor de chapas completos y en verde en JVM;
componentes compartidos ampliados sin regresión; build con credencial, permiso y backup listos.

---

## Fase 3 — User Story 1: entrar en Herramientas y consultar el precio de los metales (P1) 🎯 MVP

**Objetivo**: el módulo de Herramientas deja de ser andamiaje: selector de dos sub-herramientas
(solo eso en la primera visita), y PRECIO METALES con las cinco cotizaciones en €/g, caché de una
hora persistida y pie con nota, fuente y hora de actualización.

**Test independiente**: abrir Home → Herramientas, ver solo el selector; pulsar PRECIO METALES y
ver los cinco precios; en el panel del proveedor, exactamente 5 peticiones; salir, forzar el
cierre de la app, volver dentro de la hora: mismos precios, 0 peticiones.

- [X] T028 [P] [US1] Crear en `$SRC/data/source/remote/`: `ClienteHttp.kt` —
  `data class RespuestaHttp(val codigo: Int, val cuerpo: String)` e
  `interface ClienteHttp { @Throws(IOException::class) fun get(url: String, cabeceras: Map<String, String>): RespuestaHttp }`
  (KDoc: bloqueante a propósito; quien salta de hilo es el data source; interfaz para poder
  cambiar de cliente, R2)— y `ClienteHttpUrlConnection.kt` —
  `class ClienteHttpUrlConnection(private val tiempoConexionMs: Int = 10_000, private val tiempoLecturaMs: Int = 15_000) : ClienteHttp`
  con `java.net.HttpURLConnection` (`URL(url).openConnection() as HttpURLConnection`; HTTPS lo
  impone la URL de producción, y así el test puede usar un servidor local), `requestMethod = "GET"`,
  `connectTimeout`/`readTimeout`, `setRequestProperty` por cabecera, lee `inputStream` si
  `responseCode < 400` y `errorStream` si no (cuerpo vacío si null), `disconnect()` en `finally`;
  **nunca** registra cabeceras ni URL.
- [X] T029 [P] [US1] Crear en `$SRC/data/source/remote/`: `BigDecimalExactoSerializer.kt` —
  `object BigDecimalExactoSerializer : KSerializer<BigDecimal>` con
  `descriptor = PrimitiveSerialDescriptor("BigDecimalExacto", PrimitiveKind.STRING)`,
  `deserialize = BigDecimal((decoder as JsonDecoder).decodeJsonElement().jsonPrimitive.content)`
  y `serialize = encoder.encodeString(value.toPlainString())` (KDoc: toma el literal del cable,
  versión de red de «`BigDecimal` desde `String`», R3)—; `MetalSentinelDto.kt` —
  `@Serializable data class RespuestaMetalSentinelDto(@SerialName("ID") val id: Long? = null, val results: List<CotizacionMetalSentinelDto> = emptyList())`
  y `@Serializable data class CotizacionMetalSentinelDto(val symbol: String, val currency: String, @Serializable(with = BigDecimalExactoSerializer::class) val ask: BigDecimal, …mid, bid, high, low, change, changePercentage…, val timestamp: Long, val unit: String)`;
  `open`, `close`, `originalTime`, `extra` **no se declaran**—; y `MetalSentinelException.kt` —
  `class MetalSentinelException(val motivo: MotivoErrorCotizacion, mensaje: String, causa: Throwable? = null) : Exception(mensaje, causa)`
  (KDoc: el mensaje jamás incluye la credencial ni el cuerpo del proveedor).
- [X] T030 [US1] Crear `$SRC/data/source/remote/CotizacionesRemoteDataSource.kt` —
  `interface CotizacionesRemoteDataSource { suspend fun obtener(metal: MetalCotizado): CotizacionMetal }`
  (lanza `MetalSentinelException`)— y `$SRC/data/source/remote/MetalSentinelDataSource.kt` —
  `class MetalSentinelDataSource(private val cliente: ClienteHttp, private val dispatchers: DispatcherProvider, private val reloj: Reloj, private val credencial: String = BuildConfig.RAPIDAPI_KEY) : CotizacionesRemoteDataSource`
  con `private val json = Json { ignoreUnknownKeys = true }` y en `companion object`
  `URL_BASE = "https://metal-sentinel.p.rapidapi.com/api/metal-quote"`,
  `HOST = "metal-sentinel.p.rapidapi.com"`, **`PARAMETRO_METAL = "metal"`** (o el que confirmó
  T001; una sola constante, sin fallback), `MONEDA = "EUR"`. `obtener`: `credencial.isBlank()` →
  `MetalSentinelException(SIN_CREDENCIAL)` **sin llamar al cliente**;
  `withContext(dispatchers.io) { cliente.get("$URL_BASE?$PARAMETRO_METAL=${metal.simboloApi}&currency=$MONEDA", mapOf("x-rapidapi-host" to HOST, "x-rapidapi-key" to credencial, "Accept" to "application/json")) }`
  con `IOException` → `SIN_CONEXION`; código 401/403 → `CREDENCIAL_RECHAZADA`, 404 →
  `NO_ENCONTRADO`, 429 → `LIMITE_ALCANZADO`, 500..599 → `SERVIDOR`, otro ≥ 400 → `DESCONOCIDO`;
  `json.decodeFromString<RespuestaMetalSentinelDto>` con `SerializationException`/
  `IllegalArgumentException` → `RESPUESTA_INVALIDA`; `results.firstOrNull { it.symbol.equals(metal.simboloApi, ignoreCase = true) }`
  ausente o `currency != MONEDA` (ignore case) → `RESPUESTA_INVALIDA`; catch-all → `DESCONOCIDO`
  con causa (relanzando `CancellationException`). Mapeo a `CotizacionMetal`: `mapearUnidad(unit)`
  (`OUNCE|OZ|OZT|TROY_OUNCE` → `ONZA_TROY`, `GRAM|G` → `GRAMO`, `KILOGRAM|KG` → `KILO`, otro →
  `null`; comparación en mayúsculas sin espacios), `etiquetaUnidadOrigen = unit`,
  `instanteMercadoEpochMillis = if (timestamp > 1_000_000_000_000L) timestamp else timestamp * 1000`,
  `obtenidoEnEpochMillis = reloj.ahoraMillis()`. **Depende de T028, T029 y (contrato) T001**.
- [X] T031 [P] [US1] Crear en `$SRC/data/source/local/`: `CotizacionesLocalDataSource.kt` —
  `interface CotizacionesLocalDataSource { suspend fun leer(): InstantaneaCotizaciones?; suspend fun guardar(instantanea: InstantaneaCotizaciones) }`—;
  `InstantaneaPersistidaDto.kt` — `@Serializable` `InstantaneaPersistidaDto(val version: Int = 1, val instanteIntentoEpochMillis: Long? = null, val resultados: List<ResultadoPersistidoDto>)`,
  `ResultadoPersistidoDto(val metal: String, val cotizacion: CotizacionPersistidaDto? = null, val motivoError: String? = null, val ultimaConocida: CotizacionPersistidaDto? = null)`,
  `CotizacionPersistidaDto` con `moneda`, los siete importes como `String`, `unidadOrigen: String?`,
  `etiquetaUnidadOrigen`, `instanteMercadoEpochMillis`, `obtenidoEnEpochMillis` (KDoc: DTOs
  propios, independientes del proveedor; `origen` y `causa` no se persisten)—; y
  `CodificadorInstantanea.kt` — `class CodificadorInstantanea(private val json: Json = Json { ignoreUnknownKeys = true }) { fun codificar(instantanea: InstantaneaCotizaciones): String; fun decodificar(texto: String): InstantaneaCotizaciones? }`
  (`BigDecimal` ↔ `toPlainString()`; enums por nombre con `entries.firstOrNull`; entrada con
  metal o motivo desconocido → se **descarta**; JSON ilegible → `null`). **Depende de T013**.
- [X] T032 [US1] Crear `$SRC/data/source/local/SharedPreferencesCotizacionesLocalDataSource.kt`:
  `class SharedPreferencesCotizacionesLocalDataSource(private val context: Context, private val dispatchers: DispatcherProvider) : CotizacionesLocalDataSource`
  con `private val codificador = CodificadorInstantanea()`, `companion object { const val FICHERO = "cotizaciones"; const val CLAVE = "instantanea_json" }`,
  `private val preferencias by lazy { context.getSharedPreferences(FICHERO, Context.MODE_PRIVATE) }`;
  `leer()` en `withContext(dispatchers.io)`: texto → `codificador.decodificar`, y si es `null`
  con texto presente borra la clave; `guardar()` en `dispatchers.io` con `edit().putString(CLAVE, codificador.codificar(instantanea)).commit()`.
  KDoc: ~20 líneas de pegamento, sin test JVM; una sola clave = atómico (R4). **Depende de T031**.
- [X] T033 [US1] Crear `$SRC/data/repository/CotizacionesRepositoryImpl.kt`:
  `class CotizacionesRepositoryImpl(private val remoto: CotizacionesRemoteDataSource, private val local: CotizacionesLocalDataSource, private val reloj: Reloj, private val politica: PoliticaCacheCotizaciones = PoliticaCacheCotizaciones()) : CotizacionesRepository`
  con `private val cerrojo = Mutex()`, `private var enMemoria: InstantaneaCotizaciones? = null`;
  `obtenerCotizaciones() = cerrojo.withLock { val guardada = enMemoria ?: (local.leer() ?: InstantaneaCotizaciones.VACIA); val ahora = reloj.ahoraMillis(); when (val decision = politica.decidir(guardada, ahora)) { Servir -> guardada.copy(origen = CACHE).also { enMemoria = it }; Esperar -> guardada.copy(origen = CACHE_EN_ESPERA).also { enMemoria = it }; is Actualizar -> { val nuevos = supervisorScope { decision.pendientes.map { metal -> async { consultar(metal) } }.awaitAll() }.associateBy { it.metal }; val fusionada = guardada.fusionarCon(nuevos, ahora).copy(origen = RED); enMemoria = fusionada; local.guardar(fusionada); fusionada } } }`;
  `private suspend fun consultar(metal): ResultadoCotizacion = try { Exito(remoto.obtener(metal)) } catch (e: CancellationException) { throw e } catch (e: MetalSentinelException) { Error(metal, e.motivo, causa = e.cause) }`
  (cualquier otra excepción sube: es un bug, no un error de red). KDoc: el `Mutex` alrededor de
  toda la operación es el *single-flight*; solo los pendientes tocan la red. **Depende de T014,
  T030 y T031**.
- [X] T034 [US1] Registrar en `$SRC/core/di/DataModule.kt`:
  `single<ClienteHttp> { ClienteHttpUrlConnection() }`,
  `single { MetalSentinelDataSource(get(), get(), get()) } bind CotizacionesRemoteDataSource::class`,
  `single { SharedPreferencesCotizacionesLocalDataSource(androidContext(), get()) } bind CotizacionesLocalDataSource::class`,
  `single<CotizacionesRepository> { CotizacionesRepositoryImpl(get(), get(), get()) }`. Comentario:
  los data sources van concretos + `bind` para que `verify()` inspeccione sus constructores
  (solo mira el tipo primario, R8); el repositorio por interfaz, regla del proyecto. Completar
  T022 si quedó diferido. **Depende de T032 y T033**.
- [X] T035 [P] [US1] Completar los fakes de T023 que dependen de las interfaces de datos
  (`FakeClienteHttp`, `FakeCotizacionesRemoteDataSource`, `FakeCotizacionesLocalDataSource`) y
  crear `$TEST/data/source/remote/MetalSentinelDataSourceTest.kt` (`runTest`,
  `TestDispatcherProvider`, `RelojFalso(1_787_670_000_000)`, `FakeClienteHttp`,
  `credencial = "clave-de-prueba"`): parsea la muestra real → `mid == BigDecimal("4606.4")`,
  `variacion == BigDecimal("-45.30000000000018")`, `unidadOrigen == ONZA_TROY`,
  `etiquetaUnidadOrigen == "OUNCE"`, `instanteMercado == 1_787_665_680_000`, `obtenidoEn` del
  reloj; unidad `"LB"` → `null` + etiqueta; `results` con dos elementos → elige el del símbolo;
  `currency: "USD"` → `RESPUESTA_INVALIDA`; JSON roto → `RESPUESTA_INVALIDA`; códigos
  401/403/404/429/500/503 → motivos; `IOException` → `SIN_CONEXION`; credencial vacía →
  `SIN_CREDENCIAL` y el fake **no recibe llamada**; URL exacta
  `…/api/metal-quote?metal=AU&currency=EUR` (blinda `PARAMETRO_METAL`) y cabeceras
  `x-rapidapi-host`, `x-rapidapi-key`, `Accept`. Y `$TEST/data/source/remote/ClienteHttpUrlConnectionTest.kt`
  con `com.sun.net.httpserver.HttpServer` en `localhost:0` (HTTP plano; la URL de producción
  es HTTPS): 200 con cuerpo, 429 con cuerpo de error, cabeceras recibidas. **Depende de T030**.
- [X] T036 [P] [US1] Crear `$TEST/data/source/local/CodificadorInstantaneaTest.kt`: ida y vuelta de una instantánea con 4 éxitos + 1 error con `ultimaConocida` conserva
  `BigDecimal` exacto (`compareTo`), instantes, `unidadOrigen == null` con etiqueta cruda;
  `version == 1` presente en el JSON; metal desconocido («PLATINO») y motivo desconocido se
  descartan; JSON corrupto → `null`; `origen` no viaja. **Depende de T031**.
- [X] T037 [P] [US1] Crear `$TEST/data/repository/CotizacionesRepositoryImplTest.kt` (`runTest`,
  fakes de T023/T035, `RelojFalso`): 1.ª llamada → 5 consultas al remoto, `origen == RED`, el
  local guarda; 2.ª llamada 59 min después → 0 consultas, `CACHE`; 61 min → 5; fallo parcial
  (rodio lanza `SIN_CONEXION`) → 4 `Exito` + 1 `Error`; reintento 30 s después → 0 consultas y
  `CACHE_EN_ESPERA`; 61 s después → **solo rodio** consultado; éxito caducado que ahora falla →
  `Error` con `ultimaConocida` = la anterior; **muerte de proceso** = repositorio nuevo con el
  mismo fake local → `CACHE` sin red; local vacío o corrupto → parte de `VACIA`; dos llamadas
  concurrentes (remoto con `CompletableDeferred`) → 5 consultas en total (single-flight);
  excepción no `MetalSentinelException` del remoto se propaga. **Depende de T033**.
- [X] T038 [P] [US1] Crear `$SRC/ui/herramientas/HerramientasViewModel.kt` (sobre el
  `HerramientasUiState` de T026) —
  `class HerramientasViewModel(private val analytics: AnalyticsRepository) : ViewModel()` con
  `StateFlow` único, `init { analytics.logScreenView("herramientas") }` (el mismo nombre que
  emitía el placeholder: conserva la serie, FR-028), `fun onSubherramientaSeleccionada(s)`: si es
  la actual no hace nada; si no `HerramientasUiState(s)` + `logEvent("herramientas_subherramienta", mapOf("subherramienta" to s.analyticsId))`.
  **Depende de T026**.
- [X] T039 [P] [US1] Crear en `$SRC/ui/herramientas/precios/`: `PreciosMetalesUiState.kt` —
  `enum class FasePrecios { CARGANDO, LISTO, PARCIAL, ERROR }`,
  `data class FilaMetalPrecio(val metal: MetalCotizado, val precioFormateado: String?, val unidad: UnidadPrecio?, val etiquetaUnidadOrigen: String?, val tendencia: Tendencia?, val error: MotivoErrorCotizacion?, val desactualizada: Boolean)`,
  `data class DetalleMercado(val metal: MetalCotizado, val moneda: String, val ask: String, val bid: String, val maximo: String, val minimo: String, val variacion: String, val variacionPorcentaje: String, val tendencia: Tendencia, val unidad: UnidadPrecio?, val etiquetaUnidadOrigen: String, val instanteMercadoEpochMillis: Long, val desactualizada: Boolean)`,
  `data class PreciosMetalesUiState(val fase: FasePrecios = CARGANDO, val reintentando: Boolean = false, val filas: List<FilaMetalPrecio> = emptyList(), val unidad: UnidadPrecio = GRAMO, val seleccionado: MetalCotizado = ORO, val detalle: DetalleMercado? = null, val errorGlobal: MotivoErrorCotizacion? = null, val origen: OrigenDatos? = null, val ultimaConsultaEpochMillis: Long? = null, val puedeReintentar: Boolean = false, val avisoEspera: Boolean = false)`
  (KDoc: todo lo textual formateado salvo las **fechas**, que viajan como `Long` porque su
  formato depende del idioma y lo pone la vista (FR-030); lo que la vista traduce o colorea
  viaja como enum; por qué no se reutiliza `core/ui/UiState`, R10)—; `FormatoPrecios.kt` — `internal object FormatoPrecios`
  con `fun importe(v: BigDecimal): String` (`|v| >= 1` → `setScale(2, HALF_UP)`, si no
  `setScale(4, HALF_UP)`; `toPlainString()`; parte entera agrupada de 3 en 3 con `.`; coma
  decimal), `fun variacion(v)` (= importe con prefijo `+` si `signum() > 0`),
  `fun porcentaje(v)` (`setScale(2, HALF_UP)` con signo `+`/`-`, coma); **sin** formateo de
  fechas: el ViewModel no conoce el idioma—; y
  `PresentacionPrecios.kt` (interno) — `MetalCotizado.nombreRes` (`metal_oro`, `metal_plata`,
  `metal_cobre`, `metal_paladio`, `metal_rodio`), `MetalCotizado.imagenRes` (`modulo_oro`,
  `modulo_plata`, `cobre`, `paladio`, `rodio`), `MetalCotizado.imagenDescripcionRes`,
  `UnidadPrecio.etiquetaRes` (`precios_unidad_*`), `UnidadPrecio.simboloRes` (`unidad_euro_*`),
  `MotivoErrorCotizacion.mensajeRes` (`precios_error_*`), `Tendencia.color`
  (`Success`/`Danger`/`TextMuted`), `Tendencia.descripcionRes` (`precios_tendencia_*`) y
  `@Composable internal fun fechaHoraLocal(epochMillis: Long): String` =
  `DateUtils.formatDateTime(ctx, epochMillis, FORMAT_SHOW_DATE or FORMAT_ABBREV_MONTH or FORMAT_SHOW_YEAR) + " · " + DateUtils.formatDateTime(ctx, epochMillis, FORMAT_SHOW_TIME)`
  con `ctx = LocalContext.current` («25 ago 2026 · 10:33» en español; localizado por el
  sistema, sin meses hardcodeados, FR-030; `android.text.format.DateUtils` es API 3).
  **Depende de T011**.
- [X] T040 [US1] Crear `$SRC/ui/herramientas/precios/PreciosMetalesViewModel.kt`:
  `class PreciosMetalesViewModel(private val obtenerCotizaciones: ObtenerCotizacionesUseCase, private val convertirCotizacion: ConvertirCotizacionUseCase, private val analytics: AnalyticsRepository, private val dispatchers: DispatcherProvider) : ViewModel()`
  con `StateFlow` único, `private var instantanea: InstantaneaCotizaciones? = null`,
  `private var carga: Job? = null`, `init { analytics.logScreenView("herramientas_precios"); cargar(esReintento = false) }`;
  `private fun cargar(esReintento: Boolean)`: si `carga?.isActive == true` return;
  `carga = viewModelScope.launch(dispatchers.main) { _uiState.update { it.copy(fase = if (it.filas.isEmpty()) CARGANDO else it.fase, reintentando = esReintento) }; try { val nueva = obtenerCotizaciones(); instantanea = nueva; registrar(nueva); _uiState.value = derivar(nueva, _uiState.value) } catch (e: CancellationException) { throw e } catch (e: Exception) { analytics.recordError(e); _uiState.update { it.copy(fase = ERROR, errorGlobal = DESCONOCIDO, reintentando = false, puedeReintentar = true) } } }`.
  En esta historia `derivar` cubre: filas en el orden del enum con `precioFormateado = FormatoPrecios.importe(convertir(precioPrincipal))`
  (`convertirCotizacion(cotizacion, unidad)?.precioPrincipal`; si `unidadOrigen == null`, el
  `precioPrincipal` de origen con `etiquetaUnidadOrigen` y `unidad = null`; si
  `precioPrincipal == null` → `precioFormateado = null` y la vista pinta «—»), `tendencia`,
  `fase = LISTO` si `estaCompleta`, `origen`, `ultimaConsultaEpochMillis = instanteMasRecienteEpochMillis`;
  `registrar`: `logEvent("herramientas_precios_cargados", mapOf("fuente" to if (origen == RED) "red" else "cache", "parcial" to (!estaCompleta).toString()))`
  cuando hay ≥ 1 éxito. Los casos PARCIAL/ERROR/desactualizado/espera/reintento llegan en US5
  (dejar las ramas mínimas: sin ningún éxito → `fase = ERROR`, `errorGlobal` = motivo más
  repetido). Companion con `SCREEN_NAME`, nombres de evento y params. **Depende de T039**.
- [X] T041 [US1] Registrar `viewModelOf(::HerramientasViewModel)` y
  `viewModelOf(::PreciosMetalesViewModel)` en `$SRC/core/di/ViewModelModule.kt`. **Depende de
  T038 y T040**.
- [X] T042 [US1] Crear `$SRC/ui/herramientas/precios/PreciosMetalesContent.kt` y
  `PreciosMetalesSection.kt`: `PreciosMetalesSection(modifier: Modifier = Modifier, viewModel: PreciosMetalesViewModel = koinViewModel())`
  colecta con `collectAsStateWithLifecycle()` y llama a
  `PreciosMetalesContent(uiState, onUnidadSeleccionada, onMetalSeleccionado, onReintentar, modifier)`
  (en US1 los tres callbacks se pasan pero solo se usan los de US2/US5). `Content` pinta una
  `Column(verticalArrangement = spacedBy(JewelrySpacing.Md))` **sin** scaffold, scroll,
  `imePadding` ni padding exterior: `TarjetaAcento(acento = TealPrimary)` con título
  `precios_titulo` (titleMedium 17 sp, `TealPrimary`), subtítulo
  `precios_actualizado(fechaHoraLocal(ultimaConsultaEpochMillis))` cuando no es null (bodySmall,
  `TextMuted`; con `origen == CACHE` añade `precios_origen_cache`), y, si
  `fase == CARGANDO && filas.isEmpty()`, `CircularProgressIndicator(color = TealPrimary)` +
  `precios_cargando`; si no, cinco `FilaMetalPrecio` privadas: `Row` con `Image(imagenRes, 44.dp)`,
  `Column { Text(nombreRes, bodyLarge, TextPrimary); Text(metal.simboloApi, labelMedium, TealPrimary) }`,
  `Text(precioFormateado ?: "—", CifraGrande.copy(fontSize = 24.sp), TealPrimary)`,
  `Text(stringResource(unidad.simboloRes) o etiquetaUnidadOrigen, bodyMedium, GoldPrimary)`,
  `Icon(ic_chevron rotado -90°/90° según tendencia, tint = tendencia.color, contentDescription = tendencia.descripcionRes)`;
  `semantics(mergeDescendants = true)`; pie con `precios_nota_orientativos` y `precios_fuente`
  (bodySmall, `TextMuted` / `TealPrimary`). `@Preview` con estado `LISTO` de cinco filas y otro
  `CARGANDO`. La tarjeta de mercado, el selector de unidad y los estados de error llegan en
  US2/US5. **Depende de T039**.
- [X] T043 [US1] Crear `$SRC/ui/herramientas/HerramientasScreen.kt` con el contrato de pantalla:
  `HerramientasScreen(onInfo: () -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: HerramientasViewModel = koinViewModel())`
  que colecta con `collectAsStateWithLifecycle()` y llama a
  `HerramientasContent(uiState, onSubherramientaSeleccionada = viewModel::onSubherramientaSeleccionada, onInfo, onBack, modifier, precios = { PreciosMetalesSection() }, chapas = { Text(stringResource(R.string.placeholder_pendiente), …) })`
  (el slot de chapas se cablea en US3), y
  `HerramientasContent(uiState: HerramientasUiState, onSubherramientaSeleccionada: (Subherramienta) -> Unit, onInfo: () -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier, precios: @Composable () -> Unit, chapas: @Composable () -> Unit)`
  sin estado con `@Preview(widthDp = 411, heightDp = 891)` privadas (primera visita y PRECIOS con
  marcador). Dentro de `JewelryScaffold(onInfo, title = stringResource(R.string.modulo_herramientas_titulo), onBack)`,
  `Column(fillMaxSize, verticalScroll, imePadding, padding(Md), spacedBy(Md))`:
  `SelectorSegmentado(opciones = Subherramienta.entries.map { OpcionSegmento(stringResource(it.etiquetaRes), iconRes = it.iconRes) }, seleccionada = uiState.subherramienta?.ordinal ?: -1, onSeleccion = { onSubherramientaSeleccionada(Subherramienta.entries[it]) })`
  (dorado por defecto: lenguaje de acción, FR-026); `when (uiState.subherramienta) { null -> TarjetaPrimeraVisita(); PRECIOS -> precios(); CHAPAS -> chapas() }`;
  `TarjetaPrimeraVisita` privada = `TarjetaAcento(acento = TealPrimary)` con
  `Image(modulo_herramientas, modulo_herramientas_imagen, 96.dp)`,
  `herramientas_primera_visita_titulo` (titleMedium, `TealPrimary`) y
  `herramientas_primera_visita_texto` (bodyMedium, `TextSecondary`). Sin botones de acción ni
  barra inferior (FR-025, FR-027). **Depende de T024, T038, T041 y T042**.
- [X] T044 [US1] Sustituir en `$SRC/ui/navigation/AppNavHost.kt` el
  `composable<Route.Herramientas> { PlaceholderScreen(...) }` por
  `composable<Route.Herramientas> { HerramientasScreen(onInfo = onInfo, onBack = onBack) }`.
  `Route.Herramientas`, `HomeModule.HERRAMIENTAS` y la tarjeta de Home ya existen y no se tocan;
  `PlaceholderScreen` sigue importado para Favoritos y Ajustes (FR-001, SC-011). **Depende de T043**.
- [X] T045 [P] [US1] Crear `$TEST/ui/herramientas/HerramientasViewModelTest.kt` (mockk relaxed
  para `AnalyticsRepository`): estado inicial `subherramienta == null`;
  `logScreenView("herramientas")` exactamente una vez; elegir CHAPAS actualiza el estado y emite
  `herramientas_subherramienta {subherramienta=chapas}`; volver a elegir la misma no re-emite;
  cambiar a PRECIOS emite de nuevo. Y `$TEST/ui/herramientas/precios/PreciosMetalesViewModelTest.kt`
  (`FakeCotizacionesRepository` → `ObtenerCotizacionesUseCase` real, `ConvertirCotizacionUseCase`
  real, `TestDispatcherProvider`): estado inicial `CARGANDO` observado con Turbine antes de que
  el fake responda; con instantánea completa → `LISTO`, 5 filas en el orden del enum, oro
  `4606.4` oz → `"148,10"` en gramo, plata/cobre < 1 con 4 decimales,
  `ultimaConsultaEpochMillis == obtenidoEn` más reciente del fake, `origen == RED`; una
  cotización con mid/ask/bid a 0 → `precioFormateado == null`; `logScreenView("herramientas_precios")` una vez;
  `herramientas_precios_cargados {fuente=red, parcial=false}`; con `origen == CACHE` →
  `fuente=cache`; unidad de origen desconocida → precio de origen + etiqueta cruda y
  `unidad == null`. **Depende de T040**.
- [X] T046 [P] [US1] Crear `$ATEST/ui/herramientas/HerramientasScreenTest.kt` (monta
  `HerramientasContent` con slots `Text("marcador-precios")` / `Text("marcador-chapas")`, sin
  Koin, helper `montar(uiState, callbacks…)`): primera visita → ambas píldoras visibles, título
  de la tarjeta visible, ningún marcador visible, `onAllNodes(isSelected()).assertCountEquals(0)`;
  pulsar «PESO DE CHAPAS» propaga `CHAPAS`; estado PRECIOS muestra su marcador y no el otro; sin
  nodos «Limpiar»/«Guardar en favoritos». Y `$ATEST/ui/herramientas/precios/PreciosMetalesScreenTest.kt`:
  estado `LISTO` precocinado pinta los cinco nombres, símbolos y precios, la nota y la fuente;
  estado `CARGANDO` vacío pinta el indicador. **Depende de T043**.

**Checkpoint**: Herramientas ya es real — primera visita con solo el selector, PRECIO METALES con
cinco precios, caché persistida de una hora verificable en el panel del proveedor (SC-001).

---

## Fase 4 — User Story 2: cambiar la unidad y consultar la información del mercado (P2)

**Objetivo**: selector gramo / kilo / onza troy que convierte todas las cifras sin red, filas
pulsables y tarjeta «Información del mercado» del metal elegido.

**Test independiente**: con precios cargados, recorrer las tres unidades (oro 148,10 €/g ↔
148.099,20 €/kg ↔ 4.606,40 €/oz) y pulsar cada metal comprobando que la tarjeta cambia; 0
peticiones nuevas.

- [X] T047 [US2] Ampliar `PreciosMetalesViewModel` con `fun onUnidadSeleccionada(unidad: UnidadPrecio)`
  (si es la actual, nada; si no, re-deriva filas y detalle desde `instantanea` **sin red** y
  emite `herramientas_unidad_cambiada {unidad}`), `fun onMetalSeleccionado(metal: MetalCotizado)`
  (ídem con `herramientas_metal_seleccionado {metal}`) y la derivación de
  `detalle: DetalleMercado?` del `seleccionado`: cotización convertida con
  `convertirCotizacion` (o la de origen si `unidadOrigen == null`), `ask/bid/maximo/minimo =
  FormatoPrecios.importe`, `variacion = FormatoPrecios.variacion`, `variacionPorcentaje =
  FormatoPrecios.porcentaje` (sin convertir), `tendencia`, `unidad`, `etiquetaUnidadOrigen`,
  `instanteMercadoEpochMillis` (la vista lo formatea con `fechaHoraLocal`), `desactualizada`; `null` si el
  seleccionado no tiene dato alguno. **Depende de T045 (US1 en verde)**.
- [X] T048 [US2] Ampliar `PreciosMetalesContent`: bajo el título, `CabeceraSeccion(ic_balanza, precios_seccion_unidad, tinte = TealPrimary)`
  + `SelectorSegmentado(UnidadPrecio.entries.map { OpcionSegmento(stringResource(it.etiquetaRes), TealPrimary) }, seleccionada = unidad.ordinal, onSeleccion = { onUnidadSeleccionada(UnidadPrecio.entries[it]) })`;
  las filas pasan a ser `clickable(role = Role.Button) { onMetalSeleccionado(metal) }` y la
  seleccionada lleva borde `TealPrimary` (`border(1.dp, TealPrimary.copy(alpha = 0.65f), RoundedCornerShape(Small))`)
  y `semantics { selected = true }`; debajo de la tarjeta de precios, `TarjetaAcento(acento = GoldPrimary)`
  «Información del mercado»: `Row(ic_balanza en círculo 44 dp, Column(precios_mercado_titulo titleMedium GoldPrimary; precios_mercado_subtitulo bodySmall TextMuted))`,
  `Row(Image(imagenRes, 32.dp), Text(precios_mercado_metal(nombre, simboloApi), titleMedium), píldoras simboloApi y moneda)`
  y los ocho datos en `FilaDetalle(etiqueta, valor)` privadas apiladas de **dos en dos**
  (`Row` de dos `Column(weight 1f)`; con `LocalConfiguration.fontScale > 1.3` una por fila):
  Ask / Bid, Máximo / Mínimo, Variación (color de tendencia) / Variación %, Unidad (etiqueta de
  `unidad` o `etiquetaUnidadOrigen`) / Actualización; con `detalle == null` la tarjeta muestra
  «—». Sin chevron de navegación (spec, Assumptions). Previews con kilo y con plata seleccionada.
  **Depende de T047**.
- [X] T049 [P] [US2] Ampliar `PreciosMetalesViewModelTest`: kilo → oro `"148.099,20"` y
  `unidad == KILO`; onza → `"4.606,40"`; volver a gramo → `"148,10"`; cambiar unidad no llama al
  repositorio otra vez y emite `herramientas_unidad_cambiada {unidad=kilo}`; reseleccionar la
  misma unidad no emite; `detalle` inicial del oro con `ask "4.607,40"`, `bid "4.605,40"`,
  `maximo`, `minimo`, `variacion "-45,30"`, `variacionPorcentaje "-0,97"`; en kilo `ask` se
  convierte y `variacionPorcentaje` no; pulsar PLATA cambia `seleccionado` y `detalle.metal`,
  emite `herramientas_metal_seleccionado {metal=plata}`; metal sin dato → `detalle == null`;
  unidad de origen desconocida → detalle en origen sin conversión. **Depende de T047**.
- [X] T050 [P] [US2] Ampliar `PreciosMetalesScreenTest`: las tres unidades visibles; pulsar
  «Kilo» propaga `KILO`; pulsar la fila de Plata propaga `PLATA`; con `detalle` precocinado se
  ven las ocho etiquetas y sus valores; la fila seleccionada tiene `isSelected()`. **Depende de T048**.

**Checkpoint**: la herramienta de precios está completa (US1 + US2).

---

## Fase 5 — User Story 3: calcular el peso de una chapa (P3)

**Objetivo**: PESO DE CHAPAS con familia ORO/PLATA, ley, tres medidas y cálculo automático con
el detalle del resultado. La ilustración animada llega en US4 (aquí un hueco con la proporción
reservada).

**Test independiente**: elegir PESO DE CHAPAS, teclear 10 · 0,5 · 20 y recorrer los ocho
materiales comprobando 1,56 / 1,31 / 1,28 / 1,12 / 1,04 / 1,04 / 1,03 / 1,01 g.

- [X] T051 [P] [US3] Crear en `$SRC/ui/herramientas/chapas/`: `PesoChapasUiState.kt` —
  `enum class MedidaChapa(val maximoMm: BigDecimal) { ANCHO(BigDecimal("10000")), ESPESOR(BigDecimal("1000")), LARGO(BigDecimal("10000")) }`
  (orden = orden de pintado; §11.4), `data class ResultadoChapa(val pesoFormateado: String, val volumenFormateado: String, val densidadFormateada: String, val purezaFormateada: String, val metalFinoFormateado: String)`,
  `data class DibujoChapaUiState(val proporciones: ProporcionesChapa = ProporcionesChapa.REFERENCIA, val etiquetaAncho: String? = null, val etiquetaEspesor: String? = null, val etiquetaLargo: String? = null, val completa: Boolean = false)`,
  `data class PesoChapasUiState(val material: MaterialChapa = MaterialChapa.ORO_18K, val medidas: Map<MedidaChapa, String> = MedidaChapa.entries.associateWith { "" }, val fueraDeRango: Set<MedidaChapa> = emptySet(), val dibujo: DibujoChapaUiState = DibujoChapaUiState(), val resultado: ResultadoChapa? = null)`
  (KDoc: la familia es `material.familia`, no se duplica; el constructor sin argumentos es el
  estado inicial)—; `ProporcionesChapa.kt` — `data class ProporcionesChapa(val ancho: Float, val largo: Float, val espesor: Float)`
  con `companion object { val REF_ANCHO = BigDecimal("10"); val REF_LARGO = BigDecimal("20"); val REF_ESPESOR = BigDecimal("0.5"); fun desde(anchoMm: BigDecimal?, largoMm: BigDecimal?, espesorMm: BigDecimal?): ProporcionesChapa; val REFERENCIA = desde(null, null, null) }`:
  `a, l, e` = valores o referencia en `Float`; `m = max(a, l)`; `ancho = sqrt(a / m).coerceIn(0.30f, 1f)`,
  `largo = sqrt(l / m).coerceIn(0.30f, 1f)`, `espesor = (0.35f * cbrt(e / m)).coerceIn(0.05f, 0.45f)`
  (Kotlin puro, sin Compose)—; y `PresentacionChapas.kt` (interno) —
  `FamiliaChapa.etiquetaRes` (`chapas_familia_oro/plata`), `FamiliaChapa.acento`
  (ORO → `GoldPrimary`, PLATA → `TealPrimary`), `FamiliaChapa.metalFinoRes`
  (`metal_oro_fino` / `metal_plata_fina`), `FamiliaChapa.nombreMaterialRes`
  (`chapas_material_oro/plata`), `MaterialChapa.etiquetaRes` (`oro_ley_18k`… /
  `plata_ley_950`…), `MaterialChapa.avisoRes: Int?` (12K → `oro_aviso_12k`, 950 →
  `plata_aviso_950`, 900 → `plata_aviso_900`, resto `null`), `MedidaChapa.etiquetaRes`
  (`chapas_medida_*`), `MedidaChapa.iconRes` (`ic_ancho`, `ic_espesor`, `ic_regla`).
  **Depende de T018**.
- [X] T052 [US3] Crear `$SRC/ui/herramientas/chapas/PesoChapasViewModel.kt`:
  `class PesoChapasViewModel(private val calcularPeso: CalcularPesoChapaUseCase, private val analytics: AnalyticsRepository) : ViewModel()`
  con `StateFlow` único, `private var ultimoMaterialRegistrado: MaterialChapa? = null`,
  `init { analytics.logScreenView("herramientas_chapas") }`,
  `fun onFamiliaSeleccionada(f: FamiliaChapa) = recalcular { it.copy(material = MaterialChapa.porDefecto(f)) }`
  (si ya es la familia actual, nada; **conserva las medidas**),
  `fun onMaterialSeleccionado(m: MaterialChapa) = recalcular { it.copy(material = m) }`,
  `fun onMedidaCambiada(medida: MedidaChapa, texto: String) = recalcular { it.copy(medidas = it.medidas + (medida to texto)) }`;
  `recalcular`: parsea cada medida con `parsearDecimalPositivo`; `fueraDeRango = medidas cuyo valor > maximoMm`
  (fuera de rango cuenta como inválida); `dibujo = DibujoChapaUiState(ProporcionesChapa.desde(anchoValido, largoValido, espesorValido), etiquetas = valor válido → formatear(2) + " mm" o null, completa = las tres válidas)`;
  `resultado` solo con las tres válidas → `calcularPeso(ancho, largo, espesor, material)` →
  `ResultadoChapa(peso.setScale(2, HALF_UP), volumenCm3.setScale(3, HALF_UP), densidad.toPlainString(), finura.movePointRight(2).setScale(1, HALF_UP), metalFino.setScale(3, HALF_UP))`
  con coma decimal; luego `registrarCalculo(material)`; si no, `resultado = null` y
  `ultimoMaterialRegistrado = null`. Telemetría `herramientas_chapa_calculada {material = familia.analyticsId, ley = material.analyticsId}`
  deduplicada por material. Sin corrutinas. Companion con `SCREEN_NAME`, eventos, params y
  `ESCALA_PESO = 2`, `ESCALA_FINO = 3`, `ESCALA_VOLUMEN = 3`, `ESCALA_MM = 2`. `onLimpiar` y
  `onGuardarFavoritos` llegan en US5. **Depende de T020 y T051**.
- [X] T053 [US3] Registrar `viewModelOf(::PesoChapasViewModel)` en
  `$SRC/core/di/ViewModelModule.kt`. **Depende de T052**.
- [X] T054 [US3] Crear `$SRC/ui/herramientas/chapas/PesoChapasContent.kt` y `PesoChapasSection.kt`:
  `PesoChapasSection(modifier, viewModel = koinViewModel())` → `PesoChapasContent(uiState, onFamiliaSeleccionada, onMaterialSeleccionado, onMedidaCambiada, onLimpiar, onGuardarFavoritos, modifier)`
  (los dos últimos con lambdas vacías hasta US5). `Content`: `Column(spacedBy(Md))` sin scaffold
  ni scroll, `val acento = uiState.material.familia.acento`, en el orden del mockup:
  1) `TarjetaAcento(acento)` con `chapas_titulo` (titleMedium 17 sp, acento), `chapas_subtitulo`
  (bodySmall, `TextMuted`) y un `Box(fillMaxWidth().aspectRatio(2.4f))` reservado para
  `DibujoChapa` (US4); 2) `CabeceraSeccion(ic_lingotes, chapas_seccion_material, tinte = acento)`
  + `SelectorSegmentado(FamiliaChapa.entries.map { OpcionSegmento(stringResource(it.etiquetaRes), it.acento) }, seleccionada = familia.ordinal)`
  + `val leyes = MaterialChapa.deFamilia(familia); SelectorSegmentado(leyes.map { OpcionSegmento(stringResource(it.etiquetaRes), acento) }, seleccionada = leyes.indexOf(material), onSeleccion = { onMaterialSeleccionado(leyes[it]) })`
  + `material.avisoRes?.let { AvisoTecnico(stringResource(it)) }`;
  3) `CabeceraSeccion(ic_regla, chapas_seccion_medidas, tinte = acento)` + `TarjetaAcento(acento)`
  con `MedidaChapa.entries.forEach { CampoMedida(etiqueta = stringResource(it.etiquetaRes), valor = uiState.medidas[it].orEmpty(), onCambio = { t -> onMedidaCambiada(it, t) }, iconRes = it.iconRes, unidad = stringResource(R.string.unidad_milimetros), acento = acento, error = it in uiState.fueraDeRango, imeAction = if (it == LARGO) Done else Next) }`
  + `if (fueraDeRango.isNotEmpty()) AvisoTecnico(stringResource(R.string.chapas_aviso_rango))`;
  4) `uiState.resultado?.let { TarjetaResultadoChapa(it, material, acento) }` privada:
  `TarjetaAcento(acento)` con `Row(ic_balanza en círculo 44 dp, chapas_resultado_titulo)`,
  `Row(baseline) { Text(pesoFormateado, CifraGrande, acento); Text(unidad_gramos, titleMedium, acento) }`,
  píldora `chapas_resultado_para(stringResource(familia.nombreMaterialRes, stringResource(material.etiquetaRes)))`
  (labelMedium, borde acento α 0.65, `JewelryRadius.Pill`), `DiamondDivider(widthFraction = 1f)`
  y cuatro `FilaDetalle(etiqueta, valor, unidad)` apiladas (bodyMedium `TextSecondary` ·
  `LineaPunteada(acento α 0.55)` · bodyLarge acento · bodyMedium): Volumen / `unidad_cm3`,
  Densidad / `unidad_g_cm3`, Pureza = `chapas_pureza_formato(purezaFormateada, etiquetaRes)`,
  `familia.metalFinoRes` / `unidad_gramos`; `semantics(mergeDescendants = true)` por fila; y
  `Row(ic_info 16.dp TextMuted, chapas_nota_aproximado bodySmall TextMuted)`. Los botones llegan
  en US5. `@Preview` ×3 (inicial ORO, resultado 18K, PLATA 925 con resultado). **Depende de
  T025, T051, T052 y T053**.
- [X] T055 [US3] En `HerramientasScreen`, sustituir el marcador del slot `chapas` por
  `{ PesoChapasSection() }`. **Depende de T054**.
- [X] T056 [P] [US3] Crear `$TEST/ui/herramientas/chapas/PesoChapasViewModelTest.kt` (caso de
  uso real, mockk relaxed solo para analytics, Turbine para el estado inicial): estado inicial
  `ORO_18K`, tres textos vacíos, `resultado == null`, `dibujo == DibujoChapaUiState()`;
  `logScreenView("herramientas_chapas")` una vez; tras ANCHO «10» y ESPESOR «0,5» sigue null;
  LARGO «20» → `"1,56"`, `"0,100"`, `"15,58"`, `"75,0"`, `"1,169"`; los 8 materiales → «Mostrar»
  de §7 y PLATA_925 → fino `"0,958"`; `"0,5"` ≡ `"0.5"`; inválidas por campo
  `["", "0", "-1", "abc", "1,2,3", "1.2.3", "  "]` → null sin `fueraDeRango`; ANCHO «10000» ok,
  «10000,01» → en `fueraDeRango` y null; ESPESOR «1000,5» marcado; volver a rango lo quita;
  ORO→PLATA fija `PLATA_925`, conserva textos, resultado `"1,04"`; PLATA→ORO vuelve a `ORO_18K`;
  cambiar de ley no toca textos; dibujo con etiquetas `"10,00 mm"`/`"0,50 mm"`/`"20,00 mm"` y
  `completa == true`; un campo vacío → esa etiqueta null, `completa == false` y esa proporción
  usa la referencia; teclear tres veces → exactamente 1 `herramientas_chapa_calculada {material=oro, ley=18k}`;
  cada cambio de material emite el suyo; inválido→válido re-emite. Y
  `$TEST/ui/herramientas/chapas/ProporcionesChapaTest.kt`: el mayor de ancho/largo vale 1;
  ambos ≥ 0,30; espesor ∈ [0,05; 0,45]; monótono en cada entrada; extremos 0,01 mm y 10 000 mm
  dentro de los topes; nulos → `REFERENCIA`. **Depende de T052**.
- [X] T057 [P] [US3] Crear `$ATEST/ui/herramientas/chapas/PesoChapasScreenTest.kt` (monta
  `PesoChapasContent` en el tema, sin Koin): ORO y PLATA visibles, las 4 leyes de oro visibles y
  «925 (ley)» no; con `PLATA_925` las 4 de plata visibles y «18 K» no; exactamente 3 nodos con
  `hasSetTextAction()`; escribir en Ancho propaga `(ANCHO, "10")`; con `resultado` → «1,56»,
  «Calculado para Oro 18 K» y la nota visibles; sin resultado no existe `chapas_resultado_titulo`;
  `ORO_12K` → `oro_aviso_12k` visible, `ORO_18K` → ausente; `fueraDeRango = {ANCHO}` →
  `chapas_aviso_rango` visible. **Depende de T054**.

**Checkpoint**: las dos herramientas calculan; chapas con los ocho materiales y sus ocho
resultados de §7 (SC-003).

---

## Fase 6 — User Story 4: ver la chapa construirse con sus medidas (P4)

**Objetivo**: la ilustración isométrica dibujada en la app, que se construye al entrar y se
redibuja con cada medida en el color del metal.

**Test independiente**: abrir PESO DE CHAPAS y ver la construcción (< 1 s); teclear medidas y ver
la chapa y sus cotas cambiar; cambiar a PLATA y ver el tono plateado-turquesa.

- [ ] T058 [US4] Crear `$SRC/ui/herramientas/chapas/DibujoChapa.kt`:
  `@Composable fun DibujoChapa(estado: DibujoChapaUiState, familia: FamiliaChapa, descripcion: String, modifier: Modifier = Modifier)`.
  Ejes: ancho → x (arista frontal, cota debajo), largo → z (arista en fuga, cota a la derecha),
  espesor → y (cota a la izquierda). Proyección oblicua *cabinet* con `COS30 = 0.866f`,
  `SIN30 = 0.5f`, `K = 0.5f`: `P(x, y, z) = Offset(ox + s*(x + z*COS30*K), oy - s*(y + z*SIN30*K))`;
  vértices con `A, E, L` = proporciones animadas: `v0=P(0,0,0) v1=P(A,0,0) v2=P(A,E,0) v3=P(0,E,0) v4=P(0,E,L) v5=P(A,E,L) v6=P(A,0,L)`;
  caras frontal `v0 v1 v2 v3`, superior `v3 v2 v5 v4`, derecha `v1 v6 v5 v2`;
  `anchoUnidades = A + 0.433f*L`, `altoUnidades = E + 0.25f*L`,
  `s = min((W - 2*MX) / anchoUnidades, (H - MT - MB) / altoUnidades)` con `MX = 64.dp`,
  `MT = 12.dp`, `MB = 40.dp`; `ox = (W - s*anchoUnidades)/2`, `oy = H - MB`. Orden de pintado:
  derecha, frontal, superior, contorno 1 dp. Colores en `remember(familia)`: ORO — superior
  `Brush.linearGradient(listOf(GoldSoft, GoldPrimary, GoldSecondary), start = v4, end = v1)`,
  frontal `GoldSecondary`, derecha `lerp(GoldSecondary, Black, 0.35f)`, aristas `GoldSoft`;
  PLATA — superior `linearGradient(listOf(TextPrimary, SilverPrimary, lerp(SilverPrimary, TealPrimary, 0.35f)))`,
  frontal `SilverDark`, derecha `lerp(SilverDark, Black, 0.35f)`, aristas `SilverPrimary`; con
  `!estado.completa` alpha de caras × 0.45. Cotas en `TealPrimary`, 1.5 dp,
  `PathEffect.dashPathEffect(floatArrayOf(5.dp, 4.dp))`, flechas rellenas 6 × 4 dp orientadas con
  `atan2`: ancho en `y = v0.y + 22.dp` de `v0.x` a `v1.x` con extensiones discontinuas y etiqueta
  centrada debajo; espesor en `x = v0.x - 22.dp` de `v0.y` a `v3.y`, etiqueta a la izquierda
  centrada, flechas **por fuera** si `s*E < 3 × flecha`; largo desplazado `(+16.dp, +10.dp)` de
  `v1 → v6` con etiqueta en el punto medio `+ (12.dp, 14.dp)`. Etiquetas con
  `rememberTextMeasurer()`, `labelMedium` 12 sp `TextPrimary`, medidas en `remember(etiquetas)`,
  `drawText(layout, topLeft, alpha)`; etiqueta `null` → la línea se pinta, el texto no.
  Animación: `val a by animateFloatAsState(estado.proporciones.ancho, spring(DampingRatioNoBouncy, StiffnessLow))`
  (ídem `e`, `l`); `val inspeccion = LocalInspectionMode.current`;
  `val progreso = remember { Animatable(if (inspeccion) 1f else 0f) }`;
  `LaunchedEffect(familia) { if (!inspeccion) { progreso.snapTo(0f); progreso.animateTo(1f, tween(900, easing = FastOutSlowInEasing)) } }`;
  fases por `p = progreso.value`: 0,00–0,60 traza el contorno con `PathMeasure.getSegment(0, longitud * (p/0.6).coerceAtMost(1f), destino, true)`,
  0,35–0,80 alpha de caras con `smoothstep`, 0,70–1,00 alpha de cotas y etiquetas.
  `Modifier.drawWithCache { val camino = Path(); val contorno = Path(); val medidor = PathMeasure(); val segmento = Path(); onDrawBehind { … } }`
  con `rewind()` por frame y lectura de `a, e, l, progreso.value` **solo dentro** de
  `onDrawBehind`; `Modifier.semantics { contentDescription = descripcion }`. KDoc: las
  proporciones dibujadas jamás alimentan el cálculo (FR-024); `chapa.png` es el plan B si el
  Canvas resultara inviable. **Depende de T051**.
- [ ] T059 [US4] Integrar en `PesoChapasContent` sustituyendo el `Box` reservado por
  `DibujoChapa(estado = uiState.dibujo, familia = material.familia, descripcion = stringResource(R.string.chapas_dibujo_descripcion, stringResource(familia.nombreMaterialRes, stringResource(material.etiquetaRes)), etiquetaAncho ?: sinMedida, etiquetaLargo ?: sinMedida, etiquetaEspesor ?: sinMedida), modifier = Modifier.fillMaxWidth().aspectRatio(2.4f))`
  con `sinMedida = stringResource(R.string.chapas_dibujo_sin_medida)`; actualizar las previews
  (renderizan con progreso 1 gracias a `LocalInspectionMode`). **Depende de T058**.
- [ ] T060 [P] [US4] Ampliar `PesoChapasScreenTest`: existe un nodo con la `contentDescription`
  de la chapa que contiene «Oro 18 K», «10,00 mm», «20,00 mm» y «0,50 mm» con el estado
  precocinado; con un campo vacío contiene «sin medida». Si la animación hiciera esperar,
  `composeRule.mainClock.advanceTimeBy(1000)`. **Depende de T059**.
- [ ] T061 [US4] Verificación visual en emulador de la ilustración: construcción < 1 s al entrar
  y al cambiar ORO↔PLATA; transición suave al teclear; legible con 0,1 × 10 000 × 10 000 mm y con
  10 000 × 0,1 × 0,1 mm; sin *jank* al teclear (Layout Inspector / `adb shell dumpsys gfxinfo`
  sin frames > 16 ms sostenidos). Si fallara, documentar y decidir con el autor el plan B
  (`chapa.png`). **Depende de T059**.

**Checkpoint**: la chapa se construye y se redibuja con las medidas (SC-008).

---

## Fase 7 — User Story 5: convivir con los fallos y rematar el flujo (P5)

**Objetivo**: estados parcial, error, desactualizado y espera en PRECIO METALES con «Reintentar»;
«Limpiar» y «Guardar en favoritos» en PESO DE CHAPAS.

**Test independiente**: modo avión con y sin caché, un metal fallido simulado, build sin
credencial, «Reintentar» dentro y fuera de la espera; en chapas, los dos botones.

- [ ] T062 [US5] Ampliar `PreciosMetalesViewModel`: `fun onReintentar() = cargar(esReintento = true)`
  (ignorado con carga en curso); en `derivar`: fila con `Error` → `error = motivo`, y si
  `ultimaConocida != null` → `precioFormateado` de esa cotización con `desactualizada = true`,
  si no `precioFormateado = null`; `fase = ERROR` si los 5 son `Error` (conservando filas con
  último dato), `PARCIAL` si hay algún `Error`, `LISTO` si completa;
  `errorGlobal` = motivo más repetido solo en `ERROR`; `puedeReintentar = fase != LISTO`;
  `avisoEspera = origen == CACHE_EN_ESPERA && esReintento`; `detalle.desactualizada`;
  telemetría `herramientas_precios_error {motivo}` cuando los 5 fallan y `recordError(causa)`
  por cada `Error` con motivo `DESCONOCIDO` o `RESPUESTA_INVALIDA` que traiga causa. **Depende de
  T049 (US2 en verde)**.
- [ ] T063 [US5] Ampliar `PreciosMetalesContent`: fila con `error != null` muestra
  `Text(stringResource(error.mensajeRes), bodySmall, Danger)` bajo el nombre y, si hay precio,
  la cifra en `TextMuted` con la caption `precios_desactualizado`; sobre la lista, según estado:
  `fase == ERROR` → `AvisoTecnico(stringResource(errorGlobal.mensajeRes))` (con filas
  desactualizadas, `precios_aviso_desactualizado`); `fase == PARCIAL` →
  `AvisoTecnico(precios_aviso_parcial)`; `avisoEspera` → `AvisoTecnico(precios_aviso_espera)`;
  `puedeReintentar && !reintentando` → `BotonDorado(ic_refrescar, precios_accion_reintentar, onReintentar)`;
  `reintentando` → indicador pequeño junto al título. `SIN_CREDENCIAL` usa su mensaje sin botón
  de reintento (no cambiará hasta otra build). Previews `PARCIAL` y `ERROR` con dato antiguo.
  **Depende de T062**.
- [ ] T064 [US5] Ampliar `PesoChapasViewModel` con
  `fun onLimpiar() { ultimoMaterialRegistrado = null; _uiState.value = PesoChapasUiState() }` y
  `fun onGuardarFavoritos() = analytics.logEvent("herramientas_chapa_favoritos_proximamente")`;
  ampliar `PesoChapasContent` con la fila `Row(spacedBy(Md)) { BotonDorado(ic_refrescar, accion_limpiar, onLimpiar, weight(1f)); BotonDorado(ic_estrella, accion_guardar_favoritos, onGuardarFavoritos, weight(1f)) }`
  al final, y `PesoChapasSection` con el Toast `R.string.aviso_proximamente` desde la vista
  (patrón plata; el ViewModel no conoce Android). PRECIO METALES y la primera visita no llevan
  botones (FR-025). **Depende de T057 (US3 en verde)**.
- [ ] T065 [P] [US5] Ampliar los tests: `PreciosMetalesViewModelTest` — 4 éxitos + rodio
  `SIN_CONEXION` → `PARCIAL`, fila de rodio con `error` y `precioFormateado == null`,
  `puedeReintentar`; rodio con `ultimaConocida` → precio y `desactualizada == true`; 5 errores →
  `ERROR`, `errorGlobal == SIN_CONEXION`, `herramientas_precios_error {motivo=sin_conexion}`;
  5 errores con últimas conocidas → filas con precio desactualizado; `onReintentar` vuelve a
  llamar al fake; con carga en curso (fake con `CompletableDeferred`) se ignora; `CACHE_EN_ESPERA`
  tras reintento → `avisoEspera`; `SIN_CREDENCIAL` → `errorGlobal` y `puedeReintentar == false`;
  excepción inesperada → `recordError` + `ERROR DESCONOCIDO` conservando filas previas.
  `PesoChapasViewModelTest` — limpiar → `PesoChapasUiState()` y el siguiente cálculo re-emite;
  favoritos emite su evento y no toca el estado. `PreciosMetalesScreenTest` — `PARCIAL` muestra
  el mensaje de la fila y «Reintentar» propaga; `LISTO` no muestra «Reintentar»; `avisoEspera`
  visible. `PesoChapasScreenTest` — los dos botones existen y propagan. **Depende de T063 y T064**.

**Checkpoint**: la spec entera cubierta — cinco historias en verde.

---

## Fase 8 — Pulido y verificación

- [ ] T066 [P] Actualizar `CLAUDE.md`: los tres destinos pendientes pasan a dos (Favoritos y
  Ajustes); «tres motores» → cuatro (`MaterialChapa` + `CalculoChapa`: sin ningún redondeo,
  `movePointLeft(3)`, enum propio con test de paridad frente a `LeyOro`/`LeyPlata`) más el
  conversor de precios; «trece casos de uso» → dieciséis; el párrafo de redondeo de vista gana
  la cuarta política (chapas 2 `HALF_UP`, volumen/fino 3; precios 2/4 con miles); sección nueva
  «Red y caché» (primer código asíncrono: `ClienteHttp` sobre `HttpURLConnection` sin
  dependencias, `PoliticaCacheCotizaciones` pura, `Reloj` en `coreModule`, blob único en
  `SharedPreferences` excluido de backup, `RAPIDAPI_KEY` de `local.properties` a `BuildConfig`
  vía `providers` y **extraíble del APK**, cuota 3 000 cargas/mes, `PARAMETRO_METAL` verificado
  con curl y sin fallback); `ui/herramientas/` como sexto ejemplo (una ruta, tres ViewModels
  perezosos, sub-paquetes, secciones sin scaffold, primer `Canvas` con `drawWithCache`);
  componentes (`OpcionSegmento.iconRes`, `CampoMedida`, `MarcoCampo`), `core/util/Decimales.kt`
  y `Reloj.kt`; iconos nuevos (+5) y `rodio.png`; nota de `verify()` (tipo primario, `bind`,
  defaults solo con lambda explícita); `TestDispatcherProvider`/`RelojFalso`/fakes como patrón
  de test. **Depende de T065**.
- [ ] T067 [P] *(Opcional, `refactor:`)* Sustituir el `parsearCantidad` privado de
  `OroViewModel`, `PlataViewModel`, `SoldadurasViewModel` y `SoldaduraBaseViewModel` por
  `parsearDecimalPositivo` de `core/util/Decimales.kt`, sin cambiar comportamiento (sus tests
  existentes hacen de regresión). **Depende de T009**.
- [ ] T068 Puertas finales: `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`
  y en verde `./gradlew :app:testDebugUnitTest` (dominio + datos + ViewModels + `KoinModulesTest`),
  `./gradlew :app:lint`, `./gradlew :app:assembleDebug`,
  `./gradlew :app:compileDebugAndroidTestKotlin` y `./gradlew :app:assembleRelease` (firmar con la
  clave de debug según `CLAUDE.md` y comprobar en el emulador que PRECIO METALES parsea con R8;
  si `decodeFromString` fallara en release, añadir en `app/src/main/keepRules/rules.keep` las
  reglas estándar de kotlinx-serialization para `com.jrblanco.calculadoradejoyeros2021.data.source.**`);
  y `git grep -i -l rapidapi` sobre el árbol versionado devuelve **solo** `app/build.gradle.kts`,
  `CLAUDE.md` y ficheros bajo `specs/` o `UI_Plantillas/` (SC-012: la credencial no está en el
  repositorio). **Depende de T066**.
- [ ] T069 Verificación manual en emulador siguiendo los 21 pasos de `quickstart.md` §3 (panel
  de RapidAPI: exactamente 5 peticiones en la primera carga y 0 al volver, también tras
  `am force-stop`; unidades; tarjeta de mercado; modo avión; espera de reintento; build sin
  credencial; chapa 10 × 0,5 × 20 → 1,56 g; PLATA en teal; fuente ×2; TalkBack; mockups).
  Anotar el resultado y las desviaciones en la sección «Resultado de la verificación» al final
  de este fichero. **Depende de T068**.

---

## Dependencias y orden de ejecución

- **Fase 1 (Setup)**: T002–T007 en paralelo desde el inicio; T001 en cuanto el autor deje la
  credencial (bloquea solo la constante de T030 y la muestra EUR de T023).
- **Fase 2 (Fundacional)**: T008, T009, T011, T015, T018, T024, T025 en paralelo; T010 tras T008;
  T012 → T013 → T014 en cadena; T016 tras T013 y T015; T017 tras T016; T019 → T020 → T021 en
  cadena; T022 tras T016 y T020 (puede diferir el `factoryOf` de cotizaciones hasta T034); T023
  parcial tras T016; T026 con T038; T027 cierra la fase. **Bloquea todas las historias.**
- **Historias**: US1 → US2 → US3 → US4 → US5 en orden de prioridad. US2 amplía los ficheros de
  precios de US1; US3 es independiente de US1/US2 salvo por el slot de `HerramientasScreen`
  (T055) y podría ejecutarse en paralelo con US2 por otra persona; US4 amplía chapas; US5 amplía
  precios y chapas.
- Dentro de US1: T028, T029, T031, T038, T039 en paralelo; T030 tras T028–T029 (y T001); T032
  tras T031; T033 tras T014, T030, T031; T034 tras T032–T033; T035–T037 tras sus fuentes; T040
  tras T039; T041 tras T038 y T040; T042 tras T039; T043 tras T024, T038, T041, T042; T044 tras
  T043; T045–T046 en paralelo tras T040/T043.
- **Fase 8 (Pulido)**: tras la última historia; T067 es opcional y puede hacerse en cualquier
  momento tras T009.

## Estrategia de implementación

MVP = Fases 1 + 2 + US1: Herramientas deja de ser andamiaje y la herramienta estrella —los
precios con caché de una hora— funciona y es verificable en el panel del proveedor. Cada
historia posterior es un incremento verificable por sí solo con su checkpoint; se puede parar en
cualquiera con la app en verde. US2, US4 y US5 amplían ficheros de historias anteriores, por eso
van en secuencia; US3 es la única que podría ir en paralelo con US2.

Si T001 se retrasa, todo lo demás avanza con `PARAMETRO_METAL = "metal"` y la muestra USD del
contrato; la confirmación con la credencial real es obligatoria antes de dar por cerrada US1.

## Notas

- Los números del mockup de chapas (3,13 g; 15,55 g/cm³) **no** son fuente: manda el documento
  (§6/§7). T021 y T056 lo blindan.
- Ninguna cifra ya redondeada realimenta un cálculo ni una conversión (FR-009, FR-022): las
  conversiones parten siempre de la cotización del proveedor.
- Las fechas viajan como `Long` hasta la vista y las formatea `DateUtils` del sistema: son el
  único texto visible cuyo formato depende del idioma (FR-030); todo lo demás lo formatea el
  ViewModel, como en el resto de la app.
- La credencial no aparece en código versionado, registros, mensajes ni tests (los tests usan
  `"clave-de-prueba"`); SC-012 se comprueba con `git grep -i rapidapi` sobre el árbol
  versionado: solo deben salir `build.gradle.kts`, `CLAUDE.md` y los artefactos de `specs/`.
- Cuarta política de redondeo de vista: no unificar con oro/plata/soldaduras; documentada en
  `CLAUDE.md` (T066).
- Commit tras cada tarea o grupo lógico con Conventional Commits en español (`feat(007): …`,
  `test(007): …`, `docs(007): …`, `build(007): …`).
