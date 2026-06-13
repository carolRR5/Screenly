package dam_a51568.screenly.data.repository

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dam_a51568.screenly.data.model.WatchStatus
import dam_a51568.screenly.data.model.WatchlistItem

object WatchlistRepository {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    // Cache local para o Compose observar as alterações
    private val _items = mutableStateListOf<WatchlistItem>()
    val items: List<WatchlistItem> get() = _items

    /**
     * Obtém a coleção watchlist do utilizador atual no Firestore.
     */
    private fun watchlistCollection() = firestore
        .collection("users")
        .document(auth.currentUser?.uid ?: "")
        .collection("watchlist")

    /**
     * Inicia a observação em tempo real da watchlist do utilizador.
     * Deve ser chamado quando o utilizador faz login.
     */
    fun startListening() {
        watchlistCollection()
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _items.clear()
                    _items.addAll(
                        snapshot.documents.mapNotNull { doc ->
                            doc.toObject(WatchlistItem::class.java)
                        }
                    )
                }
            }
    }

    /**
     * Devolve os itens filtrados por estado.
     */
    fun getByStatus(status: WatchStatus): List<WatchlistItem> =
        _items.filter { it.status == status }

    /**
     * Adiciona ou atualiza um item na watchlist.
     */
    fun addOrUpdate(item: WatchlistItem) {
        val uid = auth.currentUser?.uid ?: return
        val docId = "${item.id}_${item.mediaType}"
        val itemWithUid = item.copy(uid = uid)

        watchlistCollection()
            .document(docId)
            .set(itemWithUid)
    }

    /**
     * Atualiza o estado de um item na watchlist.
     */
    fun updateStatus(id: Int, mediaType: String, newStatus: WatchStatus) {
        val docId = "${id}_${mediaType}"
        watchlistCollection()
            .document(docId)
            .update("status", newStatus.name)
    }

    /**
     * Guarda o rating e a review de um item.
     */
    fun updateRatingAndReview(id: Int, mediaType: String, rating: Float, review: String) {
        val docId = "${id}_${mediaType}"
        watchlistCollection()
            .document(docId)
            .update(mapOf("rating" to rating, "review" to review))
    }

    /**
     * Remove um item da watchlist.
     */
    fun remove(id: Int, mediaType: String) {
        val docId = "${id}_${mediaType}"
        watchlistCollection()
            .document(docId)
            .delete()
    }

    /**
     * Verifica se um item está na watchlist e devolve o seu estado.
     */
    fun getStatus(id: Int, mediaType: String): WatchStatus? =
        _items.firstOrNull { it.id == id && it.mediaType == mediaType }?.status
}