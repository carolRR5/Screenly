package dam_a51568.screenly.ui.lists

import androidx.lifecycle.ViewModel
import dam_a51568.screenly.data.model.WatchStatus
import dam_a51568.screenly.data.model.WatchlistItem
import dam_a51568.screenly.data.repository.WatchlistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel do ecrã de Listas.
 *
 * Responsável por gerir o estado da UI das listas da watchlist do utilizador.
 * Mantém o separador ativo e expõe os itens filtrados pelo estado correspondente,
 * garantindo que a UI reage automaticamente a alterações através de [StateFlow].
 *
 * Este ViewModel não possui coroutines próprias porque os dados são lidos
 * sincronamente do [WatchlistRepository], que funciona como fonte de verdade
 * em memória durante a sessão.
 *
 * @param initialStatus Estado inicial do separador a mostrar quando o ecrã abre.
 *                      Por omissão, abre no separador [WatchStatus.TO_WATCH].
 */
class ListsViewModel(initialStatus: WatchStatus = WatchStatus.TO_WATCH) : ViewModel() {
    // Estado interno mutável do separador atualmente selecionado.
    // Apenas acessível dentro do ViewModel para evitar mutações externas.
    private val _selectedTab = MutableStateFlow(initialStatus)

    // Estado público do separador selecionado, exposto como [StateFlow] imutável.
    // A UI observa este fluxo para reagir a alterações de separador.
    val selectedTab: StateFlow<WatchStatus> = _selectedTab.asStateFlow()

    /**
     * Altera o separador ativo para o estado especificado.
     * Chamado pela UI quando o utilizador toca num dos separadores do topo.
     *
     * @param status Estado de visualização correspondente ao separador selecionado.
     */
    fun selectTab(status: WatchStatus) {
        _selectedTab.value = status
    }

    /**
     * Devolve os itens da watchlist filtrados pelo estado especificado.
     *
     * A filtragem é feita em cada chamada sobre os dados em memória do
     * [WatchlistRepository]. Caso não existam itens para o estado dado,
     * é devolvida uma lista vazia, e a UI apresenta a mensagem de conteúdo vazio.
     *
     * @param status Estado de visualização pelo qual filtrar (ex: [WatchStatus.WATCHED]).
     * @return Lista de [WatchlistItem] cujo estado corresponde ao parâmetro recebido,
     *         ou lista vazia se não houver itens para esse estado.
     */
    fun getItemsByStatus(status: WatchStatus): List<WatchlistItem> =
        WatchlistRepository.getByStatus(status)
}