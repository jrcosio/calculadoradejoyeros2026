package com.jrblanco.calculadoradejoyeros2021.domain.model

import java.math.BigDecimal

/**
 * El dato de mercado de un metal tal y como lo entrega el proveedor, en la unidad en que él
 * cotiza, más los dos instantes que necesita la caché.
 *
 * Todos los importes son `BigDecimal` construidos desde el literal que llegó por la red,
 * nunca desde `Double`. [unidadOrigen] es `null` cuando el proveedor no confirma la unidad;
 * entonces el precio se muestra tal cual con [etiquetaUnidadOrigen] y no se convierte.
 *
 * @property instanteMercadoEpochMillis instante del dato según el proveedor.
 * @property obtenidoEnEpochMillis instante en que la app lo recibió; es lo que decide la
 *   vigencia de la caché, metal a metal.
 */
data class CotizacionMetal(
    val metal: MetalCotizado,
    val moneda: String,
    val ask: BigDecimal,
    val bid: BigDecimal,
    val mid: BigDecimal,
    val maximo: BigDecimal,
    val minimo: BigDecimal,
    val variacion: BigDecimal,
    val variacionPorcentaje: BigDecimal,
    val unidadOrigen: UnidadPrecio?,
    val etiquetaUnidadOrigen: String,
    val instanteMercadoEpochMillis: Long,
    val obtenidoEnEpochMillis: Long,
) {
    /**
     * La cifra que encabeza la fila: el precio medio del mercado (decisión del autor) y, si
     * el proveedor lo manda a cero, el de venta y después el de compra. `open` y `close`
     * llegan a cero en la muestra real y no se modelan. `null` si no hay ninguno.
     */
    val precioPrincipal: BigDecimal?
        get() = listOf(mid, ask, bid).firstOrNull { it.signum() > 0 }

    val tendencia: Tendencia get() = Tendencia.de(variacion)
}
