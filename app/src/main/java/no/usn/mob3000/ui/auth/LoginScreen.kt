package no.usn.mob3000.ui.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.usn.mob3000.R

// Skjermbilde for innlogging, registrering og "glemt passord".
// onLoginSuccess kalles når brukeren har logget inn, onCancel går tilbake til forrige skjerm.
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val ctx = LocalContext.current

    // State for innloggingsfelt.
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    // State for registreringsdialog.
    var showRegisterDialog by rememberSaveable { mutableStateOf(false) }
    var regEmail by rememberSaveable { mutableStateOf("") }
    var regPassword by rememberSaveable { mutableStateOf("") }
    var regConfirm by rememberSaveable { mutableStateOf("") }
    var regError by rememberSaveable { mutableStateOf<String?>(null) }

    // State for "glemt passord"-dialog.
    var showForgotDialog by rememberSaveable { mutableStateOf(false) }
    var forgotEmail by rememberSaveable { mutableStateOf("") }
    var forgotError by rememberSaveable { mutableStateOf<String?>(null) }

    // Funksjon som sjekker innloggingsdata mot demo-bruker, testbruker og registrert bruker.
    fun tryLogin() {
        if (isLoading) return   // Hindrer at brukeren trykker flere ganger mens vi "jobber".

        isLoading = true
        errorMessage = null

        val trimmedEmail = email.trim()

        // 1) Demo-bruker: fast e-post og passord kun for testing.
        val okDemoEmail = trimmedEmail.equals("demo@kino.no", ignoreCase = true)
        val okDemoPass = password == "passord123"

        // 2) Testbruker fra oppgaven (brukernavn + passord).
        val okTestUser = trimmedEmail.equals("user1", ignoreCase = true) && password == "pass123"

        // 3) Registrert bruker lagret lokalt i AuthStorage (SharedPreferences).
        val stored = AuthStorage.getUser(ctx)
        val okRegistered = stored?.let { (e, p) ->
            trimmedEmail.equals(e, ignoreCase = true) && password == p
        } ?: false

        // Hvis en av de tre sjekkene lykkes, logges brukeren inn.
        if ((okDemoEmail && okDemoPass) || okTestUser || okRegistered) {
            Toast.makeText(ctx, "Innlogging vellykket", Toast.LENGTH_SHORT).show()
            onLoginSuccess()
        } else {
            // Viser feilmelding under passordfeltet.
            errorMessage = "Feil e-post eller passord."
        }

        isLoading = false
    }

    // Hovedkolonnen for innloggingsskjermen.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(80.dp))

        // App-logo øverst på skjermen.
        Image(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = "Kinopass logo",
            modifier = Modifier.height(120.dp)
        )

        Spacer(Modifier.height(12.dp))

        // App-navn under logo.
        Text(
            text = "Kinopass",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(Modifier.height(16.dp))

        // Tittel for innloggingsdelen.
        Text(
            text = "Logg inn",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            )
        )

        Spacer(Modifier.height(32.dp))

        // Felt for e-post / brukernavn.
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("E-post / brukernavn") },
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Email
            )
        )

        Spacer(Modifier.height(16.dp))

        // Felt for passord (skjult med PasswordVisualTransformation).
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Passord") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Password
            )
        )

        // Viser feilmelding under passordfeltet hvis innlogging feilet.
        if (errorMessage != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(16.dp))

        // Innloggingsknapp – viser "Logger inn..." hvis isLoading = true.
        Button(
            onClick = { tryLogin() },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isLoading) "Logger inn..." else "Logg inn",
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.height(8.dp))

        // "Glemt passord?" åpner egen dialog (demo-funksjonalitet).
        TextButton(onClick = {
            showForgotDialog = true
            forgotEmail = email
            forgotError = null
        }) { Text("Glemt passord?") }

        // "Registrer deg" åpner dialog for opprettelse av ny bruker.
        TextButton(onClick = { showRegisterDialog = true }) {
            Text("Registrer deg")
        }

        Spacer(Modifier.height(16.dp))

        // Avbryt-knapp som går tilbake til forrige skjerm.
        TextButton(onClick = onCancel) {
            Text("Avbryt")
        }
    }

    // Registreringsdialog: lar brukeren opprette en ny lokal bruker (lagres i AuthStorage).
    if (showRegisterDialog) {
        AlertDialog(
            onDismissRequest = { showRegisterDialog = false },
            title = { Text("Registrer ny bruker") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = regEmail,
                        onValueChange = { regEmail = it },
                        label = { Text("E-post") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = regPassword,
                        onValueChange = { regPassword = it },
                        label = { Text("Passord") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation()
                    )
                    OutlinedTextField(
                        value = regConfirm,
                        onValueChange = { regConfirm = it },
                        label = { Text("Gjenta passord") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation()
                    )
                    // Viser valideringsfeil hvis noe er galt i registreringsskjemaet.
                    regError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val e = regEmail.trim()
                    val p = regPassword
                    val c = regConfirm

                    // Enkel validering av registreringsdata før lagring.
                    when {
                        e.isBlank() || p.isBlank() || c.isBlank() ->
                            regError = "Fyll ut alle feltene."
                        !e.contains("@") ->
                            regError = "E-posten ser ikke gyldig ut."
                        p.length < 6 ->
                            regError = "Passord må være minst 6 tegn."
                        p != c ->
                            regError = "Passordene er ikke like."
                        else -> {
                            // Lagre bruker lokalt og forhåndsfyll innloggingsfeltene.
                            AuthStorage.saveUser(ctx, e, p)
                            Toast.makeText(ctx, "Bruker registrert.", Toast.LENGTH_LONG).show()
                            email = e
                            password = p
                            regError = null
                            showRegisterDialog = false
                        }
                    }
                }) {
                    Text("Lagre")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRegisterDialog = false }) {
                    Text("Avbryt")
                }
            }
        )
    }

    // "Glemt passord"-dialog – viser demo-løsning (ingen ekte e-post sendes).
    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = { Text("Glemt passord") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Oppdatert tekst: støtter både e-post og brukernavn (f.eks. user1).
                    Text("Skriv inn e-post eller brukernavn. Vi sender en lenke (demo).")
                    OutlinedTextField(
                        value = forgotEmail,
                        onValueChange = { forgotEmail = it },
                        label = { Text("E-post / brukernavn") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    forgotError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val e = forgotEmail.trim()

                    // Utvidet validering:
                    //  - godtar vanlig e-post (inneholder "@")
                    //  - godtar demo-bruker "user1"
                    //  - godtar e-post som matcher registrert bruker i AuthStorage
                    val isEmail = e.contains("@")
                    val isDemoUser = e.equals("user1", ignoreCase = true)
                    val stored = AuthStorage.getUser(ctx)
                    val isStoredEmail = stored?.first?.equals(e, ignoreCase = true) == true

                    when {
                        e.isBlank() ->
                            forgotError = "Feltet kan ikke være tomt."
                        !isEmail && !isDemoUser && !isStoredEmail ->
                            forgotError = "Skriv inn gyldig e-post eller brukernavn (f.eks. demo@kino.no eller user1)."
                        else -> {
                            // Demo: vi later som vi har sendt en e-post.
                            forgotError = null
                            showForgotDialog = false
                            Toast.makeText(ctx, "E-post sendt (demo).", Toast.LENGTH_LONG).show()
                        }
                    }
                }) {
                    Text("Send lenke")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotDialog = false }) {
                    Text("Avbryt")
                }
            }
        )
    }
}
