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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import dam_a51568.screenly.data.model.MediaItem
import dam_a51568.screenly.data.remote.TmdbClient
import dam_a51568.screenly.ui.browse.BrowseFilter
import dam_a51568.screenly.ui.theme.BackgroundDark
import dam_a51568.screenly.ui.theme.BrandPurple
import dam_a51568.screenly.ui.theme.CardBackground
import dam_a51568.screenly.ui.theme.ErrorRed
import dam_a51568.screenly.ui.theme.TextPrimary
import dam_a51568.screenly.ui.theme.TextSecondary

/**
 * Ecrã de Pesquisa da aplicação Screenly.
 *
 * Apresenta uma barra de pesquisa no topo e uma grelha de resultados com 3 colunas,
 * adequada para tablet (uma vez que é o dispositivo físico que irá ser utilizado). Gere os estados
 * de idle, loading, vazio e erro.
 *
 * @param onItemClick Callback chamado quando o utilizador clica num resultado, recebendo o id e o mediaType do item selecionado.
 * @param viewModel ViewModel que gere o estado do ecrã.
 */
@Composable
fun SearchScreen(
    onItemClick: (id: Int, mediaType: String) -> Unit,
    onCategoryClick: (BrowseFilter) -> Unit,
    onGenreClick: () -> Unit,
    onCountryClick: () -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val query by viewModel.query.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Título do ecrã
        Text(
            text = "Pesquisa",
            color = TextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        SearchBar(
            query = query,
            onQueryChange = viewModel::onQueryChange
        )

        Spacer(modifier = Modifier.height(80.dp))

        when (val state = uiState) {
            is SearchUiState.Idle -> IdleContent(
                onCategoryClick = onCategoryClick,
                onGenreClick = onGenreClick,
                onCountryClick = onCountryClick
            )
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
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        placeholder = {
            Text(text = "Pesquisar filmes e séries...",
                color = TextSecondary,
                fontSize = 16.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Pesquisar",
                tint = BrandPurple,
                modifier = Modifier.size(24.dp)
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
        ),
        textStyle = TextStyle(fontSize = 16.sp)
    )
}

/**
 * Conteúdo apresentado no estado inicial, antes de qualquer pesquisa.
 * Apresenta a secção "Explorar por" com categorias clicáveis.
 *
 * @param onCategoryClick Callback chamado ao clicar numa categoria simples.
 * @param onGenreClick Callback chamado ao clicar em "Por Género".
 * @param onCountryClick Callback chamado ao clicar em "Por País".
 */
@Composable
private fun IdleContent(
    onCategoryClick: (BrowseFilter) -> Unit,
    onGenreClick: () -> Unit,
    onCountryClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Explorar por",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Descobre novos filmes e séries por categoria",
            color = TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        BrowseItem(
            title = "Mais Populares",
            description = "Os títulos mais vistos agora",
            onClick = { onCategoryClick(BrowseFilter.POPULAR) }
        )
        BrowseDivider()
        BrowseItem(
            title = "Melhor Classificados",
            description = "Os títulos com melhor nota da comunidade",
            onClick = { onCategoryClick(BrowseFilter.TOP_RATED) }
        )
        BrowseDivider()
        BrowseItem(
            title = "Lançamentos Recentes",
            description = "Títulos lançados nos últimos 6 meses",
            onClick = { onCategoryClick(BrowseFilter.RECENT) }
        )
        BrowseDivider()
        BrowseItem(
            title = "Por Género",
            description = "Acção, Comédia, Drama, Terror e mais",
            onClick = onGenreClick
        )
        BrowseDivider()
        BrowseItem(
            title = "Por País",
            description = "Explora cinema de todo o mundo",
            onClick = onCountryClick
        )
    }
}

/**
 * Item individual da secção "Explorar por".
 *
 * @param title Texto do item.
 * @param onClick Callback chamado ao clicar no item.
 */
@Composable
private fun BrowseItem(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        Text(
            text = "›",
            color = TextSecondary,
            fontSize = 24.sp
        )
    }
}

/**
 * Linha divisória entre itens da secção "Explorar por".
 */
@Composable
private fun BrowseDivider() {
    HorizontalDivider(color = CardBackground, thickness = 1.dp)
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
            fontSize = 18.sp
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
        Text(text = message, color = ErrorRed, fontSize = 16.sp)
    }
}

/**
 * Grelha de resultados com 3 colunas, adequada para tablet.
 *
 * @param results Lista de itens devolvidos pela API.
 * @param onItemClick Callback chamado quando o utilizador clica num item.
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
 *
 * @param item Dados do filme ou série.
 * @param onClick Callback chamado quando o utilizador clica no cartão.
 */
@Composable
private fun MediaItemCard(
    item: MediaItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
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