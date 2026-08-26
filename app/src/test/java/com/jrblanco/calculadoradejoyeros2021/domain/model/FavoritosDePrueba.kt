package com.jrblanco.calculadoradejoyeros2021.domain.model

import java.math.BigDecimal

/**
 * Fábrica de favoritos para los tests, al estilo de [CotizacionesDePrueba]: valores por defecto
 * razonables y todo sobrescribible, para que cada test sólo nombre lo que le importa.
 */
object FavoritosDePrueba {

    fun oro(
        masaOrigen: String = "30",
        color: ColorOro = ColorOro.BLANCO,
        ley: LeyOro = LeyOro.LEY_18K,
    ) = EntradasFavorito.Oro(BigDecimal(masaOrigen), color, ley)

    fun plata(
        masaOrigen: String = "100",
        ley: LeyPlata = LeyPlata.LEY_950,
    ) = EntradasFavorito.Plata(BigDecimal(masaOrigen), ley)

    fun soldaduraLey(
        cantidad: String = "10",
        dureza: DurezaSoldaduraLey = DurezaSoldaduraLey.MUY_FLOJA,
        color: ColorOroSoldadura = ColorOroSoldadura.AMARILLO,
        modo: ModoEntradaSoldadura = ModoEntradaSoldadura.DESDE_METAL,
    ) = EntradasFavorito.SoldaduraLey(BigDecimal(cantidad), dureza, color, modo)

    fun soldaduraClasica(
        cantidad: String = "10",
        tipo: TipoSoldaduraClasica = TipoSoldaduraClasica.FLOJA,
        modo: ModoEntradaSoldadura = ModoEntradaSoldadura.DESDE_METAL,
    ) = EntradasFavorito.SoldaduraClasica(BigDecimal(cantidad), tipo, modo)

    fun soldaduraPlata(
        cantidad: String = "10",
        tipo: TipoSoldaduraPlata = TipoSoldaduraPlata.NORMAL,
        modo: ModoEntradaSoldadura = ModoEntradaSoldadura.DESDE_METAL,
    ) = EntradasFavorito.SoldaduraPlata(BigDecimal(cantidad), tipo, modo)

    fun soldaduraBase(
        cantidad: String = "10",
        modo: ModoEntradaSoldadura = ModoEntradaSoldadura.DESDE_METAL,
    ) = EntradasFavorito.SoldaduraBase(BigDecimal(cantidad), modo)

    fun chapa(
        ancho: String = "10",
        largo: String = "20",
        espesor: String = "0.5",
        material: MaterialChapa = MaterialChapa.ORO_18K,
    ) = EntradasFavorito.Chapa(BigDecimal(ancho), BigDecimal(largo), BigDecimal(espesor), material)

    /** Las siete variantes, para los tests que recorren todas. */
    fun todas(): List<EntradasFavorito> = listOf(
        oro(), plata(), soldaduraLey(), soldaduraClasica(), soldaduraPlata(), soldaduraBase(), chapa(),
    )

    fun favorito(
        id: Long = 1L,
        guardadoEnEpochMillis: Long = 1_787_670_000_000L,
        entradas: EntradasFavorito = oro(),
    ) = Favorito(id, guardadoEnEpochMillis, entradas)
}
