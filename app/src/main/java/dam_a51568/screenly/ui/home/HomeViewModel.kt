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
 * Representa os dados carregados com sucesso para o ecrã de Início.
 * Agrupa os resultados das três chamadas à API numa única estrutura.
 *
 * @param trending Lista de títulos em tendência esta semana (filmes e séries misturados).
 * @param popularMovies Lista de filmes populares no momento.
 * @param popularTvShows Lista de séries populares no momento.
 */
data class HomeData(
    val trending: List<MediaItem>,
    val popularMovies: List<MediaItem>,
    val popularTvShows: List<MediaItem>
)

/**
 * Estados possíveis do ecrã de Início.
 *
 * Segue o padrão sealed class para garantir que a UI trata
 * explicitamente cada estado possível num bloco `when` exaustivo.
 */
sealed class HomeUiState {
    // Estado inicial e durante o primeiro carregamento; a UI mostra um spinner
    data object Loading : HomeUiState()

    // Estado de erro após falha na chamada à API; inclui a mensagem a apresentar
    data class Error(val message: String) : HomeUiState()

    // Estado de sucesso com os dados carregados e prontos a apresentar
    data class Success(val data: HomeData) : HomeUiState()
}

/**
 * ViewModel do ecrã de Início.
 *
 * Responsável por carregar em paralelo os dados das três secções da API do TMDb
 * e expor o resultado à UI através de [StateFlow]. Suporta Pull-to-Refresh,
 * distinguindo entre o carregamento inicial (mostra spinner) e o refrescamento
 * (mantém o conteúdo atual e mostra o indicador de pull-to-refresh).
 */
class HomeViewModel : ViewModel() {
    /**
     * Estado interno mutável da UI, inicializado como [HomeUiState.Loading]
     * para que a UI mostre o spinner imediatamente ao abrir o ecrã.
     */
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)

    // Estado público imutável observado pela UI para reagir a mudanças
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /**
     * Estado interno mutável do indicador de Pull-to-Refresh.
     * Separado do [_uiState] para não substituir os dados visíveis durante o refresh.
     */
    private val _isRefreshing = MutableStateFlow(false)

    // Estado público do Pull-to-Refresh, observado pelo PullToRefreshBox na UI
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        // Inicia o carregamento dos dados assim que o ViewModel é criado
        loadHomeData()
    }

    /**
     * Força o recarregamento dos dados da API, ativando o indicador de Pull-to-Refresh.
     * Chamado pela UI quando o utilizador arrasta o ecrã para baixo.
     */
    fun refresh() {
        loadHomeData(isRefresh = true)
    }

    /**
     * Carrega em paralelo os dados das três secções da API do TMDb.
     *
     * Usa [supervisorScope] para lançar as três chamadas em simultâneo com [async],
     * reduzindo o tempo total de espera. Se qualquer chamada falhar, o bloco
     * `catch` apanha a exceção e transita o estado para [HomeUiState.Error].
     * O bloco `finally` garante que o indicador de refresh é sempre desativado,
     * independentemente do sucesso ou falha das chamadas.
     *
     * @param isRefresh Se `true`, ativa o Pull-to-Refresh sem substituir o conteúdo
     *                  atual pelo spinner; se `false`, mostra o estado [HomeUiState.Loading].
     */
    private fun loadHomeData(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                // Refresh: ativa o indicador de pull-to-refresh sem apagar o conteúdo atual
                _isRefreshing.value = true
            } else {
                // Carregamento inicial: substitui o conteúdo pelo spinner
                _uiState.value = HomeUiState.Loading
            }

            try {
                // supervisorScope garante que as três coroutines correm em paralelo
                // e que a falha de uma não cancela automaticamente as restantes
                supervisorScope {
                    // Lança as três chamadas à API em simultâneo para minimizar a latência total
                    val trendingDeferred = async {
                        TmdbClient.apiService.getTrending(apiKey = TmdbClient.API_KEY)
                    }
                    val moviesDeferred = async {
                        TmdbClient.apiService.getPopularMovies(apiKey = TmdbClient.API_KEY)
                    }
                    val tvDeferred = async {
                        TmdbClient.apiService.getPopularTvShows(apiKey = TmdbClient.API_KEY)
                    }

                    // Aguarda o resultado das tendências e filtra itens sem póster
                    // ou com mediaType inválido (ex: "person") antes de converter
                    val trending = trendingDeferred.await().results
                        .filter { it.mediaType in listOf("movie", "tv") && it.posterPath != null }
                        .map { it.toMediaItem() }

                    // Aguarda os filmes populares, filtra itens sem póster
                    // e força o mediaType a "movie" (a API de populares não o devolve)
                    val movies = moviesDeferred.await().results
                        .filter { it.posterPath != null }
                        .map { it.copy(mediaType = "movie").toMediaItem() }

                    // Aguarda as séries populares, filtra itens sem póster
                    // e força o mediaType a "tv" pelo mesmo motivo que os filmes
                    val tvShows = tvDeferred.await().results
                        .filter { it.posterPath != null }
                        .map { it.copy(mediaType = "tv").toMediaItem() }

                    // Todos os dados carregados com sucesso: atualiza o estado para Success
                    _uiState.value = HomeUiState.Success(
                        HomeData(trending, movies, tvShows)
                    )
                }
            } catch (e: Exception) {
                // Qualquer erro de rede ou parsing transita o estado para Error
                // com uma mensagem genérica adequada ao utilizador final
                _uiState.value = HomeUiState.Error("Erro ao carregar conteúdos. Verifique a ligação.")
            } finally {
                // Desativa sempre o indicador de refresh, mesmo em caso de erro,
                // para que o utilizador possa tentar novamente
                _isRefreshing.value = false
            }
        }
    }
}