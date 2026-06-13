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
 * Factory para criar o ListsViewModel com o estado inicial do separador.
 * Necessária porque o ViewModel recebe um argumento no construtor.
 *
 * @param initialStatus Estado inicial do separador a mostrar.
 */
class ListsViewModelFactory(private val initialStatus: WatchStatus) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ListsViewModel(initialStatus) as T
    }
}

/**
 * Ecrã de Listas da aplicação Screenly.
 *
 * Apresenta os títulos guardados pelo utilizador organizados em três separadores
 * (To Watch, Watching, Watched) no topo do ecrã, semelhante ao estilo Letterboxd.
 * Abre no separador correspondente à lista que o utilizador selecionou no perfil.
 *
 * @param initialStatus Estado inicial do separador a mostrar.
 * @param onBack Callback chamado ao clicar no botão de retroceder.
 * @param onItemClick Callback chamado ao clicar num título.
 */
@Composable
fun ListsScreen(
    initialStatus: WatchStatus,
    onBack: () -> Unit,
    onItemClick: (id: Int, mediaType: String) -> Unit
) {
    val viewModel: ListsViewModel = viewModel(
        factory = ListsViewModelFactory(initialStatus)
    )

    val selectedTab by viewModel.selectedTab.collectAsState()
    val items = remember(selectedTab, WatchlistRepository.items) {
        viewModel.getItemsByStatus(selectedTab)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Barra de topo com botão de retroceder
        TopBar(onBack = onBack)

        // Separadores das três listas no topo
        ListsTabRow(
            selectedTab = selectedTab,
            onTabSelected = viewModel::selectTab
        )

        // Conteúdo da lista selecionada
        if (items.isEmpty()) {
            EmptyContent(status = selectedTab)
        } else {
            ListsGrid(
                items = items,
                onItemClick = onItemClick
            )
        }
    }
}

/**
 * Barra de topo com botão de retroceder.
 *
 * @param onBack Callback chamado ao clicar no botão.
 */
@Composable
private fun TopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Retroceder",
                tint = TextPrimary
            )
        }
        Text(
            text = "As minhas listas",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Barra de separadores com os três estados da watchlist.
 * Segue o estilo da Letterboxd com separadores no topo do ecrã.
 *
 * @param selectedTab Separador atualmente selecionado.
 * @param onTabSelected Callback chamado ao selecionar um separador.
 */
@Composable
private fun ListsTabRow(
    selectedTab: WatchStatus,
    onTabSelected: (WatchStatus) -> Unit
) {
    TabRow(
        selectedTabIndex = WatchStatus.entries.indexOf(selectedTab),
        containerColor = BackgroundDark,
        contentColor = BrandPurple,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(
                    tabPositions[WatchStatus.entries.indexOf(selectedTab)]
                ),
                color = BrandPurple
            )
        }
    ) {
        WatchStatus.entries.forEach { status ->
            Tab(
                selected = selectedTab == status,
                onClick = { onTabSelected(status) },
                text = {
                    Text(
                        text = when (status) {
                            WatchStatus.TO_WATCH -> "To Watch"
                            WatchStatus.WATCHING -> "Watching"
                            WatchStatus.WATCHED -> "Watched"
                        },
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
 * Conteúdo apresentado quando a lista selecionada está vazia.
 *
 * @param status Estado do separador ativo, usado para personalizar a mensagem.
 */
@Composable
private fun EmptyContent(status: WatchStatus) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (status) {
                WatchStatus.TO_WATCH -> "Ainda não adicionaste títulos para ver"
                WatchStatus.WATCHING -> "Ainda não estás a ver nenhum título"
                WatchStatus.WATCHED -> "Ainda não marcaste nenhum título como visto"
            },
            color = TextSecondary,
            fontSize = 16.sp
        )
    }
}

/**
 * Grelha de 3 colunas com os títulos da lista selecionada.
 *
 * @param items Lista de títulos a apresentar.
 * @param onItemClick Callback chamado ao clicar num título.
 */
@Composable
private fun ListsGrid(
    items: List<WatchlistItem>,
    onItemClick: (id: Int, mediaType: String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(items) { item ->
            ListMediaCard(
                item = item,
                onClick = { onItemClick(item.id, item.mediaType) }
            )
        }
    }
}

/**
 * Cartão individual de um título na grelha das listas.
 * Apresenta o póster completo com o título e a classificação por baixo.
 *
 * @param item Dados do título na watchlist.
 * @param onClick Callback chamado ao clicar no cartão.
 */
@Composable
private fun ListMediaCard(
    item: WatchlistItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = "${TmdbClient.IMAGE_BASE_URL}${item.posterPath}",
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
        )
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = item.title,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (item.rating != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "⭐ ${"%.1f".format(item.rating)}",
                    color = BrandPurple,
                    fontSize = 11.sp
                )
            }
        }
    }
}