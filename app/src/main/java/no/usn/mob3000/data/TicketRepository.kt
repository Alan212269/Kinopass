package no.usn.mob3000.data

import kotlinx.coroutines.flow.Flow
import no.usn.mob3000.data.local.dao.TicketDao
import no.usn.mob3000.data.local.entity.Ticket

// Repository fungerer som et mellomlag mellom ViewModel og databasen (DAO).
// Dette gir en renere arkitektur, enklere testing og bedre separasjon av ansvar.
class TicketRepository(private val dao: TicketDao) {

    // Returnerer en Flow med alle billetter.
    // ViewModel og UI kan observere denne for å automatisk oppdatere visningen.
    fun all(): Flow<List<Ticket>> = dao.getAll()

    // Legger til en ny billett i databasen via DAO.
    // Suspend gjør at operasjonen kjøres i en coroutine (ikke blokkerer UI).
    suspend fun add(ticket: Ticket) = dao.insert(ticket)

    // Fjerner en billett fra databasen.
    suspend fun remove(ticket: Ticket) = dao.delete(ticket)
}
