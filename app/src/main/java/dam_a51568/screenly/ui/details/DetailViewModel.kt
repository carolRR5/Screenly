package dam_a51568.screenly.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dam_a51568.screenly.data.models.TmdbGenre
import dam_a51568.screenly.data.remote.TmdbClient
import dam_a51568.screenly.data.repository.WatchlistItem
import dam_a51568.screenly.data.repository.WatchlistRepository
import dam_a51568.screenly.data.repository.WatchStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Dados do título a apresentar no ecrã de detalhes.
 * Unifica os campos de filmes e séries num único objeto para simplificar a UI.
 */
data class DetailUiData(
    val id: Int,
    val mediaType: String,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val year: String,
    val genres: List<TmdbGenre>,
    val voteAverage: Double,
    val runtime: String         // Ex: "120 min" para filmes ou "45 min/ep" para séries
)

// Estados possíveis do ecrã de detalhes.
sealed class DetailUiState {
    data object Loading : DetailUiState()
    data class Error(val message: String) : DetailUiState()
    data class Success(val data: DetailUiData) : DetailUiState()
}

/**
 * ViewModel do ecrã de detalhes.
 *
 * Carrega os dados completos de um filme ou série a partir do TMDb,
 * e gere as operações de watchlist (adicionar, atualizar estado, remover).
 */
class DetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    /**
     * Estado atual do título na watchlist do utilizador.
     * Null se o título ainda não foi adicionado.
     */
    private val _watchStatus = MutableStateFlow<WatchStatus?>(null)
    val watchStatus: StateFlow<WatchStatus?> = _watchStatus.asStateFlow()

    /**
     * Carrega os detalhes do título a partir do TMDb.
     * Determina o endpoint a usar com base no [mediaType] ("movie" ou "tv").
     *
     * @param id Identificador único do título no TMDb.
     * @param mediaType Tipo de conteúdo: "movie" para filmes, "tv" para séries.
     */
    fun loadDetails(id: Int, mediaType: String) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            try {
                val data = if (mediaType == "movie") {
                    val movie = TmdbClient.apiService.getMovieDetails(
                        id = id,
                        apiKey = TmdbClient.API_KEY
                    )
                    DetailUiData(
                        id = movie.id,
                        mediaType = "movie",
                        title = movie.title,
                        overview = movie.overview ?: "Sinopse não disponível.",
                        posterPath = movie.posterPath,
                        year = movie.releaseDate?.take(4) ?: "N/A",
                        genres = movie.genres ?: emptyList(),
                        voteAverage = movie.voteAverage ?: 0.0,
                        runtime = movie.runtime?.let { "$it min" } ?: "N/A"
                    )
                } else {
                    val tv = TmdbClient.apiService.getTvShowDetails(
                        id = id,
                        apiKey = TmdbClient.API_KEY
                    )
                    DetailUiData(
                        id = tv.id,
                        mediaType = "tv",
                        title = tv.name,
                        overview = tv.overview ?: "Sinopse não disponível.",
                        posterPath = tv.posterPath,
                        year = tv.firstAirDate?.take(4) ?: "N/A",
                        genres = tv.genres ?: emptyList(),
                        voteAverage = tv.voteAverage ?: 0.0,
                        runtime = "${tv.typicalEpisodeRuntime} min/ep"
                    )
                }

                _uiState.value = DetailUiState.Success(data)

                // Verifica se o título já está na watchlist e actualiza o estado
                _watchStatus.value = WatchlistRepository.getStatus(id, mediaType)

            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error("Erro ao carregar detalhes. Tente novamente.")
            }
        }
    }

    /**
     * Adiciona o título à watchlist com o estado especificado,
     * ou atualiza o estado se o título já existir.
     *
     * @param data Dados do título a guardar.
     * @param status Estado de visualização a atribuir.
     */
    fun addToWatchlist(data: DetailUiData, status: WatchStatus) {
        val item = WatchlistItem(
            id = data.id,
            mediaType = data.mediaType,
            title = data.title,
            posterPath = data.posterPath,
            year = data.year,
            genres = data.genres.joinToString(", ") { it.name },
            status = status
        )
        WatchlistRepository.addOrUpdate(item)
        _watchStatus.value = status
    }

    /**
     * Remove o título da watchlist do utilizador.
     *
     * @param id Identificador único do título.
     * @param mediaType Tipo de conteúdo: "movie" ou "tv".
     */
    fun removeFromWatchlist(id: Int, mediaType: String) {
        WatchlistRepository.remove(id, mediaType)
        _watchStatus.value = null
    }
}