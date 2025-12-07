package no.usn.mob3000.data

import androidx.annotation.DrawableRes

// Data-klasse som representerer en film i appen.
// Denne brukes kun lokalt i appen (ikke lagret i Room-databasen).
data class Movie(

    // Unik ID for filmen (brukes til identifisering i lister).
    val id: Int,

    // Filmtittel som vises i UI.
    val title: String,

    // Kort beskrivelse av filmen.
    val description: String,

    // Liste med tidspunkt der filmen vises (eks. "18:00", "20:30").
    val showtimes: List<String>,

    // Resource ID for filmens plakatbilde.
    // @DrawableRes sikrer at vi kun bruker gyldige drawable-ressurser.
    @DrawableRes val posterResId: Int
)
