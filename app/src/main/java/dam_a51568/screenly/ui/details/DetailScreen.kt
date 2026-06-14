package dam_a51568.screenly.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dam_a51568.screenly.data.model.WatchStatus
import dam_a51568.screenly.data.repository.WatchlistRepository
import dam_a51568.screenly.ui.theme.BackgroundDark
import dam_a51568.screenly.ui.theme.BrandPurple
import dam_a51568.screenly.ui.theme.CardBackground
import dam_a51568.screenly.ui.theme.ErrorRed
import dam_a51568.screenly.ui.theme.TextPrimary

/**
 * Separadores disponíveis na secção de créditos do ecrã de detalhes.
 * Usado por [DetailCreditsSection] para controlar qual lista está expandida.
 */
enum class CreditsTab {
    CAST, // Separador do elenco
    CREW  // Separador da equipa técnica
}

/**
 * Ecrã de Detalhes da aplicação Screenly.
 *
 * Orquestra todas as secções do ecrã num [Column] com scroll vertical:
 * - [DetailMainInfo] — póster, informações gerais e botões de watchlist
 * - [DetailRatingSection] — avaliação e review pessoal (apenas quando WATCHED)
 * - [DetailCreditsSection] — elenco e equipa técnica expansíveis
 * - [DetailSimilarSection] — títulos similares em slider horizontal
 * - [DetailReviewsSection] — reviews da comunidade com paginação progressiva
 *
 * O carregamento é disparado por [LaunchedEffect] quando [id] ou [mediaType]
 * mudam, garantindo que a navegação entre detalhes recarrega corretamente.
 *
 * @param id Identificador único do título no TMDb.
 * @param mediaType Tipo de conteúdo: "movie" para filmes ou "tv" para séries.
 * @param onBack Callback invocado ao clicar no botão de retroceder.
 * @param onSimilarItemClick Callback invocado ao clicar num título similar,
 *                           com o [id] e [mediaType] para navegação.
 * @param viewModel ViewModel que gere o estado do ecrã; criado automaticamente
 *                  pelo Compose se não for fornecido explicitamente.
 */
@Composable
fun DetailScreen(
    id: Int,
    mediaType: String,
    onBack: () -> Unit,
    onSimilarItemClick: (id: Int, mediaType: String) -> Unit,
    viewModel: DetailViewModel = viewModel()
) {
    // Observa todos os estados do ViewModel como estados do Compose
    val uiState by viewModel.uiState.collectAsState()
    val watchStatus by viewModel.watchStatus.collectAsState()
    val cast by viewModel.cast.collectAsState()
    val crew by viewModel.crew.collectAsState()
    val trailerUrl by viewModel.trailerUrl.collectAsState()
    val reviews by viewModel.reviews.collectAsState()
    val visibleReviewsCount by viewModel.visibleReviewsCount.collectAsState()
    val similarTitles by viewModel.similarTitles.collectAsState()

    // Dispara o carregamento sempre que id ou mediaType mudam
    // (ex: ao navegar de um título similar para o seu detalhe)
    LaunchedEffect(id, mediaType) {
        viewModel.loadDetails(id, mediaType)
    }

    // Coluna raiz com fundo escuro que envolve a barra de topo e o conteúdo
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Barra de topo minimalista com apenas o botão de retroceder
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Ícone espelhado para RTL
                    contentDescription = "Retroceder", // Descrição para acessibilidade
                    tint = TextPrimary
                )
            }
        }

        // Seleciona o conteúdo a mostrar consoante o estado atual da UI
        when (val state = uiState) {

            // Estado de carregamento: spinner centrado enquanto a API responde
            is DetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandPurple)
                }
            }

            // Estado de erro: mensagem a vermelho centrada no ecrã
            is DetailUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.message, color = ErrorRed)
                }
            }

            // Estado de sucesso: apresenta todas as secções num Column scrollable
            is DetailUiState.Success -> {
                // Obtém o item da watchlist correspondente ao título atual para passar
                // a classificação e review existentes à secção de avaliação.
                // Recalcula apenas quando watchStatus muda, evitando pesquisas desnecessárias.
                val currentItem = remember(watchStatus) {
                    if (watchStatus != null) {
                        WatchlistRepository.items
                            .firstOrNull { it.id == state.data.id && it.mediaType == state.data.mediaType }
                    } else null // Null se o título não estiver na watchlist
                }

                // Column scrollável que contém todas as secções do ecrã de detalhes
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()) // Scroll vertical de todo o conteúdo
                ) {
                    // Secção 1: Póster, informações gerais e botões de watchlist
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

                    // Separador visual entre secções
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        color = CardBackground, // Cor subtil para não distrair do conteúdo
                        thickness = 1.dp
                    )

                    // Secção 2: Avaliação e review pessoal, sendo que só visível quando o status estiver em WATCHED
                    if (watchStatus == WatchStatus.WATCHED) {
                        DetailRatingSection(
                            currentRating = currentItem?.rating, // Null se ainda não avaliado
                            currentReview = currentItem?.review, // Null se ainda não escrito
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

                    // Secção 3: Elenco e equipa técnica em listas expansíveis
                    DetailCreditsSection(
                        cast = cast,
                        crew = crew
                    )

                    // Secção 4: Títulos similares, sendo que só apresentada se a lista não estiver vazia
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

                    // Secção 5: Reviews da comunidade, sendo que é apresentada apenas se existirem reviews
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
                            hasMore = viewModel.hasMoreReviews, // Propriedade calculada no ViewModel
                            onLoadMore = {
                                viewModel.loadMoreReviews(
                                    state.data.id,
                                    state.data.mediaType
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp)) // Margem inferior do ecrã
                }
            }
        }
    }
}