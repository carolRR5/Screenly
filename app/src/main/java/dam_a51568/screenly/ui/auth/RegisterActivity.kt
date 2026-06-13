package dam_a51568.screenly.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import dam_a51568.screenly.MainActivity
import dam_a51568.screenly.data.model.User
import dam_a51568.screenly.data.repository.UserRepository
import dam_a51568.screenly.databinding.ActivityRegisterBinding

/**
 * Activity responsável pelo ecrã de Registo da aplicação Screenly.
 *
 * Permite que novos utilizadores criem uma conta com nome, email e palavra-passe
 * através do Firebase Authentication. Após o registo com sucesso, guarda o nome
 * do utilizador no perfil do Firebase Auth e redireciona para a [MainActivity].
 *
 * O layout é definido em XML (activity_register.xml) e acedido via View Binding.
 */
class RegisterActivity : AppCompatActivity() {

    /** View Binding que dá acesso às views do layout activity_register.xml. */
    private lateinit var binding: ActivityRegisterBinding

    /** Instância do Firebase Authentication para criar a conta do novo utilizador. */
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    /**
     * Configura os listeners de clique para os elementos interativos do ecrã:
     * - Botão "Criar conta": valida os campos e tenta registar o utilizador.
     * - Texto "Já tem conta?": fecha este ecrã e volta ao Login.
     */
    private fun setupClickListeners() {
        binding.buttonRegister.setOnClickListener {
            val name = binding.editTextName.text.toString().trim()
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString()
            val confirmPassword = binding.editTextConfirmPassword.text.toString()

            if (validateInputs(name, email, password, confirmPassword)) {
                performRegister(name, email, password)
            }
        }

        // Usa finish() para voltar ao Login sem criar uma nova instância
        binding.textViewGoToLogin.setOnClickListener {
            finish()
        }
    }

    /**
     * Valida todos os campos do formulário de registo.
     *
     * Regras de validação:
     * - Nome não pode estar vazio
     * - Email não pode estar vazio e deve ter formato válido
     * - Palavra-passe não pode estar vazia e deve ter pelo menos 6 caracteres
     * - Confirmação deve ser idêntica à palavra-passe
     *
     * @param name Nome introduzido pelo utilizador.
     * @param email Email introduzido pelo utilizador.
     * @param password Palavra-passe introduzida pelo utilizador.
     * @param confirmPassword Confirmação da palavra-passe.
     * @return `true` se todos os campos são válidos, `false` caso contrário.
     */
    private fun validateInputs(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            binding.textInputName.error = "O nome não pode estar vazio"
            isValid = false
        } else {
            binding.textInputName.error = null
        }

        if (email.isEmpty()) {
            binding.textInputEmail.error = "O email não pode estar vazio"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.textInputEmail.error = "Introduza um email válido"
            isValid = false
        } else {
            binding.textInputEmail.error = null
        }

        if (password.isEmpty()) {
            binding.textInputPassword.error = "A palavra-passe não pode estar vazia"
            isValid = false
        } else if (password.length < 6) {
            binding.textInputPassword.error = "A palavra-passe deve ter pelo menos 6 caracteres"
            isValid = false
        } else {
            binding.textInputPassword.error = null
        }

        if (confirmPassword.isEmpty()) {
            binding.textInputConfirmPassword.error = "Confirme a sua palavra-passe"
            isValid = false
        } else if (password != confirmPassword) {
            binding.textInputConfirmPassword.error = "As palavras-passe não coincidem"
            isValid = false
        } else {
            binding.textInputConfirmPassword.error = null
        }

        return isValid
    }

    /**
     * Cria uma nova conta via Firebase Auth e guarda o nome do utilizador.
     *
     * O processo tem dois passos:
     * 1. Criar a conta com email e palavra-passe
     * 2. Atualizar o perfil do Firebase Auth com o nome introduzido
     *
     * Em caso de sucesso, navega para a [MainActivity].
     * Em caso de erro, apresenta uma mensagem específica ao utilizador.
     *
     * @param name Nome do utilizador a guardar no perfil do Firebase Auth.
     * @param email Email introduzido pelo utilizador.
     * @param password Palavra-passe introduzida pelo utilizador.
     */
    private fun performRegister(name: String, email: String, password: String) {
        setLoading(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user
                
                // 1. Atualizar o perfil no Firebase Auth (para o displayName ficar disponível localmente)
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()

                firebaseUser?.updateProfile(profileUpdates)
                    ?.addOnCompleteListener {
                        // 2. Guardar os dados na Firestore (mesmo que o updateProfile falhe)
                        val newUser = User(
                            uid = firebaseUser.uid,
                            email = email,
                            displayName = name
                        )

                        UserRepository.saveUser(newUser)
                            .addOnCompleteListener {
                                setLoading(false)
                                navigateToMain()
                            }
                    }
            }
            .addOnFailureListener { exception ->
                setLoading(false)
                val message = when (exception) {
                    is FirebaseAuthUserCollisionException ->
                        "Já existe uma conta com este email"
                    is FirebaseAuthWeakPasswordException ->
                        "A palavra-passe é demasiado fraca. Use pelo menos 6 caracteres"
                    else -> "Erro ao criar conta. Tente novamente."
                }
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
    }

    /**
     * Controla o estado de carregamento da UI durante o registo.
     *
     * @param isLoading `true` para ativar o estado de carregamento, `false` para desativar.
     */
    private fun setLoading(isLoading: Boolean) {
        binding.progressBarRegister.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonRegister.isEnabled = !isLoading
    }

    /**
     * Navega para a [MainActivity] após registo bem-sucedido.
     * Limpa a back stack para que o utilizador não volte ao Registo.
     */
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}