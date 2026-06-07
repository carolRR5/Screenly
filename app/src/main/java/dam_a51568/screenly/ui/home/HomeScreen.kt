package dam_a51568.screenly.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dam_a51568.screenly.ui.theme.BackgroundDark
import dam_a51568.screenly.ui.theme.BrandPurple
import dam_a51568.screenly.ui.theme.TextPrimary

/**
 * Ecrã principal da aplicação Screenly.
 * Apresenta os títulos em tendência obtidos da API do TMDb.
 *
 * @param onItemClick Callback chamado quando o utilizador clica num título.
 */
@Composable
fun HomeScreen(
    onItemClick: (id: Int, mediaType: String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Em Tendência",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(color = BrandPurple)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "A carregar...",
                color = BrandPurple,
                fontSize = 14.sp
            )
        }
    }
}