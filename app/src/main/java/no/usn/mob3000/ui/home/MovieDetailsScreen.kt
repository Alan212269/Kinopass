package no.usn.mob3000.ui.home

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import no.usn.mob3000.data.Movie
import no.usn.mob3000.data.local.entity.Ticket
import no.usn.mob3000.ui.seats.SeatPickerDialog
import no.usn.mob3000.ui.seats.label
import no.usn.mob3000.ui.tickets.TicketsViewModel
import no.usn.mob3000.ui.tickets.TicketsVmFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Skjerm for å vise detaljer om en film: bilde, beskrivelse, visningstider og mulighet for setevalg.
// onBack brukes for å gå tilbake til forrige skjerm (HomeScreen).
@Composable
fun MovieDetailsScreen(
    movie: Movie,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as Application

    // Henter TicketsViewModel slik at billetter kan lagres direkte fra denne skjermen.
    val ticketsVm: TicketsViewModel = viewModel(factory = TicketsVmFactory(app))
    val allTickets: List<Ticket> by ticketsVm.tickets.collectAsState()

    val scrollState = rememberScrollState()

    // State: hvilket tidspunkt brukeren har valgt for setevelger-dialogen.
    var seatShowtime by remember { mutableStateOf<String?>(null) }

    // Henter hvilke seter som allerede er bestilt for dette klokkeslettet.
    fun takenSeatsFor(showtime: String): Set<String> {
        return allTickets
            .filter { t ->
                t.movieTitle == movie.title &&
                        t.time.endsWith(" $showtime")
            }
            .map { it.seat }
            .toSet()
    }

    // ---------------------------
    //  Hovedinnholdet i skjermen
    // ---------------------------
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Tilbake-knapp
        TextButton(onClick = onBack) {
            Text(text = "← Tilbake", fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Kort med bilde og detaljer om filmen
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Filmplakat
                Image(
                    painter = painterResource(id = movie.posterResId),
                    contentDescription = movie.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Filmtittel
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Beskrivelse
                Text(
                    text = movie.description,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Visningstider med bestillingsknapper
                Text(
                    text = "Visningstider",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    movie.showtimes.forEach { time ->
                        Button(
                            onClick = {
                                // Bruker trykker på et visningstidspunkt → åpner setevelger.
                                seatShowtime = time
                            },
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(text = time, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // -------------------------------
    //  Setevelger-dialog for bestilling
    // -------------------------------
    val activeShowtime = seatShowtime
    if (activeShowtime != null) {

        // Henter opptatte seter for valgt visning.
        val taken = takenSeatsFor(activeShowtime)

        SeatPickerDialog(
            movieTitle = movie.title,
            showtime = activeShowtime,
            takenSeatLabels = taken,
            onDismiss = { seatShowtime = null },

            // Når brukeren bekrefter sete:
            onSeatConfirmed = { seat ->
                val seatLabel = seat.label()

                // Lager et datoformat som harmonerer med hvordan billetter lagres.
                val today = LocalDate.now()
                val dateStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val dateTimeStr = "$dateStr $activeShowtime"

                // Lagrer billetten i Room-databasen via ViewModel.
                ticketsVm.add(
                    title = movie.title,
                    seat = seatLabel,
                    time = dateTimeStr
                )

                // Bekreftelsesmelding til brukeren.
                Toast.makeText(
                    ctx,
                    "Billett bestilt: ${movie.title} – $dateTimeStr – $seatLabel",
                    Toast.LENGTH_LONG
                ).show()

                // Steng dialogen etter bestilling.
                seatShowtime = null
            }
        )
    }
}
