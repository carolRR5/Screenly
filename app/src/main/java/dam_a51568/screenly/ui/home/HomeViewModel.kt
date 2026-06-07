package dam_a51568.screenly.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dam_a51568.screenly.data.models.TmdbMediaItem
import dam_a51568.screenly.data.remote.TmdbClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estados possíveis do ecrã de Início
sealed class HomeUiState {
    // A carregar os títulos em tendência.
    data object Loading : HomeUiState()
    // Erro ao carregar os títulos.
    data class Error(val message: String) : HomeUiState()
    // Títulos carregados com sucesso.
    data class Success(val results: List<TmdbMediaItem>) : HomeUiState()
}

/**
 * ViewModel do ecrã de Início.
 * Carrega os títulos em tendência da semana a partir da API do TMDb
 * assim que é inicializado.
 */
class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTrending()
    }

    /**
     * Carrega os títulos em tendência da semana a partir do endpoint /trending/all/week.
     * Filtra resultados sem poster ou sem media_type válido ("movie" ou "tv").
     */
    private fun loadTrending() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val response = TmdbClient.apiService.getTrending(
                    apiKey = TmdbClient.API_KEY
                )
                val filtered = response.results.filter {
                    it.mediaType in listOf("movie", "tv") && it.posterPath != null
                }
                _uiState.value = HomeUiState.Success(filtered)
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Erro ao carregar tendências. Verifique a ligação.")
            }
        }
    }
}