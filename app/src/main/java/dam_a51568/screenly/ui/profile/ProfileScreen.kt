package dam_a51568.screenly.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import dam_a51568.screenly.data.model.WatchlistItem
import dam_a51568.screenly.data.remote.TmdbClient
import dam_a51568.screenly.data.model.WatchStatus
import dam_a51568.screenly.data.repository.WatchlistRepository
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Face
import androidx.compose.ui.graphics.asImageBitmap
import dam_a51568.screenly.ui.theme.BackgroundDark
import dam_a51568.screenly.ui.theme.BrandPurple
import dam_a51568.screenly.ui.theme.CardBackground
import dam_a51568.screenly.ui.theme.TextPrimary
import dam_a51568.screenly.ui.theme.TextSecondary

/**
 * Ecrã de Perfil da aplicação Screenly.
 *
 * Apresenta as informações do utilizador autenticado, as suas estatísticas
 * de visualização, botões para navegar para as listas e as avaliações recentes.
 *
 * @param onNavigateToSettings Callback para navegar para o ecrã de Definições.
 * @param onNavigateToLists Callback para navegar para o ecrã de Listas, recebendo o estado inicial do separador.
 * @param onItemClick Callback chamado quando o utilizador clica num título.
 * @param viewModel ViewModel que gere o estado do ecrã.
 */
@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToLists: (WatchStatus) -> Unit,
    onItemClick: (id: Int, mediaType: String) -> Unit,
    viewModel: ProfileViewModel = viewModel(),
) {
    // Observa de forma reativa os dados do utilizador (Firestore/Firebase Auth)
    val user by viewModel.user.collectAsState()
    // Observa de forma reativa o estado do upload da foto de perfil
    val photoUploadState by viewModel.photoUploadState.collectAsState()

    // Estatísticas e avaliações recentes recalculadas quando a watchlist muda
    // "remember(WatchlistRepository.items)" significa que estes valores só são
    // recalculados quando a lista de itens da watchlist mudar de referência/conteúdo
    val stats = remember(WatchlistRepository.items) { viewModel.getStats() }
    val recentRatings = remember(WatchlistRepository.items) { viewModel.getRecentRatings() }

    // Contexto Android necessário para aceder ao ContentResolver (leitura de ficheiros)
    val context = androidx.compose.ui.platform.LocalContext.current
    // Launcher que abre o seletor de imagens do sistema e devolve o URI escolhido
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // Lê os bytes da imagem selecionada
            // Abre um stream de leitura para o ficheiro apontado pelo URI
            val inputStream = context.contentResolver.openInputStream(it)
            // Lê todos os bytes do ficheiro de imagem
            val imageBytes = inputStream?.readBytes()
            // Fecha o stream para libertar recursos
            inputStream?.close()
            // Se a leitura teve sucesso, envia os bytes para o ViewModel processar o upload
            imageBytes?.let {
                viewModel.uploadProfilePhoto(it)
            }
        }
    }

    // Snackbar para feedback do upload
    // Estado que controla a exibição de snackbars na UI
    val snackbarHostState = remember { SnackbarHostState() }
    // Efeito que reage a mudanças no estado do upload da foto
    LaunchedEffect(photoUploadState) {
        when (val state = photoUploadState) {
            is PhotoUploadState.Success -> {
                // Mostra mensagem de sucesso
                snackbarHostState.showSnackbar("Foto de perfil atualizada!")
                // Repõe o estado para Idle, para a snackbar não reaparecer
                viewModel.resetPhotoUploadState()
            }
            is PhotoUploadState.Error -> {
                // Mostra a mensagem de erro devolvida pelo ViewModel
                snackbarHostState.showSnackbar(state.message)
                // Repõe o estado para Idle, para a snackbar não reaparecer
                viewModel.resetPhotoUploadState()
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
        // Coluna principal com scroll vertical, ocupando todo o espaço disponível
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BackgroundDark)
                .verticalScroll(rememberScrollState())
        ) {
            // Cabeçalho com foto, nome, email, data de registo e botão de definições
            ProfileHeader(
                // Usa o displayName se não estiver vazio, caso contrário mostra "Utilizador"
                displayName = user?.displayName?.takeIf { it.isNotBlank() } ?: "Utilizador",
                // Usa o email se não estiver vazio, caso contrário string vazia
                email = user?.email?.takeIf { it.isNotBlank() } ?: "",
                photoBase64 = user?.photoBase64,
                photoUrl = user?.photoUrl,
                memberSince = viewModel.memberSince,
                onSettingsClick = onNavigateToSettings,
                // True enquanto o upload da foto estiver em curso (mostra spinner)
                isUploadingPhoto = photoUploadState is PhotoUploadState.Loading,
                // Ao clicar na foto, abre o seletor de imagens do sistema, filtrando por imagens
                onChangePhoto = { launcher.launch("image/*") }
            )

            Spacer(modifier = Modifier.height(24.dp))
            // Secção com cartões de estatísticas (vistos, horas, média, género favorito)
            StatsSection(stats = stats)
            Spacer(modifier = Modifier.height(24.dp))
            // Secção com as listas do utilizador (To Watch, Watching, Watched)
            ListsButtonsSection(
                onListClick = onNavigateToLists,
                onItemClick = onItemClick
            )
            Spacer(modifier = Modifier.height(24.dp))
            // Secção de avaliações recentes, só aparece se existirem avaliações
            if (recentRatings.isNotEmpty()) {
                SectionTitle(title = "Avaliações recentes")
                Spacer(modifier = Modifier.height(12.dp))
                RecentRatingsSection(
                    ratings = recentRatings,
                    onItemClick = onItemClick
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Cabeçalho do perfil com foto, nome, email, data de registo e botão de definições.
 */
@Composable
private fun ProfileHeader(
    displayName: String,
    email: String,
    photoBase64: String?,
    photoUrl: String?,
    memberSince: String,
    onSettingsClick: () -> Unit,
    isUploadingPhoto: Boolean,
    onChangePhoto: () -> Unit
) {
    // Linha que distribui a informação do utilizador à esquerda e o botão de definições à direita
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Linha interna com a foto de perfil e os dados textuais (nome, email, data)
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto de perfil (ou placeholder), clicável para alterar
            ProfilePhoto(
                photoBase64 = photoBase64,
                photoUrl = photoUrl,
                displayName = displayName,
                isUploading = isUploadingPhoto,
                onChangePhoto = onChangePhoto
            )

            // Coluna com nome, email e data de registo
            Column {
                // Nome de exibição do utilizador
                Text(
                    text = displayName,
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Email do utilizador
                Text(
                    text = email,
                    color = TextSecondary,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Data de criação da conta, formatada
                Text(
                    text = "Membro desde $memberSince",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        }

        // Botão de acesso ao ecrã de Definições
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Definições",
                tint = TextSecondary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * Foto de perfil do utilizador.
 * Se não houver foto, mostra um círculo com a inicial do nome.
 */
@Composable
private fun ProfilePhoto(
    photoBase64: String?,
    photoUrl: String?,
    displayName: String,
    isUploading: Boolean,
    onChangePhoto: () -> Unit
) {
    // Caixa circular clicável que contém a foto/placeholder e os indicadores sobrepostos
    Box(
        modifier = Modifier
            .size(110.dp)
            .clickable(onClick = onChangePhoto),
        contentAlignment = Alignment.Center
    ) {
        // Decide qual fonte de imagem usar, por ordem de prioridade
        when {
            // Mostra a foto em Base64 do Firestore
            photoBase64 != null -> {
                // Decodifica a string Base64 para bytes
                val imageBytes = android.util.Base64.decode(photoBase64, android.util.Base64.DEFAULT)
                // Converte os bytes num Bitmap Android
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                // Converte o Bitmap para ImageBitmap, usável pelo Compose
                val imageBitmap = bitmap.asImageBitmap()
                // Renderiza a imagem em círculo, com borda na cor da marca
                androidx.compose.foundation.Image(
                    bitmap = imageBitmap,
                    contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .border(2.dp, BrandPurple, CircleShape)
                )
            }
            // Fallback para o photoUrl do Firebase Auth
            photoUrl != null -> {
                // Carrega a imagem a partir de uma URL remota, de forma assíncrona
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .border(2.dp, BrandPurple, CircleShape)
                )
            }
            // Placeholder com inicial do nome
            else -> {
                // Círculo colorido com a primeira letra do nome do utilizador
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(BrandPurple),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        // Primeira letra do nome em maiúscula, ou "?" se o nome estiver vazio
                        text = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        color = TextPrimary,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Indicador de carregamento durante o upload
        if (isUploading) {
            // Sobrepõe um overlay semi-transparente com spinner enquanto o upload está em curso
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = BrandPurple,
                    modifier = Modifier.size(32.dp)
                )
            }
        } else {
            // Ícone de câmara sobreposto na parte inferior
            // Pequeno círculo no canto inferior direito, indicando que a foto pode ser alterada
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(BrandPurple),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = "Alterar foto",
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Secção de estatísticas do utilizador.
 */
@Composable
private fun StatsSection(stats: UserStats) {
    // Coluna com o título da secção e a linha de cartões de estatísticas
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        SectionTitle(title = "Estatísticas")
        Spacer(modifier = Modifier.height(12.dp))
        // Linha com 4 cartões de igual largura (cada um ocupa 1/4 do espaço, via weight)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cartão com o número total de títulos vistos
            StatCard(
                modifier = Modifier.weight(1f),
                value = stats.totalWatched.toString(),
                label = "Vistos"
            )
            // Cartão com o total de horas vistas
            StatCard(
                modifier = Modifier.weight(1f),
                value = "${stats.totalHours}h",
                label = "Horas"
            )
            // Cartão com a média das avaliações, ou "—" se não houver avaliações
            StatCard(
                modifier = Modifier.weight(1f),
                value = if (stats.averageRating > 0f) "%.1f".format(stats.averageRating) else "—",
                label = "Média ⭐"
            )
            // Cartão com o género favorito (calculado pelo ViewModel), texto mais pequeno
            // por poder ser uma palavra mais longa
            StatCard(
                modifier = Modifier.weight(1f),
                value = stats.favoriteGenre ?: "—",
                label = "Género fav.",
                smallText = true
            )
        }
    }
}

/**
 * Cartão individual de uma estatística.
 */
@Composable
private fun StatCard(
    value: String,
    label: String,
    smallText: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Coluna com fundo de "cartão" e cantos arredondados, valor em destaque e legenda abaixo
    Column(
        modifier = modifier
            .background(CardBackground, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Valor numérico/texto da estatística, com tamanho de fonte ajustável
        Text(
            text = value,
            color = BrandPurple,
            fontSize = if (smallText) 14.sp else 20.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        // Legenda/descrição da estatística
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}

/**
 * Secção das listas do utilizador.
 * Apresenta três subsecções (To Watch, Watching, Watched), cada uma com
 * uma lista horizontal de até 10 pósteres e um botão "›" para ver mais
 * caso existam mais de 10 títulos.
 *
 * @param onListClick Callback chamado ao clicar em "Ver mais", recebendo o estado da lista.
 * @param onItemClick Callback chamado ao clicar num título.
 */
@Composable
private fun ListsButtonsSection(
    onListClick: (WatchStatus) -> Unit,
    onItemClick: (id: Int, mediaType: String) -> Unit
) {
    // Coluna com o título "As minhas listas" e uma subsecção para cada estado da watchlist
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        SectionTitle(title = "As minhas listas")
        Spacer(modifier = Modifier.height(16.dp))

        // Itera sobre todos os valores do enum WatchStatus (TO_WATCH, WATCHING, WATCHED)
        WatchStatus.entries.forEach { status ->
            // Obtém os itens da watchlist correspondentes a este estado
            val items = WatchlistRepository.getByStatus(status)
            // Traduz o nome do enum para o rótulo apresentado ao utilizador
            val label = when (status) {
                WatchStatus.TO_WATCH -> "To Watch"
                WatchStatus.WATCHING -> "Watching"
                WatchStatus.WATCHED -> "Watched"
            }

            // Renderiza a subsecção correspondente a este estado
            ListSection(
                label = label,
                items = items,
                onItemClick = onItemClick,
                onViewMore = { onListClick(status) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Subsecção individual de uma lista.
 * Apresenta o nome da lista, uma linha horizontal com até 10 pósteres
 * e um cartão "›" no final se houver mais de 10 títulos.
 *
 * @param label Nome da lista.
 * @param items Lista completa de títulos.
 * @param onItemClick Callback chamado ao clicar num título.
 * @param onViewMore Callback chamado ao clicar no botão "›".
 */
@Composable
private fun ListSection(
    label: String,
    items: List<WatchlistItem>,
    onItemClick: (id: Int, mediaType: String) -> Unit,
    onViewMore: () -> Unit
) {
    // Número máximo de pósteres mostrados antes de exibir o botão "ver mais"
    val maxVisible = 10

    Column {
        // Nome da lista com contador
        // Linha com o nome da lista à esquerda e o número de títulos à direita
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nome da lista (ex: "To Watch")
            Text(
                text = label,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            // Contador de títulos na lista
            Text(
                text = "${items.size} títulos",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (items.isEmpty()) {
            // Estado vazio
            // Caixa centrada com mensagem indicando que a lista não tem títulos
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(CardBackground, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nenhum título nesta lista",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        } else {
            // Lista horizontal com pósteres
            // LazyRow: lista horizontal com scroll, renderiza apenas os itens visíveis
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Mostra até 10 títulos
                // Limita a lista aos primeiros "maxVisible" itens
                val visibleItems = items.take(maxVisible)
                visibleItems.forEach { watchlistItem ->
                    item {
                        // Cartão individual com o póster e título do item
                        ListPosterCard(
                            item = watchlistItem,
                            onClick = { onItemClick(watchlistItem.id, watchlistItem.mediaType) }
                        )
                    }
                }

                // Botão "›" apenas se houver mais de 10 títulos
                if (items.size > maxVisible) {
                    item {
                        // Cartão final que leva ao ecrã com a lista completa
                        ViewMoreCard(onClick = onViewMore)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Linha divisória no fundo da subsecção
        HorizontalDivider(color = CardBackground, thickness = 1.dp)
    }
}

/**
 * Cartão individual de um póster na lista horizontal.
 *
 * @param item Dados do título na watchlist.
 * @param onClick Callback chamado ao clicar no cartão.
 */
@Composable
private fun ListPosterCard(
    item: WatchlistItem,
    onClick: () -> Unit
) {
    // Coluna com largura fixa, cantos arredondados e fundo de "cartão", clicável
    Column(
        modifier = Modifier
            .width(100.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(CardBackground)
            .clickable(onClick = onClick)
    ) {
        // Imagem do póster, carregada a partir da base de imagens da TMDB
        AsyncImage(
            model = "${TmdbClient.IMAGE_BASE_URL}${item.posterPath}",
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
        )
        // Título do filme/série, limitado a 2 linhas
        Text(
            text = item.title,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(6.dp)
        )
    }
}

/**
 * Cartão "›" que aparece no final da lista horizontal quando há mais de 10 títulos.
 * Ao clicar, navega para o ecrã de listas completo.
 *
 * @param onClick Callback chamado ao clicar no cartão.
 */
@Composable
private fun ViewMoreCard(onClick: () -> Unit) {
    // Pequena caixa clicável com o símbolo "›", indicando navegação para mais itens
    Box(
        modifier = Modifier
            .width(60.dp)
            .height(50.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(CardBackground)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "›",
            color = BrandPurple,
            fontSize = 35.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Título de uma secção do ecrã de Perfil.
 */
@Composable
private fun SectionTitle(title: String) {
    // Texto em destaque, usado como cabeçalho de cada secção do ecrã
    Text(
        text = title,
        color = TextPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 24.dp)
    )
}

/**
 * Secção das avaliações recentes do utilizador.
 * Apresenta os últimos 5 títulos avaliados com póster, título e classificação.
 */
@Composable
private fun RecentRatingsSection(
    ratings: List<WatchlistItem>,
    onItemClick: (id: Int, mediaType: String) -> Unit
) {
    // Coluna com uma linha (cartão) por cada avaliação recente
    Column(
        modifier = Modifier.padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Itera sobre os itens avaliados recentemente
        ratings.forEach { item ->
            // Linha clicável com fundo de "cartão", contendo póster, título, estrelas e review
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBackground, RoundedCornerShape(12.dp))
                    .clickable { onItemClick(item.id, item.mediaType) }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pequeno póster do título avaliado
                AsyncImage(
                    model = "${TmdbClient.IMAGE_BASE_URL}${item.posterPath}",
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(48.dp)
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(6.dp))
                )

                // Coluna com título, estrelas de avaliação e review (se existir)
                Column(modifier = Modifier.weight(1f)) {
                    // Título do filme/série avaliado
                    Text(
                        text = item.title,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Linha com 5 estrelas, preenchidas conforme a avaliação (item.rating)
                    Row {
                        (1..5).forEach { star ->
                            Text(
                                // Mostra estrela preenchida (★) se o índice for <= à avaliação, senão vazia (☆)
                                text = if (star <= (item.rating ?: 0f)) "★" else "☆",
                                color = if (star <= (item.rating ?: 0f)) BrandPurple else TextSecondary,
                                fontSize = 16.sp
                            )
                        }
                    }
                    // Mostra a review do utilizador, se existir e não for apenas espaços
                    if (!item.review.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.review,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}