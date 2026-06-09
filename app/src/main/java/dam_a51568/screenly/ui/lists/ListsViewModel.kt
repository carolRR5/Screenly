package dam_a51568.screenly.ui.lists

import androidx.lifecycle.ViewModel
import dam_a51568.screenly.data.repository.WatchStatus
import dam_a51568.screenly.data.repository.WatchlistItem
import dam_a51568.screenly.data.repository.WatchlistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel do ecrã de Listas.
 * Gere o separador ativo e devolve os itens filtrados pelo estado correspondente.
 *
 * @param initialStatus Estado inicial do separador a mostrar quando o ecrã abre.
 */
class ListsViewModel(initialStatus: WatchStatus = WatchStatus.TO_WATCH) : ViewModel() {

    private val _selectedTab = MutableStateFlow(initialStatus)
    val selectedTab: StateFlow<WatchStatus> = _selectedTab.asStateFlow()

    /**
     * Altera o separador ativo.
     *
     * @param status Estado de visualização correspondente ao separador selecionado.
     */
    fun selectTab(status: WatchStatus) {
        _selectedTab.value = status
    }

    /**
     * Devolve os itens da watchlist filtrados pelo estado especificado.
     *
     * @param status Estado de visualização pelo qual filtrar.
     * @return Lista de itens com o estado correspondente.
     */
    fun getItemsByStatus(status: WatchStatus): List<WatchlistItem> =
        WatchlistRepository.getByStatus(status)
}