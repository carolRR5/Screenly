package dam_a51568.screenly.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
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
import dam_a51568.screenly.data.models.TmdbCastMember
import dam_a51568.screenly.data.models.TmdbCrewMember
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
 * Separadores disponíveis na secção de créditos do ecrã de detalhes.
 */
enum class CreditsTab { CAST, CREW }

/**
 * Ecrã de Detalhes da aplicação Screenly.
 *
 * Estrutura do ecrã:
 * - Parte superior: layout horizontal com póster à esquerda e informações à direita
 * - Parte inferior: secção de créditos (Elenco/Crew) em largura total
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
    val cast by viewModel.cast.collectAsState()
    val crew by viewModel.crew.collectAsState()
    val trailerUrl by viewModel.trailerUrl.collectAsState()

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
                cast = cast,
                crew = crew,
                trailerUrl = trailerUrl,
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
 * Indicador de carregamento.
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
 * Conteúdo de erro.
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
 * Dividido em duas zonas:
 * - Zona superior: Row com póster à esquerda e informações à direita
 * - Zona inferior: secção de créditos em largura total, preparada para
 *   receber reviews de outros utilizadores numa fase futura
 *
 * @param data Dados do título a apresentar.
 * @param watchStatus Estado actual do título na watchlist.
 * @param cast Lista de membros do elenco.
 * @param crew Lista de membros da crew.
 * @param onAddToWatchlist Callback para adicionar o título a uma lista.
 * @param onRemoveFromWatchlist Callback para remover o título da watchlist.
 * @param onSaveRatingAndReview Callback para guardar a classificação e review.
 */
@Composable
private fun DetailContent(
    data: DetailUiData,
    watchStatus: WatchStatus?,
    cast: List<TmdbCastMember>,
    crew: List<TmdbCrewMember>,
    trailerUrl: String?,
    onAddToWatchlist: (WatchStatus) -> Unit,
    onRemoveFromWatchlist: () -> Unit,
    onSaveRatingAndReview: (Float, String) -> Unit
) {
    var selectedCreditsTab by remember { mutableStateOf<CreditsTab?>(null) }

    val currentItem = remember(watchStatus) {
        if (watchStatus != null) {
            WatchlistRepository.items
                .firstOrNull { it.id == data.id && it.mediaType == data.mediaType }
        } else null
    }

    // O scroll vertical envolve todo o conteúdo do ecrã
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Zona superior: Poster + Informações
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Coluna esquerda, póster
            AsyncImage(
                model = "${TmdbClient.IMAGE_BASE_URL}${data.posterPath}",
                contentDescription = data.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(260.dp)
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(16.dp))
            )

            // Coluna direita, informações e watchlist
            Column(modifier = Modifier.weight(1f)) {

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

                // Botão de trailer, apenas visível se houver trailer disponível
                if (trailerUrl != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    TrailerButton(trailerUrl = trailerUrl)
                    Spacer(modifier = Modifier.height(4.dp))
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

        // Divisor
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 24.dp),
            color = CardBackground,
            thickness = 1.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Zona inferior: Créditos em largura total
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            // Botões Elenco / Crew
            CreditsButtons(
                selectedTab = selectedCreditsTab,
                onTabSelected = { tab ->
                    // Clicar no mesmo botão fecha a lista
                    selectedCreditsTab = if (selectedCreditsTab == tab) null else tab
                }
            )

            // Lista de créditos — aparece quando um botão está selecionado
            when (selectedCreditsTab) {
                CreditsTab.CAST -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    CastList(cast = cast)
                }
                CreditsTab.CREW -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    CrewList(crew = crew)
                }
                null -> {}
            }

            // Espaço reservado para Reviews (Fase futura)
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Botão para abrir o trailer oficial no YouTube.
 * Abre a app do YouTube ou o browser com o URL do trailer.
 *
 * @param trailerUrl URL do trailer no YouTube.
 */
@Composable
private fun TrailerButton(trailerUrl: String) {
    val context = androidx.compose.ui.platform.LocalContext.current

    Button(
        onClick = {
            val intent = android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse(trailerUrl)
            )
            context.startActivity(intent)
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = BrandPurple,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = "▶  Ver Trailer",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Dois botões para alternar entre Elenco e Crew.
 * Clicar no mesmo botão fecha a lista.
 *
 * @param selectedTab Separador atualmente selecionado, ou null se nenhum estiver aberto.
 * @param onTabSelected Callback chamado quando o utilizador clica num botão.
 */
@Composable
private fun CreditsButtons(
    selectedTab: CreditsTab?,
    onTabSelected: (CreditsTab) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = { onTabSelected(CreditsTab.CAST) },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedTab == CreditsTab.CAST) BrandPurple else CardBackground,
                contentColor = if (selectedTab == CreditsTab.CAST) Color.White else TextSecondary
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "Elenco", fontSize = 14.sp)
        }

        Button(
            onClick = { onTabSelected(CreditsTab.CREW) },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedTab == CreditsTab.CREW) BrandPurple else CardBackground,
                contentColor = if (selectedTab == CreditsTab.CREW) Color.White else TextSecondary
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "Crew", fontSize = 14.sp)
        }
    }
}

/**
 * Lista vertical com os membros do elenco.
 *
 * @param cast Lista de membros do elenco.
 */
@Composable
private fun CastList(cast: List<TmdbCastMember>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        cast.forEach { member ->
            CreditsMemberRow(
                profilePath = member.profilePath,
                name = member.name,
                subtitle = member.character
            )
        }
    }
}

/**
 * Lista vertical com os membros da crew.
 *
 * @param crew Lista de membros da crew.
 */
@Composable
private fun CrewList(crew: List<TmdbCrewMember>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        crew.forEach { member ->
            CreditsMemberRow(
                profilePath = member.profilePath,
                name = member.name,
                subtitle = "${member.job} • ${member.department}"
            )
        }
    }
}

/**
 * Linha individual de um membro do elenco ou crew.
 * Foto circular à esquerda, nome e papel à direita.
 *
 * @param profilePath Caminho da foto de perfil no TMDb, ou null se não existir.
 * @param name Nome do actor ou membro da crew.
 * @param subtitle Personagem (elenco) ou função e departamento (crew).
 */
@Composable
private fun CreditsMemberRow(
    profilePath: String?,
    name: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        // Foto circular
        if (profilePath != null) {
            AsyncImage(
                model = "${TmdbClient.IMAGE_BASE_URL}${profilePath}",
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            )
        } else {
            // Placeholder quando não há foto disponível
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(BackgroundDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Nome e papel
        Column {
            Text(
                text = name,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * Secção de gestão da watchlist no ecrã de detalhes.
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

        if (watchStatus == WatchStatus.WATCHED) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "A minha avaliação",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            StarRatingBar(
                rating = selectedRating,
                onRatingChange = { selectedRating = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = reviewText,
                onValueChange = { reviewText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(text = "Escreve a tua review...", color = TextSecondary)
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
                modifier = Modifier.clickable { onRatingChange(star.toFloat()) }
            )
        }
    }
}

/**
 * Botão individual de estado da watchlist.
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