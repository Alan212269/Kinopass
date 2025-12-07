package no.usn.mob3000.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.abs

// Ansvar: Planlegger en nøyaktig alarm som utløser et varsel 10 minutter før filmstart.
// Denne brukes av UI-et når brukeren legger inn en ny billett.
object ScreeningAlarmScheduler {

    fun scheduleExactReminder(
        context: Context,
        movieTitle: String,
        screeningEpochMillis: Long,
        zoneId: ZoneId
    ) {
        // Konverterer filmstart (epoch milliseconds) til et lokalt klokkeslett.
        val screeningZdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(screeningEpochMillis), zoneId)

        // Alarmen skal trigges 10 minutter før visningen.
        val triggerAt = screeningZdt.minusMinutes(10).toInstant().toEpochMilli()

        // Hvis tidspunktet allerede har passert, skal vi ikke sette alarm.
        if (triggerAt <= System.currentTimeMillis()) return

        // Lager en stabil og unik requestCode basert på tidspunkt + filmtittel.
        // Dette hindrer kollisjoner mellom flere alarmer.
        val base = (screeningEpochMillis % Int.MAX_VALUE).toInt()
        val requestCode = abs(base xor movieTitle.hashCode())

        // Intent sendes til BroadcastReceiver som bygger og viser varselet.
        val i = Intent(context, ScreeningReminderReceiver::class.java).apply {
            putExtra("title", movieTitle)
            putExtra("start", screeningZdt.toLocalTime().toString())
            putExtra("requestCode", requestCode)
        }

        // PendingIntent som AlarmManager bruker for å trigge varslet.
        val pi = PendingIntent.getBroadcast(
            context,
            requestCode,
            i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Henter AlarmManager fra systemet.
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Setter en nøyaktig alarm, selv om telefonen er i doze-mode.
        // Brukes for å sikre at brukeren får varsel før filmstart.
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
    }
}
