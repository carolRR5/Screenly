package dam_a51568.screenly.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dam_a51568.screenly.data.models.TmdbCastMember
import dam_a51568.screenly.data.models.TmdbCrewMember
import dam_a51568.screenly.data.models.TmdbGenre
import dam_a51568.screenly.data.remote.TmdbClient
import dam_a51568.screenly.data.repository.WatchlistItem
import dam_a51568.screenly.data.repository.WatchlistRepository
import dam_a51568.screenly.data.repository.WatchStatus
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Dados do título a apresentar no ecrã de detalhes.
 * Unifica os campos de filmes e séries num único objecto para simplificar a UI.
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
    val runtime: String
)

/** Estados possíveis do ecrã de detalhes. */
sealed class DetailUiState {
    data object Loading : DetailUiState()
    data class Error(val message: String) : DetailUiState()
    data class Success(val data: DetailUiData) : DetailUiState()
}

/**
 * ViewModel do ecrã de detalhes.
 *
 * Carrega em paralelo os dados completos do título e os seus créditos
 * (elenco e crew) a partir da API do TMDb.
 */
class DetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _watchStatus = MutableStateFlow<WatchStatus?>(null)
    val watchStatus: StateFlow<WatchStatus?> = _watchStatus.asStateFlow()

    /**
     * Elenco do título carregado da API.
     * Lista ordenada por ordem de importância no elenco.
     */
    private val _cast = MutableStateFlow<List<TmdbCastMember>>(emptyList())
    val cast: StateFlow<List<TmdbCastMember>> = _cast.asStateFlow()

    /**
     * Crew do título carregado da API.
     * Ordenada por departamento para facilitar a leitura.
     */
    private val _crew = MutableStateFlow<List<TmdbCrewMember>>(emptyList())
    val crew: StateFlow<List<TmdbCrewMember>> = _crew.asStateFlow()

    /**
     * Carrega os detalhes e os créditos do título em paralelo.
     * Usa [async] para optimizar o tempo de carregamento.
     *
     * @param id Identificador único do título no TMDb.
     * @param mediaType Tipo de conteúdo: "movie" ou "tv".
     */
    fun loadDetails(id: Int, mediaType: String) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            try {
                // Carrega detalhes e créditos em paralelo
                val detailsDeferred = async {
                    if (mediaType == "movie") {
                        TmdbClient.apiService.getMovieDetails(id, TmdbClient.API_KEY)
                            .let { movie ->
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
                            }
                    } else {
                        TmdbClient.apiService.getTvShowDetails(id, TmdbClient.API_KEY)
                            .let { tv ->
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
                    }
                }

                val creditsDeferred = async {
                    if (mediaType == "movie") {
                        TmdbClient.apiService.getMovieCredits(id, TmdbClient.API_KEY)
                    } else {
                        TmdbClient.apiService.getTvCredits(id, TmdbClient.API_KEY)
                    }
                }

                val details = detailsDeferred.await()
                val credits = creditsDeferred.await()

                _uiState.value = DetailUiState.Success(details)
                _watchStatus.value = WatchlistRepository.getStatus(id, mediaType)

                // Limita o elenco aos primeiros 20 membros mais relevantes
                _cast.value = credits.cast.take(20)

                // Filtra a crew para mostrar apenas os departamentos mais relevantes
                _crew.value = credits.crew
                    .filter { it.department in listOf("Directing", "Writing", "Production", "Sound", "Camera") }
                    .distinctBy { it.id }
                    .sortedBy { it.department }

            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error("Erro ao carregar detalhes. Tente novamente.")
            }
        }
    }

    /**
     * Adiciona o título à watchlist com o estado especificado,
     * ou actualiza o estado se o título já existir.
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
     */
    fun removeFromWatchlist(id: Int, mediaType: String) {
        WatchlistRepository.remove(id, mediaType)
        _watchStatus.value = null
    }

    /**
     * Guarda a classificação e a review pessoal de um título.
     * Apenas aplicável a títulos com estado [WatchStatus.WATCHED].
     */
    fun saveRatingAndReview(id: Int, mediaType: String, rating: Float, review: String) {
        WatchlistRepository.updateRatingAndReview(id, mediaType, rating, review)
    }
}