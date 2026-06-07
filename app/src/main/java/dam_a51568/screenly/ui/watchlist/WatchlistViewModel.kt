package dam_a51568.screenly.ui.watchlist

import androidx.lifecycle.ViewModel
import dam_a51568.screenly.data.repository.WatchStatus
import dam_a51568.screenly.data.repository.WatchlistItem
import dam_a51568.screenly.data.repository.WatchlistRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel do ecrã da Watchlist.
 *
 * Expõe a lista de itens filtrada por estado ([WatchStatus]) e gere
 * as operações de atualização e remoção de títulos da watchlist.
 *
 * Os dados são lidos diretamente do [WatchlistRepository] em memória.
 * Esta implementação será migrada para Firestore na Fase 9.
 */
class WatchlistViewModel : ViewModel() {
    /**
     * Estado atual do separador selecionado na UI.
     * Por defeito, o primeiro separador ("To Watch") está ativo.
     */
    private val _selectedTab = MutableStateFlow(WatchStatus.TO_WATCH)
    val selectedTab: StateFlow<WatchStatus> = _selectedTab.asStateFlow()

    /**
     * Altera o separador ativo e atualiza a lista apresentada.
     *
     * @param status Estado de visualização correspondente ao separador selecionado.
     */
    fun selectTab(status: WatchStatus) {
        _selectedTab.value = status
    }

    /**
     * Devolve os itens da watchlist filtrados pelo estado especificado.
     * É chamado pela UI sempre que o separador muda.
     *
     * @param status Estado de visualização pelo qual filtrar.
     * @return Lista de itens com o estado correspondente.
     */
    fun getItemsByStatus(status: WatchStatus): List<WatchlistItem> =
        WatchlistRepository.getByStatus(status)

    /**
     * Move um título para um estado diferente na watchlist.
     *
     * @param id Identificador único do título no TMDb.
     * @param mediaType Tipo de conteúdo: "movie" ou "tv".
     * @param newStatus Novo estado de visualização a atribuir.
     */
    fun moveToStatus(id: Int, mediaType: String, newStatus: WatchStatus) {
        WatchlistRepository.updateStatus(id, mediaType, newStatus)
    }

    /**
     * Remove um título da watchlist do utilizador.
     *
     * @param id Identificador único do título no TMDb.
     * @param mediaType Tipo de conteúdo: "movie" ou "tv".
     */
    fun removeItem(id: Int, mediaType: String) {
        WatchlistRepository.remove(id, mediaType)
    }

    /**
     * Guarda a classificação e a review pessoal de um título.
     * Apenas aplicável a títulos com estado [WatchStatus.WATCHED].
     *
     * @param id Identificador único do título no TMDb.
     * @param mediaType Tipo de conteúdo: "movie" ou "tv".
     * @param rating Classificação atribuída pelo utilizador (0.5 a 5.0).
     * @param review Nota pessoal escrita pelo utilizador.
     */
    fun updateRatingAndReview(id: Int, mediaType: String, rating: Float, review: String) {
        WatchlistRepository.updateRatingAndReview(id, mediaType, rating, review)
    }
}