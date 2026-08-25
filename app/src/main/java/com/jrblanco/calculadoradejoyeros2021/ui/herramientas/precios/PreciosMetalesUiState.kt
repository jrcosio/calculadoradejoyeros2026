package com.jrblanco.calculadoradejoyeros2021.ui.herramientas.precios

import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalCotizado
import com.jrblanco.calculadoradejoyeros2021.domain.model.MotivoErrorCotizacion
import com.jrblanco.calculadoradejoyeros2021.domain.model.OrigenDatos
import com.jrblanco.calculadoradejoyeros2021.domain.model.Tendencia
import com.jrblanco.calculadoradejoyeros2021.domain.model.UnidadPrecio

/** En qué punto está la pantalla de precios respecto a los datos. */
enum class FasePrecios {
    /** Primera carga, sin nada que pintar todavía. */
    CARGANDO,

    /** Los cinco metales con precio. */
    LISTO,

    /** Algún metal falló; los demás se muestran. */
    PARCIAL,

    /** Ningún metal tiene precio fresco (puede haber últimos datos conocidos). */
    ERROR,
}

/**
 * Una fila de la lista, ya formateada. [precioFormateado] es `null` cuando no hay dato; [unidad]
 * es la unidad en que se muestra y vale `null` cuando el proveedor no confirmó la de origen, en
 * cuyo caso se pinta [etiquetaUnidadOrigen] tal cual. [desactualizada] marca un precio antiguo
 * que acompaña a un [error].
 */
data class FilaMetalPrecio(
    val metal: MetalCotizado,
    val precioFormateado: String?,
    val unidad: UnidadPrecio?,
    val etiquetaUnidadOrigen: String?,
    val tendencia: Tendencia?,
    val error: MotivoErrorCotizacion?,
    val desactualizada: Boolean,
)

/** La tarjeta «Información del mercado» del metal elegido, ya formateada. */
data class DetalleMercado(
    val metal: MetalCotizado,
    val moneda: String,
    val ask: String,
    val bid: String,
    val maximo: String,
    val minimo: String,
    val variacion: String,
    val variacionPorcentaje: String,
    val tendencia: Tendencia,
    val unidad: UnidadPrecio?,
    val etiquetaUnidadOrigen: String,
    val instanteMercadoEpochMillis: Long,
    val desactualizada: Boolean,
)

/**
 * Estado de la sub-herramienta de precios.
 *
 * Todo lo textual va formateado **salvo las fechas**, que viajan como instantes (`Long`) porque
 * su formato depende del idioma y lo pone la vista con `DateUtils`; lo que la vista traduce o
 * colorea (metal, unidad, tendencia, motivo) viaja como enum. Es un data class plano y no el
 * `UiState` genérico de `core/ui`: el estado real es un producto de piezas, no tres casos, y un
 * `Error(message: String)` obligaría al ViewModel a fabricar texto.
 */
data class PreciosMetalesUiState(
    val fase: FasePrecios = FasePrecios.CARGANDO,
    val reintentando: Boolean = false,
    val filas: List<FilaMetalPrecio> = emptyList(),
    val unidad: UnidadPrecio = UnidadPrecio.GRAMO,
    val seleccionado: MetalCotizado = MetalCotizado.ORO,
    val detalle: DetalleMercado? = null,
    /** Solo en [FasePrecios.ERROR]: el motivo más repetido entre los cinco. */
    val errorGlobal: MotivoErrorCotizacion? = null,
    val origen: OrigenDatos? = null,
    /** El `obtenidoEn` más reciente: la hora de la última consulta con dato. */
    val ultimaConsultaEpochMillis: Long? = null,
    val puedeReintentar: Boolean = false,
    val avisoEspera: Boolean = false,
)
