package com.jrblanco.calculadoradejoyeros2021

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jrblanco.calculadoradejoyeros2021.ui.idioma.IdiomaAppViewModel
import com.jrblanco.calculadoradejoyeros2021.ui.idioma.ProveedorIdioma
import com.jrblanco.calculadoradejoyeros2021.ui.navigation.AppNavHost
import com.jrblanco.calculadoradejoyeros2021.ui.theme.Calculadoradejoyeros2021Theme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Barras transparentes con iconos claros: la app es de tema oscuro fijo, así
        // que los iconos del sistema deben leerse sobre el fondo azul marino.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            Calculadoradejoyeros2021Theme {
                // El idioma de la app entera lo posee la Activity, no una pantalla: cambiarlo en
                // Ajustes repinta todo el árbol. Mientras es nulo no se pinta nada —la primera
                // lectura de la preferencia es asíncrona— y lo que se ve es el fondo de ventana
                // del tema, que ya es el azul de la portada.
                val idiomaViewModel: IdiomaAppViewModel = koinViewModel()
                val idiomaState by idiomaViewModel.uiState.collectAsStateWithLifecycle()

                idiomaState.idioma?.let { idioma ->
                    ProveedorIdioma(idioma) {
                        AppNavHost()
                    }
                }
            }
        }
    }
}
