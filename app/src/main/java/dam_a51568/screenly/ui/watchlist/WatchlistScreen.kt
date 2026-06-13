package dam_a51568.screenly.ui.watchlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import dam_a51568.screenly.data.remote.TmdbClient
import dam_a51568.screenly.data.model.WatchStatus
import dam_a51568.screenly.data.model.WatchlistItem
import dam_a51568.screenly.data.repository.WatchlistRepository
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import dam_a51568.screenly.ui.theme.BackgroundDark
import dam_a51568.screenly.ui.theme.BrandPurple
import dam_a51568.screenly.ui.theme.CardBackground
import dam_a51568.screenly.ui.theme.ErrorRed
import dam_a51568.screenly.ui.theme.TextPrimary
import dam_a51568.screenly.ui.theme.TextSecondary

/**
 * Ecrã da Watchlist da aplicação Screenly.
 *
 * Apresenta os títulos guardados pelo utilizador organizados em três separadores:
 * "To Watch", "Watching" e "Watched". Cada separador mostra uma grelha de 3 colunas
 * adequada para tablet.
 *
 * @param onItemClick Callback chamado quando o utilizador clica num título,
 *                    navegando para o ecrã de detalhes.
 * @param viewModel ViewModel que gere o estado do ecrã.
 */
@Composable
fun WatchlistScreen(
    onItemClick: (id: Int, mediaType: String) -> Unit,
    viewModel: WatchlistViewModel = viewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsState()

    val items = remember(selectedTab, WatchlistRepository.items) {
        viewModel.getItemsByStatus(selectedTab)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Text(
            text = "A minha lista",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )

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
                    onClick = { viewModel.selectTab(status) },
                    text = {
                        Text(
                            text = when (status) {
                                WatchStatus.TO_WATCH -> "To Watch"
                                WatchStatus.WATCHING -> "Watching"
                                WatchStatus.WATCHED -> "Watched"
                            },
                            color = if (selectedTab == status) BrandPurple else TextSecondary,
                            fontWeight = if (selectedTab == status) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (items.isEmpty()) {
            EmptyTabContent(status = selectedTab)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(items) { item ->
                    WatchlistItemCard(
                        item = item,
                        onClick = { onItemClick(item.id, item.mediaType) },
                        onRemove = { viewModel.removeItem(item.id, item.mediaType) }
                    )
                }
            }
        }
    }
}

/**
 * Conteúdo apresentado quando o separador ativo não tem títulos.
 *
 * @param status Estado do separador ativo, usado para personalizar a mensagem.
 */
@Composable
private fun EmptyTabContent(status: WatchStatus) {
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
 * Cartão individual de um título na watchlist.
 *
 * @param item Dados do título na watchlist.
 * @param onClick Callback chamado ao clicar no cartão.
 * @param onRemove Callback chamado ao clicar no botão de remoção.
 */
@Composable
private fun WatchlistItemCard(
    item: WatchlistItem,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
    ) {
        Box {
            AsyncImage(
                model = "${TmdbClient.IMAGE_BASE_URL}${item.posterPath}",
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
            )

            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(32.dp)
                    .background(
                        color = Color(0xCC121829),
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remover",
                    tint = ErrorRed,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = item.title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.year,
                color = TextSecondary,
                fontSize = 11.sp
            )
            if (item.rating != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "⭐ ${"%.1f".format(item.rating)}",
                    color = BrandPurple,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}