package dam_a51568.screenly.data.model

/**
 * Modelo de domínio que representa um item da watchlist de um utilizador.
 *
 * Esta classe é utilizada para armazenar conteúdos (filmes ou séries)
 * que o utilizador pretende ver, está a ver ou já concluiu.
 *
 * Funciona como entidade principal da funcionalidade de watchlist,
 * sendo persistida na Firestore e sincronizada em tempo real com a UI.
 *
 * Inclui não só informação do conteúdo multimédia, mas também
 * dados relacionados com o estado de visualização e feedback do utilizador.
 *
 * @property id Identificador único do conteúdo (filme ou série) na API.
 * @property mediaType Tipo de conteúdo multimédia.
 * @property title Título do conteúdo guardado na watchlist.
 * @property posterPath Caminho relativo do póster do conteúdo
 * @property year Ano de lançamento ou estreia do conteúdo.
 * @property genres Géneros associados ao conteúdo.
 * @property status Estado atual de visualização do utilizador.
 * @property rating Avaliação atribuída pelo utilizador (pode ser nula se ainda não avaliado).
 * @property review Comentário ou review escrita pelo utilizador
 * @property addedAt Timestamp (em millis) que indica quando o item foi adicionado à watchlist.
 * @property uid Identificador do utilizador proprietário deste item
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
    val review: String? = null,
    val addedAt: Long = System.currentTimeMillis(), // Data em que foi adicionado
    val uid: String = "" // Necessário para associar corretamente os dados na Firestore
)