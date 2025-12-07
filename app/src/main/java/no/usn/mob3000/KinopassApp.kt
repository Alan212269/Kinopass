package no.usn.mob3000

import android.app.Application
import no.usn.mob3000.notifications.NotificationChannels

// Global Application-klasse for appen.
// Denne kjører før alle Activities og Composables starter.
// Perfekt sted for å initialisere ting som skal eksistere hele appens levetid.
class KinopassApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Sørger for at notifikasjonskanalen opprettes én gang ved app-start.
        // Android krever at NotificationChannels finnes før varsler kan vises (Android 8+).
        NotificationChannels.ensure(this)
    }
}
