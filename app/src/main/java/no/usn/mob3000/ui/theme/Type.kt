package no.usn.mob3000.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Appens typografioppsett for Material 3.
// Typography bestemmer standard tekststørrelser, fontfamilie og vekter på tvers av appen.
//
// Her brukes Compose sin default fontfamilie, men du kunne byttet til en
// custom font ved å importere en FontFamily fra res/font.
//
// Kun bodyLarge er overstyrt her, men MaterialTheme.typography inneholder
// mange flere stiler dersom man ønsker å utvide.
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,     // Standard systemfont
        fontWeight = FontWeight.Normal,      // Normal tekstvekt
        fontSize = 16.sp,                    // Vanlig brødtekststørrelse
        lineHeight = 24.sp,                  // Linjeavstand for bedre lesbarhet
        letterSpacing = 0.5.sp               // Litt spacing for moderne uttrykk
    )

    /*
    Eksempel på hvordan du kunne overstyrt flere tekststiler:

    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),

    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)
