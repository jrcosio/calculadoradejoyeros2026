package com.jrblanco.calculadoradejoyeros2021.ui.soldaduras

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOroSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.DurezaSoldaduraLey
import com.jrblanco.calculadoradejoyeros2021.domain.model.TipoSoldaduraClasica
import com.jrblanco.calculadoradejoyeros2021.domain.model.TipoSoldaduraPlata
import com.jrblanco.calculadoradejoyeros2021.ui.components.AvisoTecnico
import com.jrblanco.calculadoradejoyeros2021.ui.components.BotonDorado
import com.jrblanco.calculadoradejoyeros2021.ui.components.CabeceraSeccion
import com.jrblanco.calculadoradejoyeros2021.ui.components.CampoCantidad
import com.jrblanco.calculadoradejoyeros2021.ui.components.FilaMetal
import com.jrblanco.calculadoradejoyeros2021.ui.components.JewelryScaffold
import com.jrblanco.calculadoradejoyeros2021.ui.components.OpcionSegmento
import com.jrblanco.calculadoradejoyeros2021.ui.components.SelectorSegmentado
import com.jrblanco.calculadoradejoyeros2021.ui.components.TarjetaAcento
import com.jrblanco.calculadoradejoyeros2021.ui.components.TarjetaTotal
import com.jrblanco.calculadoradejoyeros2021.ui.theme.Calculadoradejoyeros2021Theme
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryColors
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelryRadius
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySize
import com.jrblanco.calculadoradejoyeros2021.ui.theme.JewelrySpacing
import org.koin.androidx.compose.koinViewModel

/**
 * Calculadora de soldaduras. Resuelve el ViewModel y delega el pintado en
 * [SoldadurasContent].
 */
@Composable
fun SoldadurasScreen(
    onInfo: () -> Unit,
    onBack: () -> Unit,
    onSoldaduraBase: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SoldadurasViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    SoldadurasContent(
        uiState = uiState,
        onFamiliaSeleccionada = viewModel::onFamiliaSeleccionada,
        onModoCambiado = viewModel::onModoCambiado,
        onCantidadCambiada = viewModel::onCantidadCambiada,
        onColorSeleccionado = viewModel::onColorSeleccionado,
        onDurezaSeleccionada = viewModel::onDurezaSeleccionada,
        onTipoClasicaSeleccionado = viewModel::onTipoClasicaSeleccionado,
        onTipoPlataSeleccionado = viewModel::onTipoPlataSeleccionado,
        onSoldaduraBase = onSoldaduraBase,
        onLimpiar = viewModel::onLimpiar,
        onGuardarFavoritos = {
            viewModel.onGuardarFavoritos()
            // Aviso efímero del sistema: los Toast se reemplazan solos y no se
            // acumulan por muchas pulsaciones que haya. El ViewModel no lo conoce.
            Toast.makeText(context, R.string.aviso_proximamente, Toast.LENGTH_SHORT).show()
        },
        onInfo = onInfo,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun SoldadurasContent(
    uiState: SoldadurasUiState,
    onFamiliaSeleccionada: (FamiliaSoldadura) -> Unit,
    onModoCambiado: (ModoEntradaSoldadura) -> Unit,
    onCantidadCambiada: (String) -> Unit,
    onColorSeleccionado: (ColorOroSoldadura) -> Unit,
    onDurezaSeleccionada: (DurezaSoldaduraLey) -> Unit,
    onTipoClasicaSeleccionado: (TipoSoldaduraClasica) -> Unit,
    onTipoPlataSeleccionado: (TipoSoldaduraPlata) -> Unit,
    onSoldaduraBase: () -> Unit,
    onLimpiar: () -> Unit,
    onGuardarFavoritos: () -> Unit,
    onInfo: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    JewelryScaffold(
        onInfo = onInfo,
        modifier = modifier,
        // Sección de módulo: título y flecha atrás, sin barra inferior.
        title = stringResource(R.string.modulo_soldaduras_titulo),
        onBack = onBack,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                // Con el teclado desplegado los resultados siguen alcanzables.
                .imePadding()
                .padding(JewelrySpacing.Md),
            verticalArrangement = Arrangement.spacedBy(JewelrySpacing.Md),
        ) {
            CabeceraSeccion(
                iconRes = R.drawable.ic_lingotes,
                titulo = stringResource(R.string.soldadura_seccion_familia),
            )
            SelectorSegmentado(
                opciones = FamiliaSoldadura.entries.map {
                    OpcionSegmento(stringResource(it.etiquetaRes), it.acento)
                },
                // En la primera visita no hay familia: -1 no marca ningún segmento.
                seleccionada = uiState.familia?.ordinal ?: -1,
                onSeleccion = { onFamiliaSeleccionada(FamiliaSoldadura.entries[it]) },
            )

            // Con familia elegida, el conmutador de modo (§2.3, FR-003): «tengo el
            // metal» o «peso final deseado». Cambiarlo vacía la cantidad (FR-023).
            uiState.familia?.let { familia ->
                CabeceraSeccion(
                    iconRes = R.drawable.ic_balanza,
                    titulo = stringResource(R.string.soldadura_seccion_modo),
                )
                SelectorSegmentado(
                    opciones = listOf(
                        OpcionSegmento(
                            stringResource(familia.etiquetaModoDirectoRes),
                            JewelryColors.TealPrimary,
                        ),
                        OpcionSegmento(
                            stringResource(R.string.soldadura_modo_peso_final),
                            JewelryColors.TealPrimary,
                        ),
                    ),
                    seleccionada = uiState.modo.ordinal,
                    onSeleccion = { onModoCambiado(ModoEntradaSoldadura.entries[it]) },
                )
            }

            // Primera visita (FR-002): solo el selector. El formulario llega al elegir.
            when (uiState.familia) {
                null -> Unit
                FamiliaSoldadura.ORO_LEY -> FormularioOroLey(
                    uiState = uiState,
                    onCantidadCambiada = onCantidadCambiada,
                    onColorSeleccionado = onColorSeleccionado,
                    onDurezaSeleccionada = onDurezaSeleccionada,
                    onSoldaduraBase = onSoldaduraBase,
                )
                FamiliaSoldadura.CLASICA -> FormularioClasica(
                    uiState = uiState,
                    onCantidadCambiada = onCantidadCambiada,
                    onTipoClasicaSeleccionado = onTipoClasicaSeleccionado,
                )
                FamiliaSoldadura.PLATA -> FormularioPlata(
                    uiState = uiState,
                    onCantidadCambiada = onCantidadCambiada,
                    onTipoPlataSeleccionado = onTipoPlataSeleccionado,
                )
            }

            // Los botones solo existen con familia elegida (FR-024): en la primera
            // visita no hay nada que limpiar ni que guardar.
            if (uiState.familia != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(JewelrySpacing.Md)) {
                    BotonDorado(
                        iconRes = R.drawable.ic_refrescar,
                        texto = stringResource(R.string.accion_limpiar),
                        onClick = onLimpiar,
                        modifier = Modifier.weight(1f),
                    )
                    BotonDorado(
                        iconRes = R.drawable.ic_estrella,
                        texto = stringResource(R.string.accion_guardar_favoritos),
                        onClick = onGuardarFavoritos,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/** El formulario de ORO LEY: base + oro de 18 K del color elegido (§5 del documento). */
@Composable
private fun FormularioOroLey(
    uiState: SoldadurasUiState,
    onCantidadCambiada: (String) -> Unit,
    onColorSeleccionado: (ColorOroSoldadura) -> Unit,
    onDurezaSeleccionada: (DurezaSoldaduraLey) -> Unit,
    onSoldaduraBase: () -> Unit,
) {
    val acentoColor = uiState.colorOro.acento

    TarjetaSoldaduraBase(onSoldaduraBase = onSoldaduraBase)

    CabeceraSeccion(
        iconRes = R.drawable.ic_paleta,
        titulo = stringResource(R.string.oro_seccion_color),
    )
    SelectorSegmentado(
        // Cada color de oro se elige en su propio tono; sin rojo (§5.1).
        opciones = ColorOroSoldadura.entries.map {
            OpcionSegmento(stringResource(it.etiquetaRes), it.acento)
        },
        seleccionada = uiState.colorOro.ordinal,
        onSeleccion = { onColorSeleccionado(ColorOroSoldadura.entries[it]) },
    )

    TarjetaEntradaSoldadura(
        cantidad = uiState.cantidadTexto,
        onCantidadCambiada = onCantidadCambiada,
        titulo = stringResource(
            when (uiState.modo) {
                ModoEntradaSoldadura.DESDE_METAL -> R.string.soldadura_entrada_oro_18k
                ModoEntradaSoldadura.PESO_FINAL -> R.string.soldadura_entrada_peso_final
            },
        ),
        imagenRes = R.drawable.modulo_oro,
        imagenDescripcion = stringResource(R.string.oro_entrada_imagen),
        acento = acentoColor,
    )

    CabeceraSeccion(
        iconRes = R.drawable.ic_lingotes,
        titulo = stringResource(R.string.soldadura_seccion_tipo),
        tinte = JewelryColors.TealPrimary,
    )
    SelectorSegmentado(
        opciones = DurezaSoldaduraLey.entries.map {
            OpcionSegmento(stringResource(it.etiquetaRes), JewelryColors.TealPrimary)
        },
        seleccionada = uiState.dureza.ordinal,
        onSeleccion = { onDurezaSeleccionada(DurezaSoldaduraLey.entries[it]) },
        // Cinco durezas no caben legibles en una fila: tres arriba y dos abajo.
        maxPorFila = 3,
    )

    uiState.resultado?.let { resultado ->
        TarjetaResultado(
            resultado = resultado,
            colorOro = uiState.colorOro,
            baseNecesaria = uiState.modo == ModoEntradaSoldadura.DESDE_METAL,
            acentoFilas = JewelryColors.TealPrimary,
            etiquetaTotal = stringResource(R.string.soldadura_total),
            acentoTotal = acentoColor,
        )

        // Recomendación de la receta (§5.6), no una advertencia: doble fundido y laminado.
        Text(
            text = stringResource(R.string.soldadura_ley_consejo_mezcla),
            style = MaterialTheme.typography.bodySmall,
            color = JewelryColors.TextMuted,
        )
    }
}

/**
 * El formulario de CLÁSICA: tres recetas de oro amarillo escaladas (§3), sin elección de
 * color (§8.1, FR-015) y con la advertencia de seguridad cuando la receta lleva cadmio.
 */
@Composable
private fun FormularioClasica(
    uiState: SoldadurasUiState,
    onCantidadCambiada: (String) -> Unit,
    onTipoClasicaSeleccionado: (TipoSoldaduraClasica) -> Unit,
) {
    TarjetaEntradaSoldadura(
        cantidad = uiState.cantidadTexto,
        onCantidadCambiada = onCantidadCambiada,
        titulo = stringResource(
            when {
                uiState.modo == ModoEntradaSoldadura.PESO_FINAL ->
                    R.string.soldadura_entrada_peso_final
                // La entrada del modo directo es el oro de la receta (FR-015).
                uiState.tipoClasica == TipoSoldaduraClasica.MUY_FLOJA_LEY ->
                    R.string.soldadura_entrada_oro_24k
                else -> R.string.soldadura_entrada_oro_18k
            },
        ),
        imagenRes = R.drawable.modulo_oro,
        imagenDescripcion = stringResource(R.string.oro_entrada_imagen),
        acento = JewelryColors.GoldPrimary,
    )

    CabeceraSeccion(
        iconRes = R.drawable.ic_lingotes,
        titulo = stringResource(R.string.soldadura_seccion_tipo),
        tinte = JewelryColors.TealPrimary,
    )
    SelectorSegmentado(
        opciones = TipoSoldaduraClasica.entries.map {
            OpcionSegmento(stringResource(it.etiquetaRes), JewelryColors.TealPrimary)
        },
        seleccionada = uiState.tipoClasica.ordinal,
        onSeleccion = { onTipoClasicaSeleccionado(TipoSoldaduraClasica.entries[it]) },
    )

    if (uiState.tipoClasica.llevaCadmio) {
        // Advertencia obligatoria de §9: la receta muy floja de ley lleva cadmio.
        AvisoTecnico(stringResource(R.string.soldadura_aviso_seguridad))
    }

    uiState.resultado?.let { resultado ->
        TarjetaResultado(
            resultado = resultado,
            colorOro = uiState.colorOro,
            baseNecesaria = false,
            acentoFilas = JewelryColors.TealPrimary,
            etiquetaTotal = stringResource(R.string.soldadura_total),
            acentoTotal = JewelryColors.GoldPrimary,
        )
    }
}

/**
 * El formulario de PLATA: latón respecto a la plata fina (§4), cuatro tipos y la nota de
 * que la muy floja se recomienda para composturas (FR-016).
 */
@Composable
private fun FormularioPlata(
    uiState: SoldadurasUiState,
    onCantidadCambiada: (String) -> Unit,
    onTipoPlataSeleccionado: (TipoSoldaduraPlata) -> Unit,
) {
    TarjetaEntradaSoldadura(
        cantidad = uiState.cantidadTexto,
        onCantidadCambiada = onCantidadCambiada,
        titulo = stringResource(
            when (uiState.modo) {
                ModoEntradaSoldadura.DESDE_METAL -> R.string.plata_entrada_titulo
                ModoEntradaSoldadura.PESO_FINAL -> R.string.soldadura_entrada_peso_final
            },
        ),
        imagenRes = R.drawable.modulo_plata,
        imagenDescripcion = stringResource(R.string.plata_entrada_imagen),
        acento = JewelryColors.SilverPrimary,
        borde = JewelryColors.SilverDark,
    )

    CabeceraSeccion(
        iconRes = R.drawable.ic_lingotes,
        titulo = stringResource(R.string.soldadura_seccion_tipo),
        tinte = JewelryColors.TealPrimary,
    )
    SelectorSegmentado(
        opciones = TipoSoldaduraPlata.entries.map {
            OpcionSegmento(stringResource(it.etiquetaRes), JewelryColors.TealPrimary)
        },
        seleccionada = uiState.tipoPlata.ordinal,
        onSeleccion = { onTipoPlataSeleccionado(TipoSoldaduraPlata.entries[it]) },
    )
    if (uiState.tipoPlata == TipoSoldaduraPlata.MUY_FLOJA) {
        // La recomendación acompaña solo a la muy floja, que es de quien habla (FR-016).
        Text(
            text = stringResource(R.string.soldadura_plata_nota_muy_floja),
            style = MaterialTheme.typography.bodySmall,
            color = JewelryColors.TextMuted,
        )
    }

    uiState.resultado?.let { resultado ->
        TarjetaResultado(
            resultado = resultado,
            colorOro = uiState.colorOro,
            baseNecesaria = false,
            acentoFilas = JewelryColors.TealPrimary,
            etiquetaTotal = stringResource(R.string.soldadura_total),
            // Lo que pesa al final es plata: el total en plateado, como su calculadora.
            acentoTotal = JewelryColors.SilverPrimary,
        )
    }
}

/** Acceso a la pantalla de la base, con la aclaración del punto de fusión (FR-011). */
@Composable
private fun TarjetaSoldaduraBase(
    onSoldaduraBase: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TarjetaAcento(modifier, acento = JewelryColors.TealPrimary) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.soldadura_ley_base_titulo),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                color = JewelryColors.TealPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )

            Spacer(Modifier.height(JewelrySpacing.Md))

            // Botón a mano, como los demás botones no-Material de la app: píldora
            // translúcida con filete teal, el estilo del mockup.
            val forma = RoundedCornerShape(JewelryRadius.Small)
            Box(
                modifier = Modifier
                    .heightIn(min = JewelrySize.MinTouchTarget)
                    .background(JewelryColors.TealPrimary.copy(alpha = 0.12f), forma)
                    .border(1.dp, JewelryColors.BorderTeal, forma)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClick = onSoldaduraBase,
                    )
                    .padding(horizontal = JewelrySpacing.Xl),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.soldadura_ley_base_boton),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                    ),
                    color = JewelryColors.TealPrimary,
                )
            }

            Spacer(Modifier.height(JewelrySpacing.Md))

            Text(
                text = stringResource(R.string.soldadura_ley_base_nota),
                style = MaterialTheme.typography.bodySmall,
                color = JewelryColors.TextMuted,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Tarjeta de entrada: la imagen del metal y el campo de gramos, con el acento que toque.
 * `internal` porque la comparten las dos pantallas del paquete.
 */
@Composable
internal fun TarjetaEntradaSoldadura(
    cantidad: String,
    onCantidadCambiada: (String) -> Unit,
    titulo: String,
    imagenRes: Int,
    imagenDescripcion: String,
    acento: Color,
    modifier: Modifier = Modifier,
    borde: Color = JewelryColors.BorderGold,
) {
    TarjetaAcento(modifier, acento = acento) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(imagenRes),
                contentDescription = imagenDescripcion,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(96.dp),
            )

            Spacer(Modifier.width(JewelrySpacing.Md))

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                    color = acento,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(JewelrySpacing.Sm))

                CampoCantidad(
                    valor = cantidad,
                    onCambio = onCantidadCambiada,
                    acento = acento,
                    borde = borde,
                )
            }
        }
    }
}

/** Las filas de resultado, el total y la nota de redondeo de §8.3 (FR-021, FR-022). */
@Composable
private fun TarjetaResultado(
    resultado: ResultadoSoldaduras,
    colorOro: ColorOroSoldadura,
    baseNecesaria: Boolean,
    acentoFilas: Color,
    etiquetaTotal: String,
    acentoTotal: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(JewelrySpacing.Md)) {
        TarjetaAcento(acento = acentoFilas) {
            resultado.filas.forEachIndexed { indice, fila ->
                if (indice > 0) Spacer(Modifier.height(JewelrySpacing.Md))
                FilaMetal(
                    imagenRes = fila.ingrediente.imagenRes,
                    imagenDescripcion = stringResource(fila.ingrediente.imagenDescripcionRes),
                    nombre = nombreDeIngrediente(fila.ingrediente, colorOro, baseNecesaria),
                    valorFormateado = fila.gramosFormateados,
                    acento = acentoFilas,
                )
            }
        }

        TarjetaTotal(
            etiqueta = etiquetaTotal,
            totalFormateado = resultado.totalFormateado,
            acento = acentoTotal,
        )

        // §8.3, opción 1: se advierte del redondeo de vista y jamás se ajusta un
        // ingrediente para cuadrar la suma mostrada.
        Text(
            text = stringResource(R.string.soldadura_nota_redondeo),
            style = MaterialTheme.typography.bodySmall,
            color = JewelryColors.TextMuted,
        )
    }
}

// --- Cómo se pinta cada familia. El resto de mapeos compartidos con la pantalla de ---
// --- la base viven en PresentacionSoldadura.kt.                                    ---

private val FamiliaSoldadura.etiquetaRes: Int
    get() = when (this) {
        FamiliaSoldadura.ORO_LEY -> R.string.soldadura_familia_oro_ley
        FamiliaSoldadura.CLASICA -> R.string.soldadura_familia_clasica
        FamiliaSoldadura.PLATA -> R.string.soldadura_familia_plata
    }

/** La etiqueta del modo directo, que nombra el metal de entrada de cada familia. */
private val FamiliaSoldadura.etiquetaModoDirectoRes: Int
    get() = when (this) {
        FamiliaSoldadura.ORO_LEY -> R.string.soldadura_modo_tengo_oro18k
        FamiliaSoldadura.CLASICA -> R.string.soldadura_modo_tengo_oro
        FamiliaSoldadura.PLATA -> R.string.soldadura_modo_tengo_plata
    }

/** Las dos familias de oro en dorado y la de plata en plateado, como el mockup. */
private val FamiliaSoldadura.acento: Color
    get() = when (this) {
        FamiliaSoldadura.ORO_LEY -> JewelryColors.GoldPrimary
        FamiliaSoldadura.CLASICA -> JewelryColors.GoldPrimary
        FamiliaSoldadura.PLATA -> JewelryColors.SilverPrimary
    }

private val TipoSoldaduraPlata.etiquetaRes: Int
    get() = when (this) {
        TipoSoldaduraPlata.MUY_FLOJA -> R.string.soldadura_plata_muy_floja
        TipoSoldaduraPlata.FLOJA -> R.string.soldadura_plata_floja
        TipoSoldaduraPlata.NORMAL -> R.string.soldadura_plata_normal
        TipoSoldaduraPlata.FUERTE -> R.string.soldadura_plata_fuerte
    }

private val TipoSoldaduraClasica.etiquetaRes: Int
    get() = when (this) {
        TipoSoldaduraClasica.FLOJA -> R.string.soldadura_clasica_floja
        TipoSoldaduraClasica.FUERTE -> R.string.soldadura_clasica_fuerte
        TipoSoldaduraClasica.MUY_FLOJA_LEY -> R.string.soldadura_clasica_muy_floja_ley
    }

private val DurezaSoldaduraLey.etiquetaRes: Int
    get() = when (this) {
        DurezaSoldaduraLey.MUY_FLOJA -> R.string.soldadura_dureza_muy_floja
        DurezaSoldaduraLey.FLOJA -> R.string.soldadura_dureza_floja
        DurezaSoldaduraLey.MEDIA -> R.string.soldadura_dureza_media
        DurezaSoldaduraLey.FUERTE -> R.string.soldadura_dureza_fuerte
        DurezaSoldaduraLey.MUY_FUERTE -> R.string.soldadura_dureza_muy_fuerte
    }

@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun SoldadurasContentPrimeraVisitaPreview() {
    Calculadoradejoyeros2021Theme {
        SoldadurasContent(
            uiState = SoldadurasUiState(),
            onFamiliaSeleccionada = {},
            onModoCambiado = {},
            onCantidadCambiada = {},
            onColorSeleccionado = {},
            onDurezaSeleccionada = {},
            onTipoClasicaSeleccionado = {},
            onTipoPlataSeleccionado = {},
            onSoldaduraBase = {},
            onLimpiar = {},
            onGuardarFavoritos = {},
            onInfo = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 891)
@Composable
private fun SoldadurasContentOroLeyPreview() {
    Calculadoradejoyeros2021Theme {
        SoldadurasContent(
            uiState = SoldadurasUiState(
                familia = FamiliaSoldadura.ORO_LEY,
                cantidadTexto = "2",
                resultado = ResultadoSoldaduras(
                    filas = listOf(
                        // El caso del mockup: 2 g de oro 18K con muy floja (r = 0,3).
                        FilaSoldadura(IngredienteSoldadura.BASE, "6,667"),
                    ),
                    totalFormateado = "8,667",
                ),
            ),
            onFamiliaSeleccionada = {},
            onModoCambiado = {},
            onCantidadCambiada = {},
            onColorSeleccionado = {},
            onDurezaSeleccionada = {},
            onTipoClasicaSeleccionado = {},
            onTipoPlataSeleccionado = {},
            onSoldaduraBase = {},
            onLimpiar = {},
            onGuardarFavoritos = {},
            onInfo = {},
            onBack = {},
        )
    }
}
