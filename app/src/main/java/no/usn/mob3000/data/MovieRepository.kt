package no.usn.mob3000.data

import no.usn.mob3000.R

// Repository som inneholder en statisk liste over filmer som vises i appen.
// Dette fungerer som en enkel "dataserver" uten backend – perfekt for et skoleprosjekt.
// UI-et kan hente data herfra for å vise liste over filmer.
object MovieRepository {

    // Liste over filmer som vises på kino akkurat nå.
    // Hver film har id, tittel, beskrivelse, visningstider og en plakat-ressurs.
    val nowShowing: List<Movie> = listOf(

        Movie(
            id = 1,
            title = "Dune: Part Two",
            description =
                "Paul Atreides fortsetter reisen sin sammen med fremmen i ørkenen på Arrakis. " +
                        "Han må velge mellom kjærligheten til Chani og ansvaret som leder for et helt folk. " +
                        "Samtidig bygger det seg opp en konflikt mellom de mektige husene som kjemper om kontroll over krydderet. " +
                        "Historien handler både om politikk, makt, skjebne og personlige ofre. " +
                        "Resultatet er en storslått sci-fi-opplevelse med store slag, visuelle effekter og et tungt moralsk dilemma.",
            showtimes = listOf("17:30", "20:15", "22:00"),
            posterResId = R.drawable.poster_dune
        ),

        Movie(
            id = 2,
            title = "Oppenheimer",
            description =
                "Oppenheimer forteller historien om fysikeren J. Robert Oppenheimer og utviklingen av atombomben under andre verdenskrig. " +
                        "Vi følger både den vitenskapelige prosessen og de etiske spørsmålene som oppstår når våpenet blir en realitet. " +
                        "Filmen viser hvordan arbeidet påvirker hans personlige liv, relasjoner og rykte. " +
                        "Etter krigen må han leve med konsekvensene av det han har vært med på å skape. " +
                        "Dette er et intenst drama om ansvar, makt og skyldfølelse, pakket inn i en dialogtung og visuelt sterk film.",
            showtimes = listOf("18:00", "21:00"),
            posterResId = R.drawable.poster_oppenheimer
        ),

        Movie(
            id = 3,
            title = "John Wick 4",
            description =
                "I John Wick 4 fortsetter John kampen mot The High Table, den organisasjonen som styrer den kriminelle underverdenen. " +
                        "Han reiser mellom flere storbyer og møter både gamle allierte og nye, farlige motstandere. " +
                        "Som alltid er innsatsen høy, og hvert møte kan bli hans siste. " +
                        "Filmen byr på ekstremt koreograferte actionsekvenser, stilfulle miljøer og et mørkt, gjennomført univers. " +
                        "Dette er en intens og energisk fortsettelse for fans av serien.",
            showtimes = listOf("19:00", "22:15"),
            posterResId = R.drawable.poster_johnwick
        ),

        Movie(
            id = 4,
            title = "Interstellar",
            description =
                "Interstellar følger en gruppe astronauter som reiser gjennom et ormehull på jakt etter et nytt hjem for menneskeheten. " +
                        "Jorden er i ferd med å bli ubeboelig, og tiden er i ferd med å renne ut. " +
                        "Hovedpersonen Cooper må balansere savnet etter familien med plikten han føler til å redde fremtiden. " +
                        "Filmen kombinerer realistisk romfart, teorier om tid og relativitet, og sterke emosjonelle øyeblikk. " +
                        "Det er en historie som både underholder og får deg til å tenke på hva vi er villige til å ofre for dem vi er glad i.",
            showtimes = listOf("16:30", "20:45"),
            posterResId = R.drawable.poster_interstellar
        )
    )
}
