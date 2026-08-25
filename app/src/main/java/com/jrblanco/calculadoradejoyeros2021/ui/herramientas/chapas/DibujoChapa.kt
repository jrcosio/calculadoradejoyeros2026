package com.jrblanco.calculadoradejoyeros2021.ui.herramientas.chapas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.domain.model.FamiliaChapa
import com.jrblanco.calculadoradejoyeros2021.ui.theme.Calculadoradejoyeros2021Theme
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySpacing
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * La chapa dibujada: una caja en proyección oblicua (*cabinet*) con sus tres cotas, que se
 * construye al entrar —primero el contorno, luego las caras, por último las cotas— y se
 * redibuja con cada medida que teclea el joyero, en el color del metal.
 *
 * Ejes: el ancho es la arista frontal (cota debajo), el largo la arista en fuga (cota a la
 * derecha) y el espesor el canto (cota a la izquierda), como el mockup. Las proporciones son
 * las **visuales** de [ProporcionesChapa]; los cálculos de peso nunca las usan (FR-024).
 *
 * Primer `Canvas` de la app. Los valores animados se leen **solo** dentro de `onDrawBehind`,
 * así cada fotograma invalida el dibujo y nada más; los `Path` se reutilizan entre fotogramas
 * gracias a `drawWithCache`. En las `@Preview` (`LocalInspectionMode`) la construcción arranca
 * ya terminada.
 */
@Composable
fun DibujoChapa(
    estado: DibujoChapaUiState,
    familia: FamiliaChapa,
    descripcion: String,
    modifier: Modifier = Modifier,
) {
    val muelle = spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)
    val ancho by animateFloatAsState(estado.proporciones.ancho, muelle, label = "ancho")
    val espesor by animateFloatAsState(estado.proporciones.espesor, muelle, label = "espesor")
    val largo by animateFloatAsState(estado.proporciones.largo, muelle, label = "largo")
    val opacidadCaras by animateFloatAsState(if (estado.completa) 1f else OPACIDAD_INCOMPLETA, label = "opacidad")

    // La construcción: 0 → 1 al entrar y cada vez que cambia el metal.
    val inspeccion = LocalInspectionMode.current
    val progreso = remember { Animatable(if (inspeccion) 1f else 0f) }
    LaunchedEffect(familia) {
        if (!inspeccion) {
            progreso.snapTo(0f)
            progreso.animateTo(1f, tween(DURACION_CONSTRUCCION_MS, easing = FastOutSlowInEasing))
        }
    }

    val paleta = remember(familia) { PaletaChapa.de(familia) }
    val colorCota = JewelryColors.TealPrimary
    val medidorTexto = rememberTextMeasurer()
    val estiloCota = MaterialTheme.typography.labelMedium.copy(color = JewelryColors.TextPrimary, fontSize = 12.sp)
    val textoAncho = estado.etiquetaAncho?.let { stringResource(R.string.chapas_dibujo_medida, it) }
    val textoEspesor = estado.etiquetaEspesor?.let { stringResource(R.string.chapas_dibujo_medida, it) }
    val textoLargo = estado.etiquetaLargo?.let { stringResource(R.string.chapas_dibujo_medida, it) }
    val cotaAncho = remember(textoAncho, estiloCota) { textoAncho?.let { medidorTexto.measure(AnnotatedString(it), estiloCota) } }
    val cotaEspesor = remember(textoEspesor, estiloCota) { textoEspesor?.let { medidorTexto.measure(AnnotatedString(it), estiloCota) } }
    val cotaLargo = remember(textoLargo, estiloCota) { textoLargo?.let { medidorTexto.measure(AnnotatedString(it), estiloCota) } }

    Spacer(
        modifier = modifier
            .semantics { contentDescription = descripcion }
            .drawWithCache {
                val caraFrontal = Path()
                val caraSuperior = Path()
                val caraDerecha = Path()
                val silueta = Path()
                val aristasInternas = Path()
                val segmento = Path()
                val medidorCamino = PathMeasure()
                val trazoCota = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 4.dp.toPx())))
                val trazoExtension = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx())))
                val trazoArista = Stroke(width = 1.dp.toPx())
                val margenLateral = 64.dp.toPx()
                val margenSuperior = 12.dp.toPx()
                val margenInferior = 40.dp.toPx()
                val separacion = 22.dp.toPx()
                val extension = 4.dp.toPx()
                val flecha = Flecha(largo = 6.dp.toPx(), medioAncho = 3.dp.toPx())
                val holguraTexto = 8.dp.toPx()
                val desplazamientoLargo = Offset(16.dp.toPx(), 10.dp.toPx())

                onDrawBehind {
                    val a = ancho
                    val e = espesor
                    val l = largo
                    val p = progreso.value

                    // Proyección oblicua: la profundidad huye a 30° y a mitad de escala.
                    val anchoUnidades = a + PROFUNDIDAD_X * l
                    val altoUnidades = e + PROFUNDIDAD_Y * l
                    val escala = min(
                        (size.width - 2 * margenLateral) / anchoUnidades,
                        (size.height - margenSuperior - margenInferior) / altoUnidades,
                    )
                    val origenX = (size.width - escala * anchoUnidades) / 2f
                    val origenY = size.height - margenInferior
                    fun punto(x: Float, y: Float, z: Float) =
                        Offset(origenX + escala * (x + z * PROFUNDIDAD_X), origenY - escala * (y + z * PROFUNDIDAD_Y))

                    val v0 = punto(0f, 0f, 0f)
                    val v1 = punto(a, 0f, 0f)
                    val v2 = punto(a, e, 0f)
                    val v3 = punto(0f, e, 0f)
                    val v4 = punto(0f, e, l)
                    val v5 = punto(a, e, l)
                    val v6 = punto(a, 0f, l)

                    caraDerecha.rewind(); caraDerecha.poligono(v1, v6, v5, v2)
                    caraFrontal.rewind(); caraFrontal.poligono(v0, v1, v2, v3)
                    caraSuperior.rewind(); caraSuperior.poligono(v3, v2, v5, v4)
                    silueta.rewind(); silueta.poligono(v0, v1, v6, v5, v4, v3)
                    aristasInternas.rewind()
                    aristasInternas.moveTo(v1.x, v1.y); aristasInternas.lineTo(v2.x, v2.y)
                    aristasInternas.lineTo(v5.x, v5.y)
                    aristasInternas.moveTo(v2.x, v2.y); aristasInternas.lineTo(v3.x, v3.y)

                    val opacidadFase = opacidadCaras * suavizar(0.35f, 0.80f, p)
                    val opacidadCotas = suavizar(0.70f, 1f, p)

                    // Caras: derecha, frontal y superior (la más clara, la última).
                    if (opacidadFase > 0f) {
                        drawPath(caraDerecha, paleta.derecha, alpha = opacidadFase)
                        drawPath(caraFrontal, paleta.frontal, alpha = opacidadFase)
                        drawPath(caraSuperior, Brush.linearGradient(paleta.superior, start = v4, end = v1), alpha = opacidadFase)
                        drawPath(aristasInternas, paleta.aristas, alpha = opacidadFase, style = trazoArista)
                    }

                    // El contorno se traza de punta a punta durante la primera parte.
                    medidorCamino.setPath(silueta, forceClosed = true)
                    segmento.rewind()
                    medidorCamino.getSegment(0f, medidorCamino.length * (p / 0.6f).coerceAtMost(1f), segmento, startWithMoveTo = true)
                    drawPath(segmento, paleta.aristas, style = trazoArista)

                    if (opacidadCotas <= 0f) return@onDrawBehind

                    // Cota del ancho: bajo la arista frontal.
                    val yAncho = v0.y + separacion
                    lineaDiscontinua(v0, Offset(v0.x, yAncho + extension), colorCota, trazoExtension, opacidadCotas)
                    lineaDiscontinua(v1, Offset(v1.x, yAncho + extension), colorCota, trazoExtension, opacidadCotas)
                    cota(Offset(v0.x, yAncho), Offset(v1.x, yAncho), colorCota, trazoCota, flecha, opacidadCotas)
                    cotaAncho?.let {
                        drawText(it, topLeft = Offset((v0.x + v1.x) / 2f - it.size.width / 2f, yAncho + holguraTexto), alpha = opacidadCotas)
                    }

                    // Cota del espesor: a la izquierda del canto.
                    val xEspesor = v0.x - separacion
                    lineaDiscontinua(v0, Offset(xEspesor - extension, v0.y), colorCota, trazoExtension, opacidadCotas)
                    lineaDiscontinua(v3, Offset(xEspesor - extension, v3.y), colorCota, trazoExtension, opacidadCotas)
                    cota(Offset(xEspesor, v0.y), Offset(xEspesor, v3.y), colorCota, trazoCota, flecha, opacidadCotas)
                    cotaEspesor?.let {
                        drawText(it, topLeft = Offset(xEspesor - holguraTexto - it.size.width, (v0.y + v3.y) / 2f - it.size.height / 2f), alpha = opacidadCotas)
                    }

                    // Cota del largo: paralela a la arista en fuga, a la derecha.
                    val desde = v1 + desplazamientoLargo
                    val hasta = v6 + desplazamientoLargo
                    val unitario = desplazamientoLargo / desplazamientoLargo.getDistance()
                    lineaDiscontinua(v1, desde + unitario * extension, colorCota, trazoExtension, opacidadCotas)
                    lineaDiscontinua(v6, hasta + unitario * extension, colorCota, trazoExtension, opacidadCotas)
                    cota(desde, hasta, colorCota, trazoCota, flecha, opacidadCotas)
                    cotaLargo?.let {
                        val medio = (desde + hasta) / 2f
                        drawText(it, topLeft = Offset(medio.x + holguraTexto, medio.y - it.size.height / 2f), alpha = opacidadCotas)
                    }
                }
            },
    )
}

private const val PROFUNDIDAD_X = 0.433f // cos 30° × ½
private const val PROFUNDIDAD_Y = 0.25f // sen 30° × ½
private const val OPACIDAD_INCOMPLETA = 0.45f
private const val DURACION_CONSTRUCCION_MS = 900

private class Flecha(val largo: Float, val medioAncho: Float)

/** Colores de las tres caras y de las aristas según el metal. */
private class PaletaChapa(
    val superior: List<Color>,
    val frontal: Color,
    val derecha: Color,
    val aristas: Color,
) {
    companion object {
        fun de(familia: FamiliaChapa): PaletaChapa = when (familia) {
            FamiliaChapa.ORO -> PaletaChapa(
                superior = listOf(JewelryColors.GoldSoft, JewelryColors.GoldPrimary, JewelryColors.GoldSecondary),
                frontal = JewelryColors.GoldSecondary,
                derecha = lerp(JewelryColors.GoldSecondary, Color.Black, 0.35f),
                aristas = JewelryColors.GoldSoft,
            )
            FamiliaChapa.PLATA -> PaletaChapa(
                superior = listOf(JewelryColors.TextPrimary, JewelryColors.SilverPrimary, lerp(JewelryColors.SilverPrimary, JewelryColors.TealPrimary, 0.35f)),
                frontal = JewelryColors.SilverDark,
                derecha = lerp(JewelryColors.SilverDark, Color.Black, 0.35f),
                aristas = JewelryColors.SilverPrimary,
            )
        }
    }
}

// Sin vararg: Kotlin no admite vararg de una value class como Offset.
private fun Path.poligono(a: Offset, b: Offset, c: Offset, d: Offset) {
    moveTo(a.x, a.y)
    lineTo(b.x, b.y)
    lineTo(c.x, c.y)
    lineTo(d.x, d.y)
    close()
}

private fun Path.poligono(a: Offset, b: Offset, c: Offset, d: Offset, e: Offset, f: Offset) {
    moveTo(a.x, a.y)
    lineTo(b.x, b.y)
    lineTo(c.x, c.y)
    lineTo(d.x, d.y)
    lineTo(e.x, e.y)
    lineTo(f.x, f.y)
    close()
}

/** Transición suave 0 → 1 entre [a] y [b] (smoothstep). */
private fun suavizar(a: Float, b: Float, x: Float): Float {
    val t = ((x - a) / (b - a)).coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}

private fun DrawScope.lineaDiscontinua(desde: Offset, hasta: Offset, color: Color, trazo: Stroke, alpha: Float) {
    drawLine(color, desde, hasta, strokeWidth = trazo.width, pathEffect = trazo.pathEffect, alpha = alpha)
}

/**
 * Línea de cota con flecha en cada extremo. Si el tramo es corto (un espesor muy fino), las
 * flechas se dibujan por fuera apuntando hacia dentro, como en delineación.
 */
private fun DrawScope.cota(desde: Offset, hasta: Offset, color: Color, trazo: Stroke, flecha: Flecha, alpha: Float) {
    lineaDiscontinua(desde, hasta, color, trazo, alpha)
    val longitud = (hasta - desde).getDistance()
    if (longitud <= 0f) return
    val direccion = (hasta - desde) / longitud
    val porFuera = longitud < 3f * flecha.largo
    // Punta en cada extremo; el cuerpo hacia dentro (normal) o hacia fuera (tramo corto).
    triangulo(desde, if (porFuera) direccion else -direccion, color, flecha, alpha)
    triangulo(hasta, if (porFuera) -direccion else direccion, color, flecha, alpha)
}

/** Triángulo relleno con la punta en [punta], apuntando en [hacia] (vector unitario). */
private fun DrawScope.triangulo(punta: Offset, hacia: Offset, color: Color, flecha: Flecha, alpha: Float) {
    val angulo = atan2(hacia.y, hacia.x)
    val base = punta - hacia * flecha.largo
    val perpendicular = Offset(-sin(angulo), cos(angulo)) * flecha.medioAncho
    val camino = Path().apply {
        moveTo(punta.x, punta.y)
        lineTo(base.x + perpendicular.x, base.y + perpendicular.y)
        lineTo(base.x - perpendicular.x, base.y - perpendicular.y)
        close()
    }
    drawPath(camino, color, alpha = alpha)
}

@Preview(showBackground = true, widthDp = 380, heightDp = 360)
@Composable
private fun DibujoChapaPreview() {
    Calculadoradejoyeros2021Theme {
        Column(
            modifier = Modifier.background(JewelryColors.Background).padding(JewelrySpacing.Md),
            verticalArrangement = Arrangement.spacedBy(JewelrySpacing.Md),
        ) {
            DibujoChapa(
                estado = DibujoChapaUiState(etiquetaAncho = "10,00", etiquetaEspesor = "0,50", etiquetaLargo = "20,00", completa = true),
                familia = FamiliaChapa.ORO,
                descripcion = "",
                modifier = Modifier.fillMaxWidth().aspectRatio(2.4f),
            )
            DibujoChapa(
                estado = DibujoChapaUiState(),
                familia = FamiliaChapa.PLATA,
                descripcion = "",
                modifier = Modifier.fillMaxWidth().aspectRatio(2.4f),
            )
        }
    }
}
