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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import no.usn.mob3000.data.MovieRepository
import no.usn.mob3000.ui.seats.Seat
import no.usn.mob3000.ui.seats.SeatPickerDialog
import no.usn.mob3000.ui.seats.label
import no.usn.mob3000.ui.tickets.TicketsViewModel
import no.usn.mob3000.ui.tickets.TicketsVmFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Hovedskjermen som viser filmer, søkefelt, detaljvisning og setevelger.
// isLoggedIn avgjør om brukeren kan bestille billetter.
// onRequireLogin kjøres hvis ikke-innlogget bruker prøver å bestille.
@Composable
fun HomeScreen(
    isLoggedIn: Boolean,
    onRequireLogin: () -> Unit
) {
    val app = LocalContext.current.applicationContext as Application
    val ctx = LocalContext.current

    // Henter TicketsViewModel slik at HomeScreen kan vise/bestille billetter.
    val ticketsVm: TicketsViewModel = viewModel(factory = TicketsVmFactory(app))
    val allTickets by ticketsVm.tickets.collectAsState()

    // Søketekst for filmfiltering.
    var query by rememberSaveable { mutableStateOf("") }

    // Hvilken film som er valgt i detaljvisning.
    var selectedMovieId by rememberSaveable { mutableStateOf<Int?>(null) }

    // State for setevelgerdialog (film + tidspunkt).
    var seatPickerMovie by remember { mutableStateOf<Movie?>(null) }
    var seatPickerShowtime by remember { mutableStateOf<String?>(null) }

    // Filtrerer filmer etter søkestreng.
    val filteredMovies = remember(query) {
        MovieRepository.nowShowing.filter { movie ->
            movie.title.contains(query, ignoreCase = true)
        }
    }

    // Henter valgt film til detaljvisning.
    val selectedMovie = selectedMovieId?.let { id ->
        MovieRepository.nowShowing.firstOrNull { it.id == id }
    }

    // Hvis film er valgt → vis detaljskjerm i stedet for liste.
    if (selectedMovie != null) {
        MovieDetailsScreen(
            movie = selectedMovie,
            onBack = { selectedMovieId = null }
        )
    } else {
        // Hovedvisning: søk + liste over filmer.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Text(
                text = "På kino nå",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Søk etter film") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Viser filmene i en scroll-liste.
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredMovies, key = { it.id }) { movie ->
                    MovieCard(
                        movie = movie,
                        onBookClick = { time ->
                            if (!isLoggedIn) {
                                // Ikke logget inn → send bruker til login-skjerm.
                                onRequireLogin()
                            } else {
                                // Logget inn → åpne setevelger.
                                seatPickerMovie = movie
                                seatPickerShowtime = time
                            }
                        },
                        onDetailsClick = {
                            // Åpner detaljvisning for valgt film.
                            selectedMovieId = movie.id
                        }
                    )
                }
            }
        }
    }

    // -----------------------
    //  Setevelger-dialog
    // -----------------------
    val currentMovie = seatPickerMovie
    val currentShowtime = seatPickerShowtime

    if (currentMovie != null && currentShowtime != null) {

        // Finn opptatte seter for denne visningen ved å sjekke lagrede billetter.
        val takenSeatLabels: Set<String> = allTickets
            .filter { t ->
                t.movieTitle == currentMovie.title &&
                        t.time.endsWith(" $currentShowtime")
            }
            .map { it.seat }
            .toSet()

        SeatPickerDialog(
            movieTitle = currentMovie.title,
            showtime = currentShowtime,
            takenSeatLabels = takenSeatLabels,
            onDismiss = {
                seatPickerMovie = null
                seatPickerShowtime = null
            },
            onSeatConfirmed = { seat: Seat ->

                // Lager tidsstreng i format yyyy-MM-dd HH:mm
                val today = LocalDate.now()
                val dateStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val dateTimeStr = "$dateStr $currentShowtime"

                // Legger til billett i databasen via ViewModel.
                ticketsVm.add(
                    title = currentMovie.title,
                    seat = seat.label(),
                    time = dateTimeStr
                )

                // Viser bekreftelse.
                Toast.makeText(
                    ctx,
                    "Billett bestilt til \"${currentMovie.title}\" (${seat.label()})",
                    Toast.LENGTH_SHORT
                ).show()

                // Lukk dialogen.
                seatPickerMovie = null
                seatPickerShowtime = null
            }
        )
    }
}

// --------------------------------------
//   Kort visningskort for en film
// --------------------------------------
@Composable
private fun MovieCard(
    movie: Movie,
    onBookClick: (String) -> Unit,   // Bruker trykker på bestill-knapp for en gitt tid
    onDetailsClick: () -> Unit       // Åpner detaljskjerm
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Plakatbilde for filmen.
            Image(
                painter = painterResource(id = movie.posterResId),
                contentDescription = movie.title,
                modifier = Modifier
                    .size(width = 80.dp, height = 120.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Filmtittel
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Kort beskrivelse
                Text(
                    text = movie.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Knapp for hvert tidspunkt filmen vises.
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    movie.showtimes.forEach { time ->
                        Button(
                            onClick = { onBookClick(time) },
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(text = time, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // "Detaljer"-knapp nederst til høyre.
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onDetailsClick) {
                        Text("Detaljer")
                    }
                }
            }
        }
    }
}
