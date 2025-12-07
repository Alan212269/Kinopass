package no.usn.mob3000.ui

import android.Manifest
import android.app.AlarmManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import no.usn.mob3000.ui.theme.KinopassRoot

// Hovedaktiviteten i appen – dette er inngangspunktet som starter hele UI-et.
// ComponentActivity brukes som grunnlag for Compose-baserte apper.
class MainActivity : ComponentActivity() {

    // Launcher for å be om POST_NOTIFICATIONS-permisjonen (Android 13+).
    // registerForActivityResult sørger for at resultatet håndteres automatisk.
    private val requestNotifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            // Vises hvis brukeren nekter varslings-tillatelse.
            if (!granted && Build.VERSION.SDK_INT >= 33) {
                Toast.makeText(
                    this,
                    "Varsler er av – skru på i Innstillinger",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //   1) Be om varslings-tillatelse (Android 13+)
        if (Build.VERSION.SDK_INT >= 33) {
            requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        //   2) Sjekk for 'exact alarms' (Android 12+)
        //      – kreves for å kunne planlegge presise filmvarsler
        if (Build.VERSION.SDK_INT >= 31) {
            val am = getSystemService(AlarmManager::class.java)
            if (!am.canScheduleExactAlarms()) {
                // Hvis eksakte alarmer ikke er tillatt → appen faller tilbake til WorkManager.
                Toast.makeText(
                    this,
                    "Exact alarms er av – slå på i Innstillinger for presise varsler",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        //   3) Start hele Compose-UI-et
        setContent {
            // KinopassRoot definerer navigasjon, tema og hele appens struktur.
            KinopassRoot()
        }
    }
}
