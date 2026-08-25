package com.jrblanco.calculadoradejoyeros2021.domain.model

import com.jrblanco.calculadoradejoyeros2021.domain.model.CotizacionesDePrueba.error
import com.jrblanco.calculadoradejoyeros2021.domain.model.CotizacionesDePrueba.exito
import com.jrblanco.calculadoradejoyeros2021.domain.model.CotizacionesDePrueba.instantaneaCompleta
import com.jrblanco.calculadoradejoyeros2021.domain.model.CotizacionesDePrueba.instantaneaParcial
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * La política es una función pura: aquí se decide cuándo se gasta cuota del proveedor, sin
 * corrutinas, sin fakes y sin esperar una hora de verdad.
 */
class PoliticaCacheCotizacionesTest {

    private val politica = PoliticaCacheCotizaciones()
    private val hora = 3_600_000L
    private val minuto = 60_000L
    private val t0 = 1_787_670_000_000L

    @Test
    fun `sin nada guardado se consultan los cinco`() {
        assertEquals(
            DecisionCache.Actualizar(MetalCotizado.entries.toSet()),
            politica.decidir(InstantaneaCotizaciones.VACIA, t0),
        )
    }

    @Test
    fun `completa y vigente se sirve sin red hasta el ultimo milisegundo`() {
        val guardada = instantaneaCompleta(obtenidoEn = t0)
        assertEquals(DecisionCache.Servir, politica.decidir(guardada, t0))
        assertEquals(DecisionCache.Servir, politica.decidir(guardada, t0 + hora - 1))
    }

    @Test
    fun `al cumplir la hora se consultan los cinco`() {
        val guardada = instantaneaCompleta(obtenidoEn = t0)
        assertEquals(
            DecisionCache.Actualizar(MetalCotizado.entries.toSet()),
            politica.decidir(guardada, t0 + hora),
        )
    }

    @Test
    fun `tras un fallo parcial reciente se espera`() {
        val guardada = instantaneaParcial(obtenidoEn = t0)
        assertEquals(DecisionCache.Esperar, politica.decidir(guardada, t0 + 30_000))
        assertEquals(DecisionCache.Esperar, politica.decidir(guardada, t0 + minuto - 1))
    }

    @Test
    fun `pasado el minuto solo se reintentan los fallidos`() {
        val guardada = instantaneaParcial(obtenidoEn = t0, fallido = MetalCotizado.RODIO)
        assertEquals(
            DecisionCache.Actualizar(setOf(MetalCotizado.RODIO)),
            politica.decidir(guardada, t0 + minuto + 1_000),
        )
    }

    @Test
    fun `un exito caducado y un error se actualizan juntos`() {
        val guardada = InstantaneaCotizaciones(
            resultados = mapOf(
                MetalCotizado.ORO to exito(MetalCotizado.ORO, obtenidoEn = t0 - hora - 1),
                MetalCotizado.PLATA to exito(MetalCotizado.PLATA, obtenidoEn = t0),
                MetalCotizado.COBRE to exito(MetalCotizado.COBRE, obtenidoEn = t0),
                MetalCotizado.PALADIO to exito(MetalCotizado.PALADIO, obtenidoEn = t0),
                MetalCotizado.RODIO to error(MetalCotizado.RODIO),
            ),
            instanteIntentoEpochMillis = t0,
        )
        assertEquals(
            DecisionCache.Actualizar(setOf(MetalCotizado.ORO, MetalCotizado.RODIO)),
            politica.decidir(guardada, t0 + 2 * minuto),
        )
    }

    @Test
    fun `tras un limite de cuota la espera es de cinco minutos`() {
        val guardada = instantaneaParcial(obtenidoEn = t0, motivo = MotivoErrorCotizacion.LIMITE_ALCANZADO)
        assertEquals(DecisionCache.Esperar, politica.decidir(guardada, t0 + 4 * minuto))
        assertEquals(
            DecisionCache.Actualizar(setOf(MetalCotizado.RODIO)),
            politica.decidir(guardada, t0 + 6 * minuto),
        )
    }

    @Test
    fun `un reloj atrasado no bloquea ni sirve datos del futuro`() {
        val guardada = instantaneaCompleta(obtenidoEn = t0)
        assertEquals(
            DecisionCache.Actualizar(MetalCotizado.entries.toSet()),
            politica.decidir(guardada, t0 - 1),
        )
    }

    @Test
    fun `los plazos se pueden acortar por constructor`() {
        val corta = PoliticaCacheCotizaciones(vigenciaMillis = 1_000, esperaReintentoMillis = 100)
        val guardada = instantaneaParcial(obtenidoEn = t0)
        assertEquals(DecisionCache.Esperar, corta.decidir(guardada, t0 + 50))
        assertEquals(DecisionCache.Actualizar(setOf(MetalCotizado.RODIO)), corta.decidir(guardada, t0 + 500))
        assertEquals(DecisionCache.Actualizar(MetalCotizado.entries.toSet()), corta.decidir(guardada, t0 + 1_000))
    }
}
