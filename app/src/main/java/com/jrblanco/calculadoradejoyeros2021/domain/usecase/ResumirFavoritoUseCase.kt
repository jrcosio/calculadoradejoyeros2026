package com.jrblanco.calculadoradejoyeros2021.domain.usecase

import com.jrblanco.calculadoradejoyeros2021.domain.model.EntradasFavorito
import com.jrblanco.calculadoradejoyeros2021.domain.model.MetalSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.ModoEntradaSoldadura
import com.jrblanco.calculadoradejoyeros2021.domain.model.ResumenFavorito

/**
 * Rehace el cálculo de un favorito con los motores de siempre. No guarda nada, no suspende y no
 * redondea: es una función pura y barata, y por eso los resultados no se persisten.
 *
 * Once casos de uso por constructor, y es el primero del proyecto que depende de otros casos de uso.
 * La alternativa era que el ViewModel de Favoritos los inyectara y repitiera el despacho de las
 * cinco calculadoras más las reglas de qué filas se pintan; eso pondría lógica de dominio en
 * presentación y la duplicaría por sexta vez. `KoinModulesTest` verifica el constructor entero.
 *
 * Recibe [EntradasFavorito] y no `Favorito`: ni el id ni la hora influyen en una sola cifra, así que
 * el resumen se puede calcular antes de que exista la fila.
 *
 * **Segunda transcripción, y hay que saberlo**: las reglas de qué fila se pinta en cada modo vienen
 * de `SoldadurasViewModel` y `SoldaduraBaseViewModel` (FR-022 de la 006). En modo directo el metal de
 * partida no se repite como fila. Si allí cambia el criterio, aquí hay que cambiarlo también; los
 * tests usan los mismos vectores que los de los motores.
 *
 * **Aviso**: como lo guardado son las entradas y no las cifras, si una versión futura corrige una
 * receta de `RecetasSoldadura` o una densidad de `MaterialChapa`, los favoritos viejos mostrarán las
 * cifras nuevas. Es deliberado: un favorito es una receta, no un recibo.
 */
class ResumirFavoritoUseCase(
    private val calcularOro: CalcularAleacionOroUseCase,
    private val calcularPlata: CalcularAleacionPlataUseCase,
    private val calcularLeyDesdeOro: CalcularSoldaduraLeyDesdeOroUseCase,
    private val calcularLeyInversa: CalcularSoldaduraLeyInversaUseCase,
    private val calcularClasica: CalcularSoldaduraClasicaUseCase,
    private val calcularClasicaInversa: CalcularSoldaduraClasicaInversaUseCase,
    private val calcularSoldaduraPlata: CalcularSoldaduraPlataUseCase,
    private val calcularSoldaduraPlataInversa: CalcularSoldaduraPlataInversaUseCase,
    private val calcularBase: CalcularSoldaduraBaseUseCase,
    private val calcularBaseInversa: CalcularSoldaduraBaseInversaUseCase,
    private val calcularChapa: CalcularPesoChapaUseCase,
) {

    operator fun invoke(entradas: EntradasFavorito): ResumenFavorito = when (entradas) {

        is EntradasFavorito.Oro -> calcularOro(entradas.masaOrigen, entradas.color, entradas.ley)
            .let { ResumenFavorito.Oro(metales = it.metales, masaFinal = it.masaFinal) }

        is EntradasFavorito.Plata -> calcularPlata(entradas.masaOrigen, entradas.ley)
            .let { ResumenFavorito.Plata(cobre = it.cobre, masaFinal = it.masaFinal) }

        is EntradasFavorito.SoldaduraLey -> when (entradas.modo) {
            // El oro introducido no se repite como fila: solo la base.
            ModoEntradaSoldadura.DESDE_METAL ->
                calcularLeyDesdeOro(entradas.cantidad, entradas.dureza, entradas.color)
                    .let { ResumenFavorito.SoldaduraLey(it.base, oro18K = null, total = it.total) }

            ModoEntradaSoldadura.PESO_FINAL ->
                calcularLeyInversa(entradas.cantidad, entradas.dureza, entradas.color)
                    .let { ResumenFavorito.SoldaduraLey(it.base, it.oro18K, it.total) }
        }

        is EntradasFavorito.SoldaduraClasica -> when (entradas.modo) {
            // El primer componente de cada receta clásica es su oro de entrada.
            ModoEntradaSoldadura.DESDE_METAL -> calcularClasica(entradas.cantidad, entradas.tipo)
                .let { ResumenFavorito.Soldadura(it.componentes.drop(1), it.total) }

            ModoEntradaSoldadura.PESO_FINAL ->
                calcularClasicaInversa(entradas.cantidad, entradas.tipo)
                    .let { ResumenFavorito.Soldadura(it.componentes, it.total) }
        }

        is EntradasFavorito.SoldaduraPlata -> when (entradas.modo) {
            // El primer componente es la plata introducida.
            ModoEntradaSoldadura.DESDE_METAL ->
                calcularSoldaduraPlata(entradas.cantidad, entradas.tipo)
                    .let { ResumenFavorito.Soldadura(it.componentes.drop(1), it.total) }

            ModoEntradaSoldadura.PESO_FINAL ->
                calcularSoldaduraPlataInversa(entradas.cantidad, entradas.tipo)
                    .let { ResumenFavorito.Soldadura(it.componentes, it.total) }
        }

        is EntradasFavorito.SoldaduraBase -> when (entradas.modo) {
            // Aquí el oro de partida es 24 K y se filtra por metal, no por posición.
            ModoEntradaSoldadura.DESDE_METAL -> calcularBase(entradas.cantidad)
                .let { calculo ->
                    ResumenFavorito.Soldadura(
                        componentes = calculo.componentes.filter { it.metal != MetalSoldadura.ORO_24K },
                        total = calculo.total,
                    )
                }

            ModoEntradaSoldadura.PESO_FINAL -> calcularBaseInversa(entradas.cantidad)
                .let { ResumenFavorito.Soldadura(it.componentes, it.total) }
        }

        is EntradasFavorito.Chapa -> calcularChapa(
            entradas.ancho,
            entradas.largo,
            entradas.espesor,
            entradas.material,
        ).let { ResumenFavorito.Chapa(peso = it.peso, volumenCm3 = it.volumenCm3, metalFino = it.metalFino) }
    }
}
