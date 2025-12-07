package no.usn.mob3000.ui.tickets

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.usn.mob3000.data.TicketRepository
import no.usn.mob3000.data.local.AppDatabase
import no.usn.mob3000.data.local.entity.Ticket
import no.usn.mob3000.notifications.ScreeningAlarmScheduler
import no.usn.mob3000.notifications.ShowNotificationWorker
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

// ViewModel for all billett-relatert logikk.
// Bruker AndroidViewModel slik at vi får tilgang til Application kontekt,
// som trengs for Room, WorkManager og AlarmManager.
class TicketsViewModel(app: Application) : AndroidViewModel(app) {

    // Oppretter Room-databasen. fallbackToDestructiveMigration gjør at
    // databasen nullstilles hvis skjemaet endres under utvikling.
    private val db = Room.databaseBuilder(
        app,
        AppDatabase::class.java,
        "kinopass.db"
    ).fallbackToDestructiveMigration().build()

    // Repository-laget som kapsler inn all database-tilgang.
    private val repo = TicketRepository(db.ticketDao())

    // Flow av alle billetter. stateIn gjør det om til StateFlow slik at UI alltid har en verdi.
    val tickets = repo.all().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000), // Automatisk stopp når UI ikke observerer
        initialValue = emptyList()
    )

    // Legger til billett i databasen + planlegger notif/varsel 10 min før filmstart.
    fun add(title: String, seat: String, time: String) = viewModelScope.launch {
        repo.add(Ticket(movieTitle = title, seat = seat, time = time))
        scheduleReminder(title, time)
    }

    // Sletter billett fra databasen.
    fun delete(ticket: Ticket) = viewModelScope.launch {
        repo.remove(ticket)
    }

    // Debug-funksjon for å teste at WorkManager-notifikasjoner fungerer.
    fun testNotification() {
        val data = Data.Builder()
            .putString("title", "Test notification")
            .putString("seat", "B12")
            .putString("time", "in 5 seconds")
            .build()

        val req = OneTimeWorkRequestBuilder<ShowNotificationWorker>()
            .setInitialDelay(5, TimeUnit.SECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(getApplication()).enqueue(req)
    }

    // Prøver først å bruke AlarmManager (eksakt alarm 10 min før filmstart).
    // Hvis det feiler pga. manglende tillatelse → fallback til WorkManager.
    private fun scheduleReminder(title: String, time: String) {
        val eventAt = parseEpochMillis(time) ?: return
        val zone = ZoneId.systemDefault()

        try {
            ScreeningAlarmScheduler.scheduleExactReminder(
                context = getApplication(),
                movieTitle = title,
                screeningEpochMillis = eventAt,
                zoneId = zone
            )
        } catch (_: SecurityException) {
            // Fallback dersom eksakt alarm ikke er tillatt (Android 13+)
            val triggerAt = eventAt - TimeUnit.MINUTES.toMillis(10)
            val delayMs = triggerAt - System.currentTimeMillis()

            if (delayMs > 0) {
                val data = Data.Builder()
                    .putString("title", title)
                    .putString("seat", "N/A")
                    .putString("time", time)
                    .build()

                val req = OneTimeWorkRequestBuilder<ShowNotificationWorker>()
                    .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                    .setInputData(data)
                    .build()

                WorkManager.getInstance(getApplication()).enqueue(req)
            }
        }
    }

    // Konverterer "yyyy-MM-dd HH:mm" eller "dd.MM.yyyy HH:mm" til epoch millis.
    // Gjør det mulig for bruker å skrive tid i forskjellige formater.
    private fun parseEpochMillis(timeStr: String): Long? {
        val patterns = listOf(
            "yyyy-MM-dd HH:mm",
            "dd.MM.yyyy HH:mm"
        )
        for (p in patterns) {
            try {
                val fmt = DateTimeFormatter.ofPattern(p)
                val ldt = LocalDateTime.parse(timeStr, fmt)
                return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (_: Exception) {
                // Ignorer og prøv neste format
            }
        }
        return null
    }
}

// ViewModelFactory slik at Compose kan opprette TicketsViewModel med Application-kontekst.
@Suppress("UNCHECKED_CAST")
class TicketsVmFactory(private val app: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TicketsViewModel(app) as T
    }
}
