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
    // Observa de forma reativa o estado atual do ecrã (Idle, Loading, Empty, Error ou Success)
    val uiState by viewModel.uiState.collectAsState()
    // Observa de forma reativa o texto atual escrito na barra de pesquisa
    val query by viewModel.query.collectAsState()

    // Coluna principal que ocupa todo o ecrã, com fundo escuro e padding nas margens
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

        // Espaço entre o título e a barra de pesquisa
        Spacer(modifier = Modifier.height(24.dp))

        // Barra de pesquisa, que reporta ao ViewModel sempre que o texto muda
        SearchBar(
            query = query,
            onQueryChange = viewModel::onQueryChange
        )

        // Espaço entre a barra de pesquisa e o conteúdo abaixo (resultados, etc.)
        Spacer(modifier = Modifier.height(80.dp))

        // Renderiza o conteúdo apropriado consoante o estado atual do ecrã
        when (val state = uiState) {
            // Estado inicial: ainda não houve pesquisa, mostra categorias para explorar
            is SearchUiState.Idle -> IdleContent(
                onCategoryClick = onCategoryClick,
                onGenreClick = onGenreClick,
                onCountryClick = onCountryClick
            )
            // A pesquisa está a ser efetuada na API, mostra um indicador de carregamento
            is SearchUiState.Loading -> LoadingContent()
            // A pesquisa terminou, mas não devolveu resultados
            is SearchUiState.Empty -> EmptyContent(query = query)
            // Ocorreu um erro durante a pesquisa, mostra a mensagem de erro
            is SearchUiState.Error -> ErrorContent(message = state.message)
            // A pesquisa devolveu resultados, mostra-os numa grelha
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
    // Campo de texto com bordo, usado como barra de pesquisa
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        placeholder = {
            // Texto exibido quando o campo está vazio
            Text(text = "Pesquisar filmes e séries...",
                color = TextSecondary,
                fontSize = 16.sp
            )
        },
        leadingIcon = {
            // Ícone de lupa à esquerda do campo
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
    // Coluna que ocupa todo o espaço disponível, com a lista de categorias a explorar
    Column(modifier = Modifier.fillMaxSize()) {
        // Título da secção
        Text(
            text = "Explorar por",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Texto descritivo da secção
        Text(
            text = "Descobre novos filmes e séries por categoria",
            color = TextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Item para navegar para os títulos mais populares
        BrowseItem(
            title = "Mais Populares",
            description = "Os títulos mais vistos agora",
            onClick = { onCategoryClick(BrowseFilter.POPULAR) }
        )
        BrowseDivider()
        // Item para navegar para os títulos mais bem classificados
        BrowseItem(
            title = "Melhor Classificados",
            description = "Os títulos com melhor nota da comunidade",
            onClick = { onCategoryClick(BrowseFilter.TOP_RATED) }
        )
        BrowseDivider()
        // Item para navegar para os lançamentos mais recentes
        BrowseItem(
            title = "Lançamentos Recentes",
            description = "Títulos lançados nos últimos 6 meses",
            onClick = { onCategoryClick(BrowseFilter.RECENT) }
        )
        BrowseDivider()
        // Item que abre o ecrã de seleção de géneros
        BrowseItem(
            title = "Por Género",
            description = "Acção, Comédia, Drama, Terror e mais",
            onClick = onGenreClick
        )
        BrowseDivider()
        // Item que abre o ecrã de seleção de países
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
    // Linha clicável com título/descrição à esquerda e seta indicativa à direita
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Coluna com o título e a descrição, ocupa o espaço restante (weight = 1f)
        Column(modifier = Modifier.weight(1f)) {
            // Título do item de navegação
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
            // Pequeno espaço entre o título e a descrição
            Spacer(modifier = Modifier.height(2.dp))
            // Descrição/subtítulo do item
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        // Símbolo ">" indicando que o item é navegável
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
    // Linha horizontal fina, sem padding adicional
    HorizontalDivider(color = CardBackground, thickness = 1.dp)
}

/**
 * Indicador de carregamento apresentado enquanto a API está a ser consultada.
 */
@Composable
private fun LoadingContent() {
    // Box que ocupa todo o ecrã, centrando o spinner
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Indicador de progresso circular, na cor da marca
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
    // Box que ocupa todo o ecrã, centrando a mensagem
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Mensagem informando que não há resultados para o termo pesquisado
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
    // Box que ocupa todo o ecrã, centrando a mensagem de erro
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Mensagem de erro, apresentada na cor de erro definida no tema
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
    // Grelha vertical com 3 colunas fixas, com espaçamento horizontal e vertical entre itens
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Itera sobre os resultados e renderiza um cartão para cada item
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
    // Coluna com cantos arredondados e fundo de "cartão", clicável
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .clickable(onClick = onClick)
    ) {
        // Imagem do poster, carregada de forma assíncrona a partir de uma URL
        AsyncImage(
            // Se o posterUrl já for uma URL completa, usa-a diretamente;
            // caso contrário, concatena com a base de imagens da TMDB
            model = if (item.posterUrl.startsWith("http")) item.posterUrl else "${TmdbClient.IMAGE_BASE_URL}${item.posterUrl}",
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
        )

        // Coluna com o título e o ano do item, abaixo do poster
        Column(modifier = Modifier.padding(8.dp)) {
            // Título do filme/série, limitado a 2 linhas, com "..." se for muito longo
            Text(
                text = item.title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            // Pequeno espaço entre o título e o ano
            Spacer(modifier = Modifier.height(2.dp))
            // Ano de lançamento do filme/série
            Text(
                text = item.year,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}