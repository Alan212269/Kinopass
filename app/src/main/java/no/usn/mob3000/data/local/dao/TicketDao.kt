package no.usn.mob3000.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import no.usn.mob3000.data.local.entity.Ticket

// DAO (Data Access Object) for å hente og endre billettdata i Room-databasen.
// Room genererer automatisk implementasjonen basert på disse metodene.
@Dao
interface TicketDao {

    // Henter alle billetter fra databasen som en Flow.
    // Flow brukes slik at UI automatisk oppdateres når data endres.
    @Query("SELECT * FROM tickets ORDER BY id DESC")
    fun getAll(): Flow<List<Ticket>>

    // Legger til en ny billett i databasen.
    // Suspend gjør at operasjonen kjøres i en coroutine (ikke på UI-tråden).
    @Insert
    suspend fun insert(ticket: Ticket): Long

    // Sletter en billett basert på Ticket-objektet.
    // Room bruker primærnøkkelen i Ticket for å finne riktig rad.
    @Delete
    suspend fun delete(ticket: Ticket)
}
