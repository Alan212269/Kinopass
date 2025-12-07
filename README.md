# Kinopass 🎬

Kinopass er en Android-app for kinobilletter. Appen lar brukeren logge inn, se filmer og forestillinger, velge sete og lagre billetter lokalt på telefonen. Prosjektet er laget som en del av MOB3000 ved Universitetet i Sørøst-Norge.

## 📱 Hovedfunksjoner

- 🔐 **Innlogging**
    - Enkel innlogging med lagring av innlogget bruker lokalt (`AuthStorage`).
    - Viser ulike skjermer avhengig av om brukeren er logget inn eller ikke.

- 🎞️ **Filmliste og detaljer**
    - Viser en liste med tilgjengelige filmer på **HomeScreen**.
    - Egen detaljskjerm for hver film (`MovieDetailsScreen`) med informasjon om film og visning.

- 🎟️ **Billetter**
    - Brukeren kan bestille billetter for en visning.
    - Billetter lagres i lokal database via Room (`Ticket`, `TicketDao`, `AppDatabase`).
    - Egen side for å se alle billetter (`TicketsScreen` + `TicketsViewModel`).

- 💺 **Setevalg**
    - Enkel visning for å velge sete (`SeatPicker`).
    - Valgt sete kobles til billetten.

- 🔔 **Varsler / påminnelser**
    - Oppsett av egne varselkanaler (`NotificationChannels`).
    - Bruk av WorkManager / Alarm for påminnelse før en forestilling:
        - `NotificationHelper`
        - `ScreeningAlarmScheduler`
        - `ScreeningReminderReceiver`
        - `ShowNotificationWorker`

## 🧱 Teknologistack

- **Språk:** Kotlin
- **UI:** Jetpack Compose
- **Arkitektur:** Enkle repositories + ViewModel for billetter
- **Database:** Room (lokal SQLite)
- **Varsler:** NotificationManager, WorkManager, BroadcastReceiver
- **Byggverktøy:** Gradle (Kotlin DSL)
- **IDE:** Android Studio

## 📂 Struktur (kort oversikt)

```text
app/
 ├── src/main/java/no/usn/mob3000/
 │    ├── KinopassApp.kt          # App entry / Compose setup
 │    ├── MainActivity.kt         # Host for NavHost/Compose
 │    ├── data/
 │    │    ├── Movie.kt
 │    │    ├── MovieRepository.kt
 │    │    ├── TicketRepository.kt
 │    │    └── local/
 │    │         ├── AppDatabase.kt
 │    │         ├── dao/TicketDao.kt
 │    │         └── entity/Ticket.kt
 │    ├── nav/Routes.kt
 │    ├── notifications/
 │    ├── ui/auth/
 │    ├── ui/home/
 │    ├── ui/seats/
 │    ├── ui/theme/
 │    └── ui/tickets/
 └── res/
      ├── drawable/   # Ikoner og filmplakater
      ├── values/     # Farger, tema, tekster
      └── xml/
