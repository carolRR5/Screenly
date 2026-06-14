package dam_a51568.screenly.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dam_a51568.screenly.data.model.MediaItem
import dam_a51568.screenly.data.remote.TmdbClient
import dam_a51568.screenly.data.repository.toMediaItem
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

// Estados possíveis do ecrã de pesquisa.
sealed class SearchUiState {
    data object Idle : SearchUiState() // Estado inicial, sem pesquisa ativa
    data object Loading : SearchUiState() // A carregar resultados da API
    data object Empty : SearchUiState() // Pesquisa feita, mas sem resultados
    data class Error(val message: String) : SearchUiState() // Erro ao pesquisar, com mensagem para o utilizador
    data class Success(val results: List<MediaItem>) : SearchUiState() // Pesquisa concluída com resultados
}

/**
 * ViewModel do ecrã de pesquisa.
 * Gere a query de pesquisa com debounce para evitar chamadas excessivas à API,
 * filtrando resultados sem póster ou sem media_type válido.
 *
 * A função debounce() do Flow ainda está marcada como @FlowPreview na biblioteca kotlinx.coroutines,
 * ou seja, é considerada uma API experimental que pode mudar em versões futuras. Este @OptIn diz ao
 * compilador que aceitamos esse risco e queremos usar debounce mesmo assim (sem ele, o código não compilava).
 */
@OptIn(FlowPreview::class)
class SearchViewModel : ViewModel() {
    // Estado mutável (privado) da UI, só pode ser alterado dentro do ViewModel
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    // Versão pública e só de leitura do estado da UI, exposta para o Composable observar
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    // Estado mutável (privado) do texto de pesquisa atual
    private val _query = MutableStateFlow("")
    // Versão pública e só de leitura do texto de pesquisa, exposta para o Composable observar
    val query: StateFlow<String> = _query.asStateFlow()

    // Bloco executado na criação do ViewModel: configura o pipeline reativo da pesquisa
    init {
        // Observa a query e só faz chamada à API após 400ms de inatividade
        _query
            // Espera 400ms sem novas emissões antes de deixar passar o valor
            // (evita disparar uma pesquisa a cada tecla premida)
            .debounce(400L)
            // Ignora emissões repetidas consecutivas (ex: o mesmo texto emitido duas vezes)
            .distinctUntilChanged()
            // Mínimo 2 caracteres para pesquisar
            // Só deixa passar valores com pelo menos 2 caracteres (sem espaços nas pontas)
            .filter { it.trim().length >= 2 }
            // Para cada valor que passa os filtros acima, dispara a pesquisa na API
            .onEach { search(it.trim()) }
            // Liga este pipeline ao ciclo de vida do ViewModel (cancela automaticamente quando o ViewModel é destruído)
            .launchIn(viewModelScope)
    }

    /**
     * Chamado pela UI sempre que o texto da barra de pesquisa muda.
     *
     * @param newQuery Novo texto introduzido pelo utilizador.
     */
    fun onQueryChange(newQuery: String) {
        // Atualiza o valor da query, o que despoleta o pipeline definido em init
        _query.value = newQuery
        // Se o texto tiver menos de 2 caracteres, volta de imediato ao estado inicial
        // (não espera pelo debounce, para a UI reagir instantaneamente ao apagar texto)
        if (newQuery.trim().length < 2) {
            _uiState.value = SearchUiState.Idle
        }
    }

    /**
     * Efetua a pesquisa na API da TMDB para o texto fornecido.
     *
     * @param query Texto de pesquisa já validado (mínimo 2 caracteres, sem espaços nas pontas).
     */
    private fun search(query: String) {
        // Lança uma coroutine associada ao ciclo de vida do ViewModel
        viewModelScope.launch {
            // Sinaliza à UI que a pesquisa está em curso (mostra o spinner)
            _uiState.value = SearchUiState.Loading
            try {
                // Chama o endpoint de pesquisa multi (filmes e séries) da TMDB
                val response = TmdbClient.apiService.searchMulti(
                    query = query,
                    apiKey = TmdbClient.API_KEY
                )
                // Filtra apenas filmes e séries com póster disponível e converte para modelo de domínio
                val filtered = response.results
                    // Mantém apenas itens do tipo "movie" ou "tv" que tenham póster definido
                    .filter { it.mediaType in listOf("movie", "tv") && it.posterPath != null }
                    // Converte cada item da API (DTO) para o modelo de domínio MediaItem
                    .map { it.toMediaItem() }

                // Define o estado final consoante existirem ou não resultados após o filtro
                _uiState.value = if (filtered.isEmpty()) {
                    // Nenhum resultado válido encontrado
                    SearchUiState.Empty
                } else {
                    // Resultados encontrados, mostra-os na grelha
                    SearchUiState.Success(filtered)
                }
            } catch (e: Exception) {
                // Qualquer erro (rede, parsing, etc.) resulta numa mensagem genérica de erro
                _uiState.value = SearchUiState.Error("Erro ao pesquisar. Verifique a ligação.")
            }
        }
    }
}