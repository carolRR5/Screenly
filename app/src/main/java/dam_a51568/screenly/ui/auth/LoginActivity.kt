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
        super.onCreate(savedInstanceState) // Chama a implementação da superclasse, obrigatório no ciclo de vida de uma Activity

        // Inicialização do FirebaseAuth (camada de autenticação)
        auth = FirebaseAuth.getInstance()

        // Verifica se já existe uma sessão ativa antes de carregar o ecrã.
        // Se sim, navega diretamente para a MainActivity e termina esta Activity, para que
        // o utilizador não precise de fazer login novamente.
        if (auth.currentUser != null) {
            navigateToMain()  // Já há um utilizador autenticado, salta o ecrã de login
            return // Termina a execução de onCreate para não carregar o layout de login
        }

        // Carrega o layout XML via View Biding e define-o como o conteúdo desta Activity
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)  // Define a view raiz do binding como o conteúdo visual desta Activity

        setupClickListeners() // Configuração dos eventos de UI (cliques nos botões e links)
    }

    /**
     * Configura os listeners de clique para os elementos interativos do ecrã:
     * - Botão "Entrar": valida os campos e tenta autenticar o utilizador.
     * - Texto "Não tem conta?": navega para o ecrã de Registo.
     */
    private fun setupClickListeners() {
        // Botão de Login (Evento de autenticação)
        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim() // Lê o texto do campo de email, remove espaços em branco no início/fim
            val password = binding.editTextPassword.text.toString()  // Lê o texto do campo de palavra-passe (sem trim, pois espaços podem ser válidos)

            // Validação local antes de contactar Firebase (reduz chamadas desnecessárias)
            if (validateInputs(email, password)) {
                performLogin(email, password)  // Campos válidos, tenta efetuar o login
            }
        }

        // Link para o ecrã de Registo
        binding.textViewGoToRegister.setOnClickListener {
            // Abre a RegisterActivity através de um Intent explícito
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
        var isValid = true // Flag que acumula o resultado geral da validação

        // Validação de email
        if (email.isEmpty()) {
            // Campo de email vazio, define mensagem de erro no TextInputLayout
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

        // Devolve o resultado. True só se ambos os campos passarem a validação
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
        setLoading(true) // Ativa o estado de loading (mostra ProgressBar e desativa o botão)

        // Chama o Firebase para autenticar com email e palavra-passe (operação assíncrona)
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                // Login bem-sucedido, navega para MainActivity
                // Desativa o estado de loading
                setLoading(false)
                navigateToMain()
            }
            .addOnFailureListener { exception ->
                // Desativa o estado de loading mesmo em caso de erro
                setLoading(false)
                // Tratamento de erros mais específicos do Firebase
                val message = when (exception) {
                    is FirebaseAuthInvalidUserException ->
                        // Não existe nenhuma conta registada com o email introduzido
                        "Não existe nenhuma conta com este email"
                    is FirebaseAuthInvalidCredentialsException ->
                        // Email mal formatado ou palavra-passe incorreta
                        "Email ou palavra-passe incorretos"
                    else ->
                        // Qualquer outro erro (rede, servidor, etc.)
                        "Erro ao fazer login. Tente novamente."
                }
                // Mostra a mensagem de erro ao utilizador através de um Toast
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
        // Mostra a ProgressBar enquanto isLoading for true, esconde-a caso contrário
        binding.progressBarLogin.visibility = if (isLoading) View.VISIBLE else View.GONE
        // Desativa o botão de login durante o carregamento, para evitar submissões duplicadas
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
        // Cria o Intent para abrir a MainActivity
        val intent = Intent(this, MainActivity::class.java).apply {
            // Limpa a back stack para que o utilizador não volte ao Login ao carregar em "Trás"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        // Inicia a MainActivity com as flags definidas
        startActivity(intent)
        // Termina esta Activity (Login) para libertar memória e impedir voltar atrás
        finish()
    }
}