package dam_a51568.screenly.data.model

/**
 * Representa um item guardado na watchlist do utilizador em memória.
 * Inclui os dados essenciais do título e o estado atual de visualização.
 */
data class WatchlistItem(
    val id: Int = 0,
    val mediaType: String = "", // "movie" ou "tv"
    val title: String = "",
    val posterPath: String? = null,
    val year: String = "",
    val genres: String = "",
    val status: WatchStatus = WatchStatus.TO_WATCH,
    val rating: Float? = null,
    val review: String? = null
)