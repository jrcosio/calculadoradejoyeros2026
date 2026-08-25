# Quickstart: validar Herramientas de punta a punta

**Feature**: `007-herramientas` · **Fecha**: 2026-08-25

Guía de validación, no de implementación. Los criterios están en [spec.md](./spec.md)
(SC-001…SC-014); el contrato del proveedor en [contracts/metal-quote.md](./contracts/metal-quote.md).

## 0. Prerrequisitos

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
# Credencial del proveedor (nunca se commitea; local.properties está en .gitignore)
grep -q '^RAPIDAPI_KEY=' local.properties || echo 'RAPIDAPI_KEY=<tu clave>' >> local.properties
```

`app/google-services.json` debe estar presente (como en cualquier build del proyecto).

## 1. Confirmar el contrato del proveedor (antes de escribir el cliente)

Ejecutar el bloque «Verificación del contrato» de [contracts/metal-quote.md](./contracts/metal-quote.md).
Resultado obtenido el 2026-08-25: HTTP 200 en `AU AG CU PD RH` con `/metal-quote?symbol=`, `currency: "EUR"`, `OUNCE` salvo el cobre (`POUND`).
La ruta `/api/metal-quote` de la web pública no existe para la suscripción y `metal=` responde 200 con error; `PARAMETRO_METAL = "symbol"` y
anotar la unidad de cada metal en el contrato. Guardar una respuesta anonimizada como fixture.

## 2. Puertas automáticas

```bash
./gradlew :app:testDebugUnitTest      # motor de chapas, política de caché, data source con la muestra real,
                                      # repositorio (caché/parcial/espera/muerte de proceso/single-flight),
                                      # formato, ViewModels con TestDispatcher, KoinModulesTest
./gradlew :app:lint
./gradlew :app:assembleDebug
./gradlew :app:compileDebugAndroidTestKotlin
./gradlew :app:assembleRelease        # los DTO @Serializable sobreviven a R8 (abrir la pantalla con el APK firmado)
```

Resultados XML en `app/build/test-results/testDebugUnitTest/`.

## 3. Verificación en emulador

Instalar el debug (`adb install -r app/build/outputs/apk/debug/app-debug.apk`) y abrir el
panel de RapidAPI para contar peticiones.

| # | Paso | Esperado | Criterio |
|---|---|---|---|
| 1 | Home → Herramientas | Solo el selector (PRECIO METALES / PESO DE CHAPAS), ninguno marcado, tarjeta «Elige una herramienta»; sin barra inferior | SC-006, SC-011 |
| 2 | Pulsar PRECIO METALES | Indicador de carga y después 5 filas (oro, plata, cobre, paladio, rodio) con imagen, símbolo, precio €/g, flecha; pie con «Precios orientativos…», «Fuente: Metal Sentinel», «Actualizado …»; tarjeta de mercado del oro | SC-001 |
| 3 | Panel de RapidAPI | **Exactamente 5** peticiones nuevas | SC-001 |
| 4 | Atrás → Herramientas → PRECIO METALES | Mismos precios al instante, misma hora; **0** peticiones nuevas | SC-001 |
| 5 | Forzar cierre de la app (`adb shell am force-stop com.jrblanco.calculadoradejoyeros2021`) y repetir el paso 4 | Igual: 0 peticiones; los precios vienen de la caché persistida | SC-001, FR-005 |
| 6 | Selector de unidad → kilo → onza → gramo | Todas las cifras convertidas (oro ≈ ×1 000 en kilo; en onza la cifra del proveedor); 0 peticiones | SC-002 |
| 7 | Pulsar Plata, Cobre, Paladio, Rodio | La fila marcada cambia y la tarjeta de mercado muestra ask, bid, máximo, mínimo, variación, variación %, unidad y actualización del metal pulsado | US2 |
| 8 | Modo avión + `adb shell date` una hora adelante (o esperar) → reabrir PRECIO METALES | Últimos precios marcados como desactualizados, aviso y «Reintentar» | SC-005, FR-006 |
| 9 | «Reintentar» dos veces seguidas | La segunda no consulta (aviso de espera); tras 60 s vuelve a intentar solo los fallidos | SC-013, FR-007 |
| 10 | Build sin `RAPIDAPI_KEY` (comentar la línea, `assembleDebug`) → PRECIO METALES | «Servicio no configurado», sin cierre, 0 peticiones | FR-015 |
| 11 | PESO DE CHAPAS | ORO 18K preseleccionados, tres campos vacíos, chapa de referencia que se construye (< 1 s), sin resultado | US3, US4 |
| 12 | Ancho 10 · Espesor 0,5 · Largo 20 | Al completar la tercera: **1,56 g**, «Calculado para Oro 18 K», volumen 0,100 cm³, densidad 15,58 g/cm³, pureza 75,0 % (18 K), oro fino 1,169 g; la chapa se redibuja con cada tecla y las cotas dicen 10,00 / 0,50 / 20,00 mm | SC-003, SC-004, SC-008 |
| 13 | 14K, 12K, 9K | 1,31 / 1,28 / 1,12 g; 12K muestra el aviso técnico | SC-003, FR-023 |
| 14 | PLATA | Leyes 950/925/900/800 con 925 marcada, acento y chapa en turquesa, medidas conservadas, 1,04 g y plata fina 0,958 g; 950 → 1,04, 900 → 1,03, 800 → 1,01 | SC-003, FR-016 |
| 15 | Espesor «0.5» en lugar de «0,5» | Mismo resultado | SC-007 |
| 16 | Ancho 10001 | Campo marcado, aviso de rango, sin resultado; volver a 10 lo quita | FR-018 |
| 17 | Cambiar a PRECIO METALES y volver | Las medidas siguen tecleadas y el resultado visible | FR-002 |
| 18 | «Limpiar» / «Guardar en favoritos» | Estado inicial de chapas / toast «Próximamente» sin cambios | FR-025 |
| 19 | Ajustes del sistema → fuente al doble | Nada se recorta; tarjeta de mercado y resultado apilados; todo alcanzable | SC-009 |
| 20 | TalkBack | Selector, filas, tarjeta, campos, ilustración («Chapa de Oro 18 K de 10,00 mm de ancho…»), resultados y avisos se anuncian | SC-010 |
| 21 | Lado a lado con `UI_Plantillas/Feature_Herramientas/*.png` | Reconocible con los estilos de la app; diferencias documentadas en Assumptions | SC-014 |

Anotar el resultado y las desviaciones en la sección «Resultado de la verificación» al final
de `tasks.md`, como en la 006.
