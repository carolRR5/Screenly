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
import dam_a51568.screenly.data.repository.WatchlistRepository
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
 * Este ficheiro rege a arquitetura de navegação central da aplicação Screenly, recorrendo ao
 * Jetpack Compose Navigation Framework. A infraestrutura baseia-se num modelo de atividade única
 * (Single-Activity Architecture), em que as transições entre ecrãs ocorrem por via de recomposição
 * e gestão reativa da pilha de retrocesso (BackStack).
 *
 * Princípios de Engenharia aplicados:
 * 1. Segurança de Tipos (Type Safety): Mitigação de erros por Strings voláteis em rotas parametrizadas.
 * 2. Desacoplamento de UI: Separação rigorosa entre a estrutura de navegação e o conteúdo dos ecrãs.
 * 3. Reatividade de Estado: Recomposição condicional da barra de navegação baseada no histórico de destinos.
 */

/**
 * Define de forma abstrata e selada todas as rotas de navegação da aplicação.
 *
 * O uso de uma 'sealed class' garante que a hierarquia seja estrita e fechada em tempo de compilação,
 * centralizando a árvore de destinos e prevenindo inconsistências sintáticas (rotas hardcoded).
 *
 * @param route Identificador textual da rota usado pelo NavHost.
 */
sealed class Screen(val route: String) {
    // Ponto de entrada: Ecrã de tendências e destaques cinematográficos
    data object Home : Screen("home")

    // Ecrã dedicado à pesquisa global com suporte a filtros avançados
    data object Search : Screen("search")

    // Ecrã agregador do perfil e estatísticas do utilizador autenticado
    data object Profile : Screen("profile")

    /**
     * Rota parametrizada para os detalhes de uma obra.
     * Requer argumentos dinâmicos obrigatoriamente injetados no padrão de caminhos URI.
     */
    data object Detail : Screen("detail/{id}/{mediaType}") {
        // Função helper utilitária para a correta interpolação de strings em tempo de execução
        fun createRoute(id: Int, mediaType: String) = "detail/$id/$mediaType"
    }

    /**
     * Rota parametrizada para a listagem personalizada (Watchlist).
     */
    data object Lists : Screen("lists/{status}") {
        // Serializa o enumerador de estado (Enum) para o seu equivalente textual na construção da URI
        fun createRoute(status: WatchStatus) = "lists/${status.name}"
    }

    // Ecrã de gestão de preferências e configurações globais do sistema
    data object Settings : Screen("settings")

    // Ecrã para a seleção e filtragem por categorias taxonómicas (Géneros)
    data object Genre : Screen("genre")

    // Ecrã para a seleção geográfica de obras conforme a região originária
    data object Country : Screen("country")

    /**
     * Rota de navegação multifiltragem.
     * Suporta passagem complexa de múltiplos argumentos para unificação de resultados da UI.
     */
    data object BrowseResults : Screen("browse/{filter}/{title}/{extra}") {
        // Função utilitária com argumento posicional padrão (default parameter) para flexibilidade de invocação
        fun createRoute(
            filter: BrowseFilter,
            title: String,
            extra: String = "none"
        ) = "browse/${filter.name}/$title/$extra"
    }
}

/**
 * Representa a estrutura de dados (Model) necessária para a modelação visual da barra de navegação inferior.
 *
 * @param screen Instância de [Screen] associada ao destino correspondente.
 * @param label Texto localizado a exibir na legenda do componente visual.
 * @param icon Vetor gráfico indicativo do separador.
 */
data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

/**
 * Controlador Principal (Ponto de Entrada da Aplicação após o processo de Autenticação).
 *
 * Esta Atividade atua como o contentor hospedeiro (Host), delegando a renderização
 * e gestão do ciclo de vida ao ecossistema declarativo do Compose.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa o Listener assíncrono em tempo real na base de dados NoSQL (Firebase Firestore).
        // Garante a reatividade de dados (Data-Driven UI) entre a persistência remota e a memória local.
        WatchlistRepository.startListening()

        // Acopla a árvore de componentes Compose ao ciclo de vida desta Activity
        setContent {
            ScreenlyApp()
        }
    }
}

/**
 * Componente Composable Raiz.
 *
 * Responsável direto pela instanciação do grafo de navegação, retenção da pilha (BackStack),
 * e coordenação dos layouts estruturais (Scaffold).
 */
@Composable
fun ScreenlyApp() {
    // Instancia e retém o controlador central de estado da navegação (padrão State Holder)
    val navController = rememberNavController()
    // Subscreve-se e observa as mutações na pilha de retrocesso em tempo real como um Estado do Compose
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    // Extrai dinamicamente a string identificadora do destino ativo no topo da pilha
    val currentRoute = navBackStackEntry?.destination?.route

    // Define a coleção imutável de itens que constituem a topologia da Bottom Navigation Bar
    val bottomNavItems = listOf(
        BottomNavItem(screen = Screen.Home, label = "Início", icon = Icons.Default.Home),
        BottomNavItem(screen = Screen.Search, label = "Pesquisa", icon = Icons.Default.Search),
        BottomNavItem(screen = Screen.Profile, label = "Perfil", icon = Icons.Default.Person)
    )

    // Avaliação booleana para controlo dinâmico de visibilidade de componentes secundários de UI.
    // A barra inferior é ocultada em ecrãs de detalhe profundo ou fluxos secundários lineares.
    val showBottomBar = currentRoute?.startsWith("detail") == false &&
            currentRoute?.startsWith("lists") == false &&
            currentRoute?.startsWith("browse") == false &&
            currentRoute != Screen.Settings.route &&
            currentRoute != Screen.Genre.route &&
            currentRoute != Screen.Country.route

    // Componente de layout estrutural que organiza os painéis visuais padrão da especificação Material Design 3
    Scaffold(
        containerColor = BackgroundDark, // Fixa a tonalidade cromática de fundo da aplicação
        bottomBar = { // Painel de injeção condicional da barra de navegação
            if (showBottomBar) {
                ScreenlyBottomBar(
                    items = bottomNavItems,
                    currentRoute = currentRoute,
                    onItemClick = { screen -> // Callback disparado ao interagir com um separador
                        // Executa a transição de rota aplicando regras de otimização de memória da pilha
                        navController.navigate(screen.route) {
                            // Desenrola a pilha de ecrãs até ao destino inicial, evitando acumulação de memória
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true // Preserva o estado interno do ecrã que foi desalocado
                            }
                            launchSingleTop = true // Previne a instanciação múltipla em duplicado caso haja duplo clique
                            restoreState = true // Recupera o estado previamente guardado do ecrã ao reordenar a stack
                        }
                    }
                )
            }
        }
    ) { innerPadding -> // Expõe as margens (paddings) de compensação física (ex: barras do sistema)
        // Contentor estrutural que mapeia as strings das rotas aos respetivos blocos executáveis de ecrãs
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route, // Define o nó de arranque do grafo
            modifier = Modifier.padding(innerPadding) // Aplica as margens do Scaffold para evitar sobreposição visual
        ) {

            // Definição de Rota: Ecrã Principal
            composable(Screen.Home.route) {
                HomeScreen(
                    onItemClick = { id, mediaType ->
                        // Navegação parametrizada imperativa gerada a partir dos argumentos capturados do clique
                        navController.navigate(Screen.Detail.createRoute(id, mediaType))
                    }
                )
            }

            // Definição de Rota: Ecrã de Pesquisa e Descoberta
            composable(Screen.Search.route) {
                SearchScreen(
                    onItemClick = { id, mediaType ->
                        navController.navigate(Screen.Detail.createRoute(id, mediaType))
                    },
                    onCategoryClick = { filter ->
                        // Resolução dinâmica de strings com base na enumeração de filtragem ativa
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

            // Definição de Rota: Perfil do Utilizador
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

            // Definição de Rota: Listas e Subwatchlists (Recebe uma String que representa o estado)
            composable(
                route = Screen.Lists.route,
                arguments = listOf(
                    navArgument("status") { type = NavType.StringType } // Declara formalmente o tipo do argumento
                )
            ) { backStackEntry -> // Obtém os dados guardados que foram passados para este ecrã
            // Procura o texto do "status" que veio na navegação. Se não encontrar, usa "TO_WATCH" como padrão
            val statusName = backStackEntry.arguments?.getString("status")
                ?: WatchStatus.TO_WATCH.name

            // Transforma o texto recebido de volta num valor aceite pelo nosso sistema (Enum)
            val status = WatchStatus.valueOf(statusName)

            ListsScreen(
                initialStatus = status,
                onBack = { navController.popBackStack() }, // Fecha o ecrã atual e volta para o ecrã anterior
                onItemClick = { id, mediaType ->
                    navController.navigate(Screen.Detail.createRoute(id, mediaType))
                }
            )
        }

            // Definição de Rota: Painel de Definições da Aplicação
            composable(Screen.Settings.route) {
                // Captura o Context do Android no escopo do Compose para criação de Intents nativas
                val context = androidx.compose.ui.platform.LocalContext.current
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        // Constrói uma Intent explícita de rutura para reinicialização do fluxo de autenticação
                        val intent = android.content.Intent(
                            context,
                            dam_a51568.screenly.ui.auth.LoginActivity::class.java
                        ).apply {
                            // Limpa a totalidade da árvore de atividades histórica evitando voltar a entrar forçadamente
                            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                                    android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        // Dispara a navegação nativa do Android a nível de Sistema Operacional
                        context.startActivity(intent)
                    }
                )
            }

            // Definição de Rota: Seleção Categórica de Géneros
            composable(Screen.Genre.route) {
                GenreScreen(
                    onBack = { navController.popBackStack() },
                    onGenreSelected = { genreId, genreName ->
                        // Redireciona para o ecrã de resultados comuns injetando a chave ID como metadata extra
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

            // Definição de Rota: Seleção Geográfica de Países
            composable(Screen.Country.route) {
                CountryScreen(
                    onBack = { navController.popBackStack() },
                    onCountrySelected = { countryCode, countryName ->
                        // Redireciona para o ecrã de resultados comuns injetando o código ISO do país
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

            // Definição de Rota Avançada: Resultados Consolidados de Navegação (Múltiplos Argumentos)
            composable(
                route = Screen.BrowseResults.route,
                arguments = listOf(
                    navArgument("filter") { type = NavType.StringType },
                    navArgument("title") { type = NavType.StringType },
                    navArgument("extra") { type = NavType.StringType }
                )
            ) { backStackEntry -> // Interceta os dados para descompactação imediata
                // Padrão de Cláusula de Guarda (Guard Clauses): Aborta imediatamente a execução do bloco
                // se as dependências cruciais de dados estiverem corrompidas ou ausentes.
                val filterName = backStackEntry.arguments?.getString("filter") ?: return@composable
                val title = backStackEntry.arguments?.getString("title") ?: return@composable
                val extra = backStackEntry.arguments?.getString("extra") ?: "none"
                val filter = BrowseFilter.valueOf(filterName)

                BrowseResultsScreen(
                    title = title,
                    filter = filter,
                    // Condicionamento semântico de argumentos baseados na tipagem do filtro ativo
                    genreId = if (filter == BrowseFilter.GENRE) extra else null,
                    countryCode = if (filter == BrowseFilter.COUNTRY) extra else null,
                    onBack = { navController.popBackStack() },
                    onItemClick = { id, mediaType ->
                        navController.navigate(Screen.Detail.createRoute(id, mediaType))
                    }
                )
            }

            // Definição de Rota Parametrizada: Ecrã de Detalhe Complexo (ID Numérico + String de Tipo)
            composable(
                route = Screen.Detail.route,
                arguments = listOf(
                    navArgument("id") { type = NavType.IntType }, // Determina a validação estrita para inteiros
                    navArgument("mediaType") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: return@composable
                val mediaType = backStackEntry.arguments?.getString("mediaType") ?: return@composable

                DetailScreen(
                    id = id,
                    mediaType = mediaType,
                    onBack = { navController.popBackStack() },
                    onSimilarItemClick = { similarId, similarMediaType ->
                        // Suporta navegação recursiva ciclada (o ecrã de detalhe chama outra instância de si próprio)
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
 * Componente de Apresentação: Barra de Navegação Inferior Customizada (Material Design 3).
 *
 * @param items Lista contendo as especificações funcionais e icónicas de cada aba.
 * @param currentRoute Rota textual sob observação para cálculo dinâmico do estado ativo.
 * @param onItemClick Canal de comunicação (Callback) que propaga o evento de seleção de rota.
 */
@Composable
fun ScreenlyBottomBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemClick: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF1A2236), // Cor opaca personalizada para o painel de fundo da barra
        contentColor = BrandPurple
    ) {
        // Itera de forma declarativa sobre a coleção de itens gerando os botões respetivos
        items.forEach { item ->
            // Avaliação lógica bidirecional para ativação visual do componente gráfico correspondente à aba ativa
            val isSelected = currentRoute == item.screen.route

            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemClick(item.screen) }, // Invoca a lógica de navegação declarativa customizada
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label // Texto acessível para leitores de ecrã (Acessibilidade)
                    )
                },
                label = { Text(text = item.label) },
                colors = NavigationBarItemDefaults.colors( // Mapeamento estético das transições de estado cromático
                    selectedIconColor = BrandPurple,
                    selectedTextColor = BrandPurple,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = BackgroundDark // Cor da cápsula de seleção em plano de fundo do ícone
                )
            )
        }
    }
}