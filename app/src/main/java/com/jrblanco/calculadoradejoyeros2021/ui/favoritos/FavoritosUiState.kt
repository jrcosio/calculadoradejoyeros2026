package com.jrblanco.calculadoradejoyeros2021.ui.favoritos

import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOroSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.DurezaSoldaduraLey
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyPlata
import com.jrblanco.calculadoradejoyeros2021.domain.model.MaterialChapa
import com.jrblanco.calculadoradejoyeros2021.domain.model.ModoEntradaSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.TipoSoldaduraClasica
import com.jrblanco.calculadoradejoyeros2021.domain.model.TipoSoldaduraPlata
import com.jrblanco.calculadoradejoyeros2021.ui.soldaduras.FamiliaSoldadura

/**
 * Lo que se ve en la pestaña Favoritos.
 *
 * [cargando] **hace falta**: la primera emisión del flujo llega un fotograma después de componer, y
 * sin esta guarda la tarjeta de «Aún no hay favoritos» parpadearía en cada visita (FR-018). Es el
 * mismo criterio por el que `MainActivity` no compone el `NavHost` hasta saber el idioma.
 *
 * [pendienteDeBorrar] vive aquí y no en un `rememberSaveable` porque los tests instrumentados del
 * proyecto montan el contenido sin ViewModel. El ViewModel guarda el id aparte y **re-deriva** este
 * campo en cada emisión, así que una emisión concurrente no puede dejar el diálogo colgado sobre un
 * favorito que ya no existe.
 */
data class FavoritosUiState(
    val cargando: Boolean = true,
    val favoritos: List<FavoritoUiModel> = emptyList(),
    val pendienteDeBorrar: FavoritoUiModel? = null,
)

/**
 * Una tarjeta del listado. Cifras ya formateadas y lo traducible como enum, según el contrato de
 * ViewModel del proyecto; la fecha viaja como `Long` porque el nombre del mes depende del idioma y
 * el ViewModel no conoce recursos.
 *
 * [lineas] viaja **completa**: el recorte a tres es constante de layout y vive en la tarjeta.
 */
data class FavoritoUiModel(
    val id: Long,
    val entradas: EntradasFavoritoUi,
    val lineas: List<LineaFavoritoUi>,
    val totalFormateado: String,
    val guardadoEnEpochMillis: Long,
) {
    /** La sección se deriva de las entradas, no se duplica. */
    val tipo: TipoFavorito get() = entradas.tipo
}

data class LineaFavoritoUi(
    val concepto: ConceptoFavorito,
    val valorFormateado: String,
)

/**
 * La sección de la que salió el favorito: le da imagen, nombre y acento a la tarjeta, y decide a
 * dónde lleva al pulsarla.
 *
 * **Cinco valores y no siete**: las tres familias de soldadura comparten pantalla y sección, así que
 * las siete variantes de `EntradasFavorito` colapsan aquí a cinco. La BASE tiene ruta propia y por
 * eso es sección propia. El dominio distingue por motor; la interfaz, por pantalla. No confundir con
 * `EntradasFavorito.analyticsId`, que tiene siete valores y es el discriminador del almacén.
 */
enum class TipoFavorito {
    ORO,
    PLATA,
    SOLDADURA,
    SOLDADURA_BASE,
    CHAPA,
    ;

    /** Identificador estable para telemetría, independiente del idioma. */
    val analyticsId: String get() = name.lowercase()
}

/**
 * Lo que el joyero eligió, con las cifras ya formateadas y lo traducible como enum. El título de la
 * tarjeta se compone de aquí.
 *
 * Las tres familias de soldadura caben en [Soldadura] con sus campos nulables porque comparten
 * pantalla y título; en dominio son tres variantes distintas porque cada una llama a otro motor.
 */
sealed interface EntradasFavoritoUi {

    val tipo: TipoFavorito

    data class Oro(
        val ley: LeyOro,
        val color: ColorOro,
        val cantidad: String,
    ) : EntradasFavoritoUi {
        override val tipo: TipoFavorito get() = TipoFavorito.ORO
    }

    data class Plata(
        val ley: LeyPlata,
        val cantidad: String,
    ) : EntradasFavoritoUi {
        override val tipo: TipoFavorito get() = TipoFavorito.PLATA
    }

    data class Soldadura(
        val familia: FamiliaSoldadura,
        val modo: ModoEntradaSoldadura,
        val cantidad: String,
        val dureza: DurezaSoldaduraLey? = null,
        val colorOro: ColorOroSoldadura? = null,
        val tipoClasica: TipoSoldaduraClasica? = null,
        val tipoPlata: TipoSoldaduraPlata? = null,
    ) : EntradasFavoritoUi {
        override val tipo: TipoFavorito get() = TipoFavorito.SOLDADURA
    }

    data class SoldaduraBase(
        val modo: ModoEntradaSoldadura,
        val cantidad: String,
    ) : EntradasFavoritoUi {
        override val tipo: TipoFavorito get() = TipoFavorito.SOLDADURA_BASE
    }

    data class Chapa(
        val material: MaterialChapa,
        val ancho: String,
        val largo: String,
        val espesor: String,
    ) : EntradasFavoritoUi {
        override val tipo: TipoFavorito get() = TipoFavorito.CHAPA
    }
}

/**
 * Lo que nombra cada línea de resultado de una tarjeta. Enum **de esta capa**: aplana `MetalLiga`,
 * `MetalSoldadura` y los detalles de una chapa en una sola lista, que es lo que una lista mezclada
 * necesita, sin meter un cuarto enum paralelo en `domain/`.
 */
enum class ConceptoFavorito {
    PLATA_FINA,
    COBRE,
    PALADIO,
    ORO_24K,
    ORO_18K,
    BASE,
    LATON,
    ZINC,
    CADMIO,
    VOLUMEN,
    METAL_FINO,
}
