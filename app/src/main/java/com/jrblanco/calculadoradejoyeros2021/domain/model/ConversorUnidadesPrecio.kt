package com.jrblanco.calculadoradejoyeros2021.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Conversión de precios entre gramo, kilo y onza troy.
 *
 * Constantes propias, como en los otros motores: ningún tipo de este paquete importa las de
 * otro documento. Multiplicación exacta primero y **una única división**, redondeada a la
 * media a [ESCALA] decimales — aquí no hay ley que proteger, solo un precio que mostrar.
 * Se convierte siempre desde la cifra del proveedor, nunca desde una ya redondeada.
 */
object ConversorUnidadesPrecio {
    val GRAMOS_POR_ONZA_TROY: BigDecimal = BigDecimal("31.1034768")
    val GRAMOS_POR_KILO: BigDecimal = BigDecimal("1000")

    /** Libra avoirdupois, la del cobre en el proveedor (no la libra troy). */
    val GRAMOS_POR_LIBRA: BigDecimal = BigDecimal("453.59237")
    const val ESCALA = 10

    fun gramosPor(unidad: UnidadPrecio): BigDecimal = when (unidad) {
        UnidadPrecio.GRAMO -> BigDecimal.ONE
        UnidadPrecio.KILO -> GRAMOS_POR_KILO
        UnidadPrecio.ONZA_TROY -> GRAMOS_POR_ONZA_TROY
        UnidadPrecio.LIBRA -> GRAMOS_POR_LIBRA
    }

    /** Un precio por [desde] expresado por [hacia]: `importe × gramos(hacia) ÷ gramos(desde)`. */
    fun convertir(importe: BigDecimal, desde: UnidadPrecio, hacia: UnidadPrecio): BigDecimal {
        if (desde == hacia) return importe
        return importe
            .multiply(gramosPor(hacia))
            .divide(gramosPor(desde), ESCALA, RoundingMode.HALF_UP)
    }
}
