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
import dam_a51568.screenly.data.model.WatchStatus
import dam_a51568.screenly.data.remote.TmdbClient
import dam_a51568.screenly.ui.theme.BrandPurple
import dam_a51568.screenly.ui.theme.CardBackground
import dam_a51568.screenly.ui.theme.ErrorRed
import dam_a51568.screenly.ui.theme.TextPrimary
import dam_a51568.screenly.ui.theme.TextSecondary
import androidx.core.net.toUri

/**
 * Zona superior do ecrã de detalhes de um título.
 *
 * Usa um layout horizontal com o póster à esquerda e todas as informações
 * à direita: título, ano, duração, classificação TMDb, botão de trailer,
 * géneros, sinopse e botões de gestão da watchlist.
 *
 * @param data Dados do título a apresentar (título, ano, géneros, sinopse, etc.).
 * @param watchStatus Estado atual do título na watchlist do utilizador, ou null se não estiver adicionado.
 * @param trailerUrl URL do trailer no YouTube, ou null se não houver trailer disponível.
 * @param onAddToWatchlist Callback invocado ao selecionar um estado da watchlist.
 * @param onRemoveFromWatchlist Callback invocado ao clicar em "Remover da lista".
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
            .padding(24.dp), // Margem exterior uniforme em todo o layout
        horizontalArrangement = Arrangement.spacedBy(32.dp) // Espaço generoso entre póster e informações
    ) {
        //Coluna esquerda: Póster
        AsyncImage(
            model = "${TmdbClient.IMAGE_BASE_URL}${data.posterPath}",
            contentDescription = data.title, // Descrição para leitores de ecrã
            contentScale = ContentScale.Crop, // Recorta a imagem para preencher sem distorção
            modifier = Modifier
                .width(260.dp) // Largura fixa para o póster no layout horizontal
                .aspectRatio(2f / 3f) // Rácio 2:3 — proporção padrão de póster de cinema
                .clip(RoundedCornerShape(16.dp)) // Cantos arredondados no póster
        )

        //Coluna direita: Informações e controlo
        Column(modifier = Modifier.weight(1f)) { // Ocupa o espaço restante após o póster

            // Título do filme ou série
            Text(
                text = data.title,
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Ano de lançamento e duração numa única linha separados por "•"
            Text(
                text = "${data.year} • ${data.runtime}",
                color = TextSecondary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Classificação média do TMDb formatada com uma casa decimal (ex: "8.4 / 10")
            Text(
                text = "⭐ ${"%.1f".format(data.voteAverage)} / 10",
                color = TextSecondary,
                fontSize = 14.sp
            )

            // Botão de trailer: só aparece se houver um URL disponível
            if (trailerUrl != null) {
                Spacer(modifier = Modifier.height(12.dp))
                TrailerButton(trailerUrl = trailerUrl)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Géneros separados por "•" (ex: "Ação • Aventura • Ficção Científica")
            // Só apresentado se a lista de géneros não estiver vazia
            if (data.genres.isNotEmpty()) {
                Text(
                    text = data.genres.joinToString(" • "),
                    color = BrandPurple,         // Cor da marca para destacar os géneros
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cabeçalho da sinopse
            Text(
                text = "Sinopse",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Texto da sinopse com altura de linha aumentada para facilitar a leitura
            Text(
                text = data.overview,
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 22.sp // Maior que o tamanho de fonte para legibilidade em parágrafos longos
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botões de gestão da watchlist (To Watch / Watching / Watched / Remover)
            WatchlistButtons(
                watchStatus = watchStatus,
                onAddToWatchlist = onAddToWatchlist,
                onRemoveFromWatchlist = onRemoveFromWatchlist
            )
        }
    }
}

/**
 * Conjunto de botões para gerir o estado do título na watchlist.
 *
 * Apresenta três botões de estado (To Watch, Watching, Watched) numa linha.
 * O botão correspondente ao estado atual aparece destacado a roxo.
 * O botão "Remover da lista" só aparece quando o título já está na watchlist.
 *
 * @param watchStatus Estado atual do título na watchlist; null se não estiver adicionado.
 * @param onAddToWatchlist Callback invocado ao selecionar um estado; recebe o novo [WatchStatus].
 * @param onRemoveFromWatchlist Callback invocado ao clicar em "Remover da lista".
 */
@Composable
private fun WatchlistButtons(
    watchStatus: WatchStatus?,
    onAddToWatchlist: (WatchStatus) -> Unit,
    onRemoveFromWatchlist: () -> Unit
) {
    // Cabeçalho da secção de watchlist
    Text(
        text = "A minha lista",
        color = TextPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold
    )

    Spacer(modifier = Modifier.height(12.dp))

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Linha com os três botões de estado da watchlist
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Cada botão passa o seu WatchStatus ao callback; o visual é gerido por WatchlistButton
            WatchlistButton(
                label = "To Watch",
                isSelected = watchStatus == WatchStatus.TO_WATCH, // Destaca se for o estado atual
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

        // Botão de remoção: só visível quando o título já está em alguma lista
        if (watchStatus != null) {
            OutlinedButton(
                onClick = onRemoveFromWatchlist,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ErrorRed // Texto vermelho para indicar ação destrutiva
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = SolidColor(ErrorRed) // Borda vermelha para reforçar o caráter destrutivo
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
 * Alterna visualmente entre ativo (fundo roxo, texto branco) e inativo
 * (fundo de cartão, texto secundário) consoante o valor de [isSelected].
 *
 * @param label Texto a apresentar no botão (ex: "To Watch").
 * @param isSelected Indica se este estado é o atualmente selecionado na watchlist.
 * @param onClick Callback invocado ao clicar no botão.
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
            // Fundo roxo se selecionado; fundo de cartão se inativo
            containerColor = if (isSelected) BrandPurple else CardBackground,
            // Texto branco se selecionado; texto secundário se inativo
            contentColor = if (isSelected) Color.White else TextSecondary
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = label, fontSize = 13.sp)
    }
}

/**
 * Botão para abrir o trailer oficial do título no YouTube.
 *
 * Usa um [Intent] explícito com [ACTION_VIEW] para delegar a abertura do URL
 * ao browser ou à app do YouTube instalada no dispositivo.
 *
 * @param trailerUrl URL completo do trailer no YouTube (ex: "https://youtube.com/watch?v=...").
 */
@Composable
fun TrailerButton(trailerUrl: String) {
    // Obtém o contexto Android necessário para lançar o Intent
    val context = androidx.compose.ui.platform.LocalContext.current

    Button(
        onClick = {
            // Cria um Intent de visualização com o URL do trailer e abre no YouTube ou browser
            val intent = android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                trailerUrl.toUri() // Converte a String em URI para o Intent
            )
            context.startActivity(intent) // Lança a app adequada para abrir o URL
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = BrandPurple, // Fundo roxo para destacar o botão de trailer
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = "▶  Ver Trailer", // Símbolo de play seguido do texto para reforço visual
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}