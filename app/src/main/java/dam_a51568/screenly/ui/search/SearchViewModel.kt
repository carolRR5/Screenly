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
    data class Error(val message: String) : SearchUiState()
    data class Success(val results: List<MediaItem>) : SearchUiState()
}

/**
 * ViewModel do ecrã de pesquisa.
 * Gere a query de pesquisa com debounce para evitar chamadas excessivas à API,
 * filtrando resultados sem póster ou sem media_type válido.
 */
@OptIn(FlowPreview::class)
class SearchViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    init {
        // Observa a query e só faz chamada à API após 400ms de inatividade
        _query
            .debounce(400L)
            .distinctUntilChanged()
            .filter { it.trim().length >= 2 } // Mínimo 2 caracteres para pesquisar
            .onEach { search(it.trim()) }
            .launchIn(viewModelScope)
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        if (newQuery.trim().length < 2) {
            _uiState.value = SearchUiState.Idle
        }
    }

    private fun search(query: String) {
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            try {
                val response = TmdbClient.apiService.searchMulti(
                    query = query,
                    apiKey = TmdbClient.API_KEY
                )
                // Filtra apenas filmes e séries com póster disponível e converte para modelo de domínio
                val filtered = response.results
                    .filter { it.mediaType in listOf("movie", "tv") && it.posterPath != null }
                    .map { it.toMediaItem() }

                _uiState.value = if (filtered.isEmpty()) {
                    SearchUiState.Empty
                } else {
                    SearchUiState.Success(filtered)
                }
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error("Erro ao pesquisar. Verifique a ligação.")
            }
        }
    }
}
