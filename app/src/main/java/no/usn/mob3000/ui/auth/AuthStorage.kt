package no.usn.mob3000.ui.auth

import android.content.Context
import android.content.SharedPreferences

// En enkel lagringsløsning for brukerens innloggingsdata.
// SharedPreferences brukes for små, nøkkel/verdi-baserte data.
// Dette fungerer som en "fake login"-løsning i en skoleapp (ikke sikkerhetskritisk).
object AuthStorage {

    // Navn på SharedPreferences-filen der data lagres.
    private const val PREFS_NAME = "kinopass_auth"

    // Nøkler for e-post og passord.
    private const val KEY_EMAIL = "user_email"
    private const val KEY_PASSWORD = "user_password"

    // Henter SharedPreferences-instansen for denne appen.
    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Lagrer e-post og passord lokalt på enheten.
    // apply() brukes for å skrive asynkront uten å blokkere UI.
    fun saveUser(ctx: Context, email: String, password: String) {
        prefs(ctx).edit()
            .putString(KEY_EMAIL, email)
            .putString(KEY_PASSWORD, password)
            .apply()
    }

    // Leser ut lagret bruker, hvis begge feltene finnes.
    // Returnerer Pair(email, password) eller null hvis brukeren ikke er lagret.
    fun getUser(ctx: Context): Pair<String, String>? {
        val p = prefs(ctx)
        val e = p.getString(KEY_EMAIL, null)
        val pw = p.getString(KEY_PASSWORD, null)

        return if (e != null && pw != null) e to pw else null
    }
}
