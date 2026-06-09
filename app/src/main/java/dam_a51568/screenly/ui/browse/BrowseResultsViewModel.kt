package dam_a51568.screenly.ui.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dam_a51568.screenly.data.models.TmdbMediaItem
import dam_a51568.screenly.data.remote.TmdbClient
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Tipos de filtro disponíveis no ecrã de resultados de navegação.
 */
enum class BrowseFilter {
    // Títulos mais populares atualmente.
    POPULAR,
    // Títulos com melhor classificação.
    TOP_RATED,
    // Títulos lançados recentemente (últimos 6 meses).
    RECENT,
    // Filtro por género
    GENRE,
    // Filtro por país de origem
    COUNTRY
}

// Estados possíveis do ecrã de resultados.
sealed class BrowseUiState {
    data object Loading : BrowseUiState()
    data class Error(val message: String) : BrowseUiState()
    data class Success(val results: List<TmdbMediaItem>) : BrowseUiState()
}

/**
 * ViewModel do ecrã de resultados de navegação.
 * Carrega filmes e séries em paralelo usando o filtro especificado.
 *
 * @param filter Tipo de filtro a aplicar.
 * @param genreId ID do género a filtrar (apenas para [BrowseFilter.GENRE]).
 * @param countryCode Código do país a filtrar (apenas para [BrowseFilter.COUNTRY]).
 */
class BrowseResultsViewModel(
    private val filter: BrowseFilter,
    private val genreId: String? = null,
    private val countryCode: String? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow<BrowseUiState>(BrowseUiState.Loading)
    val uiState: StateFlow<BrowseUiState> = _uiState.asStateFlow()

    init {
        loadResults()
    }

    /**
     * Carrega filmes e séries em paralelo com o filtro especificado.
     * Combina e ordena os resultados dos dois endpoints.
     */
    private fun loadResults() {
        viewModelScope.launch {
            _uiState.value = BrowseUiState.Loading
            try {
                val sortBy = when (filter) {
                    BrowseFilter.TOP_RATED -> "vote_average.desc"
                    else -> "popularity.desc"
                }

                // Data de 6 meses atrás para lançamentos recentes
                val recentDate = if (filter == BrowseFilter.RECENT) {
                    val calendar = java.util.Calendar.getInstance()
                    calendar.add(java.util.Calendar.MONTH, -6)
                    java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        .format(calendar.time)
                } else null

                // Carrega filmes e séries em paralelo
                val moviesDeferred = async {
                    TmdbClient.apiService.discoverMovies(
                        apiKey = TmdbClient.API_KEY,
                        sortBy = sortBy,
                        withGenres = genreId,
                        withOriginCountry = countryCode,
                        primaryReleaseDateGte = recentDate
                    )
                }

                val tvDeferred = async {
                    TmdbClient.apiService.discoverTvShows(
                        apiKey = TmdbClient.API_KEY,
                        sortBy = sortBy,
                        withGenres = genreId,
                        withOriginCountry = countryCode,
                        firstAirDateGte = recentDate
                    )
                }

                val movies = moviesDeferred.await().results
                    .filter { it.posterPath != null }
                    .map { it.copy(mediaType = "movie") }

                val tvShows = tvDeferred.await().results
                    .filter { it.posterPath != null }
                    .map { it.copy(mediaType = "tv") }

                // Intercala filmes e séries para variedade nos resultados
                val combined = mutableListOf<TmdbMediaItem>()
                val maxSize = maxOf(movies.size, tvShows.size)
                for (i in 0 until maxSize) {
                    if (i < movies.size) combined.add(movies[i])
                    if (i < tvShows.size) combined.add(tvShows[i])
                }

                _uiState.value = BrowseUiState.Success(combined)

            } catch (e: Exception) {
                _uiState.value = BrowseUiState.Error("Erro ao carregar resultados. Verifique a ligação.")
            }
        }
    }
}

/**
 * Factory para criar o [BrowseResultsViewModel] com os argumentos necessários.
 */
class BrowseResultsViewModelFactory(
    private val filter: BrowseFilter,
    private val genreId: String? = null,
    private val countryCode: String? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return BrowseResultsViewModel(filter, genreId, countryCode) as T
    }
}