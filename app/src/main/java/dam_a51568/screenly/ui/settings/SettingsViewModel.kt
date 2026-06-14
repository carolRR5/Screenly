package dam_a51568.screenly.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import dam_a51568.screenly.data.model.User
import dam_a51568.screenly.data.repository.UserRepository
import dam_a51568.screenly.data.repository.toUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 *  Estados possíveis das operações nas definições.
 */
sealed class SettingsOperationState {
    // Nenhuma operação em curso. Estado inicial e estado de "repouso" após sucesso/erro
    data object Idle : SettingsOperationState()
    // Operação em curso (ex: a aguardar resposta do Firebase)
    data object Loading : SettingsOperationState()
    // Operação concluída com sucesso, com mensagem a apresentar ao utilizador
    data class Success(val message: String) : SettingsOperationState()
    // Operação falhou com erro, com mensagem a apresentar ao utilizador
    data class Error(val message: String) : SettingsOperationState()
}

/**
 * ViewModel do ecrã de Definições.
 *
 * Gere as operações de alteração de nome, email e palavra-passe
 * através do Firebase Authentication, e o logout do utilizador.
 */
class SettingsViewModel : ViewModel() {
    // Instância singleton do Firebase Authentication
    private val auth = FirebaseAuth.getInstance()

    // Estado mutável (privado) da operação atual, só pode ser alterado dentro do ViewModel
    private val _operationState = MutableStateFlow<SettingsOperationState>(SettingsOperationState.Idle)
    // Versão pública e só de leitura do estado, exposta para a UI observar
    val operationState: StateFlow<SettingsOperationState> = _operationState.asStateFlow()

    // Dados do utilizador autenticado.
    // Propriedade calculada: converte o FirebaseUser atual para o modelo User da app,
    // ou null se não houver utilizador autenticado
    private val user: User?
        get() = auth.currentUser?.toUser()

    // Nome atual do utilizador autenticado.
    // Devolve o displayName guardado no Firebase, ou string vazia se não existir
    val currentName: String
        get() = user?.displayName ?: ""

    // Email atual do utilizador autenticado.
    // Devolve o email guardado no Firebase, ou string vazia se não existir
    val currentEmail: String
        get() = user?.email ?: ""

    // Versão da aplicação
    // Valor fixo, apresentado na secção "Sobre" das definições
    val appVersion = "1.0.0"

    /**
     * Atualiza o nome do utilizador no Firebase Auth.
     *
     * @param newName Novo nome a guardar no perfil.
     */
    fun updateName(newName: String) {
        // Validação local: o nome não pode ficar vazio (ou só espaços)
        if (newName.isBlank()) {
            // Define o estado de erro e termina a função sem chamar o Firebase
            _operationState.value = SettingsOperationState.Error("O nome não pode estar vazio")
            return
        }

        // Lança uma coroutine associada ao ciclo de vida do ViewModel
        viewModelScope.launch {
            // Sinaliza à UI que a operação está em curso (ex: mostrar spinner)
            _operationState.value = SettingsOperationState.Loading

            // Obtém o utilizador atual; se for null (sem sessão), termina a coroutine
            val firebaseUser = auth.currentUser ?: return@launch
            // Constrói o objeto de atualização de perfil com o novo nome
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()

            // 1. Atualizar no Auth
            // Pede ao Firebase Auth para atualizar o displayName do utilizador
            firebaseUser.updateProfile(profileUpdates)
                .addOnSuccessListener {
                    // 2. Atualizar na Firestore
                    // Reconstrói o objeto User a partir do firebaseUser (já com o novo nome)
                    val updatedUser = firebaseUser.toUser() // Já terá o novo displayName
                    // Guarda os dados atualizados na Firestore, para manter consistência
                    UserRepository.saveUser(updatedUser)
                        .addOnSuccessListener {
                            // Ambas as operações (Auth + Firestore) tiveram sucesso
                            _operationState.value = SettingsOperationState.Success("Nome atualizado com sucesso")
                        }
                        .addOnFailureListener {
                            // O Auth foi atualizado, mas a Firestore falhou a sincronizar
                            _operationState.value = SettingsOperationState.Error("Erro ao sincronizar com a base de dados")
                        }
                }
                .addOnFailureListener {
                    // A atualização do perfil no Firebase Auth falhou
                    _operationState.value = SettingsOperationState.Error("Erro ao atualizar o perfil")
                }
        }
    }

    /**
     * Atualiza a palavra-passe do utilizador no Firebase Auth.
     * Requer reautenticação com a palavra-passe atual por segurança.
     *
     * @param currentPassword Palavra-passe atual para voltar a autenticar.
     * @param newPassword Nova palavra-passe a definir.
     */
    fun updatePassword(currentPassword: String, newPassword: String) {
        // Validação local: a nova palavra-passe tem de cumprir o tamanho mínimo do Firebase
        if (newPassword.length < 6) {
            _operationState.value = SettingsOperationState.Error(
                "A nova palavra-passe deve ter pelo menos 6 caracteres"
            )
            // Termina a função sem chamar o Firebase
            return
        }

        // Lança uma coroutine associada ao ciclo de vida do ViewModel
        viewModelScope.launch {
            // Sinaliza à UI que a operação está em curso (ex: mostrar spinner)
            _operationState.value = SettingsOperationState.Loading

            // Obtém o utilizador atual; se for null (sem sessão), termina a coroutine
            val user = auth.currentUser ?: return@launch
            // Cria a credencial (email + palavra-passe atual) necessária para reautenticar
            val credential = EmailAuthProvider.getCredential(user.email ?: "", currentPassword)

            // Reautentica o utilizador antes de alterar a palavra-passe
            // (o Firebase exige reautenticação recente para operações sensíveis)
            user.reauthenticate(credential)
                .addOnSuccessListener {
                    // Reautenticação OK, agora pode atualizar a palavra-passe
                    user.updatePassword(newPassword)
                        .addOnSuccessListener {
                            // Palavra-passe atualizada com sucesso
                            _operationState.value = SettingsOperationState.Success(
                                "Palavra-passe atualizada com sucesso"
                            )
                        }
                        .addOnFailureListener {
                            // A atualização da palavra-passe falhou (ex: erro de rede)
                            _operationState.value = SettingsOperationState.Error(
                                "Erro ao atualizar a palavra-passe"
                            )
                        }
                }
                .addOnFailureListener {
                    // A reautenticação falhou, normalmente porque a palavra-passe
                    // atual introduzida está incorreta
                    _operationState.value = SettingsOperationState.Error("Palavra-passe actual incorreta")
                }
        }
    }

    /**
     * Termina a sessão do utilizador no Firebase Auth.
     * Após o logout, o utilizador deve ser redirecionado para o ecrã de Login.
     */
    fun logout() {
        auth.signOut()  // Operação síncrona e local: limpa a sessão atual do Firebase Auth
    }

    /**
     * Repõe o estado da operação para Idle.
     * Deve ser chamado após o utilizador dispensar uma mensagem de sucesso ou erro.
     */
    fun resetOperationState() {
        // Volta ao estado de repouso, para a UI não voltar a mostrar a mesma snackbar
        _operationState.value = SettingsOperationState.Idle
    }
}