package com.jrblanco.calculadoradejoyeros2021.core.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * [DispatcherProvider] de test: los tres dispatchers son el mismo [TestDispatcher].
 *
 * Los ViewModels lanzan siempre con `dispatchers.main`, así que basta inyectar este proveedor
 * para que el test sea determinista: no hace falta `Dispatchers.setMain` ni una regla global.
 * `UnconfinedTestDispatcher` ejecuta las corrutinas de inmediato, que es lo cómodo para
 * afirmar el estado justo después de llamar a un método del ViewModel.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TestDispatcherProvider(
    dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : DispatcherProvider {
    override val main: CoroutineDispatcher = dispatcher
    override val io: CoroutineDispatcher = dispatcher
    override val default: CoroutineDispatcher = dispatcher
}
