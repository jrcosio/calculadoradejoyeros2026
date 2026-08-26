package com.jrblanco.calculadoradejoyeros2021.domain.model

import java.math.BigDecimal

/**
 * Un cálculo que el joyero ha querido conservar: su identidad en el almacén, cuándo lo guardó y las
 * entradas exactas con las que se calculó.
 *
 * No lleva resultado ni título, y los dos por el mismo motivo: son dato derivado. Las cifras las
 * rehace [com.jrblanco.calculadoradejoyeros2021.domain.usecase.ResumirFavoritoUseCase] con los
 * motores de siempre, y el título lo compone la pantalla con sus recursos — guardar texto formateado
 * lo congelaría en un idioma, y la app habla cinco.
 *
 * El `id` y la hora viven aquí y no dentro de [entradas] porque al guardar todavía no existen: así
 * `guardar(entradas)` no tiene que inventarse un id ni una fecha falsa.
 */
data class Favorito(
    val id: Long,
    val guardadoEnEpochMillis: Long,
    val entradas: EntradasFavorito,
)

/**
 * Las entradas de un favorito, y nada más: lo único que se persiste y lo único que decide si dos
 * favoritos son el mismo.
 *
 * Una variante por **formulario**, no por pantalla: la calculadora de soldaduras tiene tres familias
 * y cada una elige sus propios motores, así que cada una es su propia variante. Los campos de cada
 * variante son exactamente los parámetros del motor que le corresponde, **en su mismo orden**, para
 * que el `when` que las resuelve no pueda equivocarse de argumento.
 *
 * Siete variantes y no una con el tipo dentro por el mismo criterio que ya usa
 * `CalcularSoldaduraClasicaUseCase`: «la prohibición va en el diseño de tipos, no en una validación».
 * Una variante única obligaría a un color nulable en las recetas clásicas, que es justo lo que §8.1
 * de su documento prohíbe elegir.
 *
 * **Ojo con `==`**: `BigDecimal.equals` compara también la escala, así que `Oro(BigDecimal("30"), …)`
 * y `Oro(BigDecimal("30.0"), …)` **no** son iguales aunque sean el mismo favorito. La identidad la
 * define la firma canónica de `CodificadorFavorito`, nunca `equals`: prohibido `distinctBy`,
 * `contains` o `indexOf` sobre entradas.
 *
 * Contrato completo en `specs/009-favoritos/contracts/favoritos-persistidos.md`.
 */
sealed interface EntradasFavorito {

    /** Identificador estable, independiente del idioma. Es también el discriminador del almacén. */
    val analyticsId: String

    data class Oro(
        val masaOrigen: BigDecimal,
        val color: ColorOro,
        val ley: LeyOro,
    ) : EntradasFavorito {
        init { exigePositiva(masaOrigen, "masaOrigen") }
        override val analyticsId: String get() = "oro"
    }

    data class Plata(
        val masaOrigen: BigDecimal,
        val ley: LeyPlata,
    ) : EntradasFavorito {
        init { exigePositiva(masaOrigen, "masaOrigen") }
        override val analyticsId: String get() = "plata"
    }

    data class SoldaduraLey(
        val cantidad: BigDecimal,
        val dureza: DurezaSoldaduraLey,
        val color: ColorOroSoldadura,
        val modo: ModoEntradaSoldadura,
    ) : EntradasFavorito {
        init { exigePositiva(cantidad, "cantidad") }
        override val analyticsId: String get() = "soldadura_ley"
    }

    data class SoldaduraClasica(
        val cantidad: BigDecimal,
        val tipo: TipoSoldaduraClasica,
        val modo: ModoEntradaSoldadura,
    ) : EntradasFavorito {
        init { exigePositiva(cantidad, "cantidad") }
        override val analyticsId: String get() = "soldadura_clasica"
    }

    data class SoldaduraPlata(
        val cantidad: BigDecimal,
        val tipo: TipoSoldaduraPlata,
        val modo: ModoEntradaSoldadura,
    ) : EntradasFavorito {
        init { exigePositiva(cantidad, "cantidad") }
        override val analyticsId: String get() = "soldadura_plata"
    }

    data class SoldaduraBase(
        val cantidad: BigDecimal,
        val modo: ModoEntradaSoldadura,
    ) : EntradasFavorito {
        init { exigePositiva(cantidad, "cantidad") }
        override val analyticsId: String get() = "soldadura_base"
    }

    data class Chapa(
        val ancho: BigDecimal,
        val largo: BigDecimal,
        val espesor: BigDecimal,
        val material: MaterialChapa,
    ) : EntradasFavorito {
        init {
            exigePositiva(ancho, "ancho")
            exigePositiva(largo, "largo")
            exigePositiva(espesor, "espesor")
        }
        override val analyticsId: String get() = "chapa"
    }

    companion object {
        /**
         * Un favorito con una medida que no sirve es **inconstruible**, y eso es lo que garantiza
         * que rehacer sus cifras nunca dispare el `require` de un motor. El decodificador del
         * almacén captura esta excepción y descarta la fila.
         *
         * El límite operativo de las chapas (10 000 mm de ancho y largo, 1 000 de espesor) **no**
         * se repite aquí: es control de interfaz y vive en su ViewModel. Un favorito sólo puede
         * nacer de un formulario que ya lo respetó.
         */
        private fun exigePositiva(valor: BigDecimal, nombre: String) {
            require(valor > BigDecimal.ZERO) { "$nombre debe ser mayor que cero: $valor" }
        }
    }
}
