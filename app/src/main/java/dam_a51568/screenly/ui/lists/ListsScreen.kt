package dam_a51568.screenly.ui.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import dam_a51568.screenly.data.remote.TmdbClient
import dam_a51568.screenly.data.model.WatchStatus
import dam_a51568.screenly.data.model.WatchlistItem
import dam_a51568.screenly.data.repository.WatchlistRepository
import dam_a51568.screenly.ui.theme.BackgroundDark
import dam_a51568.screenly.ui.theme.BrandPurple
import dam_a51568.screenly.ui.theme.CardBackground
import dam_a51568.screenly.ui.theme.TextPrimary
import dam_a51568.screenly.ui.theme.TextSecondary

/**
 * Factory para criar o [ListsViewModel] com o estado inicial do separador.
 *
 * É necessária porque o [ListsViewModel] recebe [initialStatus] no construtor,
 * o que impede a criação automática pelo sistema padrão do Compose.
 * Esta factory é passada a [viewModel] para garantir a instanciação correta.
 *
 * @param initialStatus Estado do separador que deverá estar ativo ao abrir o ecrã.
 */
class ListsViewModelFactory(private val initialStatus: WatchStatus) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        // Instancia o ListsViewModel com o estado inicial e faz cast seguro para T
        return ListsViewModel(initialStatus) as T
    }
}

/**
 * Ecrã de Listas da aplicação Screenly.
 *
 * Apresenta os títulos guardados pelo utilizador organizados em três separadores
 * (To Watch, Watching, Watched) no topo do ecrã, seguindo o estilo da Letterboxd.
 * O ecrã abre diretamente no separador correspondente à lista selecionada no perfil.
 *
 * A composable observa o [ListsViewModel] via [StateFlow] e recalcula a lista
 * de itens sempre que o separador ativo ou os dados do repositório mudam,
 * usando [remember] com [selectedTab] e [WatchlistRepository.items] como chaves.
 *
 * @param initialStatus Estado inicial do separador a apresentar ao entrar no ecrã.
 * @param onBack Callback invocado ao clicar no botão de retroceder; tipicamente
 *               chama [NavController.popBackStack].
 * @param onItemClick Callback invocado ao clicar num título, recebendo o [id]
 *                    do conteúdo e o [mediaType] ("movie" ou "tv") para navegação
 *                    para o ecrã de detalhe.
 */
@Composable
fun ListsScreen(
    initialStatus: WatchStatus,
    onBack: () -> Unit,
    onItemClick: (id: Int, mediaType: String) -> Unit
) {
    // Cria ou recupera o ViewModel usando a factory, que injeta o initialStatus
    val viewModel: ListsViewModel = viewModel(
        factory = ListsViewModelFactory(initialStatus)
    )

    // Observa o separador ativo como estado do Compose; recompõe ao mudar
    val selectedTab by viewModel.selectedTab.collectAsState()

    // Recalcula a lista filtrada apenas quando o separador ou os dados do repositório mudam,
    // evitando filtragens desnecessárias em recomposições causadas por outros estados
    val items = remember(selectedTab, WatchlistRepository.items) {
        viewModel.getItemsByStatus(selectedTab)
    }

    // Coluna raiz que ocupa todo o ecrã com fundo escuro da aplicação
    Column(
        modifier = Modifier
            .fillMaxSize() // Ocupa toda a altura e largura disponíveis
            .background(BackgroundDark) // Aplica o fundo escuro definido no tema
    ) {
        // Barra de topo com botão de retroceder e título do ecrã
        TopBar(onBack = onBack)

        // Separadores das três listas (To Watch / Watching / Watched)
        ListsTabRow(
            selectedTab = selectedTab,
            onTabSelected = viewModel::selectTab // Referência direta ao método do ViewModel
        )

        // Mostra a grelha de títulos ou a mensagem de lista vazia consoante o conteúdo
        if (items.isEmpty()) {
            // Lista vazia: apresenta mensagem contextual ao separador ativo
            EmptyContent(status = selectedTab)
        } else {
            // Lista com itens: apresenta a grelha de pósters
            ListsGrid(
                items = items,
                onItemClick = onItemClick
            )
        }
    }
}

/**
 * Barra de topo com ícone de retroceder e título do ecrã.
 *
 * Ocupa a largura total e tem padding reduzido para manter o conteúdo
 * próximo do topo, deixando mais espaço para a grelha de títulos.
 *
 * @param onBack Callback invocado ao clicar no botão de retroceder.
 */
@Composable
private fun TopBar(onBack: () -> Unit) {
    // Linha horizontal que alinha o botão e o título verticalmente ao centro
    Row(
        modifier = Modifier
            .fillMaxWidth() // Ocupa toda a largura do ecrã
            .padding(horizontal = 8.dp, vertical = 4.dp), // Padding reduzido para compactar o topo
        verticalAlignment = Alignment.CenterVertically  // Alinha botão e texto ao centro vertical
    ) {
        // Botão de retroceder com ícone de seta para a esquerda
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Ícone espelhado para idiomas RTL
                contentDescription = "Retroceder", // Descrição para acessibilidade
                tint = TextPrimary // Cor do ícone conforme o tema
            )
        }
        // Título fixo do ecrã, a negrito e com cor primária do tema
        Text(
            text = "As minhas listas",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Barra de separadores com os três estados possíveis da watchlist.
 *
 * Segue o estilo visual da Letterboxd, com os separadores no topo do ecrã
 * e um indicador roxo ([BrandPurple]) sob o separador ativo.
 * A ordem dos separadores respeita a ordem de declaração em [WatchStatus.entries].
 *
 * @param selectedTab Separador atualmente selecionado.
 * @param onTabSelected Callback invocado ao selecionar um separador diferente.
 */
@Composable
private fun ListsTabRow(
    selectedTab: WatchStatus,
    onTabSelected: (WatchStatus) -> Unit
) {
    TabRow(
        // Índice do separador ativo, calculado a partir da posição no enum
        selectedTabIndex = WatchStatus.entries.indexOf(selectedTab),
        containerColor = BackgroundDark, // Fundo da barra igual ao fundo do ecrã
        contentColor = BrandPurple, // Cor padrão do conteúdo (herdada pelos separadores)
        indicator = { tabPositions ->
            // Indicador personalizado posicionado sob o separador ativo
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(
                    // Calcula a posição exata do indicador com base no separador ativo
                    tabPositions[WatchStatus.entries.indexOf(selectedTab)]
                ),
                color = BrandPurple // Cor roxa da marca para o indicador ativo
            )
        }
    ) {
        // Itera sobre todos os valores do enum para criar um Tab por cada estado
        WatchStatus.entries.forEach { status ->
            Tab(
                selected = selectedTab == status, // Marca como selecionado se for o separador ativo
                onClick = { onTabSelected(status) }, // Notifica o ViewModel da seleção
                text = {
                    Text(
                        // Texto localizado para cada estado da watchlist
                        text = when (status) {
                            WatchStatus.TO_WATCH -> "To Watch"
                            WatchStatus.WATCHING -> "Watching"
                            WatchStatus.WATCHED  -> "Watched"
                        },
                        // Separador ativo a roxo e negrito; inativo a cinzento e normal
                        color = if (selectedTab == status) BrandPurple else TextSecondary,
                        fontWeight = if (selectedTab == status) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 15.sp
                    )
                }
            )
        }
    }
}

/**
 * Conteúdo apresentado quando a lista selecionada não tem itens.
 *
 * A mensagem é personalizada consoante o separador ativo, fornecendo
 * contexto claro ao utilizador sobre o motivo de a lista estar vazia.
 *
 * @param status Estado do separador ativo, usado para selecionar a mensagem adequada.
 */
@Composable
private fun EmptyContent(status: WatchStatus) {
    // Caixa centrada que ocupa todo o espaço restante abaixo dos separadores
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center // Centra o texto horizontal e verticalmente
    ) {
        Text(
            // Mensagem adaptada ao separador ativo para orientar o utilizador
            text = when (status) {
                WatchStatus.TO_WATCH -> "Ainda não adicionaste títulos para ver"
                WatchStatus.WATCHING -> "Ainda não estás a ver nenhum título"
                WatchStatus.WATCHED  -> "Ainda não marcaste nenhum título como visto"
            },
            color = TextSecondary, // Cor secundária para texto de estado vazio (menos destaque)
            fontSize = 16.sp
        )
    }
}

/**
 * Grelha de 3 colunas com os títulos da lista selecionada.
 *
 * Utiliza [LazyVerticalGrid] para renderização eficiente de listas longas,
 * carregando apenas os cartões visíveis no ecrã. O espaçamento entre células
 * e o padding externo garantem uma apresentação limpa e consistente.
 *
 * @param items Lista de títulos a apresentar na grelha.
 * @param onItemClick Callback invocado ao clicar num cartão, com o [id] e
 *                    o [mediaType] do título selecionado.
 */
@Composable
private fun ListsGrid(
    items: List<WatchlistItem>,
    onItemClick: (id: Int, mediaType: String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3), // Grelha fixa de 3 colunas
        horizontalArrangement = Arrangement.spacedBy(12.dp), // Espaço horizontal entre cartões
        verticalArrangement = Arrangement.spacedBy(12.dp), // Espaço vertical entre linhas
        contentPadding = PaddingValues(16.dp) // Margem exterior em torno de toda a grelha
    ) {
        // Renderiza um cartão por cada item da lista filtrada
        items(items) { item ->
            ListMediaCard(
                item = item,
                // Passa o id e o mediaType ao callback para navegação para o detalhe
                onClick = { onItemClick(item.id, item.mediaType) }
            )
        }
    }
}

/**
 * Cartão individual de um título na grelha das listas.
 *
 * Apresenta o póster com rácio 2:3 (proporção padrão de póster de cinema)
 * seguido de uma área de texto com o título e, opcionalmente, a classificação.
 * O título é truncado a duas linhas para manter a grelha compacta e uniforme.
 *
 * A classificação só é apresentada se [WatchlistItem.rating] não for nulo,
 * o que ocorre apenas em títulos com dados completos do TMDB.
 *
 * @param item Dados do título a apresentar, incluindo póster, título e classificação.
 * @param onClick Callback invocado ao clicar no cartão.
 */
@Composable
private fun ListMediaCard(
    item: WatchlistItem,
    onClick: () -> Unit
) {
    // Coluna que agrupa o póster e a área de texto, com cantos arredondados e fundo de cartão
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp)) // Arredonda os cantos do cartão inteiro
            .background(CardBackground) // Fundo do cartão distinto do fundo do ecrã
            .clickable(onClick = onClick) // Torna o cartão inteiro clicável
    ) {
        // Póster carregado assincronamente a partir do URL do TMDB
        AsyncImage(
            // Concatena a base URL do TMDB com o caminho relativo do póster
            model = "${TmdbClient.IMAGE_BASE_URL}${item.posterPath}",
            contentDescription = item.title, // Descrição para leitores de ecrã
            contentScale = ContentScale.Crop, // Recorta a imagem para preencher o espaço sem distorção
            modifier = Modifier
                .fillMaxWidth() // O póster ocupa toda a largura do cartão
                .aspectRatio(2f / 3f) // Rácio 2:3 — proporção padrão de póster de cinema
        )
        // Área de texto abaixo do póster com título e classificação
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = item.title,
                color = TextPrimary,
                fontSize = 12.sp, // Tamanho reduzido para caber em 3 colunas
                fontWeight = FontWeight.SemiBold,
                maxLines = 2, // Limita a 2 linhas para uniformidade da grelha
                overflow = TextOverflow.Ellipsis // Adiciona "…" se o título for demasiado longo
            )
            // A classificação só é apresentada se existir (campo opcional no modelo)
            if (item.rating != null) {
                Spacer(modifier = Modifier.height(2.dp)) // Pequeno espaço entre título e classificação
                Text(
                    // Formata a classificação com uma casa decimal (ex: "8.4")
                    text = "⭐ ${"%.1f".format(item.rating)}",
                    color = BrandPurple, // Cor roxa para destacar a classificação
                    fontSize = 11.sp
                )
            }
        }
    }
}