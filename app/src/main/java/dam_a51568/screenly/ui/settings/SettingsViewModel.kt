package dam_a51568.screenly.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 *  Estados possíveis das operações nas definições.
 */
sealed class SettingsOperationState {
    // Nenhuma operação em curso.
    data object Idle : SettingsOperationState()
    // Operação em curso.
    data object Loading : SettingsOperationState()
    // Operação concluída com sucesso.
    data class Success(val message: String) : SettingsOperationState()
    // Operação falhou com erro.
    data class Error(val message: String) : SettingsOperationState()
}

/**
 * ViewModel do ecrã de Definições.
 *
 * Gere as operações de alteração de nome, email e palavra-passe
 * através do Firebase Authentication, e o logout do utilizador.
 */
class SettingsViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _operationState = MutableStateFlow<SettingsOperationState>(SettingsOperationState.Idle)
    val operationState: StateFlow<SettingsOperationState> = _operationState.asStateFlow()

    // Nome actual do utilizador autenticado.
    val currentName: String
        get() = auth.currentUser?.displayName ?: ""

    // Email atual do utilizador autenticado.
    val currentEmail: String
        get() = auth.currentUser?.email ?: ""

    // Versão da aplicação
    val appVersion = "1.0.0"

    /**
     * Atualiza o nome do utilizador no Firebase Auth.
     *
     * @param newName Novo nome a guardar no perfil.
     */
    fun updateName(newName: String) {
        if (newName.isBlank()) {
            _operationState.value = SettingsOperationState.Error("O nome não pode estar vazio")
            return
        }

        viewModelScope.launch {
            _operationState.value = SettingsOperationState.Loading
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()

            auth.currentUser?.updateProfile(profileUpdates)
                ?.addOnSuccessListener {
                    _operationState.value = SettingsOperationState.Success("Nome atualizado com sucesso")
                }
                ?.addOnFailureListener {
                    _operationState.value = SettingsOperationState.Error("Erro ao atualizar o nome")
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
        if (newPassword.length < 6) {
            _operationState.value = SettingsOperationState.Error(
                "A nova palavra-passe deve ter pelo menos 6 caracteres"
            )
            return
        }

        viewModelScope.launch {
            _operationState.value = SettingsOperationState.Loading

            val user = auth.currentUser ?: return@launch
            val credential = EmailAuthProvider.getCredential(user.email ?: "", currentPassword)

            // Reautentica o utilizador antes de alterar a palavra-passe
            user.reauthenticate(credential)
                .addOnSuccessListener {
                    user.updatePassword(newPassword)
                        .addOnSuccessListener {
                            _operationState.value = SettingsOperationState.Success(
                                "Palavra-passe atualizada com sucesso"
                            )
                        }
                        .addOnFailureListener {
                            _operationState.value = SettingsOperationState.Error(
                                "Erro ao atualizar a palavra-passe"
                            )
                        }
                }
                .addOnFailureListener {
                    _operationState.value = SettingsOperationState.Error("Palavra-passe actual incorreta")
                }
        }
    }

    /**
     * Termina a sessão do utilizador no Firebase Auth.
     * Após o logout, o utilizador deve ser redirecionado para o ecrã de Login.
     */
    fun logout() {
        auth.signOut()
    }

    /**
     * Repõe o estado da operação para Idle.
     * Deve ser chamado após o utilizador dispensar uma mensagem de sucesso ou erro.
     */
    fun resetOperationState() {
        _operationState.value = SettingsOperationState.Idle
    }
}