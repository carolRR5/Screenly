package dam_a51568.screenly.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import coil.compose.AsyncImage
import dam_a51568.screenly.data.models.TmdbReview
import dam_a51568.screenly.data.remote.TmdbClient
import dam_a51568.screenly.ui.theme.BackgroundDark
import dam_a51568.screenly.ui.theme.BrandPurple
import dam_a51568.screenly.ui.theme.CardBackground
import dam_a51568.screenly.ui.theme.TextPrimary
import dam_a51568.screenly.ui.theme.TextSecondary

/**
 * Secção de reviews da comunidade do TMDb.
 * Apresenta as primeiras [visibleCount] reviews com opção de carregar mais.
 * Cada review pode ser expandida para mostrar o texto completo.
 *
 * @param reviews Lista completa de reviews carregadas.
 * @param visibleCount Número de reviews atualmente visíveis.
 * @param hasMore Indica se há mais reviews disponíveis para carregar.
 * @param onLoadMore Callback chamado ao clicar em "Ver mais reviews".
 */
@Composable
fun DetailReviewsSection(
    reviews: List<TmdbReview>,
    visibleCount: Int,
    hasMore: Boolean,
    onLoadMore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Reviews da comunidade",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        reviews.take(visibleCount).forEach { review ->
            ReviewCard(review = review)
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (hasMore) {
            Button(
                onClick = onLoadMore,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CardBackground,
                    contentColor = BrandPurple
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Ver mais reviews",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Cartão individual de uma review da comunidade.
 * Apresenta o avatar, nome, data, classificação e texto da review.
 * O texto pode ser expandido/recolhido ao clicar em "Ver mais/menos".
 *
 * @param review Dados da review a apresentar.
 */
@Composable
private fun ReviewCard(review: TmdbReview) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar do autor
            val avatarUrl = review.authorDetails.avatarPath?.let { path ->
                if (path.startsWith("/https")) path.removePrefix("/")
                else "${TmdbClient.IMAGE_BASE_URL}$path"
            }

            if (avatarUrl != null) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = review.author,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BackgroundDark),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = review.author.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = review.author,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (review.formattedDate.isNotEmpty()) {
                    Text(
                        text = review.formattedDate,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            if (review.authorDetails.rating != null) {
                Text(
                    text = "⭐ ${"%.1f".format(review.authorDetails.rating)}",
                    color = BrandPurple,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        var expanded by remember { mutableStateOf(false) }
        Text(
            text = review.content,
            color = TextSecondary,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            maxLines = if (expanded) Int.MAX_VALUE else 4,
            overflow = if (expanded) TextOverflow.Visible else TextOverflow.Ellipsis
        )

        TextButton(
            onClick = { expanded = !expanded },
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = if (expanded) "Ver menos" else "Ver mais",
                color = BrandPurple,
                fontSize = 12.sp
            )
        }
    }
}