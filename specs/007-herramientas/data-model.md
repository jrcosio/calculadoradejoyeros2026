# Data Model: Herramientas — precio de metales y peso de chapas

**Feature**: `007-herramientas` · **Fecha**: 2026-08-25
**Fuentes de verdad**: [contracts/metal-quote.md](./contracts/metal-quote.md) (proveedor) y
`UI_Plantillas/Feature_Herramientas/Especificacion_Calculadora_Peso_Chapas_Oro_Plata.md` (§ citados)

Todo el modelo de `domain/` es Kotlin puro: sin `android.*`, sin `androidx.*`, sin `R`; las
únicas APIs externas son `java.math` y `kotlinx.coroutines` (el `suspend` de la interfaz del
repositorio). Las cantidades son `BigDecimal` construidos desde literales `String` — también
los que llegan por red, tomando el literal del cable. Los instantes son `Long` epoch-millis
(no hay `java.time` en minSdk 24).

Conviven dos modelos independientes que no comparten ningún tipo: **cotizaciones** (con red y
caché) y **chapas** (el cuarto motor, puro). Los une solo la pantalla.

## Parte A — Cotizaciones

### MetalCotizado *(enum)*

| Valor | `simboloApi` | analyticsId | Imagen |
|---|---|---|---|
| `ORO` | `AU` | `"oro"` | `modulo_oro` (existente) |
| `PLATA` | `AG` | `"plata"` | `modulo_plata` (existente) |
| `COBRE` | `CU` | `"cobre"` | `cobre` (existente) |
| `PALADIO` | `PD` | `"paladio"` | `paladio` (existente) |
| `RODIO` | `RH` | `"rodio"` | `rodio` (nuevo, del encargo) |

El orden del enum **es** el orden de pintado (mockup). Las imágenes las mapea la pantalla
(`PresentacionPrecios.kt`), no el dominio.

### UnidadPrecio *(enum)*

| Valor | analyticsId | Gramos por unidad | Etiqueta visible |
|---|---|---:|---|
| `GRAMO` | `"gramo"` | 1 | €/g |
| `KILO` | `"kilo"` | 1 000 | €/kg |
| `ONZA_TROY` | `"onza_troy"` | 31,1034768 | €/oz |

**Sin valor «desconocida»**: el enum alimenta el selector y el `when` del conversor. La unidad
de origen no confirmada se modela en `CotizacionMetal.unidadOrigen: UnidadPrecio?` (null) más
`etiquetaUnidadOrigen: String` (texto crudo del proveedor).

### Tendencia *(enum)* · OrigenDatos *(enum)* · MotivoErrorCotizacion *(enum)*

- `Tendencia { SUBE, BAJA, PLANA }` con `companion fun de(variacion: BigDecimal)` por `signum()`.
- `OrigenDatos { RED, CACHE, CACHE_EN_ESPERA }`: de dónde salió la instantánea que ve el ViewModel.
- `MotivoErrorCotizacion` (+ `analyticsId`):

| Valor | Causa |
|---|---|
| `SIN_CREDENCIAL` | credencial vacía en la build (no se llama a la red) |
| `CREDENCIAL_RECHAZADA` | HTTP 401 / 403 |
| `NO_ENCONTRADO` | HTTP 404 (ruta o parámetro) |
| `LIMITE_ALCANZADO` | HTTP 429 |
| `SERVIDOR` | HTTP 5xx |
| `SIN_CONEXION` | `IOException` (sin red, timeout) |
| `RESPUESTA_INVALIDA` | JSON ilegible, símbolo ausente en `results`, moneda ≠ EUR |
| `DESCONOCIDO` | cualquier otra excepción |

### CotizacionMetal

| Campo | Tipo | Origen |
|---|---|---|
| `metal` | `MetalCotizado` | símbolo pedido |
| `moneda` | `String` | `currency` (siempre `EUR`) |
| `ask`, `bid`, `mid`, `maximo`, `minimo`, `variacion`, `variacionPorcentaje` | `BigDecimal` | `ask`, `bid`, `mid`, `high`, `low`, `change`, `changePercentage` |
| `unidadOrigen` | `UnidadPrecio?` | `unit` mapeado (`OUNCE|OZ|OZT|TROY_OUNCE` → onza troy; `GRAM|G`; `KILOGRAM|KG`; otro → null) |
| `etiquetaUnidadOrigen` | `String` | `unit` crudo |
| `instanteMercadoEpochMillis` | `Long` | `timestamp` × 1 000 (si ya > 10¹², tal cual) |
| `obtenidoEnEpochMillis` | `Long` | `Reloj.ahoraMillis()` al recibirla |

Derivados: `precioPrincipal: BigDecimal? = listOf(mid, ask, bid).firstOrNull { it.signum() > 0 }`
y `tendencia = Tendencia.de(variacion)`.

### ResultadoCotizacion *(sealed)*

- `Exito(cotizacion: CotizacionMetal)`.
- `Error(metal, motivo: MotivoErrorCotizacion, ultimaConocida: CotizacionMetal? = null, causa: Throwable? = null)`.
  `ultimaConocida` es lo que permite «precio desactualizado» por fila; `causa` solo la usa el
  ViewModel para `recordError` y **no se persiste**.

### InstantaneaCotizaciones

| Campo | Tipo | Nota |
|---|---|---|
| `resultados` | `Map<MetalCotizado, ResultadoCotizacion>` | puede faltar algún metal (instantánea vacía o parcial) |
| `instanteIntentoEpochMillis` | `Long?` | instante del **último intento** de red, global |
| `origen` | `OrigenDatos` | no se persiste; lo pone el repositorio al devolverla |

Derivados: `estaCompleta` (los 5 son `Exito`), `hayErrores`,
`esVigente(metal, ahora, vigencia)` = `Exito` con `0 <= ahora − obtenidoEn < vigencia` (delta
negativo = reloj atrasado = **no** vigente), `ultimaCotizacionConocida(metal)`,
`fusionarCon(nuevos, instanteIntento)` (un `Error` nuevo hereda `ultimaConocida` del resultado
anterior; un `Exito` nuevo sustituye), `companion val VACIA`.

**Una sola verdad**: el instante de éxito no se almacena por separado, se deriva del
`obtenidoEn` de cada éxito.

### PoliticaCacheCotizaciones *(función pura)*

`class PoliticaCacheCotizaciones(vigenciaMillis = 3 600 000, esperaReintentoMillis = 60 000, esperaTrasLimiteMillis = 300 000)`
con `fun decidir(guardada: InstantaneaCotizaciones, ahoraMillis: Long): DecisionCache`:

1. `pendientes = metales sin esVigente(…)`.
2. `pendientes` vacío → **`Servir`** (cero red).
3. `instanteIntento != null` y `0 <= ahora − instanteIntento < espera` → **`Esperar`** (cero
   red); `espera` = `esperaTrasLimiteMillis` si algún error de la instantánea es
   `LIMITE_ALCANZADO`, si no `esperaReintentoMillis`.
4. En otro caso → **`Actualizar(pendientes)`**: solo los metales sin precio vigente.

`sealed interface DecisionCache { Servir; Esperar; data class Actualizar(val pendientes: Set<MetalCotizado>) }`.

### ConversorUnidadesPrecio *(objeto)*

- `GRAMOS_POR_ONZA_TROY = BigDecimal("31.1034768")`, `GRAMOS_POR_KILO = BigDecimal("1000")`,
  `ESCALA = 10`.
- `gramosPor(unidad)`; `convertir(importe, desde, hacia)`: si iguales → `importe`; si no,
  `importe × gramosPor(hacia) ÷ gramosPor(desde)` con **una única división** `HALF_UP` a
  `ESCALA` (multiplicación exacta primero). Constantes propias, como en los otros motores.

### CotizacionesRepository *(interfaz de dominio)*

`suspend fun obtenerCotizaciones(): InstantaneaCotizaciones`. Sin `forzar`: la política decide.

### Casos de uso

| Caso de uso | Firma | Comportamiento |
|---|---|---|
| `ObtenerCotizacionesUseCase` | `suspend operator fun invoke(): InstantaneaCotizaciones` | delega en el repositorio |
| `ConvertirCotizacionUseCase` | `operator fun invoke(cotizacion, hacia): CotizacionMetal?` | `null` si `unidadOrigen == null`; convierte ask/bid/mid/máximo/mínimo/variación, deja `variacionPorcentaje`, fija `unidadOrigen = hacia` en la copia |

### Formato persistido (blob único en SharedPreferences, clave `instantanea_json`)

DTOs propios `@Serializable` en `data/source/local/`, independientes del DTO del proveedor:

```json
{
  "version": 1,
  "instanteIntentoEpochMillis": 1787670000000,
  "resultados": [
    { "metal": "ORO", "cotizacion": { "moneda": "EUR", "ask": "4607.4", "bid": "4605.4", "mid": "4606.4",
      "maximo": "4697.5", "minimo": "4604.6", "variacion": "-45.30000000000018",
      "variacionPorcentaje": "-0.974046917668312", "unidadOrigen": "ONZA_TROY", "etiquetaUnidadOrigen": "OUNCE",
      "instanteMercadoEpochMillis": 1787665680000, "obtenidoEnEpochMillis": 1787670000000 },
      "motivoError": null, "ultimaConocida": null },
    { "metal": "RODIO", "cotizacion": null, "motivoError": "SIN_CONEXION", "ultimaConocida": { "...": "..." } }
  ]
}
```

- `BigDecimal` como `String` (`toPlainString()`); enums por nombre.
- `CodificadorInstantanea.decodificar` devuelve `null` si el JSON no se entiende y **descarta**
  entradas con metal o motivo desconocidos (tolerante a versiones futuras).
- `origen` y `causa` no se persisten.

## Parte B — Chapas (cuarto motor)

### FamiliaChapa *(enum)* · MaterialChapa *(enum, §2, §5.1)*

`FamiliaChapa { ORO, PLATA }` (+ `analyticsId`).

| Valor | familia | milésimas | densidad (g/cm³) | esSoloTecnica | Etiqueta (existente) |
|---|---|---:|---:|---|---|
| `ORO_18K` | ORO | 750 | `"15.58"` | no | `oro_ley_18k` («18 K») |
| `ORO_14K` | ORO | 585 | `"13.07"` | no | `oro_ley_14k` |
| `ORO_12K` | ORO | 500 | `"12.75"` | **sí** | `oro_ley_12k` + `oro_aviso_12k` |
| `ORO_9K` | ORO | 375 | `"11.20"` | no | `oro_ley_9k` |
| `PLATA_950` | PLATA | 950 | `"10.40"` | **sí** | `plata_ley_950` + `plata_aviso_950` |
| `PLATA_925` | PLATA | 925 | `"10.36"` | no | `plata_ley_925` («925 (ley)») |
| `PLATA_900` | PLATA | 900 | `"10.31"` | **sí** | `plata_ley_900` + `plata_aviso_900` |
| `PLATA_800` | PLATA | 800 | `"10.14"` | no | `plata_ley_800` |

- `finura = BigDecimal(milesimas).movePointLeft(3)` (§3.1: 14K **es** 585, nunca 14/24).
- `analyticsId` = `"18k"`, `"14k"`, `"12k"`, `"9k"`, `"950"`, `"925"`, `"900"`, `"800"`.
- `deFamilia(familia)`; `porDefecto(ORO) = ORO_18K`, `porDefecto(PLATA) = PLATA_925`.
- **Enum propio**, no `LeyOro`/`LeyPlata`: la densidad es dato de este documento y §19 prevé
  densidades por color de una misma ley. Un **test de paridad** vigila que milésimas y
  `esSoloTecnica` coincidan con `LeyOro` (18K/14K/12K técnica/9K) y `LeyPlata`
  (950 técnica/925/900 técnica/800).
- Las densidades son **orientativas** (§5.1): nunca se etiquetan como medidas.

### CalculoChapa (§4, §8.1, §8.2)

| Campo | Fórmula |
|---|---|
| `material`, `ancho`, `largo`, `espesor` | entradas (mm) |
| `areaMm2` | `ancho × largo` |
| `volumenMm3` | `areaMm2 × espesor` |
| `volumenCm3` | `volumenMm3.movePointLeft(3)` — el ÷ 1 000 **exacto** |
| `densidad` | `material.densidad` |
| `peso` | `volumenCm3 × densidad` |
| `metalFino` | `peso × material.finura` |
| `liga` | `peso − metalFino` |

- Constante propia `MM3_POR_CM3 = BigDecimal("1000")`. **Ningún redondeo** (§10.1): solo hay
  multiplicaciones y un desplazamiento de coma. No hay `ESCALA` porque no hay división;
  llegará con los inversos (§8.3), fuera de alcance.
- Fábrica `internal fun de(ancho, largo, espesor, material)` con `check(liga >= 0)` como red
  de seguridad.

### CalcularPesoChapaUseCase

`operator fun invoke(ancho, largo, espesor, material): CalculoChapa` con
`require(> 0)` para las tres medidas (§11.1) y mensaje con el valor. Los límites operativos
(§11.4) **no** están aquí: son controles de interfaz del ViewModel.

Chapa de referencia 10 × 20 × 0,5 mm (§7): `areaMm2 = 200`, `volumenMm3 = 100`, `volumenCm3 = 0.1`.

| Material | `peso` | Mostrar (2 dec.) | `metalFino` | `liga` |
|---|---:|---:|---:|---:|
| ORO_18K | 1.558 | 1,56 | 1.1685 | 0.3895 |
| ORO_14K | 1.307 | 1,31 | 0.764595 | 0.542405 |
| ORO_12K | 1.275 | 1,28 | 0.6375 | 0.6375 |
| ORO_9K | 1.120 | 1,12 | 0.4200 | 0.7000 |
| PLATA_950 | 1.040 | 1,04 | 0.9880 | 0.0520 |
| PLATA_925 | 1.036 | 1,04 | 0.9583 | 0.0777 |
| PLATA_900 | 1.031 | 1,03 | 0.9279 | 0.1031 |
| PLATA_800 | 1.014 | 1,01 | 0.8112 | 0.2028 |

## Parte C — Estado de UI (fuera del dominio, paquete `ui/herramientas/`)

Conceptos de presentación, como `HomeModule`: ningún caso de uso los recibe.

- **Subherramienta** `{ PRECIOS, CHAPAS }` (+ `analyticsId`); `HerramientasUiState(subherramienta: Subherramienta? = null)` — `null` = primera visita.
- **PreciosMetalesUiState**: `fase: FasePrecios { CARGANDO, LISTO, PARCIAL, ERROR }`,
  `reintentando`, `filas: List<FilaMetalPrecio>` (siempre en el orden del enum),
  `unidad = GRAMO`, `seleccionado = ORO`, `detalle: DetalleMercado?`,
  `errorGlobal: MotivoErrorCotizacion?` (solo en ERROR: el motivo más repetido),
  `origen: OrigenDatos?`, `ultimaConsultaEpochMillis: Long?` (el `obtenidoEn` más reciente; la
  vista lo formatea con `DateUtils`), `puedeReintentar`, `avisoEspera`.
  - `FilaMetalPrecio(metal, precioFormateado?, unidad?, etiquetaUnidadOrigen?, tendencia?, error?, desactualizada)`.
  - `DetalleMercado(metal, moneda, ask, bid, maximo, minimo, variacion, variacionPorcentaje, tendencia, unidad?, etiquetaUnidadOrigen, instanteMercadoEpochMillis, desactualizada)`.
  - Todo lo textual va formateado **salvo las fechas**, que viajan como `Long` y las formatea la
    vista con `DateUtils` (su formato depende del idioma, FR-030); todo lo que la vista traduce
    o colorea viaja como enum y se mapea en `PresentacionPrecios.kt`.
- **Formato de precios** (`FormatoPrecios`, solo ViewModel): `|v| >= 1` → `setScale(2, HALF_UP)`,
  si no `setScale(4, HALF_UP)`; miles con punto, coma decimal; variación con signo; porcentaje
  siempre 2 decimales con signo. Sin fechas (las pone la vista).
- **PesoChapasUiState**: `material = ORO_18K` (la familia es `material.familia`, no se duplica),
  `medidas: Map<MedidaChapa, String>` (los tres textos tal cual), `fueraDeRango: Set<MedidaChapa>`,
  `dibujo: DibujoChapaUiState`, `resultado: ResultadoChapa?` (solo con las tres válidas).
  - `MedidaChapa(maximoMm) { ANCHO("10000"), ESPESOR("1000"), LARGO("10000") }` en orden de pintado (§11.4).
  - `ResultadoChapa(pesoFormateado «1,56», volumenFormateado «0,100», densidadFormateada «15,58», purezaFormateada «75,0», metalFinoFormateado «1,169»)`.
  - `DibujoChapaUiState(proporciones: ProporcionesChapa, etiquetaAncho?, etiquetaEspesor?, etiquetaLargo?, completa)`.
  - `ProporcionesChapa(ancho, largo, espesor: Float)` — normalización pura: `ancho = sqrt(a/max(a,l))`,
    `largo = sqrt(l/max(a,l))` ambos en [0,30; 1], `espesor = 0,35·cbrt(e/max(a,l))` en [0,05; 0,45];
    medida ausente → la de la chapa de referencia 10 × 20 × 0,5.
- **Formato de chapas** (solo ViewModel): peso `setScale(2, HALF_UP)`, volumen y metal fino 3,
  pureza `finura × 100` a 1 decimal, densidad literal; cotas «10,00 mm» (2 decimales). Lo
  mostrado nunca realimenta el cálculo ni el dibujo.

## Transiciones de estado (contratos de los ViewModels)

| ViewModel | Evento | Efecto |
|---|---|---|
| Herramientas | Elegir sub-herramienta | `subherramienta = elegida`; evento `herramientas_subherramienta`; sin repetición si es la misma |
| Precios | Crear (abrir la sección) | `logScreenView("herramientas_precios")`; `cargar()`: `CARGANDO` → repositorio → derivar |
| Precios | Instantánea completa | `LISTO`; filas convertidas a `unidad`; `detalle` del `seleccionado`; `ultimaConsultaEpochMillis`; evento `herramientas_precios_cargados {fuente, parcial=false}` |
| Precios | Instantánea parcial | `PARCIAL`; fila fallida con `error` (+ último dato `desactualizada` si lo hay); `puedeReintentar` |
| Precios | Todos fallan | `ERROR` + `errorGlobal` (motivo dominante); filas con último dato conocido si existe; evento `herramientas_precios_error {motivo}` |
| Precios | `origen = CACHE_EN_ESPERA` tras reintentar | `avisoEspera = true`, resto igual |
| Precios | Excepción inesperada | `recordError`; `ERROR` con `DESCONOCIDO`, conservando filas previas |
| Precios | Cambiar unidad | Re-deriva filas y detalle **sin red**; evento `herramientas_unidad_cambiada {unidad}` |
| Precios | Pulsar metal | `seleccionado`, re-deriva detalle; evento `herramientas_metal_seleccionado {metal}` |
| Precios | Reintentar | `cargar()` de nuevo; ignorado si hay carga en curso |
| Chapas | Crear | `logScreenView("herramientas_chapas")`; estado inicial con dibujo de referencia |
| Chapas | Cambiar medida | Recalcula: parseo, `fueraDeRango`, dibujo (etiquetas y `completa`), resultado solo con las tres válidas |
| Chapas | Cambiar familia | `material = porDefecto(familia)`, **conserva medidas**, recalcula |
| Chapas | Cambiar ley | `material`, recalcula |
| Chapas | Resultado válido | evento `herramientas_chapa_calculada {material, ley}` deduplicado por material; se rearma al invalidar |
| Chapas | Limpiar | `PesoChapasUiState()`; rearma la deduplicación |
| Chapas | Favoritos | solo evento `herramientas_chapa_favoritos_proximamente`; el aviso lo pone la vista |
