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
    val totalWatched: Int = 0,      // Número total de títulos marcados como "Watched"
    val totalHours: Int = 0,         // Estimativa de horas totais vistas
    val averageRating: Float = 0f,   // Média das avaliações dadas pelo utilizador
    val favoriteGenre: String? = null // Género mais frequente entre os títulos vistos
)

/**
 * Estados possíveis do upload da foto de perfil.
 */
sealed class PhotoUploadState {
    data object Idle : PhotoUploadState() // Nenhum upload em curso
    data object Loading : PhotoUploadState() // Upload em curso
    data object Success : PhotoUploadState() // Upload concluído com sucesso
    data class Error(val message: String) : PhotoUploadState() // Upload falhou, com mensagem de erro
}

/**
 * ViewModel do ecrã de Perfil.
 *
 * Gere o estado do utilizador, as estatísticas de visualização e o upload da foto de perfil.
 */
class ProfileViewModel : ViewModel() {
    // Instância singleton do Firebase Authentication
    private val auth = FirebaseAuth.getInstance()
    // UID do utilizador autenticado, ou string vazia se não houver sessão
    private val userId = auth.currentUser?.uid ?: ""

    // Dados do utilizador vindos da Firestore em tempo real.
    // Estado mutável (privado), inicializado com os dados básicos do Firebase Auth
    private val _user = MutableStateFlow<User?>(auth.currentUser?.toUser())
    // Versão pública e só de leitura, exposta para a UI observar
    val user: StateFlow<User?> = _user.asStateFlow()

    // Estado do upload da foto de perfil.
    // Estado mutável (privado) do progresso do upload
    private val _photoUploadState = MutableStateFlow<PhotoUploadState>(PhotoUploadState.Idle)
    // Versão pública e só de leitura, exposta para a UI observar
    val photoUploadState: StateFlow<PhotoUploadState> = _photoUploadState.asStateFlow()

    /**
     * Estatísticas calculadas a partir da watchlist em memória.
     * Como o WatchlistRepository.items é uma SnapshotStateList (mutableStateListOf),
     * podemos usá-lo diretamente no Compose ou converter para um Flow.
     */
    fun getStats(): UserStats {
        // Obtém apenas os itens marcados como "Watched"
        val watched = WatchlistRepository.getByStatus(WatchStatus.WATCHED)
        // Número total de títulos vistos
        val totalWatched = watched.size
        // Estima as horas totais assumindo 100 minutos por título, convertido para horas
        val totalHours = (totalWatched * 100) / 60
        // Recolhe apenas as avaliações que não são nulas
        val ratings = watched.mapNotNull { it.rating }
        // Calcula a média das avaliações, ou 0 se não houver nenhuma
        val averageRating = if (ratings.isNotEmpty()) ratings.average().toFloat() else 0f

        // Determina o género mais frequente entre os títulos vistos
        val favoriteGenre = watched
            // Divide a string de géneros de cada item em géneros individuais
            .flatMap { it.genres.split(", ") }
            // Remove entradas vazias
            .filter { it.isNotBlank() }
            // Agrupa por nome de género, contando ocorrências
            .groupingBy { it }
            .eachCount()
            // Obtém o género com a contagem mais alta
            .maxByOrNull { it.value }
            // Extrai apenas o nome do género (a chave do par)
            ?.key

        return UserStats(totalWatched, totalHours, averageRating, favoriteGenre)
    }

    /**
     * Devolve as 5 avaliações mais recentes.
     */
    fun getRecentRatings(): List<WatchlistItem> {
        return WatchlistRepository.getByStatus(WatchStatus.WATCHED)
            // Mantém apenas os itens que têm uma avaliação atribuída
            .filter { it.rating != null }
            // Ordena do mais recente para o mais antigo, com base na data em que foi adicionado
            .sortedByDescending { it.addedAt }
            // Limita aos 5 mais recentes
            .take(5)
    }

    // Bloco executado na criação do ViewModel
    init {
        // Só tenta carregar dados da Firestore se houver um utilizador autenticado
        if (userId.isNotEmpty()) {
            // Pede ao repositório os dados do utilizador na Firestore, com callback assíncrono
            UserRepository.getUser(userId) { firestoreUser ->
                if (firestoreUser != null) {
                    // Obtém também os dados básicos do Firebase Auth, para usar como fallback
                    val authUser = auth.currentUser?.toUser()
                    // Combina os dados da Firestore com os do Auth: usa o valor da Firestore
                    // se não estiver vazio, caso contrário usa o valor do Auth
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
            // Obtém o timestamp de criação da conta; se não existir, devolve "N/A"
            val timestamp = auth.currentUser?.metadata?.creationTimestamp ?: return "N/A"
            // Converte o timestamp (milissegundos) para um objeto Date
            val date = java.util.Date(timestamp)
            // Define o formato de data: nome do mês completo + ano, em português de Portugal
            val format = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale("pt", "PT"))
            // Formata a data para uma string legível, ex: "junho 2026"
            return format.format(date)
        }

    /**
     * Processa e faz upload da foto de perfil selecionada pelo utilizador.
     */
    fun uploadProfilePhoto(imageBytes: ByteArray) {
        // Sem utilizador autenticado, não há onde guardar a foto
        if (userId.isEmpty()) return

        // Lança uma coroutine associada ao ciclo de vida do ViewModel
        viewModelScope.launch {
            // Sinaliza à UI que o upload está em curso (mostra spinner sobre a foto)
            _photoUploadState.value = PhotoUploadState.Loading

            try {
                // Comprime e codifica a imagem em background
                // Executa o processamento da imagem numa thread de I/O, para não bloquear a UI
                val base64 = withContext(Dispatchers.IO) {
                    UserRepository.compressAndEncodeImage(imageBytes)
                }

                // Guarda no Firestore
                // Atualiza o campo da foto de perfil do utilizador na Firestore
                UserRepository.updateProfilePhoto(
                    uid = userId,
                    photoBase64 = base64,
                    onSuccess = {
                        // Upload concluído com sucesso
                        _photoUploadState.value = PhotoUploadState.Success
                    },
                    onError = { exception ->
                        // Falha ao guardar na Firestore, com mensagem de erro (ou genérica)
                        _photoUploadState.value = PhotoUploadState.Error(
                            exception.message ?: "Erro ao guardar a foto"
                        )
                    }
                )
            } catch (e: Exception) {
                // Falha durante a compressão/codificação da imagem
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
        // Volta ao estado de repouso, para a UI não voltar a mostrar a mesma snackbar
        _photoUploadState.value = PhotoUploadState.Idle
    }
}