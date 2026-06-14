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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dam_a51568.screenly.data.model.Genre
import dam_a51568.screenly.data.remote.TmdbClient
import dam_a51568.screenly.data.repository.toGenre
import dam_a51568.screenly.ui.theme.BackgroundDark
import dam_a51568.screenly.ui.theme.BrandPurple
import dam_a51568.screenly.ui.theme.CardBackground
import dam_a51568.screenly.ui.theme.TextPrimary
import dam_a51568.screenly.ui.theme.TextSecondary
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estados possíveis do ecrã de géneros.
 * Segue o padrão sealed class para que a UI trate cada estado num `when` exaustivo.
 */
sealed class GenreUiState {
    // Estado inicial e durante o carregamento; a UI mostra um spinner.
    data object Loading : GenreUiState()

    // Estado de erro após falha na API; inclui a mensagem a apresentar
    data class Error(val message: String) : GenreUiState()

    // Estado de sucesso com a lista de géneros únicos ordenados alfabeticamente
    data class Success(val genres: List<Genre>) : GenreUiState()
}

/**
 * ViewModel do ecrã de géneros.
 *
 * Carrega os géneros de filmes e séries em paralelo e combina-os numa única lista
 * sem duplicados. Os géneros partilhados entre filmes e séries (ex: "Ação", "Drama")
 * aparecem apenas uma vez, identificados pelo [Genre.id]. A lista final é ordenada
 * alfabeticamente para facilitar a navegação do utilizador.
 */
class GenreViewModel : ViewModel() {

    /**
     * Estado interno mutável da UI, inicializado como [GenreUiState.Loading]
     * para que o spinner apareça imediatamente ao abrir o ecrã.
     */
    private val _uiState = MutableStateFlow<GenreUiState>(GenreUiState.Loading)

    // Estado público imutável observado pela UI para reagir a mudanças
    val uiState: StateFlow<GenreUiState> = _uiState.asStateFlow()

    init {
        // Inicia o carregamento dos géneros assim que o ViewModel é criado
        loadGenres()
    }

    /**
     * Carrega os géneros de filmes e séries em paralelo e combina os resultados.
     *
     * Usa [async] para lançar os dois endpoints em simultâneo, minimizando a latência.
     * Os géneros de filmes e séries são concatenados e depois divididos pelo [Genre.id],
     * pois muitos géneros (ex: "Ação", "Comédia") existem em ambas as listas com o mesmo id.
     * O resultado final é ordenado alfabeticamente pelo nome.
     */
    private fun loadGenres() {
        viewModelScope.launch {
            _uiState.value = GenreUiState.Loading // Garante o estado de loading antes de qualquer chamada

            try {
                // Lança os dois endpoints de géneros em paralelo
                val movieGenresDeferred = async {
                    TmdbClient.apiService.getMovieGenres(TmdbClient.API_KEY)
                }
                val tvGenresDeferred = async {
                    TmdbClient.apiService.getTvGenres(TmdbClient.API_KEY)
                }

                // Aguarda e converte os géneros de filmes para o modelo de domínio
                val movieGenres = movieGenresDeferred.await().genres.map { it.toGenre() }

                // Aguarda e converte os géneros de séries para o modelo de domínio
                val tvGenres = tvGenresDeferred.await().genres.map { it.toGenre() }

                // Concatena as duas listas, remove géneros com id repetido e ordena por nome
                val combined = (movieGenres + tvGenres)
                    .distinctBy { it.id } // Remove duplicados: mesmo género existe em filmes e séries
                    .sortedBy { it.name } // Ordem alfabética para facilitar a navegação

                _uiState.value = GenreUiState.Success(combined)

            } catch (e: Exception) {
                // Qualquer falha de rede ou parsing transita para Error com mensagem genérica
                _uiState.value = GenreUiState.Error("Erro ao carregar géneros.")
            }
        }
    }
}

/**
 * Ecrã de seleção de género da aplicação Screenly.
 *
 * Apresenta todos os géneros disponíveis no TMDb (filmes + séries, sem duplicados)
 * numa grelha de 3 colunas. Ao contrário do [CountryScreen], os géneros são
 * carregados da API porque podem mudar e não são conhecidos antecipadamente.
 * Ao clicar num género, navega para o [BrowseResultsScreen] filtrado por esse género.
 *
 * @param onBack Callback invocado ao clicar no botão de retroceder.
 * @param onGenreSelected Callback invocado ao selecionar um género, recebendo
 *                        o [genreId] como String e o [genreName] para o título do ecrã.
 * @param viewModel ViewModel que gere o carregamento e o estado dos géneros.
 */
@Composable
fun GenreScreen(
    onBack: () -> Unit,
    onGenreSelected: (genreId: String, genreName: String) -> Unit,
    viewModel: GenreViewModel = viewModel()
) {
    // Observa o estado da UI (Loading / Error / Success) como estado do Compose
    val uiState by viewModel.uiState.collectAsState()

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
                text = "Explorar por género",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Seleciona o conteúdo a mostrar consoante o estado atual da UI
        when (val state = uiState) {

            // Estado de carregamento: spinner centrado enquanto a API responde
            is GenreUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandPurple)
                }
            }

            // Estado de erro: mensagem centrada com cor secundária (menos alarmante que vermelho)
            is GenreUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.message, color = TextSecondary, fontSize = 16.sp)
                }
            }

            // Estado de sucesso: grelha de 3 colunas com todos os géneros
            is GenreUiState.Success -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3), // Grelha fixa de 3 colunas
                    horizontalArrangement = Arrangement.spacedBy(12.dp), // Espaço horizontal entre cartões
                    verticalArrangement = Arrangement.spacedBy(12.dp),   // Espaço vertical entre linhas
                    contentPadding = PaddingValues(16.dp) // Margem exterior em torno da grelha
                ) {
                    items(state.genres) { genre ->
                        GenreCard(
                            genre = genre,
                            // Converte o id numérico para String para o parâmetro de navegação
                            onClick = { onGenreSelected(genre.id.toString(), genre.name) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Cartão individual de um género na grelha de seleção.
 *
 * Usa uma [Box] com altura fixa em vez de [Column] porque o conteúdo é apenas
 * texto centrado, sem necessidade de disposição vertical de múltiplos elementos.
 * A altura de 72dp é suficiente para acomodar géneros com nomes longos (2 linhas).
 *
 * @param genre Dados do género (id e nome).
 * @param onClick Callback invocado ao clicar no cartão.
 */
@Composable
private fun GenreCard(
    genre: Genre,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp) // Altura fixa para uniformidade da grelha
            .background(CardBackground, RoundedCornerShape(12.dp)) // Fundo de cartão com cantos arredondados
            .clickable(onClick = onClick), // Torna todo o cartão clicável
        contentAlignment = Alignment.Center // Centra o nome horizontal e verticalmente
    ) {
        Text(
            text = genre.name,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,  // Centra nomes com mais do que uma palavra
            modifier = Modifier.padding(8.dp) // Padding para nomes longos não tocarem nos cantos
        )
    }
}