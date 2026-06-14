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
 * Ecrã reutilizável que apresenta os resultados de qualquer filtro numa grelha
 * de 3 colunas. É partilhado por todos os pontos de entrada do Browse (popular,
 * melhor classificação, recentes, género, país), variando apenas o [filter] e
 * os argumentos opcionais [genreId] e [countryCode].
 *
 * @param title Título a apresentar na barra de topo (ex: "Ação", "Portugal").
 * @param filter Tipo de filtro a aplicar aos resultados.
 * @param genreId ID numérico do género no TMDb, usado apenas com [BrowseFilter.GENRE].
 * @param countryCode Código ISO 3166-1 alpha-2 do país, usado apenas com [BrowseFilter.COUNTRY].
 * @param onBack Callback invocado ao clicar no botão de retroceder.
 * @param onItemClick Callback invocado ao clicar num título, com o [id] e o [mediaType].
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
    // Cria o ViewModel com a factory para injetar o filtro e os argumentos opcionais
    val viewModel: BrowseResultsViewModel = viewModel(
        factory = BrowseResultsViewModelFactory(filter, genreId, countryCode)
    )

    // Observa o estado da UI (Loading / Error / Success) como estado do Compose
    val uiState by viewModel.uiState.collectAsState()

    // Coluna raiz com fundo escuro que ocupa todo o ecrã
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Barra de topo com botão de retroceder e título dinâmico do filtro
        TopBar(title = title, onBack = onBack)

        // Seleciona o conteúdo a mostrar consoante o estado atual da UI
        when (val state = uiState) {
            // Estado de carregamento: spinner centrado enquanto a API responde
            is BrowseUiState.Loading -> LoadingContent()

            // Estado de erro: mensagem centrada a vermelho
            is BrowseUiState.Error -> ErrorContent(message = state.message)

            // Estado de sucesso: grelha com os resultados intercalados
            is BrowseUiState.Success -> ResultsGrid(
                results = state.results,
                onItemClick = onItemClick
            )
        }
    }
}

/**
 * Barra de topo com botão de retroceder e título do ecrã.
 *
 * @param title Título dinâmico a apresentar (nome do filtro, género ou país).
 * @param onBack Callback invocado ao clicar no botão de retroceder.
 */
@Composable
private fun TopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp), // Padding reduzido para compactar o topo
        verticalAlignment = Alignment.CenterVertically // Alinha botão e título ao centro vertical
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Ícone espelhado para idiomas RTL
                contentDescription = "Retroceder", // Descrição para acessibilidade
                tint = TextPrimary
            )
        }
        // Título dinâmico que reflete o filtro ativo (ex: "Ação", "Portugal", "Populares")
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Indicador de carregamento centrado no ecrã.
 * Apresentado enquanto a API responde com os resultados do filtro.
 */
@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center // Centra o spinner horizontal e verticalmente
    ) {
        CircularProgressIndicator(color = BrandPurple) // Spinner com cor da marca
    }
}

/**
 * Conteúdo de erro centrado no ecrã.
 * Apresentado quando a chamada à API falha.
 *
 * @param message Mensagem de erro a apresentar ao utilizador.
 */
@Composable
private fun ErrorContent(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center // Centra a mensagem horizontal e verticalmente
    ) {
        Text(text = message, color = ErrorRed, fontSize = 16.sp) // Texto a vermelho para erro
    }
}

/**
 * Grelha de 3 colunas com os resultados filtrados.
 *
 * Usa [LazyVerticalGrid] para renderização eficiente de listas longas,
 * carregando apenas os cartões visíveis no ecrã. Os resultados são
 * filmes e séries intercalados pelo ViewModel para variedade visual.
 *
 * @param results Lista de títulos a apresentar, já intercalados pelo ViewModel.
 * @param onItemClick Callback invocado ao clicar num cartão.
 */
@Composable
private fun ResultsGrid(
    results: List<MediaItem>,
    onItemClick: (id: Int, mediaType: String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3), // Grelha fixa de 3 colunas
        horizontalArrangement = Arrangement.spacedBy(12.dp), // Espaço horizontal entre cartões
        verticalArrangement = Arrangement.spacedBy(12.dp),   // Espaço vertical entre linhas
        contentPadding = PaddingValues(16.dp) // Margem exterior em torno da grelha
    ) {
        items(results) { item ->
            BrowseMediaCard(
                item = item,
                // Passa o id e o mediaType ao callback para navegação para o detalhe
                onClick = { onItemClick(item.id, item.mediaType) }
            )
        }
    }
}

/**
 * Cartão individual de um título nos resultados de navegação.
 *
 * Idêntico ao [TrendingItemCard] do ecrã principal, com a etiqueta
 * "Filme" / "Série" sobreposta no canto superior esquerdo do póster
 * para distinguir o tipo de conteúdo na grelha mista.
 *
 * @param item Dados do filme ou série a apresentar.
 * @param onClick Callback invocado ao clicar no cartão.
 */
@Composable
private fun BrowseMediaCard(
    item: MediaItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp)) // Cantos arredondados aplicados a toda a coluna
            .background(CardBackground) // Fundo de cartão distinto do fundo do ecrã
            .clickable(onClick = onClick) // Torna todo o cartão clicável
    ) {
        // Box permite sobrepor a etiqueta de tipo sobre o canto do póster
        Box {
            AsyncImage(
                // Suporta URLs absolutas (http) e caminhos relativos do TMDb
                model = if (item.posterUrl.startsWith("http")) item.posterUrl
                else "${TmdbClient.IMAGE_BASE_URL}${item.posterUrl}",
                contentDescription = item.title,  // Descrição para leitores de ecrã
                contentScale = ContentScale.Crop, // Recorta sem distorção para preencher a área
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f) // Rácio 2:3 — proporção padrão de póster de cinema
            )

            // Etiqueta "Filme" / "Série" sobreposta no canto superior esquerdo do póster
            Surface(
                modifier = Modifier
                    .padding(6.dp) // Afasta a etiqueta dos cantos do póster
                    .align(Alignment.TopStart), // Posiciona no canto superior esquerdo
                shape = RoundedCornerShape(4.dp), // Cantos ligeiramente arredondados
                color = BrandPurple.copy(alpha = 0.9f)  // Roxo semitransparente para não tapar o póster
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
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2, // Limita a 2 linhas para uniformidade da grelha
                overflow = TextOverflow.Ellipsis // Adiciona "…" se o título for demasiado longo
            )
            Spacer(modifier = Modifier.height(2.dp)) // Pequeno espaço entre título e ano
            Text(
                text = item.year,
                color = TextSecondary, // Cor secundária para o ano (informação menos relevante)
                fontSize = 11.sp
            )
        }
    }
}