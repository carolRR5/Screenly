package dam_a51568.screenly.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import dam_a51568.screenly.data.remote.TmdbClient
import dam_a51568.screenly.data.repository.WatchStatus
import dam_a51568.screenly.data.repository.WatchlistItem
import dam_a51568.screenly.data.repository.WatchlistRepository
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
    viewModel: ProfileViewModel = viewModel()
) {
    val stats = remember { viewModel.calculateStats() }
    val recentRatings = remember { viewModel.getRecentRatings() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
    ) {
        //  Cabeçalho do perfil
        ProfileHeader(
            displayName = viewModel.displayName,
            email = viewModel.email,
            photoUrl = viewModel.photoUrl,
            memberSince = viewModel.memberSince,
            onSettingsClick = onNavigateToSettings
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Estatísticas
        StatsSection(stats = stats)

        Spacer(modifier = Modifier.height(24.dp))

        // As minhas listas, botões que navegam para o ecrã de listas
        ListsButtonsSection(onListClick = onNavigateToLists)

        Spacer(modifier = Modifier.height(24.dp))

        // Avaliações recentes
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

/**
 * Cabeçalho do perfil com foto, nome, email, data de registo e botão de definições.
 */
@Composable
private fun ProfileHeader(
    displayName: String,
    email: String,
    photoUrl: String?,
    memberSince: String,
    onSettingsClick: () -> Unit
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
                photoUrl = photoUrl,
                displayName = displayName
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
    photoUrl: String?,
    displayName: String
) {
    if (photoUrl != null) {
        AsyncImage(
            model = photoUrl,
            contentDescription = "Foto de perfil",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .border(2.dp, BrandPurple, CircleShape)
        )
    } else {
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
 * Apresenta três botões clicáveis que navegam para o ecrã de listas
 * no separador correspondente, mostrando o número de títulos em cada lista.
 *
 * @param onListClick Callback chamado ao clicar num botão, recebendo o estado da lista.
 */
@Composable
private fun ListsButtonsSection(onListClick: (WatchStatus) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        SectionTitle(title = "As minhas listas")
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            WatchStatus.entries.forEach { status ->
                val count = WatchlistRepository.getByStatus(status).size
                ListButton(
                    modifier = Modifier.weight(1f),
                    label = when (status) {
                        WatchStatus.TO_WATCH -> "To Watch"
                        WatchStatus.WATCHING -> "Watching"
                        WatchStatus.WATCHED -> "Watched"
                    },
                    count = count,
                    onClick = { onListClick(status) }
                )
            }
        }
    }
}

/**
 * Botão individual de uma lista.
 * Mostra o número de títulos em destaque e o nome da lista por baixo.
 *
 * @param label Nome da lista.
 * @param count Número de títulos na lista.
 * @param onClick Callback chamado ao clicar no botão.
 * @param modifier Modifier a aplicar ao botão.
 */
@Composable
private fun ListButton(
    label: String,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(CardBackground, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            color = BrandPurple,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 12.sp
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