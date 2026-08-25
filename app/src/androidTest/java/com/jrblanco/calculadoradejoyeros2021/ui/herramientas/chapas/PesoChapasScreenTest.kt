package com.jrblanco.calculadoradejoyeros2021.ui.herramientas.chapas

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.domain.model.FamiliaChapa
import com.jrblanco.calculadoradejoyeros2021.domain.model.MaterialChapa
import com.jrblanco.calculadoradejoyeros2021.ui.theme.Calculadoradejoyeros2021Theme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Monta [PesoChapasContent] directo, sin Koin ni NavHost, patrón de las otras calculadoras. */
class PesoChapasScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext
    private fun texto(id: Int, vararg args: Any) = contexto.getString(id, *args)

    private val estadoConResultado = PesoChapasUiState(
        medidas = mapOf(MedidaChapa.ANCHO to "10", MedidaChapa.ESPESOR to "0,5", MedidaChapa.LARGO to "20"),
        dibujo = DibujoChapaUiState(etiquetaAncho = "10,00", etiquetaEspesor = "0,50", etiquetaLargo = "20,00", completa = true),
        resultado = ResultadoChapa("1,56", "0,100", "15,58", "75,0", "1,169"),
    )

    private fun montar(
        uiState: PesoChapasUiState,
        onFamiliaSeleccionada: (FamiliaChapa) -> Unit = {},
        onMaterialSeleccionado: (MaterialChapa) -> Unit = {},
        onMedidaCambiada: (MedidaChapa, String) -> Unit = { _, _ -> },
        onLimpiar: () -> Unit = {},
        onGuardarFavoritos: () -> Unit = {},
    ) {
        composeRule.setContent {
            Calculadoradejoyeros2021Theme {
                PesoChapasContent(
                    uiState = uiState,
                    onFamiliaSeleccionada = onFamiliaSeleccionada,
                    onMaterialSeleccionado = onMaterialSeleccionado,
                    onMedidaCambiada = onMedidaCambiada,
                    onLimpiar = onLimpiar,
                    onGuardarFavoritos = onGuardarFavoritos,
                )
            }
        }
    }

    @Test
    fun conOro_seVenLasCuatroLeyesDeOroYNoLasDePlata() {
        montar(PesoChapasUiState())

        composeRule.onNodeWithText(texto(R.string.chapas_familia_oro)).assertExists()
        composeRule.onNodeWithText(texto(R.string.chapas_familia_plata)).assertExists()
        listOf(R.string.oro_ley_18k, R.string.oro_ley_14k, R.string.oro_ley_12k, R.string.oro_ley_9k)
            .forEach { composeRule.onNodeWithText(texto(it)).assertExists() }
        composeRule.onAllNodesWithText(texto(R.string.plata_ley_925)).assertCountEquals(0)
    }

    @Test
    fun conPlata_seVenLasCuatroLeyesDePlata() {
        montar(PesoChapasUiState(material = MaterialChapa.PLATA_925))

        listOf(R.string.plata_ley_950, R.string.plata_ley_925, R.string.plata_ley_900, R.string.plata_ley_800)
            .forEach { composeRule.onNodeWithText(texto(it)).assertExists() }
        composeRule.onAllNodesWithText(texto(R.string.oro_ley_18k)).assertCountEquals(0)
    }

    @Test
    fun hayExactamenteTresCamposDeMedida() {
        montar(PesoChapasUiState())

        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(3)
    }

    @Test
    fun escribirEnElAncho_propagaLaMedida() {
        var recibido: Pair<MedidaChapa, String>? = null
        montar(PesoChapasUiState(), onMedidaCambiada = { medida, texto -> recibido = medida to texto })

        composeRule.onAllNodes(hasSetTextAction())[0].performTextInput("10")

        assertEquals(MedidaChapa.ANCHO to "10", recibido)
    }

    @Test
    fun pulsarPlata_propagaLaFamilia() {
        var elegida: FamiliaChapa? = null
        montar(PesoChapasUiState(), onFamiliaSeleccionada = { elegida = it })

        composeRule.onNodeWithText(texto(R.string.chapas_familia_plata)).performClick()

        assertEquals(FamiliaChapa.PLATA, elegida)
    }

    @Test
    fun pulsarUnaLey_propagaElMaterial() {
        var elegido: MaterialChapa? = null
        montar(PesoChapasUiState(), onMaterialSeleccionado = { elegido = it })

        composeRule.onNodeWithText(texto(R.string.oro_ley_9k)).performClick()

        assertEquals(MaterialChapa.ORO_9K, elegido)
    }

    @Test
    fun conResultado_seVenElPesoElMaterialYLaNota() {
        montar(estadoConResultado)

        composeRule.onNodeWithText("1,56").assertExists()
        composeRule.onNodeWithText(texto(R.string.chapas_resultado_para, texto(R.string.chapas_material_oro, texto(R.string.oro_ley_18k)))).assertExists()
        composeRule.onNodeWithText(texto(R.string.chapas_nota_aproximado)).assertExists()
        composeRule.onNodeWithText("1,169").assertExists()
    }

    @Test
    fun sinResultado_noHayTarjetaDeResultado() {
        montar(PesoChapasUiState())

        composeRule.onAllNodesWithText(texto(R.string.chapas_resultado_titulo)).assertCountEquals(0)
        composeRule.onAllNodesWithText(texto(R.string.chapas_nota_aproximado)).assertCountEquals(0)
    }

    @Test
    fun con12K_apareceElAvisoTecnicoYCon18KNo() {
        montar(PesoChapasUiState(material = MaterialChapa.ORO_12K))
        composeRule.onNodeWithText(texto(R.string.oro_aviso_12k)).assertExists()
    }

    @Test
    fun con18K_noHayAvisoTecnico() {
        montar(PesoChapasUiState())
        composeRule.onAllNodesWithText(texto(R.string.oro_aviso_12k)).assertCountEquals(0)
    }

    @Test
    fun fueraDeRango_seVeElAvisoDeRango() {
        montar(PesoChapasUiState(fueraDeRango = setOf(MedidaChapa.ANCHO)))
        composeRule.onNodeWithText(texto(R.string.chapas_aviso_rango)).assertExists()
    }

    @Test
    fun losBotonesPropaganSusCallbacks() {
        var limpiado = false
        var guardado = false
        montar(estadoConResultado, onLimpiar = { limpiado = true }, onGuardarFavoritos = { guardado = true })

        composeRule.onNodeWithText(texto(R.string.accion_limpiar)).performClick()
        composeRule.onNodeWithText(texto(R.string.accion_guardar_favoritos)).performClick()

        assertEquals(true, limpiado)
        assertEquals(true, guardado)
    }

    // --- US4: la ilustración ---

    @Test
    fun laIlustracionAnunciaElMaterialYLasTresMedidas() {
        montar(estadoConResultado)

        val descripcion = texto(
            R.string.chapas_dibujo_descripcion,
            texto(R.string.chapas_material_oro, texto(R.string.oro_ley_18k)),
            texto(R.string.chapas_dibujo_medida, "10,00"),
            texto(R.string.chapas_dibujo_medida, "20,00"),
            texto(R.string.chapas_dibujo_medida, "0,50"),
        )
        composeRule.onNodeWithContentDescription(descripcion).assertExists()
    }

    @Test
    fun sinMedidas_laIlustracionDiceSinMedida() {
        montar(PesoChapasUiState(material = MaterialChapa.PLATA_925))

        val sinMedida = texto(R.string.chapas_dibujo_sin_medida)
        val descripcion = texto(
            R.string.chapas_dibujo_descripcion,
            texto(R.string.chapas_material_plata, texto(R.string.plata_ley_925)),
            sinMedida, sinMedida, sinMedida,
        )
        composeRule.onNodeWithContentDescription(descripcion).assertExists()
    }
}
