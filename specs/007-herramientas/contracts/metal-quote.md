# Contrato: cotización de un metal (Metal Sentinel vía RapidAPI)

**Feature**: `007-herramientas` · **Fecha**: 2026-08-25
**Fuente**: ejemplo de respuesta real embebido en la web pública del proveedor
(`https://metal-sentinel.com/gold-price-api`, código de la página, 2026-08-25) y su referencia
de endpoints (`https://metal-sentinel.com/endpoints`). La app es el **único consumidor**; este
documento fija lo que la app espera del proveedor y lo que hace con cada campo.

## Petición

```http
GET https://metal-sentinel.p.rapidapi.com/metal-quote?symbol=AU&currency=EUR
x-rapidapi-host: metal-sentinel.p.rapidapi.com
x-rapidapi-key: <credencial, nunca en el repositorio>
Accept: application/json
```

| Elemento | Valor | Nota |
|---|---|---|
| Ruta | `/metal-quote` | **Confirmada con la credencial real (2026-08-25)**: la ruta `/api/metal-quote` de la web pública responde 404 «Endpoint does not exist» para esta suscripción. Es la del ejemplo inicial del encargo |
| Parámetro del metal | `symbol` | **Confirmado (2026-08-25)**: `metal=` responde **HTTP 200** con `{"error":"The symbol field is required."}` y sin `results`, que el parser clasifica como respuesta no válida. Una sola constante (`PARAMETRO_METAL`); **prohibido** el reintento automático entre variantes: gasta cuota |
| Valores del metal | `AU`, `AG`, `CU`, `PD`, `RH` | Mayúsculas. Uno por petición → una carga completa = 5 peticiones |
| `currency` | `EUR` | Siempre. Si la respuesta trae otra moneda, ese metal es «respuesta no válida» |
| `timestamp` | no se envía | Solo documentado para históricos; fuera de alcance |

## Respuesta (200)

Ejemplo real (USD, oro, 2026-08-25):

```json
{
  "ID": 1787665737,
  "results": [
    {
      "symbol": "AU",
      "currency": "USD",
      "ask": 4607.4,
      "mid": 4606.4,
      "bid": 4605.4,
      "high": 4697.5,
      "low": 4604.6,
      "open": 0,
      "close": 0,
      "timestamp": 1787665680,
      "change": -45.30000000000018,
      "changePercentage": -0.974046917668312,
      "unit": "OUNCE",
      "originalTime": "2026-08-25T09:47:59.727Z",
      "extra": "{\"ChangePercentTrade\":-0.99,\"ChangePercentUSD\":0.02,\"ChangeTrade\":-46.23,\"ChangeUSD\":0.93}"
    }
  ]
}
```

| Campo | Tipo JSON | Uso en la app | Destino en dominio |
|---|---|---|---|
| `ID` | number | Se ignora (identificador de la petición) | — |
| `results` | array | Se busca el elemento cuyo `symbol` coincide con el pedido (sin distinguir mayúsculas); si no hay ninguno → «respuesta no válida» | — |
| `symbol` | string | Selección del elemento | `CotizacionMetal.metal` (vía `MetalCotizado.simboloApi`) |
| `currency` | string | Debe ser `EUR`; si no → «respuesta no válida» | `CotizacionMetal.moneda` |
| `mid` | number | **Precio principal** de la fila (decisión del autor) | `CotizacionMetal.mid` |
| `ask` | number | Precio de venta; sustituto de `mid` si este es 0 | `CotizacionMetal.ask` |
| `bid` | number | Precio de compra; sustituto si `mid` y `ask` son 0 | `CotizacionMetal.bid` |
| `high` / `low` | number | Máximo y mínimo | `maximo` / `minimo` |
| `change` | number | Variación absoluta; su signo da la tendencia | `variacion` |
| `changePercentage` | number | Variación porcentual; **no se convierte** de unidad | `variacionPorcentaje` |
| `unit` | string | `OUNCE` → onza troy (31,1034768 g): oro, plata, paladio y rodio. **`POUND` → libra avoirdupois (453,59237 g): el cobre.** `GRAM`/`G` → gramo. `KILOGRAM`/`KG` → kilo. Otro valor → unidad desconocida: se muestra sin convertir con la etiqueta cruda | `unidadOrigen: UnidadPrecio?` + `etiquetaUnidadOrigen` |
| `timestamp` | number | Instante del dato en **segundos** Unix (si llega > 10¹² se toma como milisegundos) | `instanteMercadoEpochMillis` |
| `open` / `close` | number | Llegan a 0 en la muestra; se ignoran y no se declaran en el DTO | — |
| `originalTime` / `extra` | string | Se ignoran (`ignoreUnknownKeys`) | — |

Reglas de parseo:

- Los números llegan como **JSON number**. Se toma el literal tal cual viene en el cable y se
  construye `BigDecimal(String)` con un serializador propio; nunca `BigDecimal(double)`
  (`-45.30000000000018` se conserva exacto).
- **Unidades confirmadas con la credencial real (2026-08-25)**: `AU`, `AG`, `PD`, `RH` en
  `OUNCE`; `CU` en `POUND` (5,61 €/lb ≈ 12,37 €/kg, coherente con el cobre en el mercado).
  `LIBRA` existe en `UnidadPrecio` solo como unidad de origen: no se ofrece en el selector. El
  camino «unidad desconocida» sigue cubriendo cualquier otra sorpresa sin romper la pantalla.
- La respuesta real anonimizada (sin `ID` ni `extra`) de los cinco metales está en
  `UI_Plantillas/Feature_Herramientas/respuesta_ejemplo_metal_quote.json`; la del oro y la del
  cobre son fixtures de `MetalSentinelDataSourceTest`.

## Errores

| Situación | Cómo llega | Motivo en dominio | Mensaje al joyero |
|---|---|---|---|
| Credencial vacía en la build | No se llama a la red | `SIN_CREDENCIAL` | Servicio no configurado |
| 401 / 403 | Código HTTP | `CREDENCIAL_RECHAZADA` | Credencial rechazada o suscripción insuficiente |
| 404 | Código HTTP | `NO_ENCONTRADO` | Servicio no disponible (ruta o parámetro) |
| 429 | Código HTTP | `LIMITE_ALCANZADO` | Límite de consultas alcanzado (espera de 5 min) |
| 5xx | Código HTTP | `SERVIDOR` | El proveedor no está disponible |
| Sin red / timeout | `IOException` | `SIN_CONEXION` | No se ha podido conectar |
| JSON ilegible, `results` sin el símbolo, moneda ≠ EUR | Parseo | `RESPUESTA_INVALIDA` | Respuesta no válida del proveedor |
| Cualquier otra | Excepción | `DESCONOCIDO` | No se ha podido obtener la cotización |

El cuerpo de error del proveedor no se muestra al joyero ni se registra: puede contener
detalles técnicos. La credencial no aparece jamás en mensajes, registros ni telemetría.

## Cuota y ritmo

- Plan gratuito publicado el 2026-08-25: **15 000 peticiones/mes**, **60/min**.
- 1 carga completa = 5 peticiones → **3 000 cargas/mes** para toda la base de usuarios.
- Con la caché de 1 h de la app, un único dispositivo consume como máximo 5 × 24 × 31 = 3 720
  peticiones/mes si se abriera la sección cada hora; la cuota es compartida por todos los
  usuarios, de ahí que esta integración directa sea de **prototipo** (spec, Assumptions).

## Verificación del contrato (Paso 0 de la implementación)

Con `RAPIDAPI_KEY` en `local.properties` y **sin imprimir la clave**:

```bash
KEY=$(grep '^RAPIDAPI_KEY=' local.properties | cut -d= -f2-)
for M in AU AG CU PD RH; do
  curl -s -o /tmp/mq_$M.json -w "$M metal= -> %{http_code}\n" \
    "https://metal-sentinel.p.rapidapi.com/metal-quote?symbol=$M&currency=EUR" \
    -H "x-rapidapi-host: metal-sentinel.p.rapidapi.com" -H "x-rapidapi-key: $KEY" -H "Accept: application/json"
done
# anotar unit y currency de cada fichero:
grep -o '"unit":"[A-Z]*"\|"currency":"[A-Z]*"' /tmp/mq_*.json
```

**Resultado obtenido (2026-08-25)**: 200 en los cinco, `currency: "EUR"`, `unit` `OUNCE` salvo el
cobre (`POUND`); `/api/metal-quote` → 404 y `?metal=` → 200 con cuerpo de error. Se guardó
una respuesta anonimizada (sin `ID` ni `extra`) como fixture de tests y anexo de este contrato.
