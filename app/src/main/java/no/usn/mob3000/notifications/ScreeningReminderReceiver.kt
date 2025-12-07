package no.usn.mob3000.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import no.usn.mob3000.R

// BroadcastReceiver som trigges når AlarmManager utløser en alarm.
// Denne bygger og viser selve varselet til brukeren.
class ScreeningReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        // Sørger for at korrekt notifikasjonskanal eksisterer (Android 8+ krever dette).
        NotificationChannels.ensure(context)

        // Henter data sendt fra alarmen: filmtittel, starttid og unik requestCode.
        val movie = intent.getStringExtra("title") ?: "Kinopass"
        val start = intent.getStringExtra("start") ?: ""
        val id = intent.getIntExtra("requestCode", (System.currentTimeMillis() % 100000).toInt())

        // Lager selve varselet som brukeren ser.
        val notification = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)        // App-ikon (trygt å bruke i alle prosjekter)
            .setContentTitle("Film starter snart")     // Overskrift i varselet
            .setContentText("$movie starter kl. $start")  // Tekst i varselet
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Viktig varsel (vises tydelig)
            .setAutoCancel(true)                       // Fjernes når brukeren trykker på det
            .build()

        // Sender varselet til systemet slik at det vises på telefonen.
        NotificationManagerCompat.from(context).notify(id, notification)
    }
}
