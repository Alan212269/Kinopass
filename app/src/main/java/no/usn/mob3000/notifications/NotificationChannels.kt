package no.usn.mob3000.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

// Håndterer opprettelse av notification-kanalen som brukes til varsler i appen.
// På Android 8+ (API 26) MÅ alle varsler knyttes til en kanal.
object NotificationChannels {

    // Fast ID som brukes når appen sender varsler.
    const val CHANNEL_ID = "kinopass_channel"

    // Sørger for at kanalen eksisterer før vi sender et varsel.
    // Hvis den allerede finnes, blir den ikke opprettet på nytt.
    fun ensure(context: Context) {
        // Notification channels kreves kun fra Android Oreo (API 26) og oppover.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // Henter systemets NotificationManager for å kunne opprette kanalen.
            val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Sjekker om kanalen finnes fra før.
            if (mgr.getNotificationChannel(CHANNEL_ID) == null) {

                // Oppretter en ny varslingskanal med høy prioritet
                // slik at filmvarsler er synlige for brukeren.
                mgr.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID,
                        "Kinopass",                       // Navn som vises i systeminnstillinger
                        NotificationManager.IMPORTANCE_HIGH
                    )
                )
            }
        }
    }
}
