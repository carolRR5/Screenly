package dam_a51568.screenly.ui.browse

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.*
import dam_a51568.screenly.ui.theme.BackgroundDark
import dam_a51568.screenly.ui.theme.CardBackground
import dam_a51568.screenly.ui.theme.TextPrimary

/**
 * Representa um país disponível para filtrar conteúdos.
 *
 * @param code Código ISO do país (ex: "PT", "US", "JP").
 * @param name Nome do país em português.
 * @param flag Emoji da bandeira do país.
 */
data class Country(
    val code: String,
    val name: String,
    val flag: String
)

/**
 * Lista de países disponíveis para filtrar conteúdos no TMDb.
 * Inclui os países com maior produção cinematográfica e televisiva.
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
 * Apresenta uma lista de países disponíveis numa grelha de 3 colunas.
 * Ao clicar num país, navega para o [BrowseResultsScreen] filtrado por esse país.
 *
 * @param onBack Callback chamado ao clicar no botão de retroceder.
 * @param onCountrySelected Callback chamado ao selecionar um país, recebendo o código e o nome do país.
 */
@Composable
fun CountryScreen(
    onBack: () -> Unit,
    onCountrySelected: (countryCode: String, countryName: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Barra de topo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Retroceder",
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

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(availableCountries) { country ->
                CountryCard(
                    country = country,
                    onClick = { onCountrySelected(country.code, country.name) }
                )
            }
        }
    }
}

/**
 * Cartão individual de um país.
 * Apresenta a bandeira e o nome do país.
 *
 * @param country Dados do país.
 * @param onClick Callback chamado ao clicar no cartão.
 */
@Composable
private fun CountryCard(
    country: Country,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = country.flag, fontSize = 32.sp)
        Text(
            text = country.name,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}