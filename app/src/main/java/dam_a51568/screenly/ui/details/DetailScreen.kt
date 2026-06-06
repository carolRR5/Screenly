package dam_a51568.screenly.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import dam_a51568.screenly.data.remote.TmdbClient
import dam_a51568.screenly.data.repository.WatchStatus
import dam_a51568.screenly.data.repository.WatchlistRepository
import dam_a51568.screenly.ui.theme.BackgroundDark
import dam_a51568.screenly.ui.theme.BrandPurple
import dam_a51568.screenly.ui.theme.CardBackground
import dam_a51568.screenly.ui.theme.ErrorRed
import dam_a51568.screenly.ui.theme.TextPrimary
import dam_a51568.screenly.ui.theme.TextSecondary

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
        TopBar(onBack = onBack)

        when (val state = uiState) {
            is DetailUiState.Loading -> LoadingContent()
            is DetailUiState.Error -> ErrorContent(message = state.message)
            is DetailUiState.Success -> DetailContent(
                data = state.data,
                watchStatus = watchStatus,
                onAddToWatchlist = { status -> viewModel.addToWatchlist(state.data, status) },
                onRemoveFromWatchlist = {
                    viewModel.removeFromWatchlist(state.data.id, state.data.mediaType)
                },
                onSaveRatingAndReview = { rating, review ->
                    viewModel.saveRatingAndReview(state.data.id, state.data.mediaType, rating, review)
                }
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
        Text(text = message, color = ErrorRed, fontSize = 16.sp)
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
 * @param onSaveRatingAndReview Callback para guardar a classificação e a review.
 */
@Composable
private fun DetailContent(
    data: DetailUiData,
    watchStatus: WatchStatus?,
    onAddToWatchlist: (WatchStatus) -> Unit,
    onRemoveFromWatchlist: () -> Unit,
    onSaveRatingAndReview: (Float, String) -> Unit
) {
    // Obtém o item atual da watchlist para ler o rating e a review já guardados
    val currentItem = remember(watchStatus) {
        if (watchStatus != null) {
            WatchlistRepository.items
                .firstOrNull { it.id == data.id && it.mediaType == data.mediaType }
        } else null
    }

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

        // Coluna direita — Informações e ações
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

            // Secção de watchlist com avaliação e review
            WatchlistSection(
                watchStatus = watchStatus,
                currentRating = currentItem?.rating,
                currentReview = currentItem?.review,
                onAddToWatchlist = onAddToWatchlist,
                onRemoveFromWatchlist = onRemoveFromWatchlist,
                onSaveRatingAndReview = onSaveRatingAndReview
            )
        }
    }
}

/**
 * Secção de gestão da watchlist no ecrã de detalhes.
 * Quando o título está no estado WATCHED, apresenta também um sistema de
 * classificação por estrelas e um campo de texto para review pessoal.
 *
 * @param watchStatus Estado atual do título na watchlist, ou null se não estiver adicionado.
 * @param currentRating Classificação atual do título, ou null se não tiver sido atribuída.
 * @param currentReview Review actual do título, ou null se não tiver sido escrita.
 * @param onAddToWatchlist Callback para adicionar o título com o estado especificado.
 * @param onRemoveFromWatchlist Callback para remover o título da watchlist.
 * @param onSaveRatingAndReview Callback para guardar a classificação e a review.
 */
@Composable
private fun WatchlistSection(
    watchStatus: WatchStatus?,
    currentRating: Float?,
    currentReview: String?,
    onAddToWatchlist: (WatchStatus) -> Unit,
    onRemoveFromWatchlist: () -> Unit,
    onSaveRatingAndReview: (Float, String) -> Unit
) {
    // Estado local para a classificação e review enquanto o utilizador edita
    var selectedRating by remember(currentRating) { mutableFloatStateOf(currentRating ?: 0f) }
    var reviewText by remember(currentReview) { mutableStateOf(currentReview ?: "") }

    Text(
        text = "A minha lista",
        color = TextPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    )

    Spacer(modifier = Modifier.height(12.dp))

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        // Botões de estado das três listas
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

        // Secção de avaliação — apenas visível quando o título está em WATCHED
        if (watchStatus == WatchStatus.WATCHED) {

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "A minha avaliação",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sistema de 5 estrelas clicáveis
            StarRatingBar(
                rating = selectedRating,
                onRatingChange = { selectedRating = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Campo de texto para a review pessoal
            OutlinedTextField(
                value = reviewText,
                onValueChange = { reviewText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Escreve a tua review...",
                        color = TextSecondary
                    )
                },
                minLines = 3,
                maxLines = 6,
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

            Spacer(modifier = Modifier.height(8.dp))

            // Botão para guardar a avaliação e a review
            // Apenas ativo se o utilizador tiver selecionado pelo menos uma estrela
            Button(
                onClick = { onSaveRatingAndReview(selectedRating, reviewText) },
                enabled = selectedRating > 0f,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandPurple,
                    contentColor = TextPrimary,
                    disabledContainerColor = CardBackground,
                    disabledContentColor = TextSecondary
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Guardar avaliação")
            }
        }

        // Botão de remoção — apenas visível se o título já estiver na watchlist
        if (watchStatus != null) {
            OutlinedButton(
                onClick = onRemoveFromWatchlist,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = SolidColor(ErrorRed)
                )
            ) {
                Text(text = "Remover da lista")
            }
        }
    }
}

/**
 * Sistema de classificação por estrelas.
 * Apresenta 5 estrelas clicáveis — a estrela clicada define a classificação (1 a 5).
 * A estrela ativa é preenchida com a cor da marca; as inativas ficam a cinzento.
 *
 * @param rating Classificação atual (0f se ainda não foi atribuída).
 * @param onRatingChange Callback chamado quando o utilizador clica numa estrela.
 */
@Composable
private fun StarRatingBar(
    rating: Float,
    onRatingChange: (Float) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        (1..5).forEach { star ->
            Text(
                text = if (star <= rating) "★" else "☆",
                fontSize = 32.sp,
                color = if (star <= rating) BrandPurple else TextSecondary,
                modifier = Modifier.clickable {
                    onRatingChange(star.toFloat())
                }
            )
        }
    }
}

/**
 * Botão individual de estado da watchlist.
 * Quando selccionado, apresenta fundo preenchido; caso contrário, apresenta contorno.
 *
 * @param label Texto do botão ("To Watch", "Watching" ou "Watched").
 * @param isSelected Indica se este estado está atualmente selecionado.
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