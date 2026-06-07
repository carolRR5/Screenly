package dam_a51568.screenly.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import dam_a51568.screenly.data.models.TmdbMediaItem
import dam_a51568.screenly.data.remote.TmdbClient
import dam_a51568.screenly.ui.theme.BackgroundDark
import dam_a51568.screenly.ui.theme.BrandPurple
import dam_a51568.screenly.ui.theme.CardBackground
import dam_a51568.screenly.ui.theme.ErrorRed
import dam_a51568.screenly.ui.theme.TextPrimary
import dam_a51568.screenly.ui.theme.TextSecondary

/**
 * Ecrã principal da aplicação Screenly.
 *
 * Apresenta os títulos em tendência da semana obtidos da API do TMDb,
 * numa grelha de 3 colunas adequada para tablet.
 *
 * @param onItemClick Callback chamado quando o utilizador clica num título,
 *                    recebendo o id e o mediaType do item seleccionado.
 * @param viewModel ViewModel que gere o estado do ecrã.
 */
@Composable
fun HomeScreen(
    onItemClick: (id: Int, mediaType: String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        // Cabeçalho do ecrã
        Text(
            text = "Em Tendência",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Os títulos mais populares desta semana",
            color = TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Conteúdo consoante o estado actual
        when (val state = uiState) {
            is HomeUiState.Loading -> LoadingContent()
            is HomeUiState.Error -> ErrorContent(message = state.message)
            is HomeUiState.Success -> TrendingGrid(
                results = state.results,
                onItemClick = onItemClick
            )
        }
    }
}

/**
 * Indicador de carregamento apresentado enquanto os dados estão a ser obtidos da API.
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
 * Conteúdo apresentado quando ocorre um erro ao carregar os títulos em tendência.
 *
 * @param message Mensagem de erro a apresentar ao utilizador.
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
 * Grelha de títulos em tendência com 3 colunas, adequada para tablet.
 *
 * @param results Lista de títulos em tendência devolvidos pela API.
 * @param onItemClick Callback chamado quando o utilizador clica num item.
 */
@Composable
private fun TrendingGrid(
    results: List<TmdbMediaItem>,
    onItemClick: (id: Int, mediaType: String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
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
 * Cartão individual de um título em tendência.
 * Apresenta o poster, o título, o ano e uma etiqueta indicando
 * se é um filme ou uma série.
 *
 * @param item Dados do filme ou série.
 * @param onClick Callback chamado quando o utilizador clica no cartão.
 */
@Composable
private fun TrendingItemCard(
    item: TmdbMediaItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .clickable(onClick = onClick)
    ) {
        // Poster com etiqueta de tipo (Filme/Série) sobreposta
        Box {
            AsyncImage(
                model = "${TmdbClient.IMAGE_BASE_URL}${item.posterPath}",
                contentDescription = item.displayTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
            )

            // Etiqueta no canto superior esquerdo a indicar o tipo de conteúdo
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

        // Informações do título
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = item.displayTitle,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.displayYear,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}