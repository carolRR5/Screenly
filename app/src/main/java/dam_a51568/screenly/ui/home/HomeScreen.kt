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
 * Suporta Pull-to-Refresh através do [PullToRefreshBox], que deteta o gesto
 * de arrastar para baixo e invoca [HomeViewModel.refresh].
 *
 * @param onItemClick Callback invocado ao clicar num título, com o [id] e o
 *                    [mediaType] ("movie" ou "tv") para navegação para o detalhe.
 * @param viewModel ViewModel que gere o estado do ecrã; criado automaticamente
 *                  pelo Compose se não for fornecido explicitamente.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onItemClick: (id: Int, mediaType: String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    // Observa o estado da UI (Loading / Error / Success) como estado do Compose
    val uiState by viewModel.uiState.collectAsState()

    // Observa o estado do Pull-to-Refresh separadamente para não apagar o conteúdo durante o refresh
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    // PullToRefreshBox deteta o gesto de arrastar e chama onRefresh quando ativado
    PullToRefreshBox(
        isRefreshing = isRefreshing, // Controla a visibilidade do indicador de refresh
        onRefresh = { viewModel.refresh() }, // Dispara o recarregamento ao soltar o gesto
        modifier = Modifier
            .fillMaxSize() // Ocupa todo o ecrã disponível
            .background(BackgroundDark) // Fundo escuro mesmo durante o carregamento
    ) {
        // Seleciona o conteúdo a mostrar consoante o estado atual da UI
        when (val state = uiState) {
            // Estado de carregamento inicial: mostra spinner centrado
            is HomeUiState.Loading -> LoadingContent()

            // Estado de erro: mostra a mensagem e um botão para tentar novamente
            is HomeUiState.Error -> ErrorContent(
                message = state.message,
                onRetry = { viewModel.refresh() }
            )

            // Estado de sucesso: mostra as três secções de conteúdo
            is HomeUiState.Success -> HomeContent(
                data = state.data,
                onItemClick = onItemClick
            )
        }
    }
}

/**
 * Indicador de carregamento apresentado durante o primeiro carregamento dos dados.
 * Centra um [CircularProgressIndicator] no ecrã com a cor roxa da marca.
 */
@Composable
private fun LoadingContent() {
    // Box ocupa todo o espaço e centra o spinner horizontal e verticalmente
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = BrandPurple) // Spinner com cor da marca
    }
}

/**
 * Conteúdo apresentado quando ocorre um erro ao carregar os dados.
 * Mostra a mensagem de erro e um botão para tentar novamente.
 *
 * @param message Mensagem de erro a apresentar ao utilizador.
 * @param onRetry Callback invocado ao clicar no botão de nova tentativa.
 */
@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    // Coluna centrada com a mensagem de erro acima do botão
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,       // Centra o conteúdo verticalmente
        horizontalAlignment = Alignment.CenterHorizontally // Centra o conteúdo horizontalmente
    ) {
        // Mensagem de erro a vermelho para chamar a atenção do utilizador
        Text(text = message, color = ErrorRed, fontSize = 16.sp)

        Spacer(modifier = Modifier.height(16.dp)) // Espaço entre a mensagem e o botão

        // Botão de nova tentativa com cor roxa da marca
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
        ) {
            Text("Tentar Novamente")
        }
    }
}

/**
 * Conteúdo principal do ecrã de Início com as três secções de títulos.
 *
 * Usa um [Column] com scroll vertical para permitir navegar entre as secções.
 * A [TrendingGrid] tem altura fixa calculada dinamicamente porque [LazyVerticalGrid]
 * não pode ser usado num [Column] scrollable sem altura definida.
 *
 * @param data Dados das três secções carregados com sucesso da API.
 * @param onItemClick Callback invocado ao clicar num título.
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
            .verticalScroll(rememberScrollState()) // Ativa o scroll vertical de todo o conteúdo
            .padding(vertical = 16.dp) // Margem superior e inferior do conteúdo
    ) {
        // Secção 1 — Tendências da semana (grelha de 3 colunas)
        SectionHeader(title = "Em Tendência Esta Semana")
        Spacer(modifier = Modifier.height(12.dp)) // Espaço entre cabeçalho e grelha
        TrendingGrid(
            results = data.trending,
            onItemClick = onItemClick
        )

        Spacer(modifier = Modifier.height(24.dp)) // Espaço entre secções

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

        Spacer(modifier = Modifier.height(16.dp)) // Margem inferior após a última secção
    }
}

/**
 * Cabeçalho de uma secção do ecrã de Início.
 * Apresenta o título a negrito com padding horizontal para alinhar com os cartões.
 *
 * @param title Texto do cabeçalho da secção.
 */
@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = TextPrimary,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp) // Alinha com o padding lateral dos cartões
    )
}

/**
 * Grelha de 3 colunas para os títulos em tendência.
 *
 * Usa [LazyVerticalGrid] com altura fixa calculada dinamicamente porque,
 * dentro de um [Column] com scroll vertical, as grelhas lazy necessitam
 * de uma altura explícita — caso contrário o Compose não consegue medir o layout.
 *
 * @param results Lista de títulos em tendência a apresentar.
 * @param onItemClick Callback invocado ao clicar num cartão.
 */
@Composable
private fun TrendingGrid(
    results: List<MediaItem>,
    onItemClick: (id: Int, mediaType: String) -> Unit
) {
    // Altura fixa de cada cartão (póster + área de texto)
    val itemHeight = 235.dp

    // Número de linhas necessárias para acomodar todos os itens em 3 colunas
    val rows = ceil(results.size / 3.0).toInt()

    // Altura total da grelha: soma das alturas dos cartões mais o espaço entre linhas
    val gridHeight = (itemHeight * rows) + (12.dp * (rows - 1))

    LazyVerticalGrid(
        columns = GridCells.Fixed(3), // Grelha fixa de 3 colunas
        horizontalArrangement = Arrangement.spacedBy(12.dp), // Espaço horizontal entre cartões
        verticalArrangement = Arrangement.spacedBy(12.dp),   // Espaço vertical entre linhas
        contentPadding = PaddingValues(horizontal = 16.dp),  // Margem lateral da grelha
        modifier = Modifier.height(gridHeight), // Altura calculada dinamicamente
        userScrollEnabled = false // Scroll desativado: o Column exterior trata do scroll vertical
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
 * Usa [LazyRow] para renderização eficiente de listas longas na horizontal.
 *
 * @param items Lista de títulos a apresentar.
 * @param onItemClick Callback invocado ao clicar num cartão.
 */
@Composable
private fun HorizontalMediaRow(
    items: List<MediaItem>,
    onItemClick: (MediaItem) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp), // Espaço entre cartões na horizontal
        contentPadding = PaddingValues(horizontal = 16.dp) // Margem lateral da lista
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
 * Cartão individual para as listas horizontais de filmes e séries populares.
 * Tem largura fixa de 140dp para manter consistência visual entre cartões.
 *
 * @param item Dados do filme ou série a apresentar.
 * @param onClick Callback invocado ao clicar no cartão.
 */
@Composable
private fun HorizontalMediaCard(
    item: MediaItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(140.dp) // Largura fixa para uniformidade na lista horizontal
            .clip(RoundedCornerShape(12.dp)) // Cantos arredondados do cartão
            .background(CardBackground) // Fundo do cartão distinto do fundo do ecrã
            .clickable(onClick = onClick) // Torna o cartão inteiro clicável
    ) {
        AsyncImage(
            // Suporta URLs absolutas (http) e caminhos relativos do TMDb
            model = if (item.posterUrl.startsWith("http")) item.posterUrl
            else "${TmdbClient.IMAGE_BASE_URL}${item.posterUrl}",
            contentDescription = item.title, // Descrição para leitores de ecrã
            contentScale = ContentScale.Crop, // Recorta a imagem para preencher sem distorção
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f) // Rácio 2:3 — proporção padrão de póster de cinema
        )
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = item.title,
                color = TextPrimary,
                fontSize = 12.sp, // Tamanho reduzido para caber na largura de 140dp
                fontWeight = FontWeight.SemiBold,
                maxLines = 2, // Limita a 2 linhas para uniformidade entre cartões
                overflow = TextOverflow.Ellipsis // Adiciona "…" se o título for demasiado longo
            )
            Spacer(modifier = Modifier.height(2.dp)) // Pequeno espaço entre título e ano
            Text(
                text = item.year,
                color = TextSecondary, // Cor secundária para informação menos relevante
                fontSize = 11.sp
            )
        }
    }
}

/**
 * Cartão individual para a grelha de títulos em tendência.
 *
 * Semelhante ao [HorizontalMediaCard] mas sem largura fixa (adapta-se à coluna)
 * e com uma etiqueta no canto superior esquerdo do póster a indicar
 * se o título é um filme ou uma série.
 *
 * @param item Dados do filme ou série a apresentar.
 * @param onClick Callback invocado ao clicar no cartão.
 */
@Composable
private fun TrendingItemCard(
    item: MediaItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp)) // Cantos arredondados do cartão
            .background(CardBackground) // Fundo do cartão
            .clickable(onClick = onClick) // Torna o cartão inteiro clicável
    ) {
        // Box permite sobrepor a etiqueta de tipo sobre o canto do póster
        Box {
            AsyncImage(
                // Suporta URLs absolutas e caminhos relativos do TMDb
                model = if (item.posterUrl.startsWith("http")) item.posterUrl
                else "${TmdbClient.IMAGE_BASE_URL}${item.posterUrl}",
                contentDescription = item.title,
                contentScale = ContentScale.Crop, // Recorta sem distorção para preencher a área
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f) // Rácio 2:3 padrão de póster de cinema
            )

            // Etiqueta de tipo ("Filme" / "Série") sobreposta no canto superior esquerdo do póster
            Surface(
                modifier = Modifier
                    .padding(6.dp) // Afasta a etiqueta dos cantos do póster
                    .align(Alignment.TopStart), // Posiciona no canto superior esquerdo
                shape = RoundedCornerShape(4.dp), // Cantos ligeiramente arredondados
                color = BrandPurple.copy(alpha = 0.9f) // Roxo semitransparente para não tapar o póster
            ) {
                Text(
                    text = if (item.mediaType == "movie") "Filme" else "Série",
                    color = TextPrimary,
                    fontSize = 10.sp, // Tamanho reduzido para a etiqueta ser discreta
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp) // Padding interno da etiqueta
                )
            }
        }

        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = item.title,
                color = TextPrimary,
                fontSize = 13.sp, // Ligeiramente maior que na lista horizontal (mais espaço disponível)
                fontWeight = FontWeight.SemiBold,
                maxLines = 2, // Limita a 2 linhas para uniformidade da grelha
                overflow = TextOverflow.Ellipsis // Adiciona "…" se o título for demasiado longo
            )
            Spacer(modifier = Modifier.height(2.dp)) // Pequeno espaço entre título e ano
            Text(
                text = item.year,
                color = TextSecondary, // Cor secundária para o ano (informação secundária)
                fontSize = 11.sp
            )
        }
    }
}