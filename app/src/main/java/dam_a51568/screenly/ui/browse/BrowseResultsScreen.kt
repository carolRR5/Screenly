package dam_a51568.screenly.ui.browse

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

/**
 * Ecrã de resultados de navegação da aplicação Screenly.
 *
 * Ecrã reutilizável que apresenta os resultados de qualquer filtro
 * numa grelha de 3 colunas. Recebe o tipo de filtro e os argumentos
 * correspondentes como parâmetros.
 *
 * @param title Título a apresentar na barra de topo.
 * @param filter Tipo de filtro a aplicar.
 * @param genreId ID do género a filtrar (apenas para [BrowseFilter.GENRE]).
 * @param countryCode Código do país a filtrar (apenas para [BrowseFilter.COUNTRY]).
 * @param onBack Callback chamado ao clicar no botão de retroceder.
 * @param onItemClick Callback chamado ao clicar num título.
 */
@Composable
fun BrowseResultsScreen(
    title: String,
    filter: BrowseFilter,
    genreId: String? = null,
    countryCode: String? = null,
    onBack: () -> Unit,
    onItemClick: (id: Int, mediaType: String) -> Unit
) {
    val viewModel: BrowseResultsViewModel = viewModel(
        factory = BrowseResultsViewModelFactory(filter, genreId, countryCode)
    )

    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        TopBar(title = title, onBack = onBack)

        when (val state = uiState) {
            is BrowseUiState.Loading -> LoadingContent()
            is BrowseUiState.Error -> ErrorContent(message = state.message)
            is BrowseUiState.Success -> ResultsGrid(
                results = state.results,
                onItemClick = onItemClick
            )
        }
    }
}

/**
 * Barra de topo com botão de retroceder e título.
 */
@Composable
private fun TopBar(title: String, onBack: () -> Unit) {
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
            text = title,
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Indicador de carregamento.
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
 * Conteúdo de erro.
 */
@Composable
private fun ErrorContent(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = ErrorRed, fontSize = 16.sp)
    }
}

/**
 * Grelha de 3 colunas com os resultados filtrados.
 *
 * @param results Lista de títulos a apresentar.
 * @param onItemClick Callback chamado ao clicar num título.
 */
@Composable
private fun ResultsGrid(
    results: List<MediaItem>,
    onItemClick: (id: Int, mediaType: String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(results) { item ->
            BrowseMediaCard(
                item = item,
                onClick = { onItemClick(item.id, item.mediaType) }
            )
        }
    }
}

/**
 * Cartão individual de um título nos resultados de navegação.
 *
 * @param item Dados do filme ou série.
 * @param onClick Callback chamado ao clicar no cartão.
 */
@Composable
private fun BrowseMediaCard(
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

            // Etiqueta Filme/Série
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