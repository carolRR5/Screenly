package dam_a51568.screenly.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import dam_a51568.screenly.data.remote.TmdbClient
import dam_a51568.screenly.data.repository.WatchStatus

// Cores da paleta da aplicação.
private val BackgroundDark = Color(0xFF121829)
private val CardBackground = Color(0xFF1A2236)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF8F9CAE)
private val BrandPurple = Color(0xFF6C5CE7)

/**
 * Ecrã de Detalhes da aplicação Screenly.
 *
 * Apresenta os dados completos de um filme ou série carregados da API do TMDb,
 * e permite ao utilizador gerir o título na sua watchlist pessoal.
 *
 * O layout usa uma linha horizontal (Row) para aproveitar o espaço do tablet:
 * poster à esquerda, informações e botões à direita.
 *
 * @param id Identificador único do título no TMDb.
 * @param mediaType Tipo de conteúdo: "movie" ou "tv".
 * @param onBack Callback chamado quando o utilizador carrega no botão de retroceder.
 * @param viewModel ViewModel que gere o estado do ecrã.
 */
@Composable
fun DetailScreen(
    id: Int,
    mediaType: String,
    onBack: () -> Unit,
    viewModel: DetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val watchStatus by viewModel.watchStatus.collectAsState()

    // Carrega os detalhes ao entrar no ecrã
    LaunchedEffect(id, mediaType) {
        viewModel.loadDetails(id, mediaType)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Barra de topo com botão de retroceder
        TopBar(onBack = onBack)

        when (val state = uiState) {
            is DetailUiState.Loading -> LoadingContent()
            is DetailUiState.Error -> ErrorContent(message = state.message)
            is DetailUiState.Success -> DetailContent(
                data = state.data,
                watchStatus = watchStatus,
                onAddToWatchlist = { status -> viewModel.addToWatchlist(state.data, status) },
                onRemoveFromWatchlist = { viewModel.removeFromWatchlist(state.data.id, state.data.mediaType) }
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
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Retroceder",
                tint = TextPrimary
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
 * Conteúdo apresentado quando ocorre um erro ao carregar os detalhes.
 *
 * @param message Mensagem de erro a apresentar ao utilizador.
 */
@Composable
private fun ErrorContent(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = Color(0xFFE17055), fontSize = 16.sp)
    }
}

/**
 * Conteúdo principal do ecrã de detalhes.
 *
 * Usa um layout horizontal (Row) para aproveitar o espaço do tablet:
 * - Coluna esquerda: poster do título
 * - Coluna direita: informações completas e botões de watchlist
 *
 * @param data Dados do título a apresentar.
 * @param watchStatus Estado atual do título na watchlist, ou null se não estiver adicionado.
 * @param onAddToWatchlist Callback para adicionar ou mover o título para um estado.
 * @param onRemoveFromWatchlist Callback para remover o título da watchlist.
 */
@Composable
private fun DetailContent(
    data: DetailUiData,
    watchStatus: WatchStatus?,
    onAddToWatchlist: (WatchStatus) -> Unit,
    onRemoveFromWatchlist: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // Coluna esquerda — Poster
        AsyncImage(
            model = "${TmdbClient.IMAGE_BASE_URL}${data.posterPath}",
            contentDescription = data.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(260.dp)
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(16.dp))
        )

        // Coluna direita — Informações e acções
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Título
            Text(
                text = data.title,
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Ano e duração
            Text(
                text = "${data.year}  •  ${data.runtime}",
                color = TextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Classificação TMDb
            Text(
                text = "⭐ ${"%.1f".format(data.voteAverage)} / 10",
                color = TextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Géneros
            if (data.genres.isNotEmpty()) {
                Text(
                    text = data.genres.joinToString(" • ") { it.name },
                    color = BrandPurple,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sinopse
            Text(
                text = "Sinopse",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = data.overview,
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Secção de watchlist
            WatchlistSection(
                watchStatus = watchStatus,
                onAddToWatchlist = onAddToWatchlist,
                onRemoveFromWatchlist = onRemoveFromWatchlist
            )
        }
    }
}

/**
 * Secção de gestão da watchlist no ecrã de detalhes.
 *
 * Se o título ainda não estiver na watchlist, apresenta três botões para o adicionar
 * a "To Watch", "Watching" ou "Watched".
 * Se já estiver adicionado, mostra o estado atual e um botão para remover.
 *
 * @param watchStatus Estado atual do título na watchlist, ou null se não estiver adicionado.
 * @param onAddToWatchlist Callback para adicionar o título com o estado especificado.
 * @param onRemoveFromWatchlist Callback para remover o título da watchlist.
 */
@Composable
private fun WatchlistSection(
    watchStatus: WatchStatus?,
    onAddToWatchlist: (WatchStatus) -> Unit,
    onRemoveFromWatchlist: () -> Unit
) {
    Text(
        text = "A minha lista",
        color = TextPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    )

    Spacer(modifier = Modifier.height(12.dp))

    if (watchStatus == null) {
        // Título ainda não está na watchlist — mostra os três botões de adição
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WatchlistButton(
                label = "To Watch",
                isSelected = false,
                onClick = { onAddToWatchlist(WatchStatus.TO_WATCH) }
            )
            WatchlistButton(
                label = "Watching",
                isSelected = false,
                onClick = { onAddToWatchlist(WatchStatus.WATCHING) }
            )
            WatchlistButton(
                label = "Watched",
                isSelected = false,
                onClick = { onAddToWatchlist(WatchStatus.WATCHED) }
            )
        }
    } else {
        // Título já está na watchlist — mostra o estado actual e opção de remover
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WatchlistButton(
                    label = "To Watch",
                    isSelected = watchStatus == WatchStatus.TO_WATCH,
                    onClick = { onAddToWatchlist(WatchStatus.TO_WATCH) }
                )
                WatchlistButton(
                    label = "Watching",
                    isSelected = watchStatus == WatchStatus.WATCHING,
                    onClick = { onAddToWatchlist(WatchStatus.WATCHING) }
                )
                WatchlistButton(
                    label = "Watched",
                    isSelected = watchStatus == WatchStatus.WATCHED,
                    onClick = { onAddToWatchlist(WatchStatus.WATCHED) }
                )
            }
            // Botão de remoção
            OutlinedButton(
                onClick = onRemoveFromWatchlist,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE17055)),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE17055))
                )
            ) {
                Text(text = "Remover da lista")
            }
        }
    }
}

/**
 * Botão individual de estado da watchlist.
 * Quando selecionado, apresenta fundo preenchido; caso contrário, apresenta contorno.
 *
 * @param label Texto do botão ("To Watch", "Watching" ou "Watched").
 * @param isSelected Indica se este estado está actualmente seleccionado.
 * @param onClick Callback chamado ao clicar no botão.
 */
@Composable
private fun WatchlistButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) BrandPurple else CardBackground,
            contentColor = if (isSelected) Color.White else TextSecondary
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = label, fontSize = 13.sp)
    }
}