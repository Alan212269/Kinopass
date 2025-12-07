package no.usn.mob3000.ui.tickets

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.launch
import no.usn.mob3000.R
import no.usn.mob3000.data.local.entity.Ticket

// Skjermen som viser alle lagrede billetter.
// Her kan brukeren:
//  - Søke i billetter
//  - Se detaljer
//  - Generere QR-kode
//  - Slette billetter
@Composable
fun TicketsScreen() {
    val app = LocalContext.current.applicationContext as Application
    val vm: TicketsViewModel = viewModel(factory = TicketsVmFactory(app))
    val allTickets: List<Ticket> by vm.tickets.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Søkestreng for filtrering.
    var query by rememberSaveable { mutableStateOf("") }

    // Beregner filtrert liste basert på søketekst.
    val list = remember(allTickets, query) {
        if (query.isBlank()) {
            allTickets
        } else {
            allTickets.filter {
                it.movieTitle.contains(query, ignoreCase = true) ||
                        it.seat.contains(query, ignoreCase = true) ||
                        it.time.contains(query, ignoreCase = true)
            }
        }
    }

    // Hvilken billett det skal genereres QR-kode for.
    var qrFor by rememberSaveable { mutableStateOf<Ticket?>(null) }

    // Hvilken billett som er valgt for sletting (bekreftes i dialog).
    var pendingDelete by rememberSaveable { mutableStateOf<Ticket?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Topptekst: viser antall billetter.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Billetter (${allTickets.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Søkefelt for tittel/sete/tid.
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(painterResource(R.drawable.ic_search), null) },
                placeholder = { Text("Søk etter tittel, sete eller tid…") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Selve listen med billetter, eller tom-tekst hvis ingen.
            if (list.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Ingen billetter å vise.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = list, key = { it.id }) { t ->
                        TicketCard(
                            ticket = t,
                            onShowQr = { qrFor = t },
                            onDelete = { pendingDelete = t }
                        )
                    }
                }
            }
        }

        // -------------------------
        //  QR-DIALOG FOR BILLETT
        // -------------------------
        qrFor?.let { t ->
            AlertDialog(
                onDismissRequest = { qrFor = null },
                title = { Text(t.movieTitle) },
                text = {
                    // Enkel tekstpayload som kan brukes til skanning i døra.
                    val payload = "ticket:${t.id}|${t.movieTitle}|${t.time}|${t.seat}"

                    // Genererer QR-bilde som Bitmap.
                    val bmp = createQr(payload, sizePx = 700)

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            bmp.asImageBitmap(),
                            contentDescription = "QR-kode"
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Vis denne i døra. Sete: ${t.seat}  •  Tid: ${t.time}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { qrFor = null }) {
                        Text("Lukk")
                    }
                }
            )
        }

        // -------------------------
        //  SLETTE-BEKREFTELSE
        // -------------------------
        pendingDelete?.let { t ->
            AlertDialog(
                onDismissRequest = { pendingDelete = null },
                title = { Text("Slette billett?") },
                text = { Text("Dette kan ikke angres.\n${t.movieTitle} – ${t.time}") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // Sletter billett via ViewModel og viser snackbar.
                            vm.delete(t)
                            pendingDelete = null
                            scope.launch {
                                snackbarHostState.showSnackbar("Billett slettet.")
                            }
                        }
                    ) {
                        Text("Slett")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingDelete = null }) {
                        Text("Avbryt")
                    }
                }
            )
        }
    }
}

// Enkelt kort som viser én billett i listen.
// Viser film, sete, tid og knapper for QR + slett.
@Composable
private fun TicketCard(
    ticket: Ticket,
    onShowQr: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_tickets),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(ticket.movieTitle, style = MaterialTheme.typography.titleMedium)
                Text("Sete: ${ticket.seat}", style = MaterialTheme.typography.bodyMedium)
                Text("Tid: ${ticket.time}", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.width(8.dp))

            TextButton(onClick = onShowQr) { Text("QR") }
            TextButton(onClick = onDelete) { Text("Slett") }
        }
    }
}

/** Enkel QR-generator basert på ZXing-biblioteket. */
private fun createQr(content: String, sizePx: Int): Bitmap {
    val hints = mapOf(EncodeHintType.MARGIN to 0)
    val bits = QRCodeWriter()
        .encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)

    val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    for (x in 0 until sizePx) {
        for (y in 0 until sizePx) {
            bmp.setPixel(
                x,
                y,
                if (bits[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
            )
        }
    }
    return bmp
}
