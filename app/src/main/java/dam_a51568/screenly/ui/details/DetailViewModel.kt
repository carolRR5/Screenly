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

/**
 * Dados do título carregados com sucesso, mapeados a partir da resposta da API.
 * Unifica filmes e séries numa estrutura comum para simplificar a UI.
 *
 * @param id Identificador único no TMDb.
 * @param mediaType "movie" ou "tv".
 * @param title Título do filme ou nome da série.
 * @param overview Sinopse; substituída por texto padrão se estiver vazia.
 * @param posterPath Caminho relativo do póster no TMDb (sem a base URL).
 * @param year Ano de lançamento ou de estreia.
 * @param genres Lista de géneros por extenso (ex: ["Ação", "Drama"]).
 * @param voteAverage Classificação média dos utilizadores do TMDb.
 * @param runtime Duração formatada: "X min" para filmes, "X min/ep" para séries.
 */
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

/**
 * Estados possíveis do ecrã de Detalhes.
 * Segue o padrão sealed class para que a UI trate cada estado num `when` exaustivo.
 */
sealed class DetailUiState {
    /** Estado inicial e durante o carregamento; a UI mostra um spinner. */
    data object Loading : DetailUiState()

    /** Estado de erro após falha na API; inclui a mensagem a apresentar ao utilizador. */
    data class Error(val message: String) : DetailUiState()

    /** Estado de sucesso com todos os dados do título carregados e prontos a apresentar. */
    data class Success(val data: DetailUiData) : DetailUiState()
}

/**
 * ViewModel do ecrã de Detalhes.
 *
 * Carrega em paralelo os dados do título, créditos, trailer, reviews e títulos
 * similares através de chamadas assíncronas à API do TMDb. Expõe cada conjunto
 * de dados num [StateFlow] independente para que a UI reaja seletivamente.
 *
 * A paginação de reviews é gerida internamente: primeiro esgota as reviews
 * já carregadas em memória (incrementando [_visibleReviewsCount] de 15 em 15)
 * antes de ir buscar a página seguinte à API.
 */
class DetailViewModel : ViewModel() {

    // Estado principal da UI (Loading / Error / Success com os dados do título).
    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    // Estado atual do título na watchlist; null se não estiver adicionado.
    private val _watchStatus = MutableStateFlow<WatchStatus?>(null)
    val watchStatus: StateFlow<WatchStatus?> = _watchStatus.asStateFlow()

    /** Lista dos primeiros 20 membros do elenco, carregada com os créditos. */
    private val _cast = MutableStateFlow<List<CastMember>>(emptyList())
    val cast: StateFlow<List<CastMember>> = _cast.asStateFlow()

    /**
     * Lista de membros da equipa técnica, filtrada pelos departamentos mais relevantes
     * (Realização, Argumento, Produção, Som, Câmara) e sem duplicados por [id].
     */
    private val _crew = MutableStateFlow<List<CrewMember>>(emptyList())
    val crew: StateFlow<List<CrewMember>> = _crew.asStateFlow()

    // URL do trailer oficial no YouTube; null se não houver trailer disponível.
    private val _trailerUrl = MutableStateFlow<String?>(null)
    val trailerUrl: StateFlow<String?> = _trailerUrl.asStateFlow()

    /**
     * Número de reviews atualmente visíveis no ecrã.
     * Inicializado a 15 e incrementado de 15 em 15 ao carregar mais.
     */
    private val _visibleReviewsCount = MutableStateFlow(15)
    val visibleReviewsCount: StateFlow<Int> = _visibleReviewsCount.asStateFlow()

    // Lista completa de reviews carregadas em memória (pode crescer com a paginação).
    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    // Página de reviews atualmente carregada da API (começa em 1)
    private var currentReviewPage = 1

    // Total de páginas de reviews disponíveis na API para este título
    private var totalReviewPages = 1

    /**
     * Indica se há mais reviews a apresentar, seja em memória ou na API.
     * Verdadeiro se [_visibleReviewsCount] for menor que o total em memória,
     * ou se ainda existirem páginas por carregar na API.
     */
    val hasMoreReviews: Boolean
        get() = _visibleReviewsCount.value < _reviews.value.size ||
                currentReviewPage < totalReviewPages

    /** Lista de títulos similares ao atual, filtrados para excluir itens sem póster. */
    private val _similarTitles = MutableStateFlow<List<MediaItem>>(emptyList())
    val similarTitles: StateFlow<List<MediaItem>> = _similarTitles.asStateFlow()

    /**
     * Carrega em paralelo todos os dados necessários para o ecrã de detalhes.
     *
     * Usa [async] para lançar as cinco chamadas à API em simultâneo, minimizando
     * a latência total. Os detalhes e créditos são aguardados primeiro porque são
     * necessários para atualizar o estado principal da UI; as restantes chamadas
     * (vídeos, reviews, similares) são resolvidas a seguir sem bloquear o ecrã.
     *
     * @param id Identificador do título no TMDb.
     * @param mediaType "movie" para filmes ou "tv" para séries.
     */
    fun loadDetails(id: Int, mediaType: String) {
        viewModelScope.launch {
            // Transita para Loading ao iniciar um novo carregamento
            _uiState.value = DetailUiState.Loading
            try {
                // Lança as cinco chamadas à API em paralelo para minimizar o tempo de espera
                val detailsDeferred = async {
                    if (mediaType == "movie") {
                        // Filmes: converte a resposta para Movie e depois para DetailUiData
                        val movie = TmdbClient.apiService.getMovieDetails(id, TmdbClient.API_KEY).toMovie()
                        DetailUiData(
                            id = movie.id,
                            mediaType = "movie",
                            title = movie.title,
                            // Substitui sinopse vazia por texto padrão para evitar campo em branco
                            overview = movie.overview.ifEmpty { "Sinopse não disponível." },
                            // Remove a base URL do póster para guardar apenas o caminho relativo
                            posterPath = movie.posterUrl.removePrefix(TmdbClient.IMAGE_BASE_URL),
                            year = movie.year,
                            genres = movie.genres,
                            voteAverage = movie.rating,
                            runtime = "${movie.runtime} min" // Duração em minutos para filmes
                        )
                    } else {
                        // Séries: converte a resposta para TvShow e depois para DetailUiData
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
                            runtime = "${tv.episodeRuntime} min/ep" // Duração por episódio para séries
                        )
                    }
                }

                // Créditos: endpoint diferente para filmes e séries
                val creditsDeferred = async {
                    if (mediaType == "movie") {
                        TmdbClient.apiService.getMovieCredits(id, TmdbClient.API_KEY)
                    } else {
                        TmdbClient.apiService.getTvCredits(id, TmdbClient.API_KEY)
                    }
                }

                // Vídeos: usado para encontrar o trailer oficial no YouTube
                val videosDeferred = async {
                    if (mediaType == "movie") {
                        TmdbClient.apiService.getMovieVideos(id, TmdbClient.API_KEY)
                    } else {
                        TmdbClient.apiService.getTvVideos(id, TmdbClient.API_KEY)
                    }
                }

                // Reviews: primeira página; páginas seguintes carregadas por loadMoreReviews
                val reviewsDeferred = async {
                    if (mediaType == "movie") {
                        TmdbClient.apiService.getMovieReviews(id, TmdbClient.API_KEY)
                    } else {
                        TmdbClient.apiService.getTvReviews(id, TmdbClient.API_KEY)
                    }
                }

                // Títulos similares: filtrados para excluir itens sem póster
                val similarDeferred = async {
                    if (mediaType == "movie") {
                        TmdbClient.apiService.getSimilarMovies(id, TmdbClient.API_KEY)
                    } else {
                        TmdbClient.apiService.getSimilarTvShows(id, TmdbClient.API_KEY)
                    }
                }

                // Aguarda os dados essenciais antes de atualizar o estado principal da UI
                val details = detailsDeferred.await()
                val credits = creditsDeferred.await()

                // Atualiza o estado para Success com os dados do título
                _uiState.value = DetailUiState.Success(details)

                // Verifica se o título já está na watchlist para mostrar o estado correto
                _watchStatus.value = WatchlistRepository.getStatus(id, mediaType)

                // Limita o elenco a 20 membros para não sobrecarregar a secção de créditos
                _cast.value = credits.cast.take(20).map { it.toCastMember() }

                // Filtra a crew pelos departamentos mais relevantes, remove duplicados pelo id
                // (a mesma pessoa pode ter várias funções) e ordena por departamento
                _crew.value = credits.crew
                    .filter { it.department in listOf("Directing", "Writing", "Production", "Sound", "Camera") }
                    .distinctBy { it.id } // Remove o mesmo membro com funções repetidas
                    .sortedBy { it.department } // Agrupa visualmente por departamento
                    .map { it.toCrewMember() }

                val videos = videosDeferred.await()

                // Dá prioridade ao trailer oficial; se não existir, usa qualquer trailer do YouTube
                _trailerUrl.value = videos.results
                    .filter { it.site == "YouTube" && it.type == "Trailer" }
                    .firstOrNull { it.official }?.youtubeUrl  // 1.ª opção: trailer oficial
                    ?: videos.results
                        .firstOrNull { it.site == "YouTube" && it.type == "Trailer" }?.youtubeUrl // 2.ª opção: qualquer trailer

                val reviewsResponse = reviewsDeferred.await()
                // Substitui as reviews existentes pelas da primeira página (reset ao carregar novo título)
                _reviews.value = reviewsResponse.results.map { it.toReview() }
                totalReviewPages = reviewsResponse.totalPages // Guarda o total para controlo de paginação
                currentReviewPage = 1 // Reinicia o contador de página
                _visibleReviewsCount.value = 15 // Reinicia o número de reviews visíveis

                // Filtra títulos similares sem póster e força o mediaType (não devolvido pela API)
                _similarTitles.value = similarDeferred.await().results
                    .filter { it.posterPath != null }
                    .map { it.copy(mediaType = mediaType).toMediaItem() }

            } catch (e: Exception) {
                // Qualquer falha de rede ou parsing transita para Error com mensagem genérica
                _uiState.value = DetailUiState.Error("Erro ao carregar detalhes. Tente novamente.")
            }
        }
    }

    /**
     * Adiciona ou atualiza o título na watchlist com o estado especificado.
     * Constrói um [WatchlistItem] a partir dos dados atuais do ecrã e delega
     * a persistência ao [WatchlistRepository].
     *
     * @param data Dados atuais do título visível no ecrã.
     * @param status Novo estado da watchlist a atribuir ao título.
     */
    fun addToWatchlist(data: DetailUiData, status: WatchStatus) {
        val item = WatchlistItem(
            id = data.id,
            mediaType = data.mediaType,
            title = data.title,
            posterPath = data.posterPath,
            year = data.year,
            genres = data.genres.joinToString(", "), // Lista de géneros convertida para String separada por vírgulas
            status = status
        )
        WatchlistRepository.addOrUpdate(item) // Cria ou substitui o item existente no repositório
        _watchStatus.value = status // Atualiza o estado local para refletir imediatamente na UI
    }

    /**
     * Remove o título da watchlist e limpa o estado local.
     *
     * @param id Identificador do título a remover.
     * @param mediaType Tipo de conteúdo do título a remover.
     */
    fun removeFromWatchlist(id: Int, mediaType: String) {
        WatchlistRepository.remove(id, mediaType) // Remove do repositório em memória
        _watchStatus.value = null // Limpa o estado local para a UI refletir a remoção
    }

    /**
     * Guarda a classificação e a review pessoal do utilizador no repositório.
     * Não atualiza nenhum StateFlow porque a UI relê os dados via [WatchlistRepository.items].
     *
     * @param id Identificador do título a atualizar.
     * @param mediaType Tipo de conteúdo do título a atualizar.
     * @param rating Classificação atribuída pelo utilizador (1f a 5f).
     * @param review Texto da review pessoal escrita pelo utilizador.
     */
    fun saveRatingAndReview(id: Int, mediaType: String, rating: Float, review: String) {
        WatchlistRepository.updateRatingAndReview(id, mediaType, rating, review)
    }

    /**
     * Carrega mais reviews, primeiro a partir das já em memória e depois da API.
     *
     * Estratégia de dois níveis:
     * 1. Se [_visibleReviewsCount] for menor que o total em memória, incrementa
     *    o contador para mostrar mais reviews sem chamada à API.
     * 2. Se todas as reviews em memória já estiverem visíveis e ainda houver
     *    páginas na API, carrega a próxima página e incrementa o contador.
     *
     * @param id Identificador do título cujas reviews carregar.
     * @param mediaType Tipo de conteúdo para selecionar o endpoint correto.
     */
    fun loadMoreReviews(id: Int, mediaType: String) {
        viewModelScope.launch {
            if (_visibleReviewsCount.value < _reviews.value.size) {
                // Ainda há reviews em memória por mostrar: incrementa o contador sem chamada à API
                _visibleReviewsCount.value += 15
                return@launch // Sai da coroutine sem ir à API
            }
            if (currentReviewPage < totalReviewPages) {
                try {
                    currentReviewPage++ // Avança para a próxima página antes da chamada
                    val response = if (mediaType == "movie") {
                        TmdbClient.apiService.getMovieReviews(id, TmdbClient.API_KEY, page = currentReviewPage)
                    } else {
                        TmdbClient.apiService.getTvReviews(id, TmdbClient.API_KEY, page = currentReviewPage)
                    }
                    // Acrescenta as novas reviews às já existentes em memória
                    _reviews.value += response.results.map { it.toReview() }
                    _visibleReviewsCount.value += 15 // Mostra as reviews recém-carregadas
                } catch (e: Exception) {
                    // Falha silenciosa: o utilizador pode tentar novamente ao clicar no botão
                }
            }
        }
    }
}