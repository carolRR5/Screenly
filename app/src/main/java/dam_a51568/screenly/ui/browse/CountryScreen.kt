package dam_a51568.screenly.ui.browse

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.*
import dam_a51568.screenly.data.model.Country
import dam_a51568.screenly.ui.theme.BackgroundDark
import dam_a51568.screenly.ui.theme.CardBackground
import dam_a51568.screenly.ui.theme.TextPrimary

/**
 * Lista de países disponíveis para filtrar conteúdos no TMDb.
 *
 * Definida como propriedade de topo para ser partilhada por qualquer
 * composable que necessite da lista sem recriar o objeto a cada recomposição.
 * Inclui os países com maior volume de produção cinematográfica e televisiva,
 * ordenados por relevância global e depois por ordem geográfica/cultural.
 */
val availableCountries = listOf(
    Country("PT", "Portugal", "🇵🇹"),
    Country("US", "Estados Unidos", "🇺🇸"),
    Country("GB", "Reino Unido", "🇬🇧"),
    Country("FR", "França", "🇫🇷"),
    Country("DE", "Alemanha", "🇩🇪"),
    Country("ES", "Espanha", "🇪🇸"),
    Country("IT", "Itália", "🇮🇹"),
    Country("JP", "Japão", "🇯🇵"),
    Country("KR", "Coreia do Sul", "🇰🇷"),
    Country("IN", "Índia", "🇮🇳"),
    Country("CN", "China", "🇨🇳"),
    Country("BR", "Brasil", "🇧🇷"),
    Country("MX", "México", "🇲🇽"),
    Country("AU", "Austrália", "🇦🇺"),
    Country("CA", "Canadá", "🇨🇦"),
    Country("RU", "Rússia", "🇷🇺"),
    Country("TR", "Turquia", "🇹🇷"),
    Country("TH", "Tailândia", "🇹🇭"),
    Country("DK", "Dinamarca", "🇩🇰"),
    Country("SE", "Suécia", "🇸🇪"),
    Country("NO", "Noruega", "🇳🇴"),
    Country("PL", "Polónia", "🇵🇱"),
    Country("AR", "Argentina", "🇦🇷"),
    Country("PH", "Filipinas", "🇵🇭"),
    Country("NZ", "Nova Zelândia", "🇳🇿"),
    Country("GR", "Grécia", "🇬🇷"),
    Country("SA", "Arábia Saudita", "🇸🇦"),
    Country("HK", "Hong Kong", "🇭🇰"),
    Country("EG", "Egito", "🇪🇬"),
    Country("AT", "Áustria", "🇦🇹")
)

/**
 * Ecrã de seleção de país da aplicação Screenly.
 *
 * Apresenta a lista [availableCountries] numa grelha de 3 colunas com bandeira
 * e nome de cada país. Não necessita de ViewModel porque os dados são estáticos
 * e não requerem chamadas à API. Ao clicar num país, navega para o
 * [BrowseResultsScreen] filtrado pelo código desse país.
 *
 * @param onBack Callback invocado ao clicar no botão de retroceder.
 * @param onCountrySelected Callback invocado ao selecionar um país, recebendo o [countryCode] (ISO 3166-1 alpha-2) e o [countryName].
 */
@Composable
fun CountryScreen(
    onBack: () -> Unit,
    onCountrySelected: (countryCode: String, countryName: String) -> Unit
) {
    // Coluna raiz com fundo escuro que ocupa todo o ecrã
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Barra de topo com botão de retroceder e título do ecrã
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp), // Padding reduzido para compactar o topo
            verticalAlignment = Alignment.CenterVertically // Alinha botão e título ao centro vertical
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Ícone espelhado para idiomas RTL
                    contentDescription = "Retroceder", // Descrição para acessibilidade
                    tint = TextPrimary
                )
            }
            Text(
                text = "Explorar por país",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Grelha de 3 colunas com todos os países disponíveis
        LazyVerticalGrid(
            columns = GridCells.Fixed(3), // Grelha fixa de 3 colunas
            horizontalArrangement = Arrangement.spacedBy(12.dp), // Espaço horizontal entre cartões
            verticalArrangement = Arrangement.spacedBy(12.dp), // Espaço vertical entre linhas
            contentPadding = PaddingValues(16.dp) // Margem exterior em torno da grelha
        ) {
            items(availableCountries) { country ->
                CountryCard(
                    country = country,
                    // Passa o código e o nome ao callback para título e filtro do BrowseResultsScreen
                    onClick = { onCountrySelected(country.code, country.name) }
                )
            }
        }
    }
}

/**
 * Cartão individual de um país na grelha de seleção.
 *
 * Apresenta a bandeira emoji em tamanho grande e o nome do país por baixo,
 * ambos centrados horizontalmente. A bandeira usa emoji Unicode para evitar
 * dependências de imagens externas.
 *
 * @param country Dados do país (código, nome e emoji da bandeira).
 * @param onClick Callback invocado ao clicar no cartão.
 */
@Composable
private fun CountryCard(
    country: Country,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(12.dp)) // Fundo de cartão com cantos arredondados
            .clickable(onClick = onClick) // Torna todo o cartão clicável
            .padding(16.dp), // Padding interno uniforme
        horizontalAlignment = Alignment.CenterHorizontally, // Centra bandeira e nome horizontalmente
        verticalArrangement = Arrangement.spacedBy(8.dp) // Espaço entre a bandeira e o nome
    ) {
        Text(
            text = country.flag, // Emoji da bandeira (ex: 🇵🇹)
            fontSize = 32.sp // Tamanho grande para a bandeira ser facilmente reconhecível
        )
        Text(
            text = country.name,
            color = TextPrimary,
            fontSize = 12.sp, // Tamanho reduzido para caber em cartões de 3 colunas
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center // Centra o texto para nomes com mais do que uma palavra
        )
    }
}