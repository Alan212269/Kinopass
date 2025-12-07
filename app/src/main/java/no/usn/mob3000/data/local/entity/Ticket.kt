package no.usn.mob3000.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Entity som representerer en billett i Room-databasen.
// Room oppretter automatisk en tabell basert på denne data-klassen.
@Entity(tableName = "tickets")
data class Ticket(

    // Primærnøkkel. autoGenerate = true gjør at Room automatisk tildeler ID.
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    // Navnet på filmen brukeren har valgt.
    val movieTitle: String,

    // Seteplassering brukt ved bestilling.
    val seat: String,

    // Tidspunkt for filmvisningen (som tekstformat, f.eks. "19:30").
    val time: String
)
