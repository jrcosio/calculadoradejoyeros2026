package com.jrblanco.calculadoradejoyeros2021.ui.favoritos

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.domain.model.FamiliaChapa
import com.jrblanco.calculadoradejoyeros2021.ui.herramientas.chapas.etiquetaRes
import com.jrblanco.calculadoradejoyeros2021.ui.herramientas.chapas.metalFinoRes
import com.jrblanco.calculadoradejoyeros2021.ui.herramientas.chapas.nombreMaterialRes
import com.jrblanco.calculadoradejoyeros2021.ui.oro.etiquetaRes
import com.jrblanco.calculadoradejoyeros2021.ui.plata.etiquetaRes
import com.jrblanco.calculadoradejoyeros2021.ui.soldaduras.etiquetaModoBaseRes
import com.jrblanco.calculadoradejoyeros2021.ui.soldaduras.etiquetaModoRes
import com.jrblanco.calculadoradejoyeros2021.ui.soldaduras.etiquetaRes
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors

// --- Cómo se pinta cada favorito. Vive aquí y no en el estado para que el ViewModel no ---
// --- conozca recursos, igual que `PresentacionAjustes` o `PresentacionChapas`.         ---

/** La imagen que identifica la sección de un vistazo: las mismas cuatro del menú, más la granalla. */
internal val TipoFavorito.imagenRes: Int
    get() = when (this) {
        TipoFavorito.ORO -> R.drawable.modulo_oro
        TipoFavorito.PLATA -> R.drawable.modulo_plata
        TipoFavorito.SOLDADURA -> R.drawable.modulo_soldaduras
        TipoFavorito.SOLDADURA_BASE -> R.drawable.granalla
        TipoFavorito.CHAPA -> R.drawable.modulo_herramientas
    }

/** El nombre de la sección. Reutiliza los títulos del menú y de cada pantalla: cero cadenas nuevas. */
internal val TipoFavorito.seccionRes: Int
    get() = when (this) {
        TipoFavorito.ORO -> R.string.modulo_oro_titulo
        TipoFavorito.PLATA -> R.string.modulo_plata_titulo
        TipoFavorito.SOLDADURA -> R.string.modulo_soldaduras_titulo
        TipoFavorito.SOLDADURA_BASE -> R.string.soldadura_base_titulo
        TipoFavorito.CHAPA -> R.string.chapas_titulo
    }

/**
 * El acento va **por sección y no por color del oro**: en un listado mezclado, teñir de turquesa un
 * oro blanco destruiría la pista «esto es ORO». El color elegido sigue vivo en el título.
 */
internal val TipoFavorito.acento: Color
    get() = when (this) {
        TipoFavorito.ORO -> JewelryColors.GoldPrimary
        TipoFavorito.PLATA -> JewelryColors.SilverPrimary
        TipoFavorito.SOLDADURA -> JewelryColors.GoldPrimary
        TipoFavorito.SOLDADURA_BASE -> JewelryColors.GoldPrimary
        TipoFavorito.CHAPA -> JewelryColors.TealPrimary
    }

/**
 * Nombre de una línea de resultado. Reutiliza las cadenas de metal que ya existen; el oro de 18 K
 * lleva el color a bordo, como en la pantalla de soldaduras, y el metal fino de una chapa depende
 * de si es de oro o de plata.
 */
@Composable
internal fun nombreDeConcepto(
    concepto: ConceptoFavorito,
    entradas: EntradasFavoritoUi,
): String = when (concepto) {
    ConceptoFavorito.PLATA_FINA -> stringResource(R.string.metal_plata_fina)
    ConceptoFavorito.COBRE -> stringResource(R.string.metal_cobre)
    ConceptoFavorito.PALADIO -> stringResource(R.string.metal_paladio)
    ConceptoFavorito.ORO_24K -> stringResource(R.string.metal_oro_24k)
    ConceptoFavorito.ORO_18K -> {
        val color = (entradas as? EntradasFavoritoUi.Soldadura)?.colorOro
        if (color == null) {
            stringResource(R.string.oro_ley_18k)
        } else {
            stringResource(R.string.soldadura_fila_oro18k, stringResource(color.etiquetaRes))
        }
    }
    ConceptoFavorito.BASE -> stringResource(R.string.soldadura_fila_base)
    ConceptoFavorito.LATON -> stringResource(R.string.metal_laton)
    ConceptoFavorito.ZINC -> stringResource(R.string.metal_zinc)
    ConceptoFavorito.CADMIO -> stringResource(R.string.metal_cadmio)
    ConceptoFavorito.VOLUMEN -> stringResource(R.string.chapas_detalle_volumen)
    ConceptoFavorito.METAL_FINO -> {
        val material = (entradas as? EntradasFavoritoUi.Chapa)?.material
        stringResource((material?.familia ?: FamiliaChapa.ORO).metalFinoRes)
    }
}

/** La unidad de cada línea: gramos, salvo el volumen de una chapa, que va en cm³. */
internal val ConceptoFavorito.unidadRes: Int
    get() = when (this) {
        ConceptoFavorito.VOLUMEN -> R.string.unidad_cm3
        else -> R.string.unidad_gramos
    }

/**
 * El título de una tarjeta: las etiquetas que ya están traducidas, unidas por « · ».
 *
 * Y no una plantilla de frase por tipo, que es lo que primero apetece. Tres motivos: reutiliza las
 * etiquetas de las cinco calculadoras y ahorra ocho cadenas por cinco idiomas; incluye el **modo**,
 * que una plantilla se come y sin el cual «10 gr» es ambiguo entre el metal de partida y el peso
 * final; y esquiva las concordancias de género y número de francés, alemán e italiano, que es donde
 * una frase montada con `%s` se rompe.
 *
 * El nombre de la sección va encima, así que el título no lo repite.
 */
@Composable
internal fun tituloDe(entradas: EntradasFavoritoUi): String {
    val partes: List<String> = when (entradas) {
        is EntradasFavoritoUi.Oro -> listOf(
            stringResource(entradas.ley.etiquetaRes),
            stringResource(entradas.color.etiquetaRes),
            gramos(entradas.cantidad),
        )

        is EntradasFavoritoUi.Plata -> listOf(
            stringResource(entradas.ley.etiquetaRes),
            gramos(entradas.cantidad),
        )

        is EntradasFavoritoUi.Soldadura -> buildList {
            add(stringResource(entradas.familia.etiquetaRes))
            entradas.dureza?.let { add(stringResource(it.etiquetaRes)) }
            entradas.tipoClasica?.let { add(stringResource(it.etiquetaRes)) }
            entradas.tipoPlata?.let { add(stringResource(it.etiquetaRes)) }
            entradas.colorOro?.let { add(stringResource(it.etiquetaRes)) }
            add(stringResource(etiquetaModoRes(entradas.familia, entradas.modo)))
            add(gramos(entradas.cantidad))
        }

        is EntradasFavoritoUi.SoldaduraBase -> listOf(
            stringResource(etiquetaModoBaseRes(entradas.modo)),
            gramos(entradas.cantidad),
        )

        is EntradasFavoritoUi.Chapa -> listOf(
            stringResource(
                entradas.material.familia.nombreMaterialRes,
                stringResource(entradas.material.etiquetaRes),
            ),
            stringResource(
                R.string.favoritos_medidas_chapa,
                entradas.ancho,
                entradas.espesor,
                entradas.largo,
            ),
        )
    }
    return partes.joinToString(SEPARADOR)
}

/** La etiqueta del total, la misma que usa la calculadora de la que salió el favorito. */
@Composable
internal fun etiquetaTotalDe(entradas: EntradasFavoritoUi): String = when (entradas) {
    is EntradasFavoritoUi.Oro ->
        stringResource(R.string.oro_total, stringResource(entradas.color.etiquetaRes))

    is EntradasFavoritoUi.Plata -> stringResource(
        R.string.plata_total,
        stringResource(R.string.chapas_ley_milesimas, entradas.ley.milesimas),
    )

    is EntradasFavoritoUi.Soldadura -> stringResource(R.string.soldadura_total)
    is EntradasFavoritoUi.SoldaduraBase -> stringResource(R.string.soldadura_base_total)
    is EntradasFavoritoUi.Chapa -> stringResource(R.string.chapas_resultado_titulo)
}

/** El mensaje del aviso efímero que las cinco calculadoras lanzan tras pulsar el botón. */
internal val AvisoFavorito.mensajeRes: Int
    get() = when (this) {
        AvisoFavorito.GUARDADO -> R.string.favoritos_aviso_guardado
        AvisoFavorito.REPETIDO -> R.string.favoritos_aviso_repetido
        AvisoFavorito.SIN_DATOS -> R.string.favoritos_aviso_sin_datos
    }

@Composable
private fun gramos(cantidad: String): String =
    stringResource(R.string.favoritos_cantidad_gramos, cantidad)

/** Puntuación, no idioma: por eso no es una cadena traducible. */
private const val SEPARADOR = " · "
