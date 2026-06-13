package dam_a51568.screenly.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dam_a51568.screenly.data.model.CastMember
import dam_a51568.screenly.data.model.CrewMember
import dam_a51568.screenly.data.model.MediaItem
import dam_a51568.screenly.data.model.Review
import dam_a51568.screenly.data.model.WatchStatus
import dam_a51568.screenly.data.model.WatchlistItem
import dam_a51568.screenly.data.remote.TmdbClient
import dam_a51568.screenly.data.repository.WatchlistRepository
import dam_a51568.screenly.data.repository.toCastMember
import dam_a51568.screenly.data.repository.toCrewMember
import dam_a51568.screenly.data.repository.toMediaItem
import dam_a51568.screenly.data.repository.toMovie
import dam_a51568.screenly.data.repository.toReview
import dam_a51568.screenly.data.repository.toTvShow
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetailUiData(
    val id: Int,
    val mediaType: String,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val year: String,
    val genres: List<String>,
    val voteAverage: Double,
    val runtime: String
)

sealed class DetailUiState {
    data object Loading : DetailUiState()
    data class Error(val message: String) : DetailUiState()
    data class Success(val data: DetailUiData) : DetailUiState()
}

class DetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _watchStatus = MutableStateFlow<WatchStatus?>(null)
    val watchStatus: StateFlow<WatchStatus?> = _watchStatus.asStateFlow()

    private val _cast = MutableStateFlow<List<CastMember>>(emptyList())
    val cast: StateFlow<List<CastMember>> = _cast.asStateFlow()

    private val _crew = MutableStateFlow<List<CrewMember>>(emptyList())
    val crew: StateFlow<List<CrewMember>> = _crew.asStateFlow()

    private val _trailerUrl = MutableStateFlow<String?>(null)
    val trailerUrl: StateFlow<String?> = _trailerUrl.asStateFlow()

    private val _visibleReviewsCount = MutableStateFlow(15)
    val visibleReviewsCount: StateFlow<Int> = _visibleReviewsCount.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private var currentReviewPage = 1
    private var totalReviewPages = 1

    val hasMoreReviews: Boolean
        get() = _visibleReviewsCount.value < _reviews.value.size ||
                currentReviewPage < totalReviewPages

    private val _similarTitles = MutableStateFlow<List<MediaItem>>(emptyList())
    val similarTitles: StateFlow<List<MediaItem>> = _similarTitles.asStateFlow()

    fun loadDetails(id: Int, mediaType: String) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            try {
                val detailsDeferred = async {
                    if (mediaType == "movie") {
                        val movie = TmdbClient.apiService.getMovieDetails(id, TmdbClient.API_KEY).toMovie()
                        DetailUiData(
                            id = movie.id,
                            mediaType = "movie",
                            title = movie.title,
                            overview = movie.overview.ifEmpty { "Sinopse não disponível." },
                            posterPath = movie.posterUrl.removePrefix(TmdbClient.IMAGE_BASE_URL),
                            year = movie.year,
                            genres = movie.genres,
                            voteAverage = movie.rating,
                            runtime = "${movie.runtime} min"
                        )
                    } else {
                        val tv = TmdbClient.apiService.getTvShowDetails(id, TmdbClient.API_KEY).toTvShow()
                        DetailUiData(
                            id = tv.id,
                            mediaType = "tv",
                            title = tv.title,
                            overview = tv.overview.ifEmpty { "Sinopse não disponível." },
                            posterPath = tv.posterUrl.removePrefix(TmdbClient.IMAGE_BASE_URL),
                            year = tv.year,
                            genres = tv.genres,
                            voteAverage = tv.rating,
                            runtime = "${tv.episodeRuntime} min/ep"
                        )
                    }
                }

                val creditsDeferred = async {
                    if (mediaType == "movie") {
                        TmdbClient.apiService.getMovieCredits(id, TmdbClient.API_KEY)
                    } else {
                        TmdbClient.apiService.getTvCredits(id, TmdbClient.API_KEY)
                    }
                }

                val videosDeferred = async {
                    if (mediaType == "movie") {
                        TmdbClient.apiService.getMovieVideos(id, TmdbClient.API_KEY)
                    } else {
                        TmdbClient.apiService.getTvVideos(id, TmdbClient.API_KEY)
                    }
                }

                val reviewsDeferred = async {
                    if (mediaType == "movie") {
                        TmdbClient.apiService.getMovieReviews(id, TmdbClient.API_KEY)
                    } else {
                        TmdbClient.apiService.getTvReviews(id, TmdbClient.API_KEY)
                    }
                }

                val similarDeferred = async {
                    if (mediaType == "movie") {
                        TmdbClient.apiService.getSimilarMovies(id, TmdbClient.API_KEY)
                    } else {
                        TmdbClient.apiService.getSimilarTvShows(id, TmdbClient.API_KEY)
                    }
                }

                val details = detailsDeferred.await()
                val credits = creditsDeferred.await()

                _uiState.value = DetailUiState.Success(details)
                _watchStatus.value = WatchlistRepository.getStatus(id, mediaType)

                _cast.value = credits.cast.take(20).map { it.toCastMember() }

                _crew.value = credits.crew
                    .filter { it.department in listOf("Directing", "Writing", "Production", "Sound", "Camera") }
                    .distinctBy { it.id }
                    .sortedBy { it.department }
                    .map { it.toCrewMember() }

                val videos = videosDeferred.await()
                _trailerUrl.value = videos.results
                    .filter { it.site == "YouTube" && it.type == "Trailer" }
                    .firstOrNull { it.official }?.youtubeUrl
                    ?: videos.results
                        .firstOrNull { it.site == "YouTube" && it.type == "Trailer" }?.youtubeUrl

                val reviewsResponse = reviewsDeferred.await()
                _reviews.value = reviewsResponse.results.map { it.toReview() }
                totalReviewPages = reviewsResponse.totalPages
                currentReviewPage = 1
                _visibleReviewsCount.value = 15

                _similarTitles.value = similarDeferred.await().results
                    .filter { it.posterPath != null }
                    .map { it.copy(mediaType = mediaType).toMediaItem() }

            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error("Erro ao carregar detalhes. Tente novamente.")
            }
        }
    }

    fun addToWatchlist(data: DetailUiData, status: WatchStatus) {
        val item = WatchlistItem(
            id = data.id,
            mediaType = data.mediaType,
            title = data.title,
            posterPath = data.posterPath,
            year = data.year,
            genres = data.genres.joinToString(", "),
            status = status
        )
        WatchlistRepository.addOrUpdate(item)
        _watchStatus.value = status
    }

    fun removeFromWatchlist(id: Int, mediaType: String) {
        WatchlistRepository.remove(id, mediaType)
        _watchStatus.value = null
    }

    fun saveRatingAndReview(id: Int, mediaType: String, rating: Float, review: String) {
        WatchlistRepository.updateRatingAndReview(id, mediaType, rating, review)
    }

    fun loadMoreReviews(id: Int, mediaType: String) {
        viewModelScope.launch {
            if (_visibleReviewsCount.value < _reviews.value.size) {
                _visibleReviewsCount.value += 15
                return@launch
            }
            if (currentReviewPage < totalReviewPages) {
                try {
                    currentReviewPage++
                    val response = if (mediaType == "movie") {
                        TmdbClient.apiService.getMovieReviews(id, TmdbClient.API_KEY, page = currentReviewPage)
                    } else {
                        TmdbClient.apiService.getTvReviews(id, TmdbClient.API_KEY, page = currentReviewPage)
                    }
                    _reviews.value += response.results.map { it.toReview() }
                    _visibleReviewsCount.value += 15
                } catch (e: Exception) { }
            }
        }
    }
}