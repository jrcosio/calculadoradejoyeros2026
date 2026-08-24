package com.jrblanco.calculadoradejoyeros2021.domain.model

import java.math.BigDecimal

/** Un ingrediente calculado con sus gramos exactos, sin redondear. */
data class ComponenteCalculado(
    val metal: MetalSoldadura,
    val gramos: BigDecimal,
)

/**
 * Resultado de escalar una receta o aplicar un factor de plata: los ingredientes en el
 * orden estable de su receta y el peso final teórico.
 *
 * Todas las cantidades están en gramos y conservan la precisión interna completa (§2.1
 * del documento técnico): el redondeo para mostrar es asunto exclusivo de la capa de
 * presentación y jamás realimenta un cálculo (§8.1, §8.3).
 */
data class CalculoSoldadura(
    val componentes: List<ComponenteCalculado>,
    /** Peso final teórico: suma exacta de los componentes. */
    val total: BigDecimal,
) {
    companion object {
        /**
         * Escala de la única división de cada cálculo. §2.1 pide aritmética decimal de
         * alta precisión; 15 decimales dejan el residuo doce órdenes por debajo de la
         * balanza de 0,001 g.
         *
         * Estas dos constantes se repiten a propósito respecto a las de [CalculoAleacion]
         * y [CalculoPlata] en lugar de compartirse: son tres documentos técnicos
         * distintos y cada motor es fiel al suyo.
         */
        const val ESCALA = 15

        /** Tolerancia puramente computacional de las verificaciones (§10). */
        val TOLERANCIA: BigDecimal = BigDecimal("1E-9")

        /**
         * Escala una receta por un factor: cada peso patrón multiplicado, exacto y sin
         * redondeos intermedios (§2.2, §8.1). Quien llama es responsable de su única
         * división —la que produce el factor— a [ESCALA] decimales.
         */
        internal fun escalar(receta: RecetaSoldadura, factor: BigDecimal): CalculoSoldadura {
            val componentes = receta.componentes.map { componente ->
                ComponenteCalculado(componente.metal, componente.pesoPatron.multiply(factor))
            }
            return de(componentes)
        }

        /**
         * Construye el resultado y verifica las propiedades de §10 como `check`: todos
         * los componentes positivos y el total igual a su suma. Red de seguridad, no
         * lógica de negocio.
         */
        internal fun de(componentes: List<ComponenteCalculado>): CalculoSoldadura {
            check(componentes.all { it.gramos > BigDecimal.ZERO }) {
                "Todos los componentes calculados deben ser positivos: $componentes"
            }

            val total = componentes.fold(BigDecimal.ZERO) { suma, componente ->
                suma.add(componente.gramos)
            }

            return CalculoSoldadura(componentes = componentes, total = total)
        }
    }
}
