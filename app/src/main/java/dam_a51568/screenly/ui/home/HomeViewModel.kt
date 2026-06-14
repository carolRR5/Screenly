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
import kotlinx.coroutines.supervisorScope

/**
 * Representa os dados carregados para o ecrã de Início.
 */
data class HomeData(
    val trending: List<MediaItem>,
    val popularMovies: List<MediaItem>,
    val popularTvShows: List<MediaItem>
)

// Estados possíveis do ecrã de Início
sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Error(val message: String) : HomeUiState()
    data class Success(val data: HomeData) : HomeUiState()
}

/**
 * ViewModel do ecrã de Início com suporte para Pull-to-Refresh.
 */
class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadHomeData()
    }

    /**
     * Força o recarregamento dos dados da API.
     */
    fun refresh() {
        loadHomeData(isRefresh = true)
    }

    private fun loadHomeData(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _isRefreshing.value = true
            } else {
                _uiState.value = HomeUiState.Loading
            }

            try {
                supervisorScope {
                    val trendingDeferred = async {
                        TmdbClient.apiService.getTrending(apiKey = TmdbClient.API_KEY)
                    }
                    val moviesDeferred = async {
                        TmdbClient.apiService.getPopularMovies(apiKey = TmdbClient.API_KEY)
                    }
                    val tvDeferred = async {
                        TmdbClient.apiService.getPopularTvShows(apiKey = TmdbClient.API_KEY)
                    }

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
                        HomeData(trending, movies, tvShows)
                    )
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Erro ao carregar conteúdos. Verifique a ligação.")
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
