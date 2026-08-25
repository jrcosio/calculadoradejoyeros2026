package com.jrblanco.calculadoradejoyeros2021.ui.herramientas.precios

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Formato de los importes de la pantalla de precios: coma decimal y punto de miles, dos
 * decimales a partir de 1 y cuatro por debajo (el cobre por gramo ronda 0,0089 €), redondeo a
 * la media. Es la primera pantalla de la app con cifras de seis dígitos (el kilo de oro), de ahí
 * los miles. Solo JDK, sin `Locale`: determinista. Las fechas **no** se formatean aquí: las
 * pone la vista, porque dependen del idioma.
 */
internal object FormatoPrecios {

    fun importe(valor: BigDecimal): String {
        val decimales = if (valor.abs() >= BigDecimal.ONE) 2 else 4
        return conMiles(valor.setScale(decimales, RoundingMode.HALF_UP))
    }

    /** Como [importe], con «+» delante si sube. */
    fun variacion(valor: BigDecimal): String = signo(valor) + importe(valor)

    /** Siempre dos decimales y signo: un porcentaje no es un importe. */
    fun porcentaje(valor: BigDecimal): String = signo(valor) + conMiles(valor.setScale(2, RoundingMode.HALF_UP))

    private fun signo(valor: BigDecimal): String = if (valor.signum() > 0) "+" else ""

    private fun conMiles(valor: BigDecimal): String {
        val texto = valor.toPlainString()
        val negativo = texto.startsWith("-")
        val partes = texto.removePrefix("-").split('.')
        val entera = partes[0].reversed().chunked(3).joinToString(".").reversed()
        return buildString {
            if (negativo) append('-')
            append(entera)
            partes.getOrNull(1)?.let { decimal ->
                append(',')
                append(decimal)
            }
        }
    }
}
