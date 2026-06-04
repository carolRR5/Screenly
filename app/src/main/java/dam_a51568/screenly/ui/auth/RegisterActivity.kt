package dam_a51568.screenly.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import dam_a51568.screenly.MainActivity
import dam_a51568.screenly.databinding.ActivityRegisterBinding

/**
 * Activity responsável pelo ecrã de Registo da aplicação.
 *
 * Permite que novos utilizadores criem uma conta com email e palavra-passe através do Firebase
 * Authentication. Após o registo com sucesso, o utilizador é redirecionado para a MainActivity, entrando
 * diretamente na aplicação sem necessidade de fazer login separadamente.
 *
 * Esta Activity é iniciada a partir da LoginActivity quando o utilizador clica em "Não tem conta? Registe-se aqui".
 */
class RegisterActivity : AppCompatActivity() {
    // View Biding que dá acesso às views do layout activity_register.xml
    private lateinit var binding: ActivityRegisterBinding
    // Instância do Firebase Authentication para criar a conta do novo utilizador
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Carrega o layout XML via View Biding e define-o como o conteúdo desta Activity
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    /**
     * Configura os listeners de clique para os elementos interativos do ecrã:
     * - Botão "Criar conta": valida os campos e tenta registar o utilizador.
     * - Texto "Já tem Conta?": fecha este ecrã e volta ao Login através do finish, reutilizando
     * a instância da LoginActivity.
     */
    private fun setupClickListeners() {
        // Botão de Registo
        binding.buttonRegister.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString()
            val confirmPassword = binding.editTextConfirmPassword.text.toString()

            if (validateInputs(email, password, confirmPassword)) {
                performRegister(email, password)
            }
        }

        // Usa finish() em vez de startActivity() para evitar criar uma nova instância do Login
        binding.textViewGoToLogin.setOnClickListener {
            finish() // Simplesmente fecha o ecrã de Registo e volta ao Login
        }
    }

    /**
     * Valida todos os campos do formulário de registo antes de enviar o pedido ao Firebase.
     * Os erros são apresentados diretamente nos TextInputLayout correspondentes, por baixo de cada campo.
     *
     * Regras de validação:
     * - Email não pode estar vazio e deve ter um formato válido, ou seja, por exemplo, nome@dominio.com
     * - Palavra-passe não pode estar vazia e deve ter pelo menos 6 caracteres.
     * - Confirmação da palavra-passe deve ser idêntica à palavra-passe introduzida.
     *
     * @param email Texto introduzido no campo de email.
     * @param password Texto introduzido no campo de palavra-passe.
     * @param confirmPassword Texto introduzido no campo de confirmação de palavra-passe.
     * @return 'true' se todos os campos são válidos, 'false' caso contrário.
     */
    private fun validateInputs(email: String, password: String, confirmPassword: String): Boolean {
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
     * Cria um nova conta via Firebase Auth com email e palavra-passe.
     *
     * Durante o processo, desativa o botão e mostra um ProgressBar para indicar que a operação está
     * em curso e, também para evitar cliques duplicados.
     *
     * Em caso de sucesso, vai diretamente para a MainActivity sem necessidade de login adicional, pois
     * o Firebase Auth já cria e inicia a sessão automaticamente.
     *
     * Em caso de erro, apresenta uma mensagem específica ao utilizador consoante o tipo de exceção
     * devolvida pelo Firebase:
     * - FirebaseAuthUserCollisionException: já existe uma conta com este email.
     * - FirebaseAuthWeakPasswordException: palavra-passe demasiado fraca.
     * - Outros erros genéricos.
     *
     * @param email Email introduzido pelo utilizador.
     * @param password Palavra-passe introduzida pelo utilizador.
     */
    private fun performRegister(email: String, password: String) {
        setLoading(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                setLoading(false)
                navigateToMain()
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
     * Controla o estado de carregamento da Ui durante o registo.
     * Quando está a carregar, mostra a ProgressBar e desativa o botão para evitar que o utilizador
     * submeta o formulário várias vezes.
     *
     * @param isLoading 'true' para ativar o estado de carregamento, 'false' para desativar.
     */
    private fun setLoading(isLoading: Boolean) {
        binding.progressBarRegister.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonRegister.isEnabled = !isLoading
    }

    /**
     * Navega para a MainActivity após registo bem-sucedido.
     *
     * As flags Intent.FLAG_ACTIVITY_NEW_TASK e Intent.FLAG_ACTIVITY_CLEAR_TASK limpam corretamente a
     * back stack, garantindo que o utilizador não consegue voltar ao ecrã de Registo ou de Login ao
     * carregar no botão "Trás" do dispositivo.
     */
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
