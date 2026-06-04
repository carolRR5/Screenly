package dam_a51568.screenly.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import dam_a51568.screenly.MainActivity
import dam_a51568.screenly.databinding.ActivityLoginBinding

/**
 * Activity responsável pelo ecrã de Login da aplicação.
 *
 * Esta Activity serve dois propósitos:
 *      1. Verificar, ao iniciar, se o utilizador já tem a sessão ativa no Firebase. Caso o utilizador já
 *         tenha o feito, redireciona-o imediatamente para a MainActivity, evitando que tenha que fazer login
 *         sempre que abra a app.
 *      2. Permitir que o utilizador autentique com email e palavra-passe via Firebase Authentication.
 */
class LoginActivity : AppCompatActivity() {
    // View Binding que dá acesso às views do layout activity_login.xml
    private lateinit var binding: ActivityLoginBinding
    // Instância do Firebase Authentication para gerir a sessão do utilizador
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Verifica se já existe uma sessão ativa antes de carregar o ecrã.
        // Se sim, navega diretamente para a MainActivity e termina esta Activity, para que
        // o utilizador não precise de fazer login novamente.
        if (auth.currentUser != null) {
            navigateToMain()
            return
        }

        // Carrega o layout XML via View Biding e define-o como o conteúdo desta Activity
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    /**
     * Configura os listeners de clique para os elementos interativos do ecrã:
     * - Botão "Entrar": valida os campos e tenta autenticar o utilizador.
     * - Texto "Não tem conta?": navega para o ecrã de Registo.
     */
    private fun setupClickListeners() {
        // Botão de Login
        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString()

            if (validateInputs(email, password)) {
                performLogin(email, password)
            }
        }

        // Link para o ecrã de Registo
        binding.textViewGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    /**
     * Valida os campos de email e palavra-passe antes de enviar o pedido ao Firebase.
     * Os erros são apresentados diretamente nos layouts correspondentes, por baixo de campo, para ser
     * mais claro.
     *
     * Regras de validação:
     * - Email não pode estar vazio e deve ter um formato válido, ou seja, por exemplo, nome@dominio.com
     * - Palavra-passe não pode estar vazia e deve ter pelo menos 6 caracteres.
     *
     * @param email Texto introduzido no campo de email.
     * @param password Texto introduzido no campo de palavra-passe.
     * @return 'true' se ambos os campos são válidos, 'false' caso contrário.
     */
    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.textInputEmail.error = "O email não pode estar vazio"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.textInputEmail.error = "Introduza um email válido"
            isValid = false
        } else {
            // Limpa o erro caso o campo tenha sido corrigido pelo utilizador
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

        return isValid
    }

    /**
     * Tenta autenticar o utilizador via Firebase Authentication com email e palavra-passe.
     *
     * Durante o processo, desativa o botão e mostra uma ProgressBar para indicar que a operação está
     * em curso e evitar cliques duplicados.
     *
     * Em caso de sucesso, navega para a MainActivity.
     * Em caso de erro, apresenta uma mensagem específica ao utilizador consoante o tipo de exceção
     * devolvida pelo Firebase:
     * - FirebaseAuthInvalidUserException: não exite conta com este email.
     * - FirebaseAuthInvalidCredentialsException: credenciais incorretas.
     * - Outras mensagens de erro geral.
     */
    private fun performLogin(email: String, password: String) {
        setLoading(true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                setLoading(false)
                navigateToMain()
            }
            .addOnFailureListener { exception ->
                setLoading(false)
                val message = when (exception) {
                    is FirebaseAuthInvalidUserException ->
                        "Não existe nenhuma conta com este email"
                    is FirebaseAuthInvalidCredentialsException ->
                        "Email ou palavra-passe incorrectos"
                    else -> "Erro ao fazer login. Tente novamente."
                }
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
    }

    /**
     * Controla o estado de carregamento da UI durante a autenticação.
     * Quando está a carregar, mostra a ProgressBar e desativa o botão para evitar que o utilizador
     * submeta várias vezes.
     *
     * @param isLoading 'true' para ativar o estado de carregamento, 'false' para desativar.
     */
    private fun setLoading(isLoading: Boolean) {
        binding.progressBarLogin.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonLogin.isEnabled = !isLoading
    }

    /**
     * Navega para a MainActivity após autenticação bem-sucedida.
     *
     * As flags Intent.FLAG_ACTIVITY_NEW_TASK e Intent.FLAG_ACTIVITY_CLEAR_TASK limpam completamente
     * a back stack, garantindo que o utilizador não consegue voltar ao ecrã de Login
     * ao carregar no botão "Trás" do dispositivo.
     */
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            // Limpa a back stack para que o utilizador não volte ao Login ao carregar em "Trás"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}