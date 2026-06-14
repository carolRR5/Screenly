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
 *
 * Apresenta dois botões alternáveis (Elenco e Crew) que expandem a respetiva
 * lista de membros abaixo. Clicar no botão já selecionado fecha a lista,
 * comportando-se como um toggle. Apenas uma lista pode estar visível de cada vez.
 *
 * @param cast Lista de membros do elenco com nome, personagem e foto.
 * @param crew Lista de membros da crew com nome, função, departamento e foto.
 */
@Composable
fun DetailCreditsSection(
    cast: List<CastMember>,
    crew: List<CrewMember>
) {
    // Separador atualmente selecionado; null significa que nenhuma lista está expandida
    var selectedTab by remember { mutableStateOf<CreditsTab?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp) // Alinha com o restante conteúdo do ecrã de detalhes
    ) {
        // Linha com os dois botões de alternância lado a lado
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

            // Botão de Elenco: ativo (roxo) se selectedTab == CAST, inativo caso contrário
            Button(
                onClick = {
                    // Toggle: se já estiver selecionado fecha a lista; caso contrário abre-a
                    selectedTab = if (selectedTab == CreditsTab.CAST) null else CreditsTab.CAST
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTab == CreditsTab.CAST) BrandPurple else CardBackground,
                    contentColor = if (selectedTab == CreditsTab.CAST) Color.White else TextSecondary
                ),
                shape = RoundedCornerShape(8.dp) // Cantos ligeiramente arredondados
            ) {
                Text(text = "Elenco", fontSize = 14.sp)
            }

            // Botão de Crew: ativo (roxo) se selectedTab == CREW, inativo caso contrário
            Button(
                onClick = {
                    // Toggle: mesmo comportamento que o botão de Elenco
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

        // Conteúdo expandido consoante o separador selecionado
        when (selectedTab) {
            CreditsTab.CAST -> {
                Spacer(modifier = Modifier.height(16.dp)) // Espaço entre os botões e a lista
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Itera sobre todos os membros do elenco e cria uma linha por cada um
                    cast.forEach { member ->
                        CreditsMemberRow(
                            profilePath = member.profileUrl,
                            name = member.name,
                            subtitle = member.character // Personagem interpretada pelo ator
                        )
                    }
                }
            }
            CreditsTab.CREW -> {
                Spacer(modifier = Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Itera sobre todos os membros da crew e cria uma linha por cada um
                    crew.forEach { member ->
                        CreditsMemberRow(
                            profilePath = member.profileUrl,
                            name = member.name,
                            // Combina função e departamento num único subtítulo (ex: "Director • Realização")
                            subtitle = "${member.job} • ${member.department}"
                        )
                    }
                }
            }
            // Nenhum separador selecionado: não apresenta nenhuma lista
            null -> {}
        }

        Spacer(modifier = Modifier.height(24.dp)) // Margem inferior da secção
    }
}

/**
 * Linha individual de um membro do elenco ou da crew.
 *
 * Apresenta uma foto circular à esquerda e o nome com o papel à direita.
 * Se a foto não estiver disponível, mostra um ícone de pessoa como substituto.
 *
 * @param profilePath Caminho relativo ou URL absoluta da foto de perfil; null se não existir.
 * @param name Nome do ator ou membro da crew.
 * @param subtitle Personagem (elenco) ou "função • departamento" (crew).
 */
@Composable
private fun CreditsMemberRow(
    profilePath: String?,
    name: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,  // Alinha foto e texto ao centro vertical
        horizontalArrangement = Arrangement.spacedBy(12.dp), // Espaço entre a foto e o texto
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(12.dp)) // Fundo de cartão com cantos arredondados
            .padding(12.dp) // Padding interno do cartão
    ) {
        if (profilePath != null) {
            // Foto de perfil carregada assincronamente a partir do TMDb
            AsyncImage(
                // Suporta URLs absolutas e caminhos relativos do TMDb
                model = if (profilePath.startsWith("http")) profilePath
                else "${TmdbClient.IMAGE_BASE_URL}$profilePath",
                contentDescription = name, // Descrição para leitores de ecrã
                contentScale = ContentScale.Crop, // Recorta a imagem para preencher o círculo
                modifier = Modifier
                    .size(56.dp) // Tamanho fixo para manter uniformidade entre linhas
                    .clip(CircleShape) // Recorte circular para a foto de perfil
            )
        } else {
            // Substituto circular quando não há foto disponível no TMDb
            Box(
                modifier = Modifier
                    .size(56.dp) // Mesmo tamanho que a foto real
                    .clip(CircleShape) // Forma circular idêntica à foto
                    .background(BackgroundDark), // Fundo escuro para contrastar com o ícone
                contentAlignment = Alignment.Center // Centra o ícone dentro do círculo
            ) {
                Icon(
                    imageVector = Icons.Default.Person, // Ícone genérico de pessoa
                    contentDescription = null, // Decorativo; o nome já descreve o membro
                    tint = TextSecondary, // Cor subtil para não distrair
                    modifier = Modifier.size(32.dp) // Ícone menor que o círculo para não ficar cortado
                )
            }
        }

        // Coluna com o nome e o papel do membro
        Column {
            Text(
                text = name,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold // Nome em destaque relativamente ao subtítulo
            )
            Spacer(modifier = Modifier.height(2.dp)) // Pequeno espaço entre nome e papel
            Text(
                text = subtitle,
                color = TextSecondary, // Papel em cor secundária, menos destaque que o nome
                fontSize = 12.sp
            )
        }
    }
}