package dam_a51568.screenly.ui.profile

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dam_a51568.screenly.data.model.User
import dam_a51568.screenly.data.model.WatchStatus
import dam_a51568.screenly.data.model.WatchlistItem
import dam_a51568.screenly.data.repository.UserRepository
import dam_a51568.screenly.data.repository.WatchlistRepository
import dam_a51568.screenly.data.repository.toUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Representa as estatísticas do utilizador calculadas a partir da watchlist.
 *
 * @param totalWatched Total de títulos marcados como vistos.
 * @param totalHours Total de horas de conteúdo visto (calculado a partir da duração).
 * @param averageRating Média das classificações atribuídas pelo utilizador.
 * @param favoriteGenre Género mais frequente nos títulos vistos, ou null se não houver dados.
 */
data class UserStats(
    val totalWatched: Int,
    val totalHours: Int,
    val averageRating: Float,
    val favoriteGenre: String?
)

/**
 * ViewModel do ecrã de Perfil.
 *
 * Fornece os dados do utilizador autenticado (nome, email, foto, data de registo)
 * a partir do Firebase Auth, e calcula as estatísticas da watchlist em memória.
 */
class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    /** Dados do utilizador vindos da Firestore (em tempo real). */
    private val _user = MutableStateFlow<User?>(auth.currentUser?.toUser())
    val user: StateFlow<User?> = _user.asStateFlow()

    init {
        // Começa a observar os dados na Firestore assim que o ViewModel é criado
        val uid = auth.currentUser?.uid
        if (uid != null) {
            UserRepository.getUser(uid) { firestoreUser ->
                if (firestoreUser != null) {
                    _user.value = firestoreUser
                }
            }
        }
    }

    /**
     * Data de criação da conta no Firebase Auth, formatada como string.
     * Usada para mostrar "Membro desde..." no perfil.
     */
    val memberSince: String
        get() {
            val timestamp = auth.currentUser?.metadata?.creationTimestamp ?: return "N/A"
            val date = java.util.Date(timestamp)
            val format = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale("pt", "PT"))
            return format.format(date)
        }

    /**
     * Separador atualmente selecionado na secção das listas do perfil.
     * Por defeito mostra a lista "Watched".
     */
    private val _selectedTab = MutableStateFlow(WatchStatus.WATCHED)
    val selectedTab: StateFlow<WatchStatus> = _selectedTab.asStateFlow()

    /**
     * Altera o separador ativo na secção das listas.
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

    /**
     * Calcula as estatísticas do utilizador a partir dos títulos marcados como vistos.
     *
     * - Total de títulos vistos
     * - Total de horas (estimativa baseada em 100 min por título)
     * - Média das classificações atribuídas
     * - Género mais frequente nos títulos vistos
     *
     * @return Objecto [UserStats] com as estatísticas calculadas.
     */
    fun calculateStats(): UserStats {
        val watched = WatchlistRepository.getByStatus(WatchStatus.WATCHED)

        val totalWatched = watched.size

        // Estimativa de horas — será mais precisa após migração para Firestore
        // onde poderemos guardar a duração real de cada título
        val totalMinutes = totalWatched * 100
        val totalHours = totalMinutes / 60

        val ratings = watched.mapNotNull { it.rating }
        val averageRating = if (ratings.isNotEmpty()) ratings.average().toFloat() else 0f

        // Calcula o género mais frequente nos títulos vistos
        val favoriteGenre = watched
            .flatMap { it.genres.split(", ") }
            .filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key

        return UserStats(
            totalWatched = totalWatched,
            totalHours = totalHours,
            averageRating = averageRating,
            favoriteGenre = favoriteGenre
        )
    }

    /**
     * Devolve os últimos 5 títulos avaliados pelo utilizador,
     * ordenados do mais recente para o mais antigo.
     *
     * @return Lista de até 5 itens com classificação atribuída.
     */
    fun getRecentRatings(): List<WatchlistItem> =
        WatchlistRepository.getByStatus(WatchStatus.WATCHED)
            .filter { it.rating != null }
            .takeLast(5)
            .reversed()
}