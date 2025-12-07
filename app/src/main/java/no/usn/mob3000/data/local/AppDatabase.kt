package no.usn.mob3000.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import no.usn.mob3000.data.local.dao.TicketDao
import no.usn.mob3000.data.local.entity.Ticket

// Definerer Room-databasen for appen.
// Her registreres alle Entity-klasser som skal lagres i databasen.
@Database(
    entities = [Ticket::class],   // Liste over tabeller som Room skal håndtere
    version = 1,                  // Endres hvis databasen får strukturelle endringer (migrations)
    exportSchema = false          // Slår av schema-eksport siden dette er et studentprosjekt
)
abstract class AppDatabase : RoomDatabase() {

    // Gir tilgang til DAO-en for billetter.
    // Room genererer implementasjonen automatisk.
    abstract fun ticketDao(): TicketDao
}
