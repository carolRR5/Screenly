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
    val operationState by viewModel.operationState.collectAsState()

    // Diálogos visíveis
    var showNameDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Mostra snackbar com o resultado da operação
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(operationState) {
        when (val state = operationState) {
            is SettingsOperationState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetOperationState()
            }
            is SettingsOperationState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetOperationState()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Barra de topo
            TopBar(onBack = onBack)

            Spacer(modifier = Modifier.height(8.dp))

            // Secção Conta
            SettingsSectionTitle(title = "Conta")

            SettingsItem(
                title = "Alterar nome",
                subtitle = viewModel.currentName,
                onClick = { showNameDialog = true }
            )
            SettingsDivider()
            SettingsItem(
                title = "Alterar palavra-passe",
                subtitle = "••••••••",
                onClick = { showPasswordDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Secção Sobre
            SettingsSectionTitle(title = "Sobre")

            SettingsItem(
                title = "Versão da aplicação",
                subtitle = viewModel.appVersion,
                onClick = {}
            )

            Spacer(modifier = Modifier.height(650.dp))


            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
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
                    Text(
                        text = "Terminar sessão",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showNameDialog) {
        ChangeNameDialog(
            currentName = viewModel.currentName,
            isLoading = operationState is SettingsOperationState.Loading,
            onConfirm = { newName ->
                viewModel.updateName(newName)
                showNameDialog = false
            },
            onDismiss = { showNameDialog = false }
        )
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            isLoading = operationState is SettingsOperationState.Loading,
            onConfirm = { currentPassword, newPassword ->
                viewModel.updatePassword(currentPassword, newPassword)
                showPasswordDialog = false
            },
            onDismiss = { showPasswordDialog = false }
        )
    }

    if (showLogoutDialog) {
        LogoutConfirmDialog(
            onConfirm = {
                viewModel.logout()
                onLogout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}

/**
 * Barra de topo com botão de retroceder e título.
 */
@Composable
private fun TopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Retroceder",
                tint = TextPrimary
            )
        }
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 13.sp
            )
        }
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
    var newName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = {
            Text(text = "Alterar nome", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
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
            TextButton(
                onClick = { onConfirm(newName) },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = BrandPurple
                    )
                } else {
                    Text("Guardar", color = BrandPurple)
                }
            }
        },
        dismissButton = {
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
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = {
            Text(
                text = "Alterar palavra-passe",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                OutlinedTextField(
                    value = confirmNewPassword,
                    onValueChange = { confirmNewPassword = it },
                    label = { Text("Confirmar nova palavra-passe", color = TextSecondary) },
                    singleLine = true,
                    isError = passwordError.isNotEmpty(),
                    supportingText = if (passwordError.isNotEmpty()) {
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
            TextButton(
                onClick = {
                    if (newPassword != confirmNewPassword) {
                        passwordError = "As palavras-passes não coincidem"
                    } else {
                        onConfirm(currentPassword, newPassword)
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = BrandPurple
                    )
                } else {
                    Text("Guardar", color = BrandPurple)
                }
            }
        },
        dismissButton = {
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
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        title = {
            Text(
                text = "Terminar sessão",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Tens a certeza que queres terminar a sessão?",
                color = TextSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Terminar sessão", color = ErrorRed, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}