package no.usn.mob3000.ui.seats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Representerer et sete i salen: rad, nummer og om det er opptatt eller ikke.
data class Seat(
    val row: Int,
    val number: Int,
    val isTaken: Boolean
)

// Praktisk label som brukes både i UI og ved lagring i databasen.
fun Seat.label(): String = "Rad $row, sete $number"

/**
 * Lager sete-layout basert på hvilke seter som er opptatt.
 *
 * takenSeatLabels inneholder strenger som "Rad 2, sete 4"
 * og brukes til å markere seter som allerede er bestilt.
 */
fun defaultSeatLayout(takenSeatLabels: Set<String>): List<List<Seat>> {
    val rows = 1..3          // 3 rader i salen
    val numbers = 1..6       // 6 seter per rad

    return rows.map { r ->
        numbers.map { n ->
            val lbl = "Rad $r, sete $n"
            Seat(
                row = r,
                number = n,
                isTaken = takenSeatLabels.contains(lbl) // true hvis sete allerede er tatt
            )
        }
    }
}

// Dialog som lar brukeren velge sete til en bestemt film og visningstid.
// Håndterer både layout, valgt sete og bekreftelse/avbryt.
@Composable
fun SeatPickerDialog(
    movieTitle: String,
    showtime: String,
    takenSeatLabels: Set<String>,
    onDismiss: () -> Unit,
    onSeatConfirmed: (Seat) -> Unit
) {
    // Genererer sete-layout én gang per endring i takenSeatLabels.
    val layout = remember(takenSeatLabels) { defaultSeatLayout(takenSeatLabels) }

    // Holder styr på hvilket sete brukeren har valgt i dialogen.
    var selectedSeat by remember { mutableStateOf<Seat?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Velg sete",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Viser hvilken film og tidspunkt denne bestillingen gjelder.
                Text(
                    text = "\"$movieTitle\" kl. $showtime",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(4.dp))

                // Enkel markering av hvor lerretet er i salen.
                Text(
                    text = "Lerret",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 4.dp, bottom = 12.dp)
                )

                // Selve sete-gridet (rad for rad).
                layout.forEach { row ->
                    Row(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { seat ->
                            SeatBox(
                                seat = seat,
                                isSelected = selectedSeat == seat,
                                onClick = {
                                    // Bare tillat valg av sete som ikke er opptatt.
                                    if (!seat.isTaken) {
                                        selectedSeat = seat
                                    }
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.height(4.dp))

                // Forklaring på fargekodene (ledig, valgt, opptatt).
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    LegendDot(
                        color = MaterialTheme.colorScheme.primary,
                        label = "Ledig"
                    )
                    LegendDot(
                        color = MaterialTheme.colorScheme.secondary,
                        label = "Valgt"
                    )
                    LegendDot(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        label = "Opptatt"
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Viser hvilket sete som er valgt akkurat nå.
                selectedSeat?.let {
                    Text(
                        text = "Valgt sete: ${it.label()}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Sender valgt sete tilbake til kallende skjerm (Home/MovieDetails).
                    selectedSeat?.let(onSeatConfirmed)
                },
                enabled = selectedSeat != null   // Kan ikke bekrefte uten å ha valgt sete.
            ) {
                Text("Bekreft sete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Avbryt")
            }
        }
    )
}

// Tegner et enkelt sete som en boks med farge basert på status (ledig/valgt/opptatt).
@Composable
private fun SeatBox(
    seat: Seat,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = when {
        seat.isTaken -> MaterialTheme.colorScheme.surfaceVariant   // Opptatte seter
        isSelected -> MaterialTheme.colorScheme.secondary          // Valgt sete
        else -> MaterialTheme.colorScheme.primary                  // Ledige seter
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color)
            .clickable(enabled = !seat.isTaken, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${seat.row}/${seat.number}",          // Enkel visning av posisjon (rad/nummer)
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

// Liten "prikk + tekst" brukt i legend for å forklare fargene.
@Composable
private fun LegendDot(
    color: androidx.compose.ui.graphics.Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 11.sp)
    }
}
