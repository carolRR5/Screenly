package dam_a51568.screenly.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import dam_a51568.screenly.data.model.MediaItem
import dam_a51568.screenly.data.remote.TmdbClient
import dam_a51568.screenly.ui.theme.BackgroundDark
import dam_a51568.screenly.ui.theme.BrandPurple
import dam_a51568.screenly.ui.theme.CardBackground
import dam_a51568.screenly.ui.theme.ErrorRed
import dam_a51568.screenly.ui.theme.TextPrimary
import dam_a51568.screenly.ui.theme.TextSecondary
import kotlin.math.ceil

/**
 * Ecrã principal da aplicação Screenly.
 *
 * Apresenta três secções de conteúdo carregadas da API do TMDb:
 * - Tendências da semana: grelha de 3 colunas
 * - Filmes populares: lista horizontal com scroll
 * - Séries populares: lista horizontal com scroll
 *
 * @param onItemClick Callback chamado quando o utilizador clica num título.
 * @param viewModel ViewModel que gere o estado do ecrã.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onItemClick: (id: Int, mediaType: String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        when (val state = uiState) {
            is HomeUiState.Loading -> LoadingContent()
            is HomeUiState.Error -> ErrorContent(
                message = state.message,
                onRetry = { viewModel.refresh() }
            )
            is HomeUiState.Success -> HomeContent(
                data = state.data,
                onItemClick = onItemClick
            )
        }
    }
}

/**
 * Indicador de carregamento apresentado enquanto os dados estão a ser obtidos.
 */
@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = BrandPurple)
    }
}

/**
 * Conteúdo apresentado quando ocorre um erro ao carregar os dados.
 */
@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, color = ErrorRed, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
        ) {
            Text("Tentar Novamente")
        }
    }
}

/**
 * Conteúdo principal do ecrã de Início com as três secções.
 * Usa um [Column] com scroll vertical para permitir navegar entre as secções.
 *
 * @param data Dados das três secções carregados da API.
 * @param onItemClick Callback chamado quando o utilizador clica num título.
 */
@Composable
private fun HomeContent(
    data: HomeData,
    onItemClick: (id: Int, mediaType: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp)
    ) {
        // Secção 1 — Tendências da semana (grelha de 3 colunas)
        SectionHeader(title = "Em Tendência Esta Semana")
        Spacer(modifier = Modifier.height(12.dp))
        TrendingGrid(
            results = data.trending,
            onItemClick = onItemClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Secção 2 — Filmes populares (scroll horizontal)
        SectionHeader(title = "Filmes Populares")
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalMediaRow(
            items = data.popularMovies,
            onItemClick = { item -> onItemClick(item.id, item.mediaType) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Secção 3 — Séries populares (scroll horizontal)
        SectionHeader(title = "Séries Populares")
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalMediaRow(
            items = data.popularTvShows,
            onItemClick = { item -> onItemClick(item.id, item.mediaType) }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Cabeçalho de uma secção do ecrã de Início.
 *
 * @param title Título da secção.
 */
@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = TextPrimary,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

/**
 * Grelha de 3 colunas para os títulos em tendência.
 * Usa [LazyVerticalGrid] com altura fixa para funcionar dentro
 * do [Column] com scroll vertical.
 *
 * @param results Lista de títulos em tendência.
 * @param onItemClick Callback chamado ao clicar num item.
 */
@Composable
private fun TrendingGrid(
    results: List<MediaItem>,
    onItemClick: (id: Int, mediaType: String) -> Unit
) {
    // Altura fixa necessária porque a grelha está num Column com scroll
    val itemHeight = 235.dp
    val rows = ceil(results.size / 3.0).toInt()
    val gridHeight = (itemHeight * rows) + (12.dp * (rows - 1))

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = Modifier.height(gridHeight),
        userScrollEnabled = false // O scroll é feito pelo Column exterior
    ) {
        items(results) { item ->
            TrendingItemCard(
                item = item,
                onClick = { onItemClick(item.id, item.mediaType) }
            )
        }
    }
}

/**
 * Lista horizontal com scroll para filmes ou séries populares.
 *
 * @param items Lista de títulos a apresentar.
 * @param onItemClick Callback chamado ao clicar num item.
 */
@Composable
private fun HorizontalMediaRow(
    items: List<MediaItem>,
    onItemClick: (MediaItem) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(items) { item ->
            HorizontalMediaCard(
                item = item,
                onClick = { onItemClick(item) }
            )
        }
    }
}

/**
 * Cartão individual para a lista horizontal.
 * Tem largura fixa de 140dp para manter consistência visual.
 *
 * @param item Dados do filme ou série.
 * @param onClick Callback chamado ao clicar no cartão.
 */
@Composable
private fun HorizontalMediaCard(
    item: MediaItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = if (item.posterUrl.startsWith("http")) item.posterUrl else "${TmdbClient.IMAGE_BASE_URL}${item.posterUrl}",
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
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.year,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

/**
 * Cartão individual para a grelha de tendências.
 * Apresenta o póster com uma etiqueta a indicar se é filme ou série.
 *
 * @param item Dados do filme ou série.
 * @param onClick Callback chamado ao clicar no cartão.
 */
@Composable
private fun TrendingItemCard(
    item: MediaItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .clickable(onClick = onClick)
    ) {
        Box {
            AsyncImage(
                model = if (item.posterUrl.startsWith("http")) item.posterUrl else "${TmdbClient.IMAGE_BASE_URL}${item.posterUrl}",
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
            )

            // Etiqueta no canto superior esquerdo
            Surface(
                modifier = Modifier
                    .padding(6.dp)
                    .align(Alignment.TopStart),
                shape = RoundedCornerShape(4.dp),
                color = BrandPurple.copy(alpha = 0.9f)
            ) {
                Text(
                    text = if (item.mediaType == "movie") "Filme" else "Série",
                    color = TextPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
        }
    }
}