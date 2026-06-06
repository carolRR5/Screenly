package dam_a51568.screenly.data.repository

import androidx.compose.runtime.mutableStateListOf

/**
 * Estado de um título na watchlist do utilizador.
 */
enum class WatchStatus {
    TO_WATCH,
    WATCHING,
    WATCHED
}

/**
 * Representa um item guardado na watchlist do utilizador em memória.
 * Inclui os dados essenciais do título e o estado actual de visualização.
 */
data class WatchlistItem(
    val id: Int,
    val mediaType: String,       // "movie" ou "tv"
    val title: String,
    val posterPath: String?,
    val year: String,
    val genres: String,
    val status: WatchStatus,
    val rating: Float? = null,   // Classificação de 0.5 a 5.0 (apenas para WATCHED)
    val review: String? = null   // Nota pessoal (apenas para WATCHED)
)

/**
 * Repositório singleton que gere as listas pessoais do utilizador em memória RAM.
 * Esta implementação será substituída pela integração com Firebase Firestore numa fase posterior.
 *
 * Usa [mutableStateListOf] para que o Compose observe automaticamente as alterações à lista.
 */
object WatchlistRepository {

    private val _items = mutableStateListOf<WatchlistItem>()

    // Lista observável de todos os itens na watchlist do utilizador.
    val items: List<WatchlistItem> get() = _items

    // Devolve os itens filtrados por estado.
    fun getByStatus(status: WatchStatus): List<WatchlistItem> =
        _items.filter { it.status == status }

    /**
     * Adiciona um título à watchlist com o estado especificado.
     * Se o título já existir (mesmo id + mediaType), atualiza o estado.
     */
    fun addOrUpdate(item: WatchlistItem) {
        val index = _items.indexOfFirst { it.id == item.id && it.mediaType == item.mediaType }
        if (index >= 0) {
            _items[index] = item
        } else {
            _items.add(item)
        }
    }

    /**
     * Altera o estado de um título já existente na watchlist.
     * Mantém o rating e a review caso o item esteja a ser movido para WATCHED.
     */
    fun updateStatus(id: Int, mediaType: String, newStatus: WatchStatus) {
        val index = _items.indexOfFirst { it.id == id && it.mediaType == mediaType }
        if (index >= 0) {
            _items[index] = _items[index].copy(status = newStatus)
        }
    }

    /**
     * Guarda a classificação e a review pessoal de um título.
     * Só deve ser chamado para títulos com estado WATCHED.
     */
    fun updateRatingAndReview(id: Int, mediaType: String, rating: Float, review: String) {
        val index = _items.indexOfFirst { it.id == id && it.mediaType == mediaType }
        if (index >= 0) {
            _items[index] = _items[index].copy(rating = rating, review = review)
        }
    }

    // Remove um título da watchlist.
    fun remove(id: Int, mediaType: String) {
        _items.removeAll { it.id == id && it.mediaType == mediaType }
    }

    // Verifica se um título já está na watchlist e devolve o seu estado ou null.
    fun getStatus(id: Int, mediaType: String): WatchStatus? =
        _items.firstOrNull { it.id == id && it.mediaType == mediaType }?.status
}
