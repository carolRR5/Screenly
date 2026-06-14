package dam_a51568.screenly.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dam_a51568.screenly.data.model.User
import dam_a51568.screenly.data.model.WatchStatus
import dam_a51568.screenly.data.model.WatchlistItem
import dam_a51568.screenly.data.repository.UserRepository
import dam_a51568.screenly.data.repository.WatchlistRepository
import dam_a51568.screenly.data.repository.toUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Representa as estatísticas do utilizador calculadas a partir da watchlist.
 */
data class UserStats(
    val totalWatched: Int = 0,
    val totalHours: Int = 0,
    val averageRating: Float = 0f,
    val favoriteGenre: String? = null
)

/**
 * Estados possíveis do upload da foto de perfil.
 */
sealed class PhotoUploadState {
    data object Idle : PhotoUploadState()
    data object Loading : PhotoUploadState()
    data object Success : PhotoUploadState()
    data class Error(val message: String) : PhotoUploadState()
}

/**
 * ViewModel do ecrã de Perfil.
 *
 * Gere o estado do utilizador, as estatísticas de visualização e o upload da foto de perfil.
 */
class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid ?: ""

    // Dados do utilizador vindos da Firestore em tempo real.
    private val _user = MutableStateFlow<User?>(auth.currentUser?.toUser())
    val user: StateFlow<User?> = _user.asStateFlow()

    // Estado do upload da foto de perfil.
    private val _photoUploadState = MutableStateFlow<PhotoUploadState>(PhotoUploadState.Idle)
    val photoUploadState: StateFlow<PhotoUploadState> = _photoUploadState.asStateFlow()

    /**
     * Estatísticas calculadas a partir da watchlist em memória.
     * Como o WatchlistRepository.items é uma SnapshotStateList (mutableStateListOf),
     * podemos usá-lo diretamente no Compose ou converter para um Flow.
     */
    fun getStats(): UserStats {
        val watched = WatchlistRepository.getByStatus(WatchStatus.WATCHED)
        val totalWatched = watched.size
        val totalHours = (totalWatched * 100) / 60
        val ratings = watched.mapNotNull { it.rating }
        val averageRating = if (ratings.isNotEmpty()) ratings.average().toFloat() else 0f

        val favoriteGenre = watched
            .flatMap { it.genres.split(", ") }
            .filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key

        return UserStats(totalWatched, totalHours, averageRating, favoriteGenre)
    }

    /**
     * Devolve as 5 avaliações mais recentes.
     */
    fun getRecentRatings(): List<WatchlistItem> {
        return WatchlistRepository.getByStatus(WatchStatus.WATCHED)
            .filter { it.rating != null }
            .sortedByDescending { it.addedAt }
            .take(5)
    }

    init {
        if (userId.isNotEmpty()) {
            UserRepository.getUser(userId) { firestoreUser ->
                if (firestoreUser != null) {
                    val authUser = auth.currentUser?.toUser()
                    _user.value = firestoreUser.copy(
                        displayName = firestoreUser.displayName?.takeIf { it.isNotBlank() } ?: authUser?.displayName,
                        email = firestoreUser.email?.takeIf { it.isNotBlank() } ?: authUser?.email,
                        photoUrl = firestoreUser.photoUrl ?: authUser?.photoUrl
                    )
                }
            }
        }
    }

    /**
     * Data de criação da conta no Firebase Auth, formatada como string.
     */
    val memberSince: String
        get() {
            val timestamp = auth.currentUser?.metadata?.creationTimestamp ?: return "N/A"
            val date = java.util.Date(timestamp)
            val format = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale("pt", "PT"))
            return format.format(date)
        }

    /**
     * Processa e faz upload da foto de perfil selecionada pelo utilizador.
     */
    fun uploadProfilePhoto(imageBytes: ByteArray) {
        if (userId.isEmpty()) return

        viewModelScope.launch {
            _photoUploadState.value = PhotoUploadState.Loading

            try {
                // Comprime e codifica a imagem em background
                val base64 = withContext(Dispatchers.IO) {
                    UserRepository.compressAndEncodeImage(imageBytes)
                }

                // Guarda no Firestore
                UserRepository.updateProfilePhoto(
                    uid = userId,
                    photoBase64 = base64,
                    onSuccess = {
                        _photoUploadState.value = PhotoUploadState.Success
                    },
                    onError = { exception ->
                        _photoUploadState.value = PhotoUploadState.Error(
                            exception.message ?: "Erro ao guardar a foto"
                        )
                    }
                )
            } catch (e: Exception) {
                _photoUploadState.value = PhotoUploadState.Error(
                    e.message ?: "Erro ao processar a imagem"
                )
            }
        }
    }

    /**
     * Repõe o estado do upload para Idle.
     */
    fun resetPhotoUploadState() {
        _photoUploadState.value = PhotoUploadState.Idle
    }
}
