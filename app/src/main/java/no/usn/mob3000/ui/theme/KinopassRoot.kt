package no.usn.mob3000.ui.theme

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import no.usn.mob3000.R
import no.usn.mob3000.ui.auth.LoginScreen
import no.usn.mob3000.ui.home.HomeScreen
import no.usn.mob3000.ui.tickets.TicketsScreen

// Representerer toppnivå-skjermene i appen (bunn-navigasjonen).
// Hver destination har en route, tittel (string-resurs) og ikon (drawable-resurs).
private sealed class MainDestination(
    val route: String,
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int
) {
    object Home : MainDestination(
        route = "home",
        labelRes = R.string.nav_home,
        iconRes = R.drawable.ic_home
    )

    object Tickets : MainDestination(
        route = "tickets",
        labelRes = R.string.nav_tickets,
        iconRes = R.drawable.ic_tickets
    )
}

// Liste over alle toppnivå-destinasjoner i bunn-navigasjonen.
private val mainDestinations = listOf(
    MainDestination.Home,
    MainDestination.Tickets
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KinopassRoot() {
    // NavController styrer all Compose-navigasjon i appen.
    val navController = rememberNavController()
    val ctx = LocalContext.current

    // Enkel state for om brukeren er logget inn eller ikke.
    var isLoggedIn by rememberSaveable { mutableStateOf(false) }

    // Hvis bruker prøver å gå til f.eks. Tickets uten å være logget inn,
    // lagres ønsket route her slik at vi kan navigere dit etter innlogging.
    var pendingRouteAfterLogin by rememberSaveable { mutableStateOf<String?>(null) }

    // Brukes til å finne nåværende destinasjon for å markere riktig tab og toppbar.
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        // Topp-appbar vises bare på Home (ikke på login eller andre skjermbilder).
        topBar = {
            if (currentDestination.isTopLevelDestinationInHierarchy(MainDestination.Home.route)) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_logo),
                                contentDescription = "Kinopass-logo"
                            )
                            Text(
                                text = "Kinopass",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    },
                    actions = {
                        // Knapp for å logge inn / logge ut i topbaren.
                        Button(
                            onClick = {
                                if (isLoggedIn) {
                                    // Logg ut: nullstill state og gå til Home.
                                    isLoggedIn = false
                                    pendingRouteAfterLogin = null

                                    Toast.makeText(
                                        ctx,
                                        "Du er nå logget ut.",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    navController.navigate(MainDestination.Home.route) {
                                        popUpTo(MainDestination.Home.route) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                } else {
                                    // Ikke logget inn → åpne login-skjerm fra Home.
                                    pendingRouteAfterLogin = MainDestination.Home.route
                                    navController.navigate("login")
                                }
                            }
                        ) {
                            Text(text = if (isLoggedIn) "Logg ut" else "Logg inn")
                        }
                    }
                )
            }
        },
        // Bunn-navigasjon som alltid vises (Home + Tickets).
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                mainDestinations.forEach { dest ->
                    val selected = currentDestination.isTopLevelDestinationInHierarchy(dest.route)

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                // Naviger til valgt toppnivå-side og bevar state der det er mulig.
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = dest.iconRes),
                                contentDescription = stringResource(id = dest.labelRes)
                            )
                        },
                        label = {
                            Text(text = stringResource(id = dest.labelRes))
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // NavHost definerer alle skjermene (routes) i appen og hvordan man navigerer mellom dem.
        NavHost(
            navController = navController,
            startDestination = MainDestination.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // -----------------------
            //  HJEM-SKJERM
            // -----------------------
            composable(MainDestination.Home.route) {
                HomeScreen(
                    isLoggedIn = isLoggedIn,
                    onRequireLogin = {
                        // Bruker prøver å kjøpe billett fra Home uten å være logget inn.
                        pendingRouteAfterLogin = MainDestination.Home.route
                        navController.navigate("login")
                    }
                )
            }

            // -----------------------
            //  BILLETT-SKJERM
            // -----------------------
            composable(MainDestination.Tickets.route) {
                if (isLoggedIn) {
                    // Hvis brukeren er logget inn → vis billettoversikten.
                    TicketsScreen()
                } else {
                    // Ikke logget inn → vis Login direkte i Tickets-ruta.
                    LoginScreen(
                        onLoginSuccess = {
                            isLoggedIn = true
                            // Etter innlogging her blir brukeren værende på Tickets-fanen.
                        },
                        onCancel = {
                            // Avbryt login fra Tickets → gå tilbake til Home.
                            pendingRouteAfterLogin = null
                            navController.navigate(MainDestination.Home.route) {
                                popUpTo(MainDestination.Home.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            // -----------------------
            //  EGEN LOGIN-ROUTE
            //  (Brukes især fra Home / kjøpsflyt)
            // -----------------------
            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        isLoggedIn = true

                        // Gå til ruten brukeren opprinnelig prøvde å åpne,
                        // ellers tilbake til Home.
                        val target = pendingRouteAfterLogin ?: MainDestination.Home.route
                        pendingRouteAfterLogin = null

                        navController.navigate(target) {
                            popUpTo(MainDestination.Home.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onCancel = {
                        // Avbryter login → gå tilbake til Home og nullstill pending route.
                        pendingRouteAfterLogin = null
                        navController.navigate(MainDestination.Home.route) {
                            popUpTo(MainDestination.Home.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}

// Hjelpefunksjon for å sjekke om currentDestination er en del av en gitt toppnivå-route.
// Brukes for å avgjøre om vi skal vise topbaren og markere riktig tab.
private fun NavDestination?.isTopLevelDestinationInHierarchy(route: String): Boolean {
    return this?.hierarchy?.any { it.route == route } == true
}
