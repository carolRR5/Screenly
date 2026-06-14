package dam_a51568.screenly.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
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
import dam_a51568.screenly.data.model.WatchlistItem
import dam_a51568.screenly.data.remote.TmdbClient
import dam_a51568.screenly.data.model.WatchStatus
import dam_a51568.screenly.data.repository.WatchlistRepository
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Face
import androidx.compose.ui.graphics.asImageBitmap
import dam_a51568.screenly.ui.theme.BackgroundDark
import dam_a51568.screenly.ui.theme.BrandPurple
import dam_a51568.screenly.ui.theme.CardBackground
import dam_a51568.screenly.ui.theme.TextPrimary
import dam_a51568.screenly.ui.theme.TextSecondary

/**
 * Ecrã de Perfil da aplicação Screenly.
 *
 * Apresenta as informações do utilizador autenticado, as suas estatísticas
 * de visualização, botões para navegar para as listas e as avaliações recentes.
 *
 * @param onNavigateToSettings Callback para navegar para o ecrã de Definições.
 * @param onNavigateToLists Callback para navegar para o ecrã de Listas,
 *                          recebendo o estado inicial do separador.
 * @param onItemClick Callback chamado quando o utilizador clica num título.
 * @param viewModel ViewModel que gere o estado do ecrã.
 */
@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToLists: (WatchStatus) -> Unit,
    onItemClick: (id: Int, mediaType: String) -> Unit,
    viewModel: ProfileViewModel = viewModel(),
) {
    val user by viewModel.user.collectAsState()
    val photoUploadState by viewModel.photoUploadState.collectAsState()

    // Estatísticas e avaliações recentes recalculadas quando a watchlist muda
    val stats = remember(WatchlistRepository.items) { viewModel.getStats() }
    val recentRatings = remember(WatchlistRepository.items) { viewModel.getRecentRatings() }

    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // Lê os bytes da imagem selecionada
            val inputStream = context.contentResolver.openInputStream(it)
            val imageBytes = inputStream?.readBytes()
            inputStream?.close()
            imageBytes?.let {
                viewModel.uploadProfilePhoto(it)
            }
        }
    }

    // Snackbar para feedback do upload
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(photoUploadState) {
        when (val state = photoUploadState) {
            is PhotoUploadState.Success -> {
                snackbarHostState.showSnackbar("Foto de perfil atualizada!")
                viewModel.resetPhotoUploadState()
            }
            is PhotoUploadState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetPhotoUploadState()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BackgroundDark)
                .verticalScroll(rememberScrollState())
        ) {
            ProfileHeader(
                displayName = user?.displayName?.takeIf { it.isNotBlank() } ?: "Utilizador",
                email = user?.email?.takeIf { it.isNotBlank() } ?: "",
                photoBase64 = user?.photoBase64,
                photoUrl = user?.photoUrl,
                memberSince = viewModel.memberSince,
                onSettingsClick = onNavigateToSettings,
                isUploadingPhoto = photoUploadState is PhotoUploadState.Loading,
                onChangePhoto = { launcher.launch("image/*") }
            )

            Spacer(modifier = Modifier.height(24.dp))
            StatsSection(stats = stats)
            Spacer(modifier = Modifier.height(24.dp))
            ListsButtonsSection(
                onListClick = onNavigateToLists,
                onItemClick = onItemClick
            )
            Spacer(modifier = Modifier.height(24.dp))
            if (recentRatings.isNotEmpty()) {
                SectionTitle(title = "Avaliações recentes")
                Spacer(modifier = Modifier.height(12.dp))
                RecentRatingsSection(
                    ratings = recentRatings,
                    onItemClick = onItemClick
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Cabeçalho do perfil com foto, nome, email, data de registo e botão de definições.
 */
@Composable
private fun ProfileHeader(
    displayName: String,
    email: String,
    photoBase64: String?,
    photoUrl: String?,
    memberSince: String,
    onSettingsClick: () -> Unit,
    isUploadingPhoto: Boolean,
    onChangePhoto: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfilePhoto(
                photoBase64 = photoBase64,
                photoUrl = photoUrl,
                displayName = displayName,
                isUploading = isUploadingPhoto,
                onChangePhoto = onChangePhoto
            )

            Column {
                Text(
                    text = displayName,
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = email,
                    color = TextSecondary,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Membro desde $memberSince",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        }

        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Definições",
                tint = TextSecondary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * Foto de perfil do utilizador.
 * Se não houver foto, mostra um círculo com a inicial do nome.
 */
@Composable
private fun ProfilePhoto(
    photoBase64: String?,
    photoUrl: String?,
    displayName: String,
    isUploading: Boolean,
    onChangePhoto: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(110.dp)
            .clickable(onClick = onChangePhoto),
        contentAlignment = Alignment.Center
    ) {
        when {
            // Mostra a foto em Base64 do Firestore
            photoBase64 != null -> {
                val imageBytes = android.util.Base64.decode(photoBase64, android.util.Base64.DEFAULT)
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                val imageBitmap = bitmap.asImageBitmap()
                androidx.compose.foundation.Image(
                    bitmap = imageBitmap,
                    contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .border(2.dp, BrandPurple, CircleShape)
                )
            }
            // Fallback para o photoUrl do Firebase Auth
            photoUrl != null -> {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .border(2.dp, BrandPurple, CircleShape)
                )
            }
            // Placeholder com inicial do nome
            else -> {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(BrandPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        color = TextPrimary,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Indicador de carregamento durante o upload
        if (isUploading) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = BrandPurple,
                    modifier = Modifier.size(32.dp)
                )
            }
        } else {
            // Ícone de câmara sobreposto na parte inferior
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(BrandPurple),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = "Alterar foto",
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Secção de estatísticas do utilizador.
 */
@Composable
private fun StatsSection(stats: UserStats) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        SectionTitle(title = "Estatísticas")
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                value = stats.totalWatched.toString(),
                label = "Vistos"
            )
            StatCard(
                modifier = Modifier.weight(1f),
                value = "${stats.totalHours}h",
                label = "Horas"
            )
            StatCard(
                modifier = Modifier.weight(1f),
                value = if (stats.averageRating > 0f) "%.1f".format(stats.averageRating) else "—",
                label = "Média ⭐"
            )
            StatCard(
                modifier = Modifier.weight(1f),
                value = stats.favoriteGenre ?: "—",
                label = "Género fav.",
                smallText = true
            )
        }
    }
}

/**
 * Cartão individual de uma estatística.
 */
@Composable
private fun StatCard(
    value: String,
    label: String,
    smallText: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(CardBackground, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = BrandPurple,
            fontSize = if (smallText) 14.sp else 20.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}

/**
 * Secção das listas do utilizador.
 * Apresenta três subsecções (To Watch, Watching, Watched), cada uma com
 * uma lista horizontal de até 10 pósteres e um botão "›" para ver mais
 * caso existam mais de 10 títulos.
 *
 * @param onListClick Callback chamado ao clicar em "Ver mais", recebendo o estado da lista.
 * @param onItemClick Callback chamado ao clicar num título.
 */
@Composable
private fun ListsButtonsSection(
    onListClick: (WatchStatus) -> Unit,
    onItemClick: (id: Int, mediaType: String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        SectionTitle(title = "As minhas listas")
        Spacer(modifier = Modifier.height(16.dp))

        WatchStatus.entries.forEach { status ->
            val items = WatchlistRepository.getByStatus(status)
            val label = when (status) {
                WatchStatus.TO_WATCH -> "To Watch"
                WatchStatus.WATCHING -> "Watching"
                WatchStatus.WATCHED -> "Watched"
            }

            ListSection(
                label = label,
                items = items,
                onItemClick = onItemClick,
                onViewMore = { onListClick(status) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Subsecção individual de uma lista.
 * Apresenta o nome da lista, uma linha horizontal com até 10 pósteres
 * e um cartão "›" no final se houver mais de 10 títulos.
 *
 * @param label Nome da lista.
 * @param items Lista completa de títulos.
 * @param onItemClick Callback chamado ao clicar num título.
 * @param onViewMore Callback chamado ao clicar no botão "›".
 */
@Composable
private fun ListSection(
    label: String,
    items: List<WatchlistItem>,
    onItemClick: (id: Int, mediaType: String) -> Unit,
    onViewMore: () -> Unit
) {
    val maxVisible = 10

    Column {
        // Nome da lista com contador
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${items.size} títulos",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (items.isEmpty()) {
            // Estado vazio
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(CardBackground, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum título nesta lista",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        } else {
            // Lista horizontal com pósteres
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Mostra até 10 títulos
                val visibleItems = items.take(maxVisible)
                visibleItems.forEach { watchlistItem ->
                    item {
                        ListPosterCard(
                            item = watchlistItem,
                            onClick = { onItemClick(watchlistItem.id, watchlistItem.mediaType) }
                        )
                    }
                }

                // Botão "›" apenas se houver mais de 10 títulos
                if (items.size > maxVisible) {
                    item {
                        ViewMoreCard(onClick = onViewMore)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        HorizontalDivider(color = CardBackground, thickness = 1.dp)
    }
}

/**
 * Cartão individual de um póster na lista horizontal.
 *
 * @param item Dados do título na watchlist.
 * @param onClick Callback chamado ao clicar no cartão.
 */
@Composable
private fun ListPosterCard(
    item: WatchlistItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(CardBackground)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = "${TmdbClient.IMAGE_BASE_URL}${item.posterPath}",
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
        )
        Text(
            text = item.title,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(6.dp)
        )
    }
}

/**
 * Cartão "›" que aparece no final da lista horizontal quando há mais de 10 títulos.
 * Ao clicar, navega para o ecrã de listas completo.
 *
 * @param onClick Callback chamado ao clicar no cartão.
 */
@Composable
private fun ViewMoreCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(60.dp)
            .height(50.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(CardBackground)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "›",
            color = BrandPurple,
            fontSize = 35.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Título de uma secção do ecrã de Perfil.
 */
@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        color = TextPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 24.dp)
    )
}

/**
 * Secção das avaliações recentes do utilizador.
 * Apresenta os últimos 5 títulos avaliados com póster, título e classificação.
 */
@Composable
private fun RecentRatingsSection(
    ratings: List<WatchlistItem>,
    onItemClick: (id: Int, mediaType: String) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ratings.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBackground, RoundedCornerShape(12.dp))
                    .clickable { onItemClick(item.id, item.mediaType) }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = "${TmdbClient.IMAGE_BASE_URL}${item.posterPath}",
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(48.dp)
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(6.dp))
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        (1..5).forEach { star ->
                            Text(
                                text = if (star <= (item.rating ?: 0f)) "★" else "☆",
                                color = if (star <= (item.rating ?: 0f)) BrandPurple else TextSecondary,
                                fontSize = 16.sp
                            )
                        }
                    }
                    if (!item.review.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.review,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}