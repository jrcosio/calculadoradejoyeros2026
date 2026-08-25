package com.jrblanco.calculadoradejoyeros2021.core.util

import java.math.BigDecimal

/**
 * Parsea lo que el joyero teclea en un campo numérico de la app.
 *
 * Coma y punto valen como separador decimal: se normalizan antes de parsear. Vacío, texto
 * no numérico o un valor menor o igual que cero devuelven `null`, que las pantallas
 * interpretan como «sin resultado» sin mensaje alarmante. No se admiten separadores de
 * miles: `toBigDecimalOrNull` rechaza «1,2,3» y «1.2.3» por sí solo.
 *
 * Nació privado —y repetido— en los ViewModels de oro, plata y soldaduras; se promueve aquí
 * cuando la calculadora de chapas lo pide por quinta vez (regla del segundo consumidor).
 */
fun parsearDecimalPositivo(texto: String): BigDecimal? =
    texto.trim().replace(',', '.').toBigDecimalOrNull()?.takeIf { it > BigDecimal.ZERO }
