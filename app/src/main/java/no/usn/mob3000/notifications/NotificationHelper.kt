package no.usn.mob3000.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

// Helper-klasse som sikrer at varslingskanalen for påminnelser (10-min før film)
// eksisterer før appen sender varsler. Android 8+ krever kanaler.
object NotificationHelper {

    // ID for påminnelseskanalen – brukes når varsler sendes via Worker eller BroadcastReceiver.
    const val CHANNEL_ID = "kinopass_reminders"

    // Navnet som vises i Androids systeminnstillinger.
    private const val CHANNEL_NAME = "KinoPass Reminders"

    // Oppretter varslingskanalen hvis den ikke finnes fra før.
    fun ensureChannel(context: Context) {

        // Notification channels er kun nødvendig på Android 8.0+ (API 26).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Sjekker om kanalen finnes. Hvis ikke, opprettes den.
            if (mgr.getNotificationChannel(CHANNEL_ID) == null) {

                // Oppretter en kanal med standard importance – passende for påminnelser.
                val ch = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                )

                mgr.createNotificationChannel(ch)
            }
        }
    }
}
