package dam_a51568.screenly.ui.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import dam_a51568.screenly.data.repository.WatchStatus
import dam_a51568.screenly.data.remote.TmdbClient
import dam_a51568.screenly.ui.theme.BrandPurple
import dam_a51568.screenly.ui.theme.CardBackground
import dam_a51568.screenly.ui.theme.ErrorRed
import dam_a51568.screenly.ui.theme.TextPrimary
import dam_a51568.screenly.ui.theme.TextSecondary

/**
 * Zona superior do ecrã de detalhes.
 * Layout horizontal com póster à esquerda e informações à direita.
 * Inclui título, ano, duração, classificação TMDb, trailer, géneros,
 * sinopse e botões de watchlist.
 *
 * @param data Dados do título a apresentar.
 * @param watchStatus Estado actual do título na watchlist.
 * @param trailerUrl URL do trailer no YouTube, ou null se não existir.
 * @param onAddToWatchlist Callback para adicionar o título a uma lista.
 * @param onRemoveFromWatchlist Callback para remover o título da watchlist.
 */
@Composable
fun DetailMainInfo(
    data: DetailUiData,
    watchStatus: WatchStatus?,
    trailerUrl: String?,
    onAddToWatchlist: (WatchStatus) -> Unit,
    onRemoveFromWatchlist: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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

        // Coluna direita — Informações e watchlist
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = data.title,
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${data.year} • ${data.runtime}",
                color = TextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "⭐ ${"%.1f".format(data.voteAverage)} / 10",
                color = TextSecondary,
                fontSize = 14.sp
            )

            // Botão de trailer
            if (trailerUrl != null) {
                Spacer(modifier = Modifier.height(12.dp))
                TrailerButton(trailerUrl = trailerUrl)
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (data.genres.isNotEmpty()) {
                Text(
                    text = data.genres.joinToString(" • ") { it.name },
                    color = BrandPurple,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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

            WatchlistButtons(
                watchStatus = watchStatus,
                onAddToWatchlist = onAddToWatchlist,
                onRemoveFromWatchlist = onRemoveFromWatchlist
            )
        }
    }
}

/**
 * Botões de gestão da watchlist.
 *
 * @param watchStatus Estado actual do título na watchlist.
 * @param onAddToWatchlist Callback para adicionar o título com o estado especificado.
 * @param onRemoveFromWatchlist Callback para remover o título da watchlist.
 */
@Composable
private fun WatchlistButtons(
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
 * Botão individual de estado da watchlist.
 *
 * @param label Texto do botão.
 * @param isSelected Indica se este estado está atualmente selecionado.
 * @param onClick Callback chamado ao clicar no botão.
 */
@Composable
fun WatchlistButton(
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

/**
 * Botão para abrir o trailer oficial no YouTube.
 *
 * @param trailerUrl URL do trailer no YouTube.
 */
@Composable
fun TrailerButton(trailerUrl: String) {
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