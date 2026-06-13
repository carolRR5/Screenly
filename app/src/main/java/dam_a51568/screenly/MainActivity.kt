package dam_a51568.screenly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import dam_a51568.screenly.data.model.WatchStatus
import dam_a51568.screenly.ui.browse.BrowseFilter
import dam_a51568.screenly.ui.browse.BrowseResultsScreen
import dam_a51568.screenly.ui.browse.CountryScreen
import dam_a51568.screenly.ui.browse.GenreScreen
import dam_a51568.screenly.ui.details.DetailScreen
import dam_a51568.screenly.ui.home.HomeScreen
import dam_a51568.screenly.ui.lists.ListsScreen
import dam_a51568.screenly.ui.profile.ProfileScreen
import dam_a51568.screenly.ui.search.SearchScreen
import dam_a51568.screenly.ui.settings.SettingsScreen
import dam_a51568.screenly.ui.theme.BackgroundDark
import dam_a51568.screenly.ui.theme.BrandPurple
import dam_a51568.screenly.ui.theme.TextSecondary

/**
 * Destinos de navegação da aplicação.
 * Cada objeto representa uma rota única no grafo de navegação.
 */
sealed class Screen(val route: String) {
    // Ecrã principal com títulos em tendência.
    data object Home : Screen("home")

    // Ecrã de pesquisa de filmes e séries.
    data object Search : Screen("search")

    // Ecrã de perfil do utilizador.
    data object Profile : Screen("profile")

    // Ecrã de detalhes de um título, recebe id e mediaType como argumentos.
    data object Detail : Screen("detail/{id}/{mediaType}") {
        fun createRoute(id: Int, mediaType: String) = "detail/$id/$mediaType"
    }

    // Ecrã de listas do utilizador — recebe o estado inicial do separador.
    data object Lists : Screen("lists/{status}") {
        fun createRoute(status: WatchStatus) = "lists/${status.name}"
    }

    // Ecrã de definições do utilizador.
    data object Settings : Screen("settings")

    // Ecrã de seleção de género.
    data object Genre : Screen("genre")

    // Ecrã de seleção de país.
    data object Country : Screen("country")

    // Ecrã de resultados de navegação por categoria, género ou país.
    data object BrowseResults : Screen("browse/{filter}/{title}/{extra}") {
        fun createRoute(
            filter: BrowseFilter,
            title: String,
            extra: String = "none"
        ) = "browse/${filter.name}/$title/$extra"
    }
}

/**
 * Representa um item da Bottom Navigation Bar.
 *
 * @param screen Destino de navegação associado.
 * @param label Texto apresentado por baixo do ícone.
 * @param icon Ícone do separador.
 */
data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

/**
 * Activity principal da aplicação Screenly.
 *
 * É o ponto de entrada do fluxo principal (após autenticação).
 * Configura o Compose Navigation com a Bottom Bar e define o grafo
 * de navegação entre todos os ecrãs principais da aplicação.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScreenlyApp()
        }
    }
}

/**
 * Composable raiz da aplicação.
 * Configura o NavHost com todos os destinos e a Bottom Navigation Bar.
 */
@Composable
fun ScreenlyApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem(screen = Screen.Home, label = "Início", icon = Icons.Default.Home),
        BottomNavItem(screen = Screen.Search, label = "Pesquisa", icon = Icons.Default.Search),
        BottomNavItem(screen = Screen.Profile, label = "Perfil", icon = Icons.Default.Person)
    )

    // A Bottom Bar não aparece nos ecrãs de detalhes, listas, definições, géneros, países e resultados
    val showBottomBar = currentRoute?.startsWith("detail") == false &&
            currentRoute?.startsWith("lists") == false &&
            currentRoute?.startsWith("browse") == false &&
            currentRoute != Screen.Settings.route &&
            currentRoute != Screen.Genre.route &&
            currentRoute != Screen.Country.route

    Scaffold(
        containerColor = BackgroundDark,
        bottomBar = {
            if (showBottomBar) {
                ScreenlyBottomBar(
                    items = bottomNavItems,
                    currentRoute = currentRoute,
                    onItemClick = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Ecrã de Início
            composable(Screen.Home.route) {
                HomeScreen(
                    onItemClick = { id, mediaType ->
                        navController.navigate(Screen.Detail.createRoute(id, mediaType))
                    }
                )
            }

            // Ecrã de Pesquisa
            composable(Screen.Search.route) {
                SearchScreen(
                    onItemClick = { id, mediaType ->
                        navController.navigate(Screen.Detail.createRoute(id, mediaType))
                    },
                    onCategoryClick = { filter ->
                        val title = when (filter) {
                            BrowseFilter.POPULAR -> "Mais Populares"
                            BrowseFilter.TOP_RATED -> "Melhor Classificados"
                            BrowseFilter.RECENT -> "Lançamentos Recentes"
                            else -> ""
                        }
                        navController.navigate(
                            Screen.BrowseResults.createRoute(filter, title)
                        )
                    },
                    onGenreClick = {
                        navController.navigate(Screen.Genre.route)
                    },
                    onCountryClick = {
                        navController.navigate(Screen.Country.route)
                    }
                )
            }

            // Ecrã de Perfil
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onNavigateToLists = { status ->
                        navController.navigate(Screen.Lists.createRoute(status))
                    },
                    onItemClick = { id, mediaType ->
                        navController.navigate(Screen.Detail.createRoute(id, mediaType))
                    }
                )
            }

            // Ecrã de Listas
            composable(
                route = Screen.Lists.route,
                arguments = listOf(
                    navArgument("status") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val statusName = backStackEntry.arguments?.getString("status")
                    ?: WatchStatus.TO_WATCH.name
                val status = WatchStatus.valueOf(statusName)
                ListsScreen(
                    initialStatus = status,
                    onBack = { navController.popBackStack() },
                    onItemClick = { id, mediaType ->
                        navController.navigate(Screen.Detail.createRoute(id, mediaType))
                    }
                )
            }

            // Ecrã de Definições
            composable(Screen.Settings.route) {
                val context = androidx.compose.ui.platform.LocalContext.current
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        val intent = android.content.Intent(
                            context,
                            dam_a51568.screenly.ui.auth.LoginActivity::class.java
                        ).apply {
                            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                                    android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        context.startActivity(intent)
                    }
                )
            }

            // Ecrã de Géneros
            composable(Screen.Genre.route) {
                GenreScreen(
                    onBack = { navController.popBackStack() },
                    onGenreSelected = { genreId, genreName ->
                        navController.navigate(
                            Screen.BrowseResults.createRoute(
                                filter = BrowseFilter.GENRE,
                                title = genreName,
                                extra = genreId
                            )
                        )
                    }
                )
            }

            // Ecrã de Países
            composable(Screen.Country.route) {
                CountryScreen(
                    onBack = { navController.popBackStack() },
                    onCountrySelected = { countryCode, countryName ->
                        navController.navigate(
                            Screen.BrowseResults.createRoute(
                                filter = BrowseFilter.COUNTRY,
                                title = countryName,
                                extra = countryCode
                            )
                        )
                    }
                )
            }

            // Ecrã de Resultados de Navegação
            composable(
                route = Screen.BrowseResults.route,
                arguments = listOf(
                    navArgument("filter") { type = NavType.StringType },
                    navArgument("title") { type = NavType.StringType },
                    navArgument("extra") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val filterName = backStackEntry.arguments?.getString("filter")
                    ?: return@composable
                val title = backStackEntry.arguments?.getString("title") ?: return@composable
                val extra = backStackEntry.arguments?.getString("extra") ?: "none"
                val filter = BrowseFilter.valueOf(filterName)

                BrowseResultsScreen(
                    title = title,
                    filter = filter,
                    genreId = if (filter == BrowseFilter.GENRE) extra else null,
                    countryCode = if (filter == BrowseFilter.COUNTRY) extra else null,
                    onBack = { navController.popBackStack() },
                    onItemClick = { id, mediaType ->
                        navController.navigate(Screen.Detail.createRoute(id, mediaType))
                    }
                )
            }

            // Ecrã de Detalhes
            composable(
                route = Screen.Detail.route,
                arguments = listOf(
                    navArgument("id") { type = NavType.IntType },
                    navArgument("mediaType") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: return@composable
                val mediaType = backStackEntry.arguments?.getString("mediaType")
                    ?: return@composable
                DetailScreen(
                    id = id,
                    mediaType = mediaType,
                    onBack = { navController.popBackStack() },
                    onSimilarItemClick = { similarId, similarMediaType ->
                        navController.navigate(
                            Screen.Detail.createRoute(similarId, similarMediaType)
                        )
                    }
                )
            }
        }
    }
}

/**
 * Bottom Navigation Bar da aplicação.
 *
 * @param items Lista de itens a apresentar na barra.
 * @param currentRoute Rota actual do NavController.
 * @param onItemClick Callback chamado quando o utilizador clica num separador.
 */
@Composable
fun ScreenlyBottomBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemClick: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF1A2236),
        contentColor = BrandPurple
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.screen.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemClick(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(text = item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrandPurple,
                    selectedTextColor = BrandPurple,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = BackgroundDark
                )
            )
        }
    }
}