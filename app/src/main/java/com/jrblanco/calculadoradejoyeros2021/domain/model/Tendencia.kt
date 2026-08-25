package com.jrblanco.calculadoradejoyeros2021.domain.model

import java.math.BigDecimal

/** Hacia dónde va un metal en la sesión, según el signo de su variación. */
enum class Tendencia {
    SUBE,
    BAJA,
    PLANA,
    ;

    companion object {
        fun de(variacion: BigDecimal): Tendencia = when (variacion.signum()) {
            1 -> SUBE
            -1 -> BAJA
            else -> PLANA
        }
    }
}
