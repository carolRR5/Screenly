package dam_a51568.screenly.ui.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dam_a51568.screenly.ui.theme.BrandPurple
import dam_a51568.screenly.ui.theme.CardBackground
import dam_a51568.screenly.ui.theme.TextPrimary
import dam_a51568.screenly.ui.theme.TextSecondary

/**
 * Secção de avaliação pessoal em largura total.
 * Apresenta o sistema de estrelas e o campo de review pessoal.
 * Apenas visível quando o título está no estado WATCHED.
 *
 * @param currentRating Classificação atual, ou null se não tiver sido atribuída.
 * @param currentReview Review atual, ou null se não tiver sido escrita.
 * @param onSaveRatingAndReview Callback para guardar a classificação e a review.
 */
@Composable
fun DetailRatingSection(
    currentRating: Float?,
    currentReview: String?,
    onSaveRatingAndReview: (Float, String) -> Unit
) {
    var selectedRating by remember(currentRating) { mutableFloatStateOf(currentRating ?: 0f) }
    var reviewText by remember(currentReview) { mutableStateOf(currentReview ?: "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text(
            text = "A minha avaliação",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        StarRatingBar(
            rating = selectedRating,
            onRatingChange = { selectedRating = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(12.dp))

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

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Sistema de classificação por estrelas.
 * Apresenta 5 estrelas clicáveis.
 *
 * @param rating Classificação atual (0f se ainda não foi atribuída).
 * @param onRatingChange Callback chamado quando o utilizador clica numa estrela.
 */
@Composable
fun StarRatingBar(
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