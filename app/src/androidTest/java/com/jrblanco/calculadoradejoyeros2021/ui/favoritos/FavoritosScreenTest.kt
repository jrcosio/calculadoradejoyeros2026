package com.jrblanco.calculadoradejoyeros2021.ui.favoritos

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.LeyOro
import com.jrblanco.calculadoradejoyeros2021.domain.model.MaterialChapa
import com.jrblanco.calculadoradejoyeros2021.domain.model.ModoEntradaSoldadura
import com.jrblanco.calculadoradejoyeros2021.ui.theme.Calculadoradejoyeros2021Theme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Monta `FavoritosContent` directo, **sin Koin ni NavHost**, con estados precocinados: el mismo
 * patrón que los otros tests instrumentados del proyecto.
 *
 * Lo que más protege este fichero es el diseño de **dos zonas pulsables**: que la estrella propague
 * lo suyo y no abra el favorito, y que siga siendo un nodo con descripción propia dentro de una
 * tarjeta fusionada — lo único que permite a un lector de pantalla quitar un favorito.
 */
class FavoritosScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private fun texto(id: Int) = contexto.getString(id)

    private val oro = FavoritoUiModel(
        id = 1L,
        entradas = EntradasFavoritoUi.Oro(LeyOro.LEY_18K, ColorOro.BLANCO, "30"),
        lineas = listOf(
            LineaFavoritoUi(ConceptoFavorito.PLATA_FINA, "6,564"),
            LineaFavoritoUi(ConceptoFavorito.COBRE, "3,382"),
            LineaFavoritoUi(ConceptoFavorito.PALADIO, "3,382"),
        ),
        totalFormateado = "39,960",
        guardadoEnEpochMillis = 1_787_670_000_000L,
    )

    private val chapa = FavoritoUiModel(
        id = 2L,
        entradas = EntradasFavoritoUi.Chapa(MaterialChapa.ORO_18K, "10", "20", "0,5"),
        lineas = listOf(
            LineaFavoritoUi(ConceptoFavorito.VOLUMEN, "0,100"),
            LineaFavoritoUi(ConceptoFavorito.METAL_FINO, "1,169"),
        ),
        totalFormateado = "1,56",
        guardadoEnEpochMillis = 1_787_400_000_000L,
    )

    private val baseInversa = FavoritoUiModel(
        id = 3L,
        entradas = EntradasFavoritoUi.SoldaduraBase(ModoEntradaSoldadura.PESO_FINAL, "10"),
        lineas = listOf(
            LineaFavoritoUi(ConceptoFavorito.ORO_24K, "7,541"),
            LineaFavoritoUi(ConceptoFavorito.COBRE, "0,407"),
            LineaFavoritoUi(ConceptoFavorito.PLATA_FINA, "0,603"),
            LineaFavoritoUi(ConceptoFavorito.ZINC, "0,694"),
            LineaFavoritoUi(ConceptoFavorito.CADMIO, "0,754"),
        ),
        totalFormateado = "10,000",
        guardadoEnEpochMillis = 1_787_300_000_000L,
    )

    private fun montar(
        uiState: FavoritosUiState,
        onAbrir: (FavoritoUiModel) -> Unit = {},
        onQuitar: (FavoritoUiModel) -> Unit = {},
        onCancelarBorrado: () -> Unit = {},
        onConfirmarBorrado: () -> Unit = {},
    ) {
        composeRule.setContent {
            Calculadoradejoyeros2021Theme {
                FavoritosContent(
                    uiState = uiState,
                    onAbrir = onAbrir,
                    onQuitar = onQuitar,
                    onCancelarBorrado = onCancelarBorrado,
                    onConfirmarBorrado = onConfirmarBorrado,
                    onTabSelect = {},
                    onInfo = {},
                )
            }
        }
    }

    // --- Carga y estado vacío ---

    @Test
    fun mientras_carga_no_se_pinta_ni_la_lista_ni_la_invitacion() {
        montar(FavoritosUiState(cargando = true))

        composeRule.onNodeWithText(texto(R.string.favoritos_vacio_titulo)).assertDoesNotExist()
        composeRule.onAllNodesWithContentDescription(texto(R.string.favoritos_quitar))
            .assertCountEquals(0)
    }

    @Test
    fun sin_favoritos_se_ve_la_invitacion_y_ninguna_estrella() {
        montar(FavoritosUiState(cargando = false))

        composeRule.onNodeWithText(texto(R.string.favoritos_vacio_titulo)).assertIsDisplayed()
        composeRule.onAllNodesWithContentDescription(texto(R.string.favoritos_quitar))
            .assertCountEquals(0)
    }

    @Test
    fun la_invitacion_nombra_el_boton_con_el_que_se_guarda() {
        montar(FavoritosUiState(cargando = false))

        composeRule.onNodeWithText(
            texto(R.string.favoritos_vacio_texto).format(texto(R.string.accion_guardar_favoritos)),
        ).assertIsDisplayed()
    }

    // --- La lista ---

    @Test
    fun cada_tarjeta_muestra_su_seccion_titulo_cifras_y_total() {
        montar(FavoritosUiState(cargando = false, favoritos = listOf(oro, chapa)))

        composeRule.onNodeWithText(texto(R.string.modulo_oro_titulo)).assertIsDisplayed()
        composeRule.onNodeWithText(texto(R.string.chapas_titulo)).assertIsDisplayed()
        composeRule.onNodeWithText("6,564").assertIsDisplayed()
        composeRule.onNodeWithText("39,960").assertIsDisplayed()
        composeRule.onNodeWithText("1,56").assertIsDisplayed()
    }

    @Test
    fun hay_una_estrella_por_tarjeta() {
        montar(FavoritosUiState(cargando = false, favoritos = listOf(oro, chapa)))

        composeRule.onAllNodesWithContentDescription(texto(R.string.favoritos_quitar))
            .assertCountEquals(2)
    }

    @Test
    fun una_tarjeta_con_cinco_cifras_muestra_tres_y_cuenta_las_que_faltan() {
        montar(FavoritosUiState(cargando = false, favoritos = listOf(baseInversa)))

        composeRule.onNodeWithText("7,541").assertIsDisplayed()
        composeRule.onNodeWithText("0,407").assertIsDisplayed()
        composeRule.onNodeWithText("0,603").assertIsDisplayed()
        // La cuarta y la quinta no se pintan; en su lugar, el contador.
        composeRule.onNodeWithText("0,694").assertDoesNotExist()
        composeRule.onNodeWithText(texto(R.string.favoritos_mas_lineas).format(2)).assertIsDisplayed()
    }

    // --- Las dos zonas pulsables ---

    @Test
    fun pulsar_la_tarjeta_propaga_su_favorito() {
        val abiertos = mutableListOf<Long>()
        montar(
            FavoritosUiState(cargando = false, favoritos = listOf(oro)),
            onAbrir = { abiertos += it.id },
        )

        composeRule.onNodeWithText(texto(R.string.modulo_oro_titulo)).performClick()

        assertEquals(listOf(1L), abiertos)
    }

    @Test
    fun pulsar_la_estrella_propaga_quitar_y_no_abre_el_favorito() {
        val abiertos = mutableListOf<Long>()
        val quitados = mutableListOf<Long>()
        montar(
            FavoritosUiState(cargando = false, favoritos = listOf(oro)),
            onAbrir = { abiertos += it.id },
            onQuitar = { quitados += it.id },
        )

        composeRule.onAllNodesWithContentDescription(texto(R.string.favoritos_quitar))
            .onFirst()
            .performClick()

        assertEquals(listOf(1L), quitados)
        assertTrue("La estrella no puede abrir el favorito", abiertos.isEmpty())
    }

    // --- El diálogo ---

    @Test
    fun con_borrado_pendiente_se_ve_la_pregunta_nombrando_el_favorito() {
        montar(
            FavoritosUiState(cargando = false, favoritos = listOf(oro), pendienteDeBorrar = oro),
        )

        composeRule.onNodeWithText(texto(R.string.favoritos_borrar_titulo)).assertIsDisplayed()
        composeRule.onNodeWithText(texto(R.string.favoritos_borrar_confirmar)).assertIsDisplayed()
        composeRule.onNodeWithText(texto(R.string.accion_cancelar)).assertIsDisplayed()
    }

    @Test
    fun confirmar_y_cancelar_propagan_lo_suyo() {
        var confirmados = 0
        var cancelados = 0
        montar(
            FavoritosUiState(cargando = false, favoritos = listOf(oro), pendienteDeBorrar = oro),
            onCancelarBorrado = { cancelados++ },
            onConfirmarBorrado = { confirmados++ },
        )

        composeRule.onNodeWithText(texto(R.string.favoritos_borrar_confirmar)).performClick()
        assertEquals(1, confirmados)

        composeRule.onNodeWithText(texto(R.string.accion_cancelar)).performClick()
        assertEquals(1, cancelados)
    }

    @Test
    fun sin_borrado_pendiente_no_hay_pregunta() {
        montar(FavoritosUiState(cargando = false, favoritos = listOf(oro)))

        composeRule.onNodeWithText(texto(R.string.favoritos_borrar_titulo)).assertDoesNotExist()
    }
}
