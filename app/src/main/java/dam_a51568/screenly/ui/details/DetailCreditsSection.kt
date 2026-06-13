package dam_a51568.screenly.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import dam_a51568.screenly.data.model.CastMember
import dam_a51568.screenly.data.model.CrewMember
import dam_a51568.screenly.data.remote.TmdbClient
import dam_a51568.screenly.ui.theme.BackgroundDark
import dam_a51568.screenly.ui.theme.BrandPurple
import dam_a51568.screenly.ui.theme.CardBackground
import dam_a51568.screenly.ui.theme.TextPrimary
import dam_a51568.screenly.ui.theme.TextSecondary

/**
 * Secção de créditos do ecrã de detalhes.
 * Apresenta dois botões (Elenco e Crew) que expandem as respetivas listas.
 * Clicar no mesmo botão fecha a lista.
 *
 * @param cast Lista de membros do elenco.
 * @param crew Lista de membros da crew.
 */
@Composable
fun DetailCreditsSection(
    cast: List<CastMember>,
    crew: List<CrewMember>
) {
    var selectedTab by remember { mutableStateOf<CreditsTab?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    selectedTab = if (selectedTab == CreditsTab.CAST) null else CreditsTab.CAST
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == CreditsTab.CAST) BrandPurple else CardBackground,
                    contentColor = if (selectedTab == CreditsTab.CAST) Color.White else TextSecondary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Elenco", fontSize = 14.sp)
            }

            Button(
                onClick = {
                    selectedTab = if (selectedTab == CreditsTab.CREW) null else CreditsTab.CREW
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == CreditsTab.CREW) BrandPurple else CardBackground,
                    contentColor = if (selectedTab == CreditsTab.CREW) Color.White else TextSecondary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Crew", fontSize = 14.sp)
            }
        }

        when (selectedTab) {
            CreditsTab.CAST -> {
                Spacer(modifier = Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    cast.forEach { member ->
                        CreditsMemberRow(
                            profilePath = member.profileUrl,
                            name = member.name,
                            subtitle = member.character
                        )
                    }
                }
            }
            CreditsTab.CREW -> {
                Spacer(modifier = Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    crew.forEach { member ->
                        CreditsMemberRow(
                            profilePath = member.profileUrl,
                            name = member.name,
                            subtitle = "${member.job} • ${member.department}"
                        )
                    }
                }
            }
            null -> {}
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Linha individual de um membro do elenco ou crew.
 * Foto circular à esquerda, nome e papel à direita.
 *
 * @param profilePath Caminho da foto de perfil no TMDb, ou null se não existir.
 * @param name Nome do actor ou membro da crew.
 * @param subtitle Personagem (elenco) ou função e departamento (crew).
 */
@Composable
private fun CreditsMemberRow(
    profilePath: String?,
    name: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        if (profilePath != null) {
            AsyncImage(
                model = if (profilePath.startsWith("http")) profilePath else "${TmdbClient.IMAGE_BASE_URL}$profilePath",
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(BackgroundDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Column {
            Text(
                text = name,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}