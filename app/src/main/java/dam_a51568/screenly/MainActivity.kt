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
import dam_a51568.screenly.ui.details.DetailScreen
import dam_a51568.screenly.ui.home.HomeScreen
import dam_a51568.screenly.ui.profile.ProfileScreen
import dam_a51568.screenly.ui.search.SearchScreen
import dam_a51568.screenly.ui.theme.BackgroundDark
import dam_a51568.screenly.ui.theme.BrandPurple
import dam_a51568.screenly.ui.theme.TextSecondary

/**
 * Destinos de navegação da aplicação.
 * Cada objeto representa uma rota única no grafo de navegação.
 */
sealed class Screen(val route: String) {
    /** Ecrã principal com títulos em tendência. */
    data object Home : Screen("home")
    /** Ecrã de pesquisa de filmes e séries. */
    data object Search : Screen("search")
    /** Ecrã de perfil do utilizador. */
    data object Profile : Screen("profile")
    /** Ecrã de detalhes de um título — recebe id e mediaType como argumentos. */
    data object Detail : Screen("detail/{id}/{mediaType}") {
        fun createRoute(id: Int, mediaType: String) = "detail/$id/$mediaType"
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

    // Itens da Bottom Navigation Bar
    val bottomNavItems = listOf(
        BottomNavItem(
            screen = Screen.Home,
            label = "Início",
            icon = Icons.Default.Home
        ),
        BottomNavItem(
            screen = Screen.Search,
            label = "Pesquisa",
            icon = Icons.Default.Search
        ),
        BottomNavItem(
            screen = Screen.Profile,
            label = "Perfil",
            icon = Icons.Default.Person
        )
    )

    // Rotas onde a Bottom Bar não deve aparecer (ex: ecrã de detalhes)
    val routesWithoutBottomBar = listOf(Screen.Detail.route)
    val showBottomBar = routesWithoutBottomBar.none {
        currentRoute?.startsWith("detail") == true
    }

    Scaffold(
        containerColor = BackgroundDark,
        bottomBar = {
            if (showBottomBar) {
                ScreenlyBottomBar(
                    items = bottomNavItems,
                    currentRoute = currentRoute,
                    onItemClick = { screen ->
                        navController.navigate(screen.route) {
                            // Evita criar múltiplas instâncias do mesmo destino
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
                    }
                )
            }

            // Ecrã de Perfil
            composable(Screen.Profile.route) {
                ProfileScreen()
            }

            // Ecrã de Detalhes — recebe id (Int) e mediaType (String) como argumentos
            composable(
                route = Screen.Detail.route,
                arguments = listOf(
                    navArgument("id") { type = NavType.IntType },
                    navArgument("mediaType") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: return@composable
                val mediaType = backStackEntry.arguments?.getString("mediaType") ?: return@composable
                DetailScreen(
                    id = id,
                    mediaType = mediaType,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

/**
 * Bottom Navigation Bar da aplicação.
 * Apresenta três separadores: Início, Pesquisa e Perfil.
 *
 * @param items Lista de itens a apresentar na barra.
 * @param currentRoute Rota actual do NavController, usada para destacar o separador activo.
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
                label = {
                    Text(text = item.label)
                },
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