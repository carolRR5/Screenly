package dam_a51568.screenly.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import dam_a51568.screenly.data.models.TmdbMediaItem
import dam_a51568.screenly.data.remote.TmdbClient
import dam_a51568.screenly.ui.theme.CardBackground
import dam_a51568.screenly.ui.theme.TextPrimary
import dam_a51568.screenly.ui.theme.TextSecondary

/**
 * Secção de títulos similares com slider horizontal.
 * Apresenta os títulos similares num LazyRow com scroll horizontal.
 * Ao clicar num título navega para o seu ecrã de detalhes.
 *
 * @param titles Lista de títulos similares.
 * @param onItemClick Callback chamado ao clicar num título.
 */
@Composable
fun DetailSimilarSection(
    titles: List<TmdbMediaItem>,
    onItemClick: (id: Int, mediaType: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Text(
            text = "Títulos Similares",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            items(titles) { item ->
                SimilarTitleCard(
                    item = item,
                    onClick = { onItemClick(item.id, item.mediaType) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Cartão individual de um título similar.
 * Largura fixa de 120dp para manter consistência no slider.
 *
 * @param item Dados do título similar.
 * @param onClick Callback chamado ao clicar no cartão.
 */
@Composable
private fun SimilarTitleCard(
    item: TmdbMediaItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardBackground)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = "${TmdbClient.IMAGE_BASE_URL}${item.posterPath}",
            contentDescription = item.displayTitle,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
        )
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = item.displayTitle,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.displayYear,
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}