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
 * Secção de avaliação pessoal do ecrã de detalhes.
 *
 * Apresenta um sistema de 5 estrelas e um campo de texto para review pessoal.
 * O botão de guardar só fica ativo quando a classificação é maior que zero
 * e existem alterações não guardadas relativamente ao que está na Firestore.
 * Esta secção só é apresentada quando o título está no estado WATCHED.
 *
 * @param currentRating Classificação atualmente guardada, ou null se ainda não avaliado.
 * @param currentReview Review atualmente guardada, ou null se ainda não escrita.
 * @param onSaveRatingAndReview Callback invocado ao guardar, com a classificação e a review atuais.
 */
@Composable
fun DetailRatingSection(
    currentRating: Float?,
    currentReview: String?,
    onSaveRatingAndReview: (Float, String) -> Unit
) {
    // Estado local da estrela selecionada; reiniciado sempre que currentRating mudar (ex: após guardar)
    var selectedRating by remember(currentRating) { mutableFloatStateOf(currentRating ?: 0f) }

    // Estado local do texto da review; reiniciado sempre que currentReview mudar
    var reviewText by remember(currentReview) { mutableStateOf(currentReview ?: "") }

    // Verdadeiro se o utilizador alterou a classificação ou a review relativamente ao valor guardado
    val hasChanges = selectedRating != (currentRating ?: 0f) || reviewText != (currentReview ?: "")

    // Gestor de foco usado para esconder o teclado após guardar a avaliação
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp) // Alinha com o restante conteúdo do ecrã
    ) {
        // Cabeçalho da secção de avaliação
        Text(
            text = "A minha avaliação",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Sistema de estrelas: atualiza selectedRating ao clicar numa estrela
        StarRatingBar(
            rating = selectedRating,
            onRatingChange = { selectedRating = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de texto para a review pessoal
        OutlinedTextField(
            value = reviewText,
            onValueChange = { reviewText = it }, // Atualiza o estado local a cada tecla
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(text = "Escreve a tua review...", color = TextSecondary)
            },
            minLines = 3, // Altura mínima para o campo ser visualmente convidativo
            maxLines = 6, // Limita a altura máxima para não dominar o ecrã
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = BrandPurple, // Borda roxa quando o campo está em foco
                unfocusedBorderColor = TextSecondary, // Borda subtil quando inativo
                cursorColor = BrandPurple, // Cursor roxo para consistência com o tema
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                onSaveRatingAndReview(selectedRating, reviewText) // Persiste na Firestore via ViewModel
                focusManager.clearFocus() // Esconde o teclado virtual após guardar
            },
            // Só permite guardar se houver uma classificação atribuída e alterações por guardar
            enabled = selectedRating > 0f && hasChanges,
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandPurple,
                contentColor = TextPrimary,
                // Cores atenuadas quando o botão está desativado
                disabledContainerColor = CardBackground.copy(alpha = 0.5f),
                disabledContentColor = TextSecondary
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth() // Botão ocupa toda a largura para facilitar o toque
        ) {
            // Texto do botão muda consoante o estado: confirma se já guardado, pede ação se não
            Text(
                text = if (!hasChanges && selectedRating > 0f) "Avaliação guardada ✓"
                else "Guardar avaliação"
            )
        }

        Spacer(modifier = Modifier.height(24.dp)) // Margem inferior da secção
    }
}

/**
 * Sistema de classificação por estrelas com 5 posições.
 *
 * Cada estrela é clicável e atualiza a classificação ao ser tocada.
 * Estrelas preenchidas (★) representam a classificação atual;
 * estrelas vazias (☆) representam posições ainda não selecionadas.
 *
 * @param rating Classificação atual entre 0 e 5; 0f significa sem classificação.
 * @param onRatingChange Callback invocado ao clicar numa estrela, com o novo valor (1f a 5f).
 */
@Composable
fun StarRatingBar(
    rating: Float,
    onRatingChange: (Float) -> Unit
) {
    // Linha horizontal com as 5 estrelas espaçadas
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        (1..5).forEach { star ->
            Text(
                // Estrela preenchida se o seu índice for igual ou inferior à classificação atual
                text = if (star <= rating) "★" else "☆",
                fontSize = 32.sp, // Tamanho grande para facilitar o toque em dispositivos móveis
                // Roxo para estrelas preenchidas; cinzento para as por preencher
                color = if (star <= rating) BrandPurple else TextSecondary,
                // Ao clicar, converte o índice inteiro da estrela para Float e notifica o callback
                modifier = Modifier.clickable { onRatingChange(star.toFloat()) }
            )
        }
    }
}