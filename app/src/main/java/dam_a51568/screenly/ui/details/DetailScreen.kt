package dam_a51568.screenly.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dam_a51568.screenly.data.repository.WatchStatus
import dam_a51568.screenly.data.repository.WatchlistRepository
import dam_a51568.screenly.ui.theme.BackgroundDark
import dam_a51568.screenly.ui.theme.BrandPurple
import dam_a51568.screenly.ui.theme.CardBackground
import dam_a51568.screenly.ui.theme.ErrorRed
import dam_a51568.screenly.ui.theme.TextPrimary

/**
 * Separadores disponíveis na secção de créditos do ecrã de detalhes.
 */
enum class CreditsTab {
    CAST,
    CREW
}

/**
 * Ecrã de Detalhes da aplicação Screenly.
 *
 * Orquestra as várias secções do ecrã:
 * - [DetailMainInfo] — póster, informações e watchlist
 * - [DetailRatingSection] — avaliação pessoal (apenas em WATCHED)
 * - [DetailCreditsSection] — elenco e crew
 * - [DetailSimilarSection] — títulos similares
 * - [DetailReviewsSection] — reviews da comunidade
 *
 * @param id Identificador único do título no TMDb.
 * @param mediaType Tipo de conteúdo: "movie" ou "tv".
 * @param onBack Callback chamado ao clicar no botão de retroceder.
 * @param onSimilarItemClick Callback chamado ao clicar num título similar.
 * @param viewModel ViewModel que gere o estado do ecrã.
 */
@Composable
fun DetailScreen(
    id: Int,
    mediaType: String,
    onBack: () -> Unit,
    onSimilarItemClick: (id: Int, mediaType: String) -> Unit,
    viewModel: DetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val watchStatus by viewModel.watchStatus.collectAsState()
    val cast by viewModel.cast.collectAsState()
    val crew by viewModel.crew.collectAsState()
    val trailerUrl by viewModel.trailerUrl.collectAsState()
    val reviews by viewModel.reviews.collectAsState()
    val visibleReviewsCount by viewModel.visibleReviewsCount.collectAsState()
    val similarTitles by viewModel.similarTitles.collectAsState()

    LaunchedEffect(id, mediaType) {
        viewModel.loadDetails(id, mediaType)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Barra de topo com botão de retroceder
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

        when (val state = uiState) {
            is DetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandPurple)
                }
            }
            is DetailUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.message, color = ErrorRed)
                }
            }
            is DetailUiState.Success -> {
                val currentItem = remember(watchStatus) {
                    if (watchStatus != null) {
                        WatchlistRepository.items
                            .firstOrNull { it.id == state.data.id && it.mediaType == state.data.mediaType }
                    } else null
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Póster + Informações + Watchlist
                    DetailMainInfo(
                        data = state.data,
                        watchStatus = watchStatus,
                        trailerUrl = trailerUrl,
                        onAddToWatchlist = { status ->
                            viewModel.addToWatchlist(state.data, status)
                        },
                        onRemoveFromWatchlist = {
                            viewModel.removeFromWatchlist(state.data.id, state.data.mediaType)
                        }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        color = CardBackground,
                        thickness = 1.dp
                    )

                    // Avaliação pessoal (apenas em WATCHED)
                    if (watchStatus == WatchStatus.WATCHED) {
                        DetailRatingSection(
                            currentRating = currentItem?.rating,
                            currentReview = currentItem?.review,
                            onSaveRatingAndReview = { rating, review ->
                                viewModel.saveRatingAndReview(
                                    state.data.id,
                                    state.data.mediaType,
                                    rating,
                                    review
                                )
                            }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            color = CardBackground,
                            thickness = 1.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Elenco e Crew
                    DetailCreditsSection(
                        cast = cast,
                        crew = crew
                    )

                    // Títulos Similares
                    if (similarTitles.isNotEmpty()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            color = CardBackground,
                            thickness = 1.dp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        DetailSimilarSection(
                            titles = similarTitles,
                            onItemClick = onSimilarItemClick
                        )
                    }

                    // Reviews da comunidade
                    if (reviews.isNotEmpty()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            color = CardBackground,
                            thickness = 1.dp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        DetailReviewsSection(
                            reviews = reviews,
                            visibleCount = visibleReviewsCount,
                            hasMore = viewModel.hasMoreReviews,
                            onLoadMore = {
                                viewModel.loadMoreReviews(
                                    state.data.id,
                                    state.data.mediaType
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}