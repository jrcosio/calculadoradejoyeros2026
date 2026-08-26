package com.jrblanco.calculadoradejoyeros2021.ui.soldaduras

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jrblanco.calculadoradejoyeros2021.core.util.DispatcherProvider
import com.jrblanco.calculadoradejoyeros2021.core.util.parsearDecimalPositivo
import com.jrblanco.calculadoradejoyeros2021.domain.model.CalculoSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.CalculoSoldaduraLey
import com.jrblanco.calculadoradejoyeros2021.domain.model.ColorOroSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.DurezaSoldaduraLey
import com.jrblanco.calculadoradejoyeros2021.domain.model.EntradasFavorito
import com.jrblanco.calculadoradejoyeros2021.domain.model.ModoEntradaSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.ResultadoGuardado
import com.jrblanco.calculadoradejoyeros2021.domain.model.TipoSoldaduraClasica
import com.jrblanco.calculadoradejoyeros2021.domain.model.TipoSoldaduraPlata
import com.jrblanco.calculadoradejoyeros2021.domain.repository.AnalyticsRepository
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraClasicaInversaUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraClasicaUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraLeyDesdeOroUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraLeyInversaUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraPlataInversaUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.CalcularSoldaduraPlataUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.GuardarFavoritoUseCase
import com.jrblanco.calculadoradejoyeros2021.domain.usecase.ObtenerFavoritoUseCase
import com.jrblanco.calculadoradejoyeros2021.ui.favoritos.AvisoFavorito
import com.jrblanco.calculadoradejoyeros2021.ui.favoritos.FormatoFavoritos
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SoldadurasViewModel(
    private val calcularLeyDesdeOro: CalcularSoldaduraLeyDesdeOroUseCase,
    private val calcularLeyInversa: CalcularSoldaduraLeyInversaUseCase,
    private val calcularClasica: CalcularSoldaduraClasicaUseCase,
    private val calcularClasicaInversa: CalcularSoldaduraClasicaInversaUseCase,
    private val calcularPlata: CalcularSoldaduraPlataUseCase,
    private val calcularPlataInversa: CalcularSoldaduraPlataInversaUseCase,
    private val guardarFavorito: GuardarFavoritoUseCase,
    private val obtenerFavorito: ObtenerFavoritoUseCase,
    private val analytics: AnalyticsRepository,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SoldadurasUiState())
    val uiState: StateFlow<SoldadurasUiState> = _uiState.asStateFlow()

    /**
     * Deduplicación de telemetría: el recálculo es por pulsación de tecla y registrar
     * cada una sería ruido. Se emite `soldaduras_calculado` cuando un cálculo válido
     * estrena combinación familia×modo×tipo×color o cuando la entrada vuelve a ser
     * válida, y nunca con la cantidad (FR-027).
     */
    private var ultimaCombinacionRegistrada: Combinacion? = null

    /** Guarda contra la reentrada de [cargarFavorito]; ver el KDoc del homólogo en oro. */
    private var favoritoAplicado = false

    init {
        // El mismo nombre que emitía el placeholder: conserva la serie histórica.
        analytics.logScreenView(SCREEN_NAME)
    }

    /**
     * Cambiar de familia arranca su formulario limpio (FR-023): la misma cifra tecleada
     * significaría otro metal, así que nunca se reinterpreta en silencio.
     */
    fun onFamiliaSeleccionada(familia: FamiliaSoldadura) {
        ultimaCombinacionRegistrada = null
        _uiState.value = SoldadurasUiState(familia = familia)
    }

    /**
     * Cambiar de modo vacía cantidad y resultado pero conserva las selecciones (FR-023):
     * un «10» tecleado como metal no puede pasar a leerse como peso final.
     */
    fun onModoCambiado(modo: ModoEntradaSoldadura) {
        ultimaCombinacionRegistrada = null
        _uiState.value = _uiState.value.copy(modo = modo, cantidadTexto = "", resultado = null)
    }

    fun onCantidadCambiada(texto: String) = recalcular { it.copy(cantidadTexto = texto) }

    fun onColorSeleccionado(color: ColorOroSoldadura) = recalcular { it.copy(colorOro = color) }

    fun onDurezaSeleccionada(dureza: DurezaSoldaduraLey) = recalcular { it.copy(dureza = dureza) }

    fun onTipoClasicaSeleccionado(tipo: TipoSoldaduraClasica) =
        recalcular { it.copy(tipoClasica = tipo) }

    fun onTipoPlataSeleccionado(tipo: TipoSoldaduraPlata) = recalcular { it.copy(tipoPlata = tipo) }

    /** Vuelve al formulario inicial conservando la familia; rearma la telemetría. */
    fun onLimpiar() {
        ultimaCombinacionRegistrada = null
        _uiState.value = SoldadurasUiState(familia = _uiState.value.familia)
    }

    /**
     * Guarda el cálculo que hay en pantalla. Sin familia elegida no hay formulario, así que tampoco
     * hay nada que guardar: se avisa igual que con el campo vacío.
     */
    fun onGuardarFavoritos() {
        val estado = _uiState.value
        val familia = estado.familia
        val cantidad = parsearCantidad(estado.cantidadTexto)
        if (familia == null || cantidad == null) {
            avisar(AvisoFavorito.SIN_DATOS)
            return
        }

        val entradas = when (familia) {
            FamiliaSoldadura.ORO_LEY -> EntradasFavorito.SoldaduraLey(
                cantidad = cantidad,
                dureza = estado.dureza,
                color = estado.colorOro,
                modo = estado.modo,
            )
            FamiliaSoldadura.CLASICA -> EntradasFavorito.SoldaduraClasica(
                cantidad = cantidad,
                tipo = estado.tipoClasica,
                modo = estado.modo,
            )
            FamiliaSoldadura.PLATA -> EntradasFavorito.SoldaduraPlata(
                cantidad = cantidad,
                tipo = estado.tipoPlata,
                modo = estado.modo,
            )
        }

        viewModelScope.launch(dispatchers.main) {
            val resultado = guardarFavorito(entradas)
            avisar(
                if (resultado is ResultadoGuardado.Guardado) {
                    AvisoFavorito.GUARDADO
                } else {
                    AvisoFavorito.REPETIDO
                },
            )
            analytics.logEvent(EVENT_FAVORITO, mapOf(PARAM_RESULTADO to resultado.analyticsId))
        }
    }

    fun onAvisoFavoritoMostrado() {
        if (_uiState.value.avisoFavorito == null) return
        _uiState.value = _uiState.value.copy(avisoFavorito = null)
    }

    /**
     * Rellena el formulario con un favorito guardado.
     *
     * El estado se construye **en una sola asignación**, y aquí no es una preferencia de estilo:
     * `onFamiliaSeleccionada` reasigna `SoldadurasUiState(familia = familia)` por FR-023 de la 006,
     * o sea que borra todo lo demás. Encadenar los setters públicos perdería la cantidad y emitiría
     * eventos `soldaduras_calculado` intermedios que no corresponden a ningún cálculo del joyero.
     */
    fun cargarFavorito(id: Long) {
        if (favoritoAplicado) return
        favoritoAplicado = true

        viewModelScope.launch(dispatchers.main) {
            val favorito = obtenerFavorito(id) ?: return@launch
            ultimaCombinacionRegistrada = null

            val estado = when (val entradas = favorito.entradas) {
                is EntradasFavorito.SoldaduraLey -> SoldadurasUiState(
                    familia = FamiliaSoldadura.ORO_LEY,
                    modo = entradas.modo,
                    cantidadTexto = FormatoFavoritos.cantidadEntrada(entradas.cantidad),
                    colorOro = entradas.color,
                    dureza = entradas.dureza,
                )
                is EntradasFavorito.SoldaduraClasica -> SoldadurasUiState(
                    familia = FamiliaSoldadura.CLASICA,
                    modo = entradas.modo,
                    cantidadTexto = FormatoFavoritos.cantidadEntrada(entradas.cantidad),
                    tipoClasica = entradas.tipo,
                )
                is EntradasFavorito.SoldaduraPlata -> SoldadurasUiState(
                    familia = FamiliaSoldadura.PLATA,
                    modo = entradas.modo,
                    cantidadTexto = FormatoFavoritos.cantidadEntrada(entradas.cantidad),
                    tipoPlata = entradas.tipo,
                )
                // Un favorito de otra sección: se ignora en silencio.
                else -> return@launch
            }
            _uiState.value = estado.copy(resultado = calcular(estado))
        }
    }

    private fun avisar(aviso: AvisoFavorito) {
        _uiState.value = _uiState.value.copy(avisoFavorito = aviso)
    }

    private fun recalcular(cambio: (SoldadurasUiState) -> SoldadurasUiState) {
        // El aviso se apaga en cuanto el joyero toca algo.
        val estado = cambio(_uiState.value).copy(avisoFavorito = null)
        _uiState.value = estado.copy(resultado = calcular(estado))
    }

    private fun calcular(estado: SoldadurasUiState): ResultadoSoldaduras? {
        // Sin familia elegida no hay formulario: ni cálculo ni telemetría (FR-002).
        val familia = estado.familia ?: return null

        val cantidad = parsearCantidad(estado.cantidadTexto)
        if (cantidad == null) {
            // Entrada inválida: sin resultados y sin error visible. Al volver a ser
            // válida, el cálculo se registra de nuevo.
            ultimaCombinacionRegistrada = null
            return null
        }

        val resultado = when (familia) {
            FamiliaSoldadura.ORO_LEY -> calcularOroLey(estado, cantidad)
            FamiliaSoldadura.CLASICA -> calcularFamiliaClasica(estado, cantidad)
            FamiliaSoldadura.PLATA -> calcularFamiliaPlata(estado, cantidad)
        }

        registrarCalculo(estado, familia)
        return resultado
    }

    /**
     * ORO LEY (§5.4). En modo directo la respuesta es la base necesaria para el oro que
     * se tiene, con el total de soldadura resultante; en modo inverso, el reparto
     * completo base + oro del color elegido.
     */
    private fun calcularOroLey(estado: SoldadurasUiState, cantidad: BigDecimal): ResultadoSoldaduras {
        val calculo: CalculoSoldaduraLey
        val filas: List<FilaSoldadura>

        when (estado.modo) {
            ModoEntradaSoldadura.DESDE_METAL -> {
                calculo = calcularLeyDesdeOro(cantidad, estado.dureza, estado.colorOro)
                // El oro introducido no se repite como fila (FR-022): solo la base.
                filas = listOf(FilaSoldadura(IngredienteSoldadura.BASE, formatearGramos(calculo.base)))
            }

            ModoEntradaSoldadura.PESO_FINAL -> {
                calculo = calcularLeyInversa(cantidad, estado.dureza, estado.colorOro)
                filas = listOf(
                    FilaSoldadura(IngredienteSoldadura.BASE, formatearGramos(calculo.base)),
                    FilaSoldadura(IngredienteSoldadura.ORO_18K, formatearGramos(calculo.oro18K)),
                )
            }
        }

        return ResultadoSoldaduras(filas = filas, totalFormateado = formatearGramos(calculo.total))
    }

    /**
     * CLÁSICA (§3). En modo directo se entra por el oro de la receta (18 K en floja y
     * fuerte, 24 K en muy floja de ley) y esa fila no se repite en el resultado
     * (FR-022); en modo inverso se reparte el peso final completo.
     */
    private fun calcularFamiliaClasica(
        estado: SoldadurasUiState,
        cantidad: BigDecimal,
    ): ResultadoSoldaduras {
        val calculo: CalculoSoldadura
        val filas: List<FilaSoldadura>

        when (estado.modo) {
            ModoEntradaSoldadura.DESDE_METAL -> {
                calculo = calcularClasica(cantidad, estado.tipoClasica)
                // El primer componente de cada receta clásica es su oro de entrada.
                filas = calculo.componentes
                    .drop(1)
                    .map { FilaSoldadura(it.metal.ingrediente, formatearGramos(it.gramos)) }
            }

            ModoEntradaSoldadura.PESO_FINAL -> {
                calculo = calcularClasicaInversa(cantidad, estado.tipoClasica)
                filas = calculo.componentes
                    .map { FilaSoldadura(it.metal.ingrediente, formatearGramos(it.gramos)) }
            }
        }

        return ResultadoSoldaduras(filas = filas, totalFormateado = formatearGramos(calculo.total))
    }

    /**
     * PLATA (§4). El factor es latón respecto a la plata fina, no sobre el peso final.
     * En modo directo la plata introducida no se repite como fila (FR-022): solo el
     * latón; en modo inverso se reparte el peso final entre plata y latón.
     */
    private fun calcularFamiliaPlata(
        estado: SoldadurasUiState,
        cantidad: BigDecimal,
    ): ResultadoSoldaduras {
        val calculo: CalculoSoldadura
        val filas: List<FilaSoldadura>

        when (estado.modo) {
            ModoEntradaSoldadura.DESDE_METAL -> {
                calculo = calcularPlata(cantidad, estado.tipoPlata)
                // El primer componente es la plata introducida.
                filas = calculo.componentes
                    .drop(1)
                    .map { FilaSoldadura(it.metal.ingrediente, formatearGramos(it.gramos)) }
            }

            ModoEntradaSoldadura.PESO_FINAL -> {
                calculo = calcularPlataInversa(cantidad, estado.tipoPlata)
                filas = calculo.componentes
                    .map { FilaSoldadura(it.metal.ingrediente, formatearGramos(it.gramos)) }
            }
        }

        return ResultadoSoldaduras(filas = filas, totalFormateado = formatearGramos(calculo.total))
    }

    /** Coma y punto valen (§8.1): se normalizan antes de parsear. Inválido o ≤ 0 → null. Delegado en `core/util/Decimales.kt`. */
    private fun parsearCantidad(texto: String): BigDecimal? = parsearDecimalPositivo(texto)

    private fun registrarCalculo(estado: SoldadurasUiState, familia: FamiliaSoldadura) {
        val combinacion = Combinacion(
            familia = familia,
            modo = estado.modo,
            tipo = when (familia) {
                FamiliaSoldadura.ORO_LEY -> estado.dureza.analyticsId
                FamiliaSoldadura.CLASICA -> estado.tipoClasica.analyticsId
                FamiliaSoldadura.PLATA -> estado.tipoPlata.analyticsId
            },
            // El color solo existe en ORO LEY (§8.1): las clásicas no lo tienen.
            color = if (familia == FamiliaSoldadura.ORO_LEY) estado.colorOro.analyticsId else null,
        )
        if (combinacion == ultimaCombinacionRegistrada) return
        ultimaCombinacionRegistrada = combinacion

        val params = buildMap {
            put(PARAM_FAMILIA, combinacion.familia.analyticsId)
            put(PARAM_MODO, combinacion.modo.analyticsId)
            put(PARAM_TIPO, combinacion.tipo)
            combinacion.color?.let { put(PARAM_COLOR, it) }
        }
        analytics.logEvent(EVENT_CALCULO, params)
    }

    /**
     * Redondeo exclusivo de presentación, con coma decimal española. Nunca realimenta el
     * cálculo (§8.1, §8.3).
     *
     * A la media (`HALF_UP`), como en oro y a diferencia del truncado de plata: aquí no
     * hay ley de contraste que proteger — son recetas de taller y la cifra más cercana
     * al valor exacto es la más útil. La suma visible puede desviarse una milésima del
     * total en los repartos con división infinita, y la respuesta es la nota de §8.3,
     * jamás ajustar un ingrediente.
     */
    private fun formatearGramos(valor: BigDecimal): String =
        valor.setScale(3, RoundingMode.HALF_UP).toPlainString().replace('.', ',')

    /** Clave de deduplicación del evento de cálculo: la combinación completa. */
    private data class Combinacion(
        val familia: FamiliaSoldadura,
        val modo: ModoEntradaSoldadura,
        val tipo: String,
        val color: String?,
    )

    private companion object {
        const val SCREEN_NAME = "soldaduras"
        const val EVENT_CALCULO = "soldaduras_calculado"
        const val EVENT_FAVORITO = "soldaduras_favorito_guardado"
        const val PARAM_RESULTADO = "resultado"
        const val PARAM_FAMILIA = "familia"
        const val PARAM_MODO = "modo"
        const val PARAM_TIPO = "tipo"
        const val PARAM_COLOR = "color"
    }
}
