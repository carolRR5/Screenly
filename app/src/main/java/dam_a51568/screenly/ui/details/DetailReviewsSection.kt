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
import dam_a51568.screenly.data.model.Review
import dam_a51568.screenly.data.remote.TmdbClient
import dam_a51568.screenly.ui.theme.BackgroundDark
import dam_a51568.screenly.ui.theme.BrandPurple
import dam_a51568.screenly.ui.theme.CardBackground
import dam_a51568.screenly.ui.theme.TextPrimary
import dam_a51568.screenly.ui.theme.TextSecondary

/**
 * Secção de reviews da comunidade do TMDb.
 *
 * Apresenta as primeiras [visibleCount] reviews carregadas, com paginação
 * progressiva através do botão "Ver mais reviews". Primeiro esgota as reviews
 * já em memória antes de ir buscar a próxima página à API.
 * Só é apresentada quando a lista [reviews] não está vazia — essa verificação
 * é feita no [DetailScreen] antes de invocar esta composable.
 *
 * @param reviews Lista completa de reviews atualmente carregadas em memória.
 * @param visibleCount Número de reviews atualmente visíveis no ecrã.
 * @param hasMore Indica se há mais reviews a mostrar (em memória ou na API).
 * @param onLoadMore Callback invocado ao clicar em "Ver mais reviews".
 */
@Composable
fun DetailReviewsSection(
    reviews: List<Review>,
    visibleCount: Int,
    hasMore: Boolean,
    onLoadMore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp) // Alinha com o restante conteúdo do ecrã
    ) {
        // Cabeçalho da secção
        Text(
            text = "Reviews da comunidade",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(16.dp)) // Espaço entre cabeçalho e primeira review

        // Apresenta apenas as primeiras visibleCount reviews da lista
        reviews.take(visibleCount).forEach { review ->
            ReviewCard(review = review)
            Spacer(modifier = Modifier.height(12.dp)) // Espaço entre cartões de review
        }

        // Botão "Ver mais" só visível enquanto existirem reviews adicionais
        if (hasMore) {
            Button(
                onClick = onLoadMore,
                modifier = Modifier.fillMaxWidth(), // Ocupa toda a largura para facilitar o toque
                colors = ButtonDefaults.buttonColors(
                    containerColor = CardBackground, // Fundo subtil para não competir com o conteúdo
                    contentColor = BrandPurple // Texto roxo para indicar ação secundária
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

        Spacer(modifier = Modifier.height(24.dp)) // Margem inferior da secção
    }
}

/**
 * Cartão individual de uma review da comunidade do TMDb.
 *
 * Apresenta o avatar do autor (ou inicial do nome como substituto),
 * o nome, a data, a classificação (se disponível) e o texto da review.
 * O texto pode ser expandido ou recolhido ao clicar em "Ver mais/menos",
 * mostrando por omissão apenas as primeiras 4 linhas.
 *
 * @param review Dados da review a apresentar.
 */
@Composable
private fun ReviewCard(review: Review) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(12.dp)) // Fundo de cartão com cantos arredondados
            .padding(16.dp) // Padding interno uniforme do cartão
    ) {
        // Linha de cabeçalho: avatar + nome/data à esquerda, classificação à direita
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp), // Espaço entre avatar e texto
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Constrói o URL do avatar a partir do caminho relativo ou absoluto
            val avatarUrl = review.avatarUrl?.let { path ->
                // Suporta URLs absolutas (ex: Gravatar) e caminhos relativos do TMDb
                if (path.startsWith("http")) path
                else "${TmdbClient.IMAGE_BASE_URL}$path"
            }

            if (avatarUrl != null) {
                // Avatar circular carregado assincronamente
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = review.author, // Descrição para leitores de ecrã
                    contentScale = ContentScale.Crop, // Recorta para preencher o círculo
                    modifier = Modifier
                        .size(40.dp)  // Tamanho compacto para o cabeçalho da review
                        .clip(CircleShape) // Forma circular para o avatar
                )
            } else {
                // Substituto circular com a inicial do nome quando não há avatar disponível
                Box(
                    modifier = Modifier
                        .size(40.dp) // Mesmo tamanho do avatar real
                        .clip(CircleShape) // Forma circular idêntica ao avatar
                        .background(BackgroundDark), // Fundo escuro para contrastar com a inicial
                    contentAlignment = Alignment.Center // Centra a inicial dentro do círculo
                ) {
                    Text(
                        // Primeira letra do nome em maiúscula; "?" se o nome estiver vazio
                        text = review.author.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Nome do autor e data da review, ocupam o espaço restante após o avatar
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = review.author,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold // Nome em destaque relativamente à data
                )
                // Data só apresentada se não estiver vazia (campo opcional na API)
                if (review.formattedDate.isNotEmpty()) {
                    Text(
                        text = review.formattedDate,
                        color = TextSecondary, // Cor secundária para a data (menos relevante)
                        fontSize = 12.sp
                    )
                }
            }

            // Classificação do autor, só apresentada se existir (campo opcional na API)
            if (review.rating != null) {
                Text(
                    text = "⭐ ${"%.1f".format(review.rating)}", // Uma casa decimal (ex: "8.5")
                    color = BrandPurple,  // Cor roxa para destacar a classificação
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp)) // Espaço entre cabeçalho e texto da review

        // Estado local de expansão: false por omissão para mostrar apenas 4 linhas
        var expanded by remember { mutableStateOf(false) }

        // Texto da review truncado a 4 linhas ou expandido consoante o estado
        Text(
            text = review.content,
            color = TextSecondary,
            fontSize = 13.sp,
            lineHeight = 20.sp, // Altura de linha aumentada para legibilidade em reviews longas
            maxLines = if (expanded) Int.MAX_VALUE else 4, // Sem limite se expandido
            overflow = if (expanded) TextOverflow.Visible else TextOverflow.Ellipsis // "…" se truncado
        )

        // Botão de texto para alternar entre expandido e recolhido
        TextButton(
            onClick = { expanded = !expanded }, // Inverte o estado de expansão ao clicar
            contentPadding = PaddingValues(0.dp) // Remove o padding padrão para alinhar com o texto
        ) {
            Text(
                text = if (expanded) "Ver menos" else "Ver mais", // Texto adapta-se ao estado
                color = BrandPurple, // Cor roxa para indicar que é clicável
                fontSize = 12.sp
            )
        }
    }
}