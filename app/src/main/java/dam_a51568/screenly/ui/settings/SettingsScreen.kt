package dam_a51568.screenly.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dam_a51568.screenly.ui.theme.BackgroundDark
import dam_a51568.screenly.ui.theme.BrandPurple
import dam_a51568.screenly.ui.theme.CardBackground
import dam_a51568.screenly.ui.theme.ErrorRed
import dam_a51568.screenly.ui.theme.TextPrimary
import dam_a51568.screenly.ui.theme.TextSecondary

/**
 * Ecrã de Definições da aplicação Screenly.
 *
 * Permite ao utilizador alterar o nome, email e palavra-passe da conta,
 * consultar a versão da aplicação e terminar a sessão.
 *
 * @param onBack Callback chamado ao clicar no botão de retroceder.
 * @param onLogout Callback chamado após o logout, para navegar para o Login.
 * @param viewModel ViewModel que gere as operações de conta.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    // Observa o estado da operação atual (idle, loading, sucesso ou erro) de forma reativa
    val operationState by viewModel.operationState.collectAsState()

    /**
     * Cada diálogo tem uma flag booleana própria que controla se está visível ou não.
     *
     * 1. O utilizador clica num SettingsItem -> a flag correspondente passa a "true"
     * 2. O Compose recompõe o ecrã -> o bloco "if (showXxxDialog) { ... }" no fundo
     * desta função passa a renderizar o diálogo
     * 3. Dentro do diálogo, o utilizador escolhe:
     *     - "Cancelar" (onDismiss) -> apenas fecha o diálogo (flag volta a "false")
     *     - "Guardar"/"Confirmar" (onConfirm) -> chama a operação no ViewModel
     *     (ex: viewModel.updateName(...)) E também fecha o diálogo
     * 4. O diálogo fecha-se de imediato, mesmo que a operação no Firebase ainda esteja
     * a correr em segundo plano. Por isso existe o sistema de snackbar abaixo,
     * que avisa o utilizador do resultado (sucesso/erro) depois do diálogo já ter
     * desaparecido do ecrã.
     */

    // Controla a visibilidade do diálogo de alteração de nome
    var showNameDialog by remember { mutableStateOf(false) }
    // Controla a visibilidade do diálogo de alteração de palavra-passe
    var showPasswordDialog by remember { mutableStateOf(false) }
    // Controla a visibilidade do diálogo de confirmação de logout
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Mostra snackbar com o resultado da operação
    // Estado que controla a exibição de snackbars na UI
    val snackbarHostState = remember { SnackbarHostState() }
    // Efeito que reage a mudanças no estado da operação (operationState é um StateFlow
    // no ViewModel que evolui: Idle -> Loading -> Success/Error).
    // Como o diálogo já fechou quando a operação termina, é esta snackbar que informa
    // o utilizador do resultado final (ex: "Nome atualizado com sucesso" ou "Erro: ...").
    LaunchedEffect(operationState) {
        when (val state = operationState) {
            is SettingsOperationState.Success -> {
                // Mostra a mensagem de sucesso na snackbar
                snackbarHostState.showSnackbar(state.message)
                // Repõe o estado para Idle, para a snackbar não reaparecer
                // se o ecrã recompuser por outro motivo
                viewModel.resetOperationState()
            }
            is SettingsOperationState.Error -> {
                // Mostra a mensagem de erro na snackbar
                snackbarHostState.showSnackbar(state.message)
                // Repõe o estado para Idle, para a snackbar não reaparecer
                // se o ecrã recompuser por outro motivo
                viewModel.resetOperationState()
            }
            // Para os restantes estados (Idle, Loading) não é necessário fazer nada aqui
            else -> {}
        }
    }

    // Estrutura base do ecrã, com fundo escuro e suporte para snackbars
    Scaffold(
        containerColor = BackgroundDark,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        // Coluna principal, com scroll vertical, que ocupa todo o espaço disponível
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Barra de topo
            // Renderiza a barra de topo com o botão de retroceder
            TopBar(onBack = onBack)

            // Espaço entre a barra de topo e a primeira secção
            Spacer(modifier = Modifier.height(8.dp))

            // Secção Conta
            // Título da secção "Conta"
            SettingsSectionTitle(title = "Conta")

            // Item que permite alterar o nome, mostra o nome atual como subtítulo.
            // Ao clicar, liga a flag showNameDialog -> o Compose recompõe e o bloco
            // "if (showNameDialog)" no fundo desta função passa a mostrar o diálogo
            SettingsItem(
                title = "Alterar nome",
                subtitle = viewModel.currentName,
                onClick = { showNameDialog = true }
            )
            // Linha divisória entre os itens da secção Conta
            SettingsDivider()
            // Item que permite alterar a palavra-passe, subtítulo mostra pontos para ocultar o valor.
            // Ao clicar, liga a flag showPasswordDialog, fazendo aparecer o respetivo diálogo
            SettingsItem(
                title = "Alterar palavra-passe",
                subtitle = "••••••••",
                onClick = { showPasswordDialog = true }
            )

            // Espaço entre a secção Conta e a secção Sobre
            Spacer(modifier = Modifier.height(24.dp))

            // Secção Sobre
            // Título da secção "Sobre"
            SettingsSectionTitle(title = "Sobre")

            // Item informativo com a versão da app, sem ação ao clicar (lambda vazia)
            SettingsItem(
                title = "Versão da aplicação",
                subtitle = viewModel.appVersion,
                onClick = {}
            )

            // Espaço grande que empurra o botão de logout para baixo, simulando "fim da página"
            Spacer(modifier = Modifier.height(650.dp))


            // Box centralizada horizontalmente para conter o botão de logout
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Botão "Terminar sessão" - ao clicar, liga showLogoutDialog, fazendo
                // aparecer o diálogo de confirmação de logout no fundo desta função
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .padding(24.dp)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    // Texto do botão de logout
                    Text(
                        text = "Terminar sessão",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Mostra o diálogo de alteração de nome, se ativo.
    // Este bloco só é executado quando showNameDialog == true (ver explicação geral acima)
    if (showNameDialog) {
        ChangeNameDialog(
            currentName = viewModel.currentName,
            // Indica ao diálogo se há uma operação em curso (para mostrar spinner no botão
            // "Guardar" e desativá-lo, evitando duplo clique)
            isLoading = operationState is SettingsOperationState.Loading,
            onConfirm = { newName ->
                // Confirmar: dispara a operação assíncrona no ViewModel (vai atualizar
                // o nome no Firebase Auth e/ou Firestore)
                viewModel.updateName(newName)
                // O diálogo fecha-se já aqui, mesmo que a operação acima ainda não
                // tenha terminado. O resultado (sucesso/erro) será mostrado mais
                // tarde através da snackbar (ver LaunchedEffect acima)
                showNameDialog = false
            },
            // Cancelar: apenas fecha o diálogo, sem chamar o ViewModel
            onDismiss = { showNameDialog = false }
        )
    }

    // Mostra o diálogo de alteração de palavra-passe, se ativo.
    // Mesmo padrão do diálogo de nome: visível enquanto showPasswordDialog == true
    if (showPasswordDialog) {
        ChangePasswordDialog(
            // Indica ao diálogo se há uma operação em curso (para mostrar spinner no botão
            // "Guardar" e desativá-lo, evitando duplo clique)
            isLoading = operationState is SettingsOperationState.Loading,
            onConfirm = { currentPassword, newPassword ->
                // Confirmar: dispara a operação assíncrona no ViewModel. A palavra-passe
                // atual é necessária para o Firebase reautenticar o utilizador antes
                // de aceitar a alteração
                viewModel.updatePassword(currentPassword, newPassword)
                // Fecha o diálogo de imediato; o resultado (sucesso/erro) chega depois
                // através da snackbar (ver LaunchedEffect acima)
                showPasswordDialog = false
            },
            // Cancelar: apenas fecha o diálogo, sem chamar o ViewModel
            onDismiss = { showPasswordDialog = false }
        )
    }

    // Mostra o diálogo de confirmação de logout, se ativo.
    // Mais simples que os anteriores: não usa isLoading nem operationState, porque
    // o logout é seguido de imediato por navegação para o ecrã de Login (não há
    // necessidade de snackbar, já que o próprio ecrã muda)
    if (showLogoutDialog) {
        LogoutConfirmDialog(
            onConfirm = {
                // Termina a sessão do utilizador no Firebase
                viewModel.logout()
                // Notifica o ecrã pai (ScreenlyApp/NavHost) para navegar para o Login
                onLogout()
            },
            // Cancelar: apenas fecha o diálogo, sem fazer logout
            onDismiss = { showLogoutDialog = false }
        )
    }
}

/**
 * Barra de topo com botão de retroceder e título.
 */
@Composable
private fun TopBar(onBack: () -> Unit) {
    // Linha horizontal que alinha o ícone de retroceder e o título verticalmente ao centro
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botão com ícone de seta para trás, que dispara o callback onBack
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Retroceder",
                tint = TextPrimary
            )
        }
        // Título do ecrã
        Text(
            text = "Definições",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Título de uma secção das definições.
 *
 * @param title Texto do título da secção.
 */
@Composable
private fun SettingsSectionTitle(title: String) {
    // Texto pequeno, em destaque com a cor da marca, usado como cabeçalho de secção
    Text(
        text = title,
        color = BrandPurple,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

/**
 * Item individual das definições com título, subtítulo e ação ao clicar.
 *
 * @param title Texto principal do item.
 * @param subtitle Texto secundário, por exemplo, o valor atual da definição.
 * @param onClick Callback chamado ao clicar no item.
 */
@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    // Linha clicável que ocupa toda a largura, com fundo de "cartão" e espaçamento interno
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Coluna com o título e subtítulo do item, alinhados à esquerda
        Column {
            // Título principal do item
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            // Pequeno espaço entre o título e o subtítulo
            Spacer(modifier = Modifier.height(2.dp))
            // Subtítulo (valor atual ou descrição) do item
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 13.sp
            )
        }
        // Símbolo ">" indicando que o item é navegável/clicável
        Text(
            text = "›",
            color = TextSecondary,
            fontSize = 20.sp
        )
    }
}

/**
 * Linha divisória entre itens das definições.
 */
@Composable
private fun SettingsDivider() {
    // Linha horizontal fina, com recuo à esquerda para alinhar com o texto dos itens
    HorizontalDivider(
        modifier = Modifier.padding(start = 24.dp),
        color = BackgroundDark,
        thickness = 1.dp
    )
}

/**
 * Diálogo para alterar o nome do utilizador.
 *
 * @param currentName Nome actual do utilizador.
 * @param isLoading Indica se a operação está em curso.
 * @param onConfirm Callback chamado ao confirmar com o novo nome.
 * @param onDismiss Callback chamado ao cancelar.
 */
@Composable
private fun ChangeNameDialog(
    currentName: String,
    isLoading: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Estado local do campo de texto, inicializado com o nome atual do utilizador
    var newName by remember { mutableStateOf(currentName) }

    // Diálogo modal para edição do nome
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = {
            // Título do diálogo
            Text(text = "Alterar nome", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            // Campo de texto para introduzir o novo nome
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Novo nome", color = TextSecondary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = BrandPurple,
                    unfocusedBorderColor = TextSecondary,
                    cursorColor = BrandPurple
                )
            )
        },
        confirmButton = {
            // Botão de confirmação, desativado enquanto a operação estiver em curso
            TextButton(
                onClick = { onConfirm(newName) },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    // Mostra um spinner pequeno enquanto a operação está em curso
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = BrandPurple
                    )
                } else {
                    // Texto normal do botão quando não há operação em curso
                    Text("Guardar", color = BrandPurple)
                }
            }
        },
        dismissButton = {
            // Botão para cancelar e fechar o diálogo sem guardar
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}

/**
 * Diálogo para alterar a palavra-passe do utilizador.
 * Requer a palavra-passe atual para voltar a autenticar.
 *
 * @param isLoading Indica se a operação está em curso.
 * @param onConfirm Callback chamado ao confirmar com a palavra-passe actual e a nova.
 * @param onDismiss Callback chamado ao cancelar.
 */
@Composable
private fun ChangePasswordDialog(
    isLoading: Boolean,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    // Estado do campo da palavra-passe atual
    var currentPassword by remember { mutableStateOf("") }
    // Estado do campo da nova palavra-passe
    var newPassword by remember { mutableStateOf("") }
    // Estado do campo de confirmação da nova palavra-passe
    var confirmNewPassword by remember { mutableStateOf("") }
    // Mensagem de erro local (ex.: palavras-passe não coincidem)
    var passwordError by remember { mutableStateOf("") }

    // Diálogo modal para alteração da palavra-passe
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = {
            // Título do diálogo
            Text(
                text = "Alterar palavra-passe",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            // Coluna com os três campos de palavra-passe, espaçados entre si
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Campo para a palavra-passe atual (necessária para reautenticação)
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Palavra-passe atual", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrandPurple,
                        unfocusedBorderColor = TextSecondary,
                        cursorColor = BrandPurple
                    )
                )
                // Campo para a nova palavra-passe
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Nova palavra-passe", color = TextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrandPurple,
                        unfocusedBorderColor = TextSecondary,
                        cursorColor = BrandPurple
                    )
                )
                // Campo para confirmar a nova palavra-passe, com suporte a mensagem de erro
                OutlinedTextField(
                    value = confirmNewPassword,
                    onValueChange = { confirmNewPassword = it },
                    label = { Text("Confirmar nova palavra-passe", color = TextSecondary) },
                    singleLine = true,
                    isError = passwordError.isNotEmpty(),
                    supportingText = if (passwordError.isNotEmpty()) {
                        // Mostra a mensagem de erro abaixo do campo, se existir
                        { Text(passwordError, color = ErrorRed) }
                    } else null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BrandPurple,
                        unfocusedBorderColor = TextSecondary,
                        cursorColor = BrandPurple
                    )
                )
            }
        },
        confirmButton = {
            // Botão de confirmação, desativado enquanto a operação estiver em curso
            TextButton(
                onClick = {
                    // Validação local: as duas novas palavras-passe têm de coincidir
                    if (newPassword != confirmNewPassword) {
                        // Define a mensagem de erro a mostrar no campo de confirmação
                        passwordError = "As palavras-passes não coincidem"
                    } else {
                        // Dispara o callback de confirmação com a palavra-passe atual e a nova
                        onConfirm(currentPassword, newPassword)
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    // Mostra um spinner pequeno enquanto a operação está em curso
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = BrandPurple
                    )
                } else {
                    // Texto normal do botão quando não há operação em curso
                    Text("Guardar", color = BrandPurple)
                }
            }
        },
        dismissButton = {
            // Botão para cancelar e fechar o diálogo sem guardar
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}

/**
 * Diálogo de confirmação antes de terminar a sessão.
 *
 * @param onConfirm Callback chamado ao confirmar o logout.
 * @param onDismiss Callback chamado ao cancelar.
 */
@Composable
private fun LogoutConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    // Diálogo modal simples de confirmação de logout
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = {
            // Título do diálogo
            Text(
                text = "Terminar sessão",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            // Mensagem de confirmação apresentada ao utilizador
            Text(
                text = "Tens a certeza que queres terminar a sessão?",
                color = TextSecondary
            )
        },
        confirmButton = {
            // Botão que confirma o logout, destacado em vermelho
            TextButton(onClick = onConfirm) {
                Text("Terminar sessão", color = ErrorRed, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            // Botão para cancelar e manter a sessão ativa
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}