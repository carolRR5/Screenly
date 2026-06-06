package dam_a51568.screenly.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import dam_a51568.screenly.data.models.TmdbMediaItem
import dam_a51568.screenly.data.remote.TmdbClient

/** Cores da paleta da aplicação. */
private val BackgroundDark = Color(0xFF121829)
private val CardBackground = Color(0xFF1A2236)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF8F9CAE)
private val BrandPurple = Color(0xFF6C5CE7)

/**
 * Ecrã de Pesquisa da aplicação Screenly.
 *
 * Apresenta uma barra de pesquisa no topo e uma grelha de resultados com 3 colunas,
 * adequada para tablet. Gere os estados de idle, loading, vazio e erro.
 *
 * @param onItemClick Callback chamado quando o utilizador clica num resultado,
 *                    recebendo o id e o mediaType do item selecionado.
 * @param viewModel ViewModel que gere o estado do ecrã.
 */
@Composable
fun SearchScreen(
    onItemClick: (id: Int, mediaType: String) -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val query by viewModel.query.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        // Barra de pesquisa
        SearchBar(
            query = query,
            onQueryChange = viewModel::onQueryChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Conteúdo consoante o estado actual
        when (val state = uiState) {
            is SearchUiState.Idle -> IdleContent()
            is SearchUiState.Loading -> LoadingContent()
            is SearchUiState.Empty -> EmptyContent(query = query)
            is SearchUiState.Error -> ErrorContent(message = state.message)
            is SearchUiState.Success -> ResultsGrid(
                results = state.results,
                onItemClick = onItemClick
            )
        }
    }
}

/**
 * Barra de pesquisa com ícone de lupa.
 * O texto é enviado ao ViewModel em cada alteração, que aplica debounce internamente.
 *
 * @param query Texto actual da pesquisa.
 * @param onQueryChange Callback chamado quando o utilizador altera o texto.
 */
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = "Pesquisar filmes e séries...",
                color = TextSecondary
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Pesquisar",
                tint = BrandPurple
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = BrandPurple,
            unfocusedBorderColor = TextSecondary,
            cursorColor = BrandPurple,
            focusedContainerColor = CardBackground,
            unfocusedContainerColor = CardBackground
        )
    )
}

/**
 * Conteúdo apresentado no estado inicial, antes de qualquer pesquisa.
 */
@Composable
private fun IdleContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Pesquisa um filme ou série",
                color = TextSecondary,
                fontSize = 18.sp
            )
        }
    }
}

/**
 * Indicador de carregamento apresentado enquanto a API está a ser consultada.
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
 * Conteúdo apresentado quando a pesquisa não devolve resultados.
 *
 * @param query Texto pesquisado, apresentado na mensagem.
 */
@Composable
private fun EmptyContent(query: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Sem resultados para \"$query\"",
            color = TextSecondary,
            fontSize = 16.sp
        )
    }
}

/**
 * Conteúdo apresentado quando ocorre um erro na chamada à API.
 *
 * @param message Mensagem de erro a apresentar ao utilizador.
 */
@Composable
private fun ErrorContent(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = Color(0xFFE17055),
            fontSize = 16.sp
        )
    }
}

/**
 * Grelha de resultados com 3 colunas, adequada para tablet.
 * Cada item mostra o poster, o título e o ano do filme ou série.
 *
 * @param results Lista de itens devolvidos pela API.
 * @param onItemClick Callback chamado quando o utilizador clica num item.
 */
@Composable
private fun ResultsGrid(
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
            MediaItemCard(
                item = item,
                onClick = { onItemClick(item.id, item.mediaType) }
            )
        }
    }
}

/**
 * Cartão individual de um resultado de pesquisa.
 * Apresenta o poster do título, o nome e o ano de lançamento.
 *
 * @param item Dados do filme ou série.
 * @param onClick Callback chamado quando o utilizador clica no cartão.
 */
@Composable
private fun MediaItemCard(
    item: TmdbMediaItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .clickable(onClick = onClick)
    ) {
        // Poster do filme/série carregado com Coil
        AsyncImage(
            model = "${TmdbClient.IMAGE_BASE_URL}${item.posterPath}",
            contentDescription = item.displayTitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f) // Proporção standard de poster de filme
        )

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