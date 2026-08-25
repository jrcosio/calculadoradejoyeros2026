package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.ConversorUnidadesPrecio
import com.jrblanco.calculadoradejoyeros2021.domain.model.CotizacionMetal
import com.jrblanco.calculadoradejoyeros2021.domain.model.UnidadPrecio
import java.math.BigDecimal

/**
 * Expresa una cotización en la unidad que eligió el joyero.
 *
 * Convierte todos los importes lineales (venta, compra, medio, máximo, mínimo, variación) y
 * deja intacta la variación porcentual, que no depende de la unidad. Devuelve `null` si el
 * proveedor no confirmó la unidad de origen: ese precio se muestra tal cual, sin convertir.
 */
class ConvertirCotizacionUseCase {
    operator fun invoke(cotizacion: CotizacionMetal, hacia: UnidadPrecio): CotizacionMetal? {
        val desde = cotizacion.unidadOrigen ?: return null
        if (desde == hacia) return cotizacion
        fun convertir(importe: BigDecimal) = ConversorUnidadesPrecio.convertir(importe, desde, hacia)
        return cotizacion.copy(
            ask = convertir(cotizacion.ask),
            bid = convertir(cotizacion.bid),
            mid = convertir(cotizacion.mid),
            maximo = convertir(cotizacion.maximo),
            minimo = convertir(cotizacion.minimo),
            variacion = convertir(cotizacion.variacion),
            unidadOrigen = hacia,
        )
    }
}
