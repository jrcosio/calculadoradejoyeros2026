package com.jrblanco.calculadoradejoyeros2021.ui.favoritos

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.ui.components.GoldSeparator
import com.jrblanco.calculadoradejoyeros2021.ui.components.LineaPunteada
import com.jrblanco.calculadoradejoyeros2021.ui.components.TarjetaAcento
import com.jrblanco.calculadoradejoyeros2021.ui.components.fechaLocal
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySize
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySpacing

/**
 * Una tarjeta del listado de favoritos: imagen y nombre de la sección para reconocerla de un
 * vistazo, un título con las entradas, las cifras del resultado, el total y la fecha.
 *
 * No reutiliza [com.jrblanco.calculadoradejoyeros2021.ui.components.ModuleCard]: aquella tiene
 * altura fija de 158 dp, dos textos exactos y una sola zona pulsable. Tampoco `FilaMetal` ni
 * `TarjetaTotal`, que son piezas de pantalla de detalle — con su imagen de 44 dp y su cifra de
 * 26 sp, cuatro de ellas dentro de una tarjeta de lista serían un muro. De ahí las dos filas
 * compactas privadas de este fichero.
 *
 * **Dos zonas pulsables y cómo sobreviven al lector de pantalla**: la tarjeta se fusiona en un solo
 * nodo para anunciarse como una frase, y la estrella lleva **su propio** `mergeDescendants`, que la
 * convierte en frontera de fusión y la salva como segundo nodo enfocable. Sin ese detalle, TalkBack
 * no podría quitar un favorito, y no es algo que se vea mirando la pantalla.
 */
@Composable
internal fun TarjetaFavorito(
    favorito: FavoritoUiModel,
    onAbrir: () -> Unit,
    onQuitar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val acento = favorito.tipo.acento
    val visibles = favorito.lineas.take(MAX_LINEAS_VISIBLES)
    val ocultas = favorito.lineas.size - visibles.size

    TarjetaAcento(
        modifier = modifier.semantics(mergeDescendants = true) {},
        acento = acento,
        onClick = onAbrir,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(favorito.tipo.imagenRes),
                // El nombre de la sección va justo al lado: dentro de un nodo fusionado, describir
                // la imagen sería ruido antes de lo que importa.
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(64.dp),
            )
            Spacer(Modifier.width(JewelrySpacing.Md))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(favorito.tipo.seccionRes),
                    style = MaterialTheme.typography.labelMedium,
                    color = acento,
                )
                Spacer(Modifier.height(JewelrySpacing.Xs))
                Text(
                    text = tituloDe(favorito.entradas),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                    color = JewelryColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            EstrellaFavorito(onQuitar)
        }

        if (visibles.isNotEmpty()) {
            Spacer(Modifier.height(JewelrySpacing.Sm))
            GoldSeparator()
            Spacer(Modifier.height(JewelrySpacing.Sm))
            visibles.forEach { linea ->
                FilaResumenFavorito(linea, favorito.entradas, acento)
            }
            if (ocultas > 0) {
                Text(
                    text = stringResource(R.string.favoritos_mas_lineas, ocultas),
                    style = MaterialTheme.typography.labelMedium,
                    color = JewelryColors.TextMuted,
                )
            }
        }

        Spacer(Modifier.height(JewelrySpacing.Sm))
        FilaTotalFavorito(
            etiqueta = etiquetaTotalDe(favorito.entradas),
            total = favorito.totalFormateado,
            acento = acento,
        )
        Text(
            text = stringResource(R.string.favoritos_guardado_el, fechaLocal(favorito.guardadoEnEpochMillis)),
            style = MaterialTheme.typography.labelMedium,
            color = JewelryColors.TextMuted,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Fila compacta de resultado: nombre, puntos de guía, cifra y unidad. Sin imagen: es una lista. */
@Composable
private fun FilaResumenFavorito(
    linea: LineaFavoritoUi,
    entradas: EntradasFavoritoUi,
    acento: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = nombreDeConcepto(linea.concepto, entradas),
            style = MaterialTheme.typography.bodyMedium,
            color = JewelryColors.TextSecondary,
        )
        LineaPunteada(
            color = acento.copy(alpha = 0.45f),
            modifier = Modifier.weight(1f).padding(horizontal = JewelrySpacing.Sm),
        )
        Text(
            text = linea.valorFormateado,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = acento,
        )
        Spacer(Modifier.width(JewelrySpacing.Xs))
        Text(
            text = stringResource(linea.concepto.unidadRes),
            style = MaterialTheme.typography.labelMedium,
            color = JewelryColors.GoldPrimary,
        )
    }
}

/** El total, con la balanza delante y la etiqueta de su propia calculadora. */
@Composable
private fun FilaTotalFavorito(etiqueta: String, total: String, acento: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_balanza),
            contentDescription = null,
            tint = acento,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(JewelrySpacing.Sm))
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = JewelryColors.TextPrimary,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = total,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = acento,
        )
        Spacer(Modifier.width(JewelrySpacing.Xs))
        Text(
            text = stringResource(R.string.unidad_gramos),
            style = MaterialTheme.typography.labelMedium,
            color = JewelryColors.GoldPrimary,
        )
    }
}

/**
 * La estrella que quita el favorito. Rellena y no de trazo: la de trazo dice «guardar», y aquí todo
 * lo que se ve **ya está** guardado, así que sólo tiene sentido «quitar». Por eso tampoco hay estado
 * vacío ni `toggleable`: no conmuta en el sitio, abre una pregunta, y su `Role` es `Button`.
 *
 * El `semantics(mergeDescendants = true)` es lo que la mantiene como nodo enfocable propio dentro de
 * la tarjeta fusionada. No es decoración.
 */
@Composable
private fun EstrellaFavorito(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(JewelrySize.MinTouchTarget)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) {},
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_estrella_llena),
            contentDescription = stringResource(R.string.favoritos_quitar),
            tint = JewelryColors.GoldPrimary,
            modifier = Modifier.size(22.dp),
        )
    }
}

/**
 * Tres líneas de resultado como máximo, y el resto contadas.
 *
 * Es constante de **layout**, hermana del `158.dp` de `ModuleCard`, así que vive aquí y no en el
 * ViewModel: el estado emite la lista completa y la tarjeta decide cuánto cabe. El detalle está a un
 * toque, y recalculado idéntico.
 */
private const val MAX_LINEAS_VISIBLES = 3
