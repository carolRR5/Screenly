package dam_a51568.screenly.ui.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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
 * Tipos de filtro disponíveis no ecrã de resultados de navegação.
 * Cada valor corresponde a um conjunto diferente de parâmetros enviados à API do TMDb.
 */
enum class BrowseFilter {
    // Títulos ordenados por popularidade decrescente.
    POPULAR,
    // Títulos ordenados por classificação média decrescente.
    TOP_RATED,
    // Títulos lançados nos últimos 6 meses, ordenados por popularidade
    RECENT,
    // Filtra pelo género especificado em BrowseResultsViewModel.genreId
    GENRE,
    // Filtra pelo país de origem especificado em BrowseResultsViewModel.countryCode
    COUNTRY
}

/**
 * Estados possíveis do ecrã de resultados de navegação.
 * Segue o padrão sealed class para que a UI trate cada estado num `when` exaustivo.
 */
sealed class BrowseUiState {
    // Estado inicial e durante o carregamento
    data object Loading : BrowseUiState()

    // Estado de erro após falha na API. Inclui a mensagem a apresentar
    data class Error(val message: String) : BrowseUiState()

    // Estado de sucesso com os resultados intercalados de filmes e séries.
    data class Success(val results: List<MediaItem>) : BrowseUiState()
}

/**
 * ViewModel do ecrã de resultados de navegação.
 *
 * Carrega filmes e séries em paralelo usando o endpoint `discover` do TMDb
 * com os parâmetros adequados ao filtro especificado. Os resultados são
 * intercalados (filme, série, filme, série…) para garantir variedade visual
 * na grelha, independentemente do filtro aplicado.
 *
 * @param filter Tipo de filtro que determina os parâmetros da chamada à API.
 * @param genreId ID numérico do género no TMDb; obrigatório para [BrowseFilter.GENRE].
 * @param countryCode Código ISO do país; obrigatório para [BrowseFilter.COUNTRY].
 */
class BrowseResultsViewModel(
    private val filter: BrowseFilter,
    private val genreId: String? = null,
    private val countryCode: String? = null
) : ViewModel() {

    /**
     * Estado interno mutável da UI, inicializado como [BrowseUiState.Loading]
     * para que o spinner apareça imediatamente ao abrir o ecrã.
     */
    private val _uiState = MutableStateFlow<BrowseUiState>(BrowseUiState.Loading)

    // Estado público imutável observado pela UI para reagir a mudanças
    val uiState: StateFlow<BrowseUiState> = _uiState.asStateFlow()

    init {
        // Inicia o carregamento assim que o ViewModel é criado
        loadResults()
    }

    /**
     * Carrega filmes e séries em paralelo com o filtro especificado e combina os resultados.
     *
     * Usa [async] para lançar os dois endpoints em simultâneo, minimizando a latência.
     * O parâmetro `sortBy` é derivado do [filter]: TOP_RATED usa classificação decrescente,
     * todos os outros usam popularidade decrescente.
     * Para [BrowseFilter.RECENT], calcula a data de 6 meses atrás para o parâmetro de data mínima.
     * Os resultados são intercalados manualmente para alternar filmes e séries na grelha.
     */
    private fun loadResults() {
        viewModelScope.launch {
            _uiState.value = BrowseUiState.Loading

            try {
                // Determina o critério de ordenação: classificação para TOP_RATED, popularidade para os restantes
                val sortBy = when (filter) {
                    BrowseFilter.TOP_RATED -> "vote_average.desc"
                    else -> "popularity.desc"
                }

                // Calcula a data mínima de lançamento apenas para o filtro RECENT (últimos 6 meses)
                val recentDate = if (filter == BrowseFilter.RECENT) {
                    val calendar = java.util.Calendar.getInstance()
                    calendar.add(java.util.Calendar.MONTH, -6) // Subtrai 6 meses à data atual
                    java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        .format(calendar.time) // Formata como "yyyy-MM-dd" para a API
                } else null // Null para os outros filtros: a API ignora o parâmetro

                // Lança as duas chamadas à API em paralelo para minimizar o tempo de espera
                val moviesDeferred = async {
                    TmdbClient.apiService.discoverMovies(
                        apiKey = TmdbClient.API_KEY,
                        sortBy = sortBy,
                        withGenres = genreId, // Null se não for filtro de género
                        withOriginCountry = countryCode, // Null se não for filtro de país
                        primaryReleaseDateGte = recentDate // Null se não for filtro de recentes
                    )
                }

                val tvDeferred = async {
                    TmdbClient.apiService.discoverTvShows(
                        apiKey = TmdbClient.API_KEY,
                        sortBy = sortBy,
                        withGenres = genreId,
                        withOriginCountry = countryCode,
                        firstAirDateGte = recentDate // Equivalente ao release date mas para séries
                    )
                }

                // Aguarda os filmes e filtra itens sem póster antes de converter para MediaItem
                val movies = moviesDeferred.await().results
                    .filter { it.posterPath != null } // Exclui itens sem póster
                    .map { it.copy(mediaType = "movie").toMediaItem() } // Força mediaType (não devolvido pelo discover)

                // Aguarda as séries com o mesmo tratamento
                val tvShows = tvDeferred.await().results
                    .filter { it.posterPath != null }
                    .map { it.copy(mediaType = "tv").toMediaItem() }

                // Intercala filmes e séries para variedade visual na grelha (filme, série, filme, série…)
                val combined = mutableListOf<MediaItem>()
                val maxSize = maxOf(movies.size, tvShows.size) // Itera até ao tamanho da lista maior
                for (i in 0 until maxSize) {
                    if (i < movies.size) combined.add(movies[i])  // Adiciona o filme da posição i
                    if (i < tvShows.size) combined.add(tvShows[i]) // Adiciona a série da posição i
                }

                _uiState.value = BrowseUiState.Success(combined)

            } catch (e: Exception) {
                // Qualquer falha de rede ou parsing transita para Error com mensagem genérica
                _uiState.value = BrowseUiState.Error("Erro ao carregar resultados. Verifique a ligação.")
            }
        }
    }
}

/**
 * Factory para criar o [BrowseResultsViewModel] com os argumentos do filtro.
 *
 * Necessária porque o [BrowseResultsViewModel] recebe parâmetros no construtor,
 * o que impede a criação automática pelo sistema padrão do Compose.
 *
 * @param filter Tipo de filtro a aplicar.
 * @param genreId ID do género; null se não for filtro de género.
 * @param countryCode Código do país; null se não for filtro de país.
 */
class BrowseResultsViewModelFactory(
    private val filter: BrowseFilter,
    private val genreId: String? = null,
    private val countryCode: String? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        // Instancia o ViewModel com os três argumentos e faz cast seguro para T
        return BrowseResultsViewModel(filter, genreId, countryCode) as T
    }
}