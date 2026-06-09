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
import dam_a51568.screenly.data.models.TmdbGenre
import dam_a51568.screenly.data.remote.TmdbClient
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

// Estados possíveis do ecrã de géneros.
sealed class GenreUiState {
    data object Loading : GenreUiState()
    data class Error(val message: String) : GenreUiState()
    data class Success(val genres: List<TmdbGenre>) : GenreUiState()
}

/**
 * ViewModel do ecrã de géneros.
 * Carrega os géneros de filmes e séries em paralelo e combina-os sem duplicados.
 */
class GenreViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<GenreUiState>(GenreUiState.Loading)
    val uiState: StateFlow<GenreUiState> = _uiState.asStateFlow()

    init {
        loadGenres()
    }

    /**
     * Carrega os géneros de filmes e séries em paralelo.
     * Remove duplicados mantendo géneros únicos pelo id.
     */
    private fun loadGenres() {
        viewModelScope.launch {
            _uiState.value = GenreUiState.Loading
            try {
                val movieGenresDeferred = async {
                    TmdbClient.apiService.getMovieGenres(TmdbClient.API_KEY)
                }
                val tvGenresDeferred = async {
                    TmdbClient.apiService.getTvGenres(TmdbClient.API_KEY)
                }

                val movieGenres = movieGenresDeferred.await().genres
                val tvGenres = tvGenresDeferred.await().genres

                // Combina os dois e remove duplicados pelo id
                val combined = (movieGenres + tvGenres)
                    .distinctBy { it.id }
                    .sortedBy { it.name }

                _uiState.value = GenreUiState.Success(combined)
            } catch (e: Exception) {
                _uiState.value = GenreUiState.Error("Erro ao carregar géneros.")
            }
        }
    }
}

/**
 * Ecrã de seleção de género da aplicação Screenly.
 *
 * Apresenta todos os géneros disponíveis no TMDb numa grelha de 3 colunas.
 * Ao clicar num género, navega para o [BrowseResultsScreen] filtrado por esse género.
 *
 * @param onBack Callback chamado ao clicar no botão de retroceder.
 * @param onGenreSelected Callback chamado ao selecionar um género, recebendo o id e o nome do género.
 * @param viewModel ViewModel que gere o estado do ecrã.
 */
@Composable
fun GenreScreen(
    onBack: () -> Unit,
    onGenreSelected: (genreId: String, genreName: String) -> Unit,
    viewModel: GenreViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                text = "Explorar por género",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        when (val state = uiState) {
            is GenreUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BrandPurple)
                }
            }
            is GenreUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.message, color = TextSecondary, fontSize = 16.sp)
                }
            }
            is GenreUiState.Success -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(state.genres) { genre ->
                        GenreCard(
                            genre = genre,
                            onClick = { onGenreSelected(genre.id.toString(), genre.name) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Cartão individual de um género.
 *
 * @param genre Dados do género.
 * @param onClick Callback chamado ao clicar no cartão.
 */
@Composable
private fun GenreCard(
    genre: TmdbGenre,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(CardBackground, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = genre.name,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(8.dp)
        )
    }
}