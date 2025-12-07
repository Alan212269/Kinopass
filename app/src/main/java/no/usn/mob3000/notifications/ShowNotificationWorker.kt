package no.usn.mob3000.notifications

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

// Worker som kjører i bakgrunnen via WorkManager.
// Brukes til å vise et varsel uavhengig av om appen er åpen.
// CoroutineWorker gjør at jobben kan kjøres asynkront uten å blokkere tråder.
class ShowNotificationWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {

        // Data sendt inn når jobben ble planlagt (f.eks. film, sete og tid).
        val title = inputData.getString("title") ?: "Movie"
        val seat = inputData.getString("seat") ?: "Seat"
        val time = inputData.getString("time") ?: ""

        // Sørger for at riktig notifikasjonskanal finnes før varselet vises.
        NotificationHelper.ensureChannel(applicationContext)

        // Tekst som vises i varselet.
        val text = "Starts at $time • Seat $seat"

        // Bygger varselet som skal vises til brukeren.
        val notification = NotificationCompat.Builder(applicationContext, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Standard ikon, tilgjengelig på alle enheter
            .setContentTitle("KinoPass: $title")            // Overskrift i varselet
            .setContentText(text)                           // Kort tekst
            .setStyle(NotificationCompat.BigTextStyle().bigText(text)) // Utvidet tekst når brukeren åpner varselet
            .setAutoCancel(true)                            // Fjernes når brukeren trykker på varselet
            .build()

        // Viser varselet via NotificationManager.
        NotificationManagerCompat.from(applicationContext)
            .notify(System.currentTimeMillis().toInt(), notification)

        // Signalerer at arbeidet ble vellykket utført.
        return Result.success()
    }
}
