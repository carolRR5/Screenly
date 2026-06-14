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
    // View Binding que dá acesso às views do layout activity_register.xml.
    private lateinit var binding: ActivityRegisterBinding

    // Instância do Firebase Authentication para criar a conta do novo utilizador.
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        // Chama a implementação da superclasse, obrigatório no ciclo de vida de uma Activity
        super.onCreate(savedInstanceState)

        // Obtém a instância singleton do FirebaseAuth (camada de autenticação)
        auth = FirebaseAuth.getInstance()

        // Faz o "inflate" do layout XML via View Binding, criando o objeto binding
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        // Define a view raiz do binding como o conteúdo visual desta Activity
        setContentView(binding.root)

        // Configuração dos eventos de UI (cliques nos botões e links)
        setupClickListeners()
    }

    /**
     * Configura os listeners de clique para os elementos interativos do ecrã:
     * - Botão "Criar conta": valida os campos e tenta registar o utilizador.
     * - Texto "Já tem conta?": fecha este ecrã e volta ao Login.
     */
    private fun setupClickListeners() {
        // Botão de Registo (Evento de criação de conta)
        binding.buttonRegister.setOnClickListener {
            // Lê o texto do campo de nome, remove espaços em branco no início/fim
            val name = binding.editTextName.text.toString().trim()
            // Lê o texto do campo de email, remove espaços em branco no início/fim
            val email = binding.editTextEmail.text.toString().trim()
            // Lê o texto do campo de palavra-passe (sem trim, pois espaços podem ser válidos)
            val password = binding.editTextPassword.text.toString()
            // Lê o texto do campo de confirmação de palavra-passe
            val confirmPassword = binding.editTextConfirmPassword.text.toString()

            // Validação local antes de contactar Firebase (reduz chamadas desnecessárias)
            if (validateInputs(name, email, password, confirmPassword)) {
                // Todos os campos válidos, tenta efetuar o registo
                performRegister(name, email, password)
            }
        }

        // Usa finish() para voltar ao Login sem criar uma nova instância
        binding.textViewGoToLogin.setOnClickListener {
            // Termina esta Activity, devolvendo o controlo à LoginActivity que está por baixo na stack
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
        // Flag que acumula o resultado geral da validação
        var isValid = true

        // Validação do nome
        if (name.isEmpty()) {
            // Campo de nome vazio, define mensagem de erro no TextInputLayout
            binding.textInputName.error = "O nome não pode estar vazio"
            isValid = false
        } else {
            // Limpa o erro caso o campo tenha sido corrigido pelo utilizador
            binding.textInputName.error = null
        }

        // Validação de email
        if (email.isEmpty()) {
            // Campo de email vazio
            binding.textInputEmail.error = "O email não pode estar vazio"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // Email não corresponde ao padrão regex de um endereço de email válido
            binding.textInputEmail.error = "Introduza um email válido"
            isValid = false
        } else {
            // Limpa o erro caso o campo tenha sido corrigido pelo utilizador
            binding.textInputEmail.error = null
        }

        // Validação de password
        if (password.isEmpty()) {
            // Campo de palavra-passe vazio
            binding.textInputPassword.error = "A palavra-passe não pode estar vazia"
            isValid = false
        } else if (password.length < 6) {
            // Palavra-passe abaixo do tamanho mínimo exigido pelo Firebase (6 caracteres)
            binding.textInputPassword.error = "A palavra-passe deve ter pelo menos 6 caracteres"
            isValid = false
        } else {
            // Limpa o erro caso o campo tenha sido corrigido pelo utilizador
            binding.textInputPassword.error = null
        }

        // Validação da confirmação de password
        if (confirmPassword.isEmpty()) {
            // Campo de confirmação vazio
            binding.textInputConfirmPassword.error = "Confirme a sua palavra-passe"
            isValid = false
        } else if (password != confirmPassword) {
            // As duas palavras-passe introduzidas não coincidem
            binding.textInputConfirmPassword.error = "As palavras-passe não coincidem"
            isValid = false
        } else {
            // Limpa o erro caso o campo tenha sido corrigido pelo utilizador
            binding.textInputConfirmPassword.error = null
        }

        // Devolve o resultado final: true só se todos os campos passarem a validação
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
        // Ativa o estado de loading (mostra ProgressBar e desativa o botão)
        setLoading(true)

        // Chama o Firebase para criar uma nova conta com email e palavra-passe (operação assíncrona)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                // Obtém o utilizador Firebase recém-criado a partir do resultado
                val firebaseUser = authResult.user

                // 1. Atualizar o perfil no Firebase Auth (para o displayName ficar disponível localmente)
                val profileUpdates = UserProfileChangeRequest.Builder()
                    // Define o nome introduzido pelo utilizador como displayName
                    .setDisplayName(name)
                    .build()

                // Atualiza o perfil do utilizador Firebase com o nome definido acima
                firebaseUser?.updateProfile(profileUpdates)
                    ?.addOnCompleteListener {
                        // 2. Guardar os dados na Firestore (mesmo que o updateProfile falhe)
                        // Cria o objeto User com os dados a persistir na Firestore
                        val newUser = User(
                            uid = firebaseUser.uid,
                            email = email,
                            displayName = name
                        )

                        // Guarda o novo utilizador no repositório (Firestore)
                        UserRepository.saveUser(newUser)
                            .addOnCompleteListener {
                                // Desativa o estado de loading
                                setLoading(false)
                                // Redireciona o utilizador para o ecrã principal
                                navigateToMain()
                            }
                    }
            }
            .addOnFailureListener { exception ->
                // Desativa o estado de loading mesmo em caso de erro
                setLoading(false)
                // Tratamento de erros mais específicos do Firebase
                val message = when (exception) {
                    is FirebaseAuthUserCollisionException ->
                        // Já existe uma conta registada com este email
                        "Já existe uma conta com este email"
                    is FirebaseAuthWeakPasswordException ->
                        // A palavra-passe não cumpre os requisitos mínimos de segurança do Firebase
                        "A palavra-passe é demasiado fraca. Use pelo menos 6 caracteres"
                    else ->
                        // Qualquer outro erro (rede, servidor, etc.)
                        "Erro ao criar conta. Tente novamente."
                }
                // Mostra a mensagem de erro ao utilizador através de um Toast
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
    }

    /**
     * Controla o estado de carregamento da UI durante o registo.
     *
     * @param isLoading `true` para ativar o estado de carregamento, `false` para desativar.
     */
    private fun setLoading(isLoading: Boolean) {
        // Mostra a ProgressBar enquanto isLoading for true, esconde-a caso contrário
        binding.progressBarRegister.visibility = if (isLoading) View.VISIBLE else View.GONE
        // Desativa o botão de registo durante o carregamento, para evitar submissões duplicadas
        binding.buttonRegister.isEnabled = !isLoading
    }

    /**
     * Navega para a [MainActivity] após registo bem-sucedido.
     * Limpa a back stack para que o utilizador não volte ao Registo.
     */
    private fun navigateToMain() {
        // Cria o Intent para abrir a MainActivity
        val intent = Intent(this, MainActivity::class.java).apply {
            // Limpa a back stack para que o utilizador não volte ao Registo ao carregar em "Trás"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        // Inicia a MainActivity com as flags definidas
        startActivity(intent)
        // Termina esta Activity (Registo) para libertar memória e impedir voltar atrás
        finish()
    }
}