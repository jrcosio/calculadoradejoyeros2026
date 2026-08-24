package com.jrblanco.calculadoradejoyeros2021.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Resultado completo de rebajar plata fina 999‰ hasta una ley objetivo añadiendo cobre.
 *
 * Todas las cantidades están en gramos y conservan la precisión interna completa
 * (§14 del documento técnico): el redondeo para mostrar es asunto exclusivo de la capa
 * de presentación y jamás realimenta un cálculo (§21).
 *
 * El cobre es un campo y no un mapa de metales porque §2 y §33 lo fijan como **único**
 * metal de liga de esta calculadora: nada de zinc, germanio, estaño ni níquel.
 */
data class CalculoPlata(
    /** Gramos de plata fina 999‰ de partida. */
    val masaOrigen: BigDecimal,
    /** Plata pura realmente contenida en la plata de partida (§5.1). */
    val plataPura: BigDecimal,
    /** Gramos exactos de cobre a añadir (§5.3). */
    val cobre: BigDecimal,
    /** Peso final de la aleación (§5.2). */
    val masaFinal: BigDecimal,
    /** Ley teórica resultante; nunca por debajo de la objetivo (§20). */
    val leyTeorica: BigDecimal,
) {
    companion object {
        /**
         * Finura real de la plata de partida: 999‰, nunca 1000 (§4). 100 g de plata fina
         * contienen 99,900 g de plata pura, no 100.
         *
         * Estas tres constantes se repiten a propósito respecto a las de [CalculoAleacion]
         * en lugar de compartirse: son dos documentos técnicos distintos y el motor de
         * plata no debe depender de un tipo que por dentro se llama «oro».
         */
        val FINURA_ORIGEN: BigDecimal = BigDecimal("0.999")

        /**
         * Escala de las divisiones internas. §15 pide aritmética decimal de alta
         * precisión; 15 decimales dejan el error computacional muy por debajo de
         * cualquier balanza de taller.
         */
        const val ESCALA = 15

        /** Tolerancia puramente computacional de las verificaciones (§20). */
        val TOLERANCIA: BigDecimal = BigDecimal("1E-9")

        /**
         * Construye el resultado a partir de las dos masas y lo verifica.
         *
         * La multiplicación es exacta: aquí no se redondea nada (§14). Quien llama es
         * responsable de haber redondeado su única división **a favor de la ley** (a la
         * baja el peso final en el modo directo, al alza la plata necesaria en el
         * inverso), y la verificación final lo garantiza.
         */
        internal fun de(
            masaOrigen: BigDecimal,
            masaFinal: BigDecimal,
            ley: LeyPlata,
        ): CalculoPlata {
            val plataPura = masaOrigen.multiply(FINURA_ORIGEN)
            val cobre = masaFinal.subtract(masaOrigen)
            val leyTeorica = plataPura.divide(masaFinal, ESCALA, RoundingMode.DOWN)

            // Verificación obligatoria (§20): red de seguridad, no lógica de negocio.
            check(cobre > BigDecimal.ZERO) {
                "El cobre a añadir debe ser positivo y salió $cobre"
            }
            check(leyTeorica >= ley.finura) {
                "La ley resultante ($leyTeorica) queda por debajo de la objetivo (${ley.finura})"
            }

            return CalculoPlata(
                masaOrigen = masaOrigen,
                plataPura = plataPura,
                cobre = cobre,
                masaFinal = masaFinal,
                leyTeorica = leyTeorica,
            )
        }
    }
}
