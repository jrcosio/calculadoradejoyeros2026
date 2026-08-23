package com.jrblanco.calculadoradejoyeros2021

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jrblanco.calculadoradejoyeros2021.ui.navigation.AppNavHost
import com.jrblanco.calculadoradejoyeros2021.ui.theme.Calculadoradejoyeros2021Theme

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
                AppNavHost()
            }
        }
    }
}
