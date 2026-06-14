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
import dam_a51568.screenly.data.model.MediaItem
import dam_a51568.screenly.data.remote.TmdbClient
import dam_a51568.screenly.ui.theme.CardBackground
import dam_a51568.screenly.ui.theme.TextPrimary
import dam_a51568.screenly.ui.theme.TextSecondary

/**
 * Secção de títulos similares com slider horizontal.
 *
 * Apresenta os títulos similares ao atual num [LazyRow] com scroll horizontal.
 * Só é apresentada quando a lista [titles] não está vazia — essa verificação
 * é feita no [DetailScreen] antes de invocar esta composable.
 *
 * @param titles Lista de títulos similares a apresentar.
 * @param onItemClick Callback invocado ao clicar num título, com o [id]
 *                    e o [mediaType] para navegação para o ecrã de detalhes.
 */
@Composable
fun DetailSimilarSection(
    titles: List<MediaItem>,
    onItemClick: (id: Int, mediaType: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp) // Margem inferior antes do próximo separador ou secção
    ) {
        // Cabeçalho da secção com padding lateral alinhado ao restante conteúdo do ecrã
        Text(
            text = "Títulos Similares",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(12.dp)) // Espaço entre o cabeçalho e o slider

        // Slider horizontal com renderização eficiente via LazyRow
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp), // Espaço entre cartões
            contentPadding = PaddingValues(horizontal = 24.dp) // Margem lateral do slider
        ) {
            // Cria um cartão por cada título similar da lista
            items(titles) { item ->
                SimilarTitleCard(item) {
                    // Passa o id e o mediaType ao callback para navegação para o detalhe
                    onItemClick(item.id, item.mediaType)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp)) // Margem inferior da secção
    }
}

/**
 * Cartão individual de um título similar no slider horizontal.
 *
 * Tem largura fixa de 120dp — ligeiramente menor que os cartões das listas
 * horizontais do ecrã principal (140dp) para caber mais títulos no slider.
 * O póster usa o rácio 2:3 padrão de cinema para consistência visual.
 *
 * @param item Dados do título similar, incluindo póster, título e ano.
 * @param onClick Callback invocado ao clicar no cartão.
 */
@Composable
private fun SimilarTitleCard(
    item: MediaItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.dp) // Largura fixa menor que os cartões do ecrã principal
            .clip(RoundedCornerShape(12.dp))  // Cantos arredondados aplicados a toda a coluna
            .background(CardBackground) // Fundo de cartão distinto do fundo do ecrã
            .clickable(onClick = onClick) // Torna todo o cartão clicável
    ) {
        AsyncImage(
            // Suporta URLs absolutas (http) e caminhos relativos do TMDb
            model = if (item.posterUrl.startsWith("http")) item.posterUrl
            else "${TmdbClient.IMAGE_BASE_URL}${item.posterUrl}",
            contentDescription = item.title, // Descrição para leitores de ecrã
            contentScale = ContentScale.Crop, // Recorta a imagem para preencher sem distorção
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f) // Rácio 2:3 — proporção padrão de póster de cinema
        )
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = item.title,
                color = TextPrimary,
                fontSize = 11.sp, // Tamanho reduzido para caber na largura de 120dp
                fontWeight = FontWeight.SemiBold,
                maxLines = 2, // Limita a 2 linhas para uniformidade entre cartões
                overflow = TextOverflow.Ellipsis // Adiciona "…" se o título for demasiado longo
            )
            Spacer(modifier = Modifier.height(2.dp)) // Pequeno espaço entre título e ano
            Text(
                text = item.year,
                color = TextSecondary, // Cor secundária para o ano (informação menos relevante)
                fontSize = 10.sp // Tamanho mínimo para não ocupar demasiado espaço
            )
        }
    }
}