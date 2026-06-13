package dam_a51568.screenly.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dam_a51568.screenly.data.model.MediaItem
import dam_a51568.screenly.data.remote.TmdbClient
import dam_a51568.screenly.data.repository.toMediaItem
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Representa os dados carregados para o ecrã de Início.
 * Contém três listas independentes — tendências, filmes e séries populares.
 *
 * @param trending Títulos em tendência da semana (filmes e séries).
 * @param popularMovies Filmes mais populares atualmente.
 * @param popularTvShows Séries mais populares atualmente.
 */
data class HomeData(
    val trending: List<MediaItem>,
    val popularMovies: List<MediaItem>,
    val popularTvShows: List<MediaItem>
)

// Estados possíveis do ecrã de Início
sealed class HomeUiState {
    // A carregar os dados das três secções.
    data object Loading : HomeUiState()
    // Erro ao carregar os dados.
    data class Error(val message: String) : HomeUiState()
    // Dados carregados com sucesso.
    data class Success(val data: HomeData) : HomeUiState()
}

/**
 * ViewModel do ecrã de Início.
 *
 * Carrega em paralelo os títulos em tendência, os filmes populares
 * e as séries populares a partir da API do TMDb, usando [async] para
 * otimizar o tempo de carregamento.
 */
class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    /**
     * Carrega as três listas em paralelo usando coroutines.
     * Usa [async] para que os três pedidos sejam feitos simultaneamente,
     * reduzindo o tempo total de carregamento.
     */
    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                // Os três pedidos são feitos em paralelo
                val trendingDeferred = async {
                    TmdbClient.apiService.getTrending(apiKey = TmdbClient.API_KEY)
                }
                val moviesDeferred = async {
                    TmdbClient.apiService.getPopularMovies(apiKey = TmdbClient.API_KEY)
                }
                val tvDeferred = async {
                    TmdbClient.apiService.getPopularTvShows(apiKey = TmdbClient.API_KEY)
                }

                // Aguarda os três resultados
                val trending = trendingDeferred.await().results
                    .filter { it.mediaType in listOf("movie", "tv") && it.posterPath != null }
                    .map { it.toMediaItem() }

                val movies = moviesDeferred.await().results
                    .filter { it.posterPath != null }
                    .map { it.copy(mediaType = "movie").toMediaItem() }

                val tvShows = tvDeferred.await().results
                    .filter { it.posterPath != null }
                    .map { it.copy(mediaType = "tv").toMediaItem() }

                _uiState.value = HomeUiState.Success(
                    HomeData(
                        trending = trending,
                        popularMovies = movies,
                        popularTvShows = tvShows
                    )
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Erro ao carregar conteúdos. Verifique a ligação.")
            }
        }
    }
}