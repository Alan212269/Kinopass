package no.usn.mob3000.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Fargetema for lys modus i Material 3.
// Brukes når systemet er i lys modus (eller hvis appen tvinger lys tema).
private val LightColors: ColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),      // Primærfarge for knapper/aksjoner
    secondary = Color(0xFF625B71),    // Sekundærfarge brukt i mindre UI-elementer
    background = Color(0xFFFCF7FF),   // Bakgrunnsfarge for hele appen
    surface = Color(0xFFFCF7FF),      // Bakgrunn for kort, paneler, top/bottom-bar
    onPrimary = Color.White,          // Tekst/ikon-farge på primary-elementer
    onSecondary = Color.White,
    onBackground = Color(0xFF1C1B1F), // Tekst på lys bakgrunn
    onSurface = Color(0xFF1C1B1F)
)

// Fargetema for mørk modus i Material 3.
// Brukes når systemet er i dark mode.
private val DarkColors: ColorScheme = darkColorScheme(
    primary = Color(0xFF4C3A88),      // Mørkere variant av primærfargen
    secondary = Color(0xFF625B71),
    background = Color(0xFF121212),   // Mørk bakgrunn som passer OLED/AMOLED
    surface = Color(0xFF121212),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFECECEC), // Lys tekst på mørk bakgrunn
    onSurface = Color(0xFFECECEC)
)

// Globale app-temaet som hele UI-et ligger inni.
// Bestemmer hvilke farger og typografi appen bruker.
// useDarkTheme → velger LightColors eller DarkColors automatisk basert på systeminnstilling.
@Composable
fun KinopassTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (useDarkTheme) DarkColors else LightColors,
        typography = MaterialTheme.typography, // Bruker standard typografi
        content = content                      // Selve UI-et som skal bruke dette temaet
    )
}
