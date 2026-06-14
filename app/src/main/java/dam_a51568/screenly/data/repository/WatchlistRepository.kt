package dam_a51568.screenly.data.repository

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dam_a51568.screenly.data.model.WatchStatus
import dam_a51568.screenly.data.model.WatchlistItem

/**
 * Repositório responsável pela gestão da watchlist pessoal de cada utilizador.
 *
 * Esta classe centraliza todas as operações relacionadas com conteúdos guardados
 * pelo utilizador, incluindo:
 * - Adição de filmes e séries à watchlist;
 * - Atualização do estado de visualização;
 * - Armazenamento de classificações e críticas pessoais;
 * - Remoção de conteúdos;
 * - Sincronização automática com a Firestore em tempo real.
 *
 * O repositório utiliza Firebase Authentication para identificar o utilizador
 * autenticado e Firebase Firestore para persistir os dados na nuvem.
 *
 * Adicionalmente, mantém uma cache local observável por
 * mutableStateListOf(), permitindo que o Jetpack Compose atualize
 * automaticamente a interface sempre que existirem alterações nos dados.
 */
object WatchlistRepository {
    // Instância da Firestore criada apenas quando for necessária
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    // Instância responsável pela autenticação do utilizador
    private val auth by lazy { FirebaseAuth.getInstance() }

    /**
     * Cache local observável da watchlist.
     *
     * Como é uma StateList do Compose, qualquer alteração aos seus elementos
     * provoca automaticamente a recomposição dos componentes visuais que a utilizam.
     */
    private val _items = mutableStateListOf<WatchlistItem>()

    /**
     * Exposição apenas para leitura da cache interna.
     *
     * Desta forma, outras camadas da aplicação podem consultar os dados, mas não conseguem modificá-los diretamente.
     */
    val items: List<WatchlistItem> get() = _items

    /**
     * Obtém uma referência para a subcoleção "watchlist" pertencente
     * ao utilizador atualmente autenticado.
     *
     * Estrutura da Firestore:
     *
     * users
     *  └── uid
     *       └── watchlist
     *            └── documento
     *
     * @return Referência para a coleção watchlist do utilizador atual.
     */
    private fun watchlistCollection() = firestore
        .collection("users") // Acede à coleção principal onde estão armazenados todos os utilizadores

        // Seleciona o documento correspondente ao utilizador autenticado
        // Se não existir utilizador autenticado, utiliza uma string vazia
        .document(auth.currentUser?.uid ?: "")
        .collection("watchlist") // Acede à subcoleção onde são guardados os conteúdos da watchlist

    /**
     * Inicia a observação em tempo real da watchlist do utilizador.
     * Deve ser chamado quando o utilizador faz login.
     *
     * É criado um Snapshot Listener que permanece ativo e recebe automaticamente notificações
     * sempre que existirem alterações na coleção watchlist da Firestore.
     *
     * Sempre que novos dados chegam:
     * 1. A cache local é limpa;
     * 2. Os documentos são convertidos para objetos WatchlistItem;
     * 3. A cache é reconstruída;
     * 4. O Compose atualiza automaticamente a interface.
     */
    fun startListening() {
        watchlistCollection() // Obtém a referência para a watchlist do utilizador atual
            // Regista um listener que será executado sempre que existirem alterações na coleção watchlist da Firestore
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) { // Verifica se foi recebido um snapshot válido
                    _items.clear() // Remove todos os elementos atualmente presentes na cache local
                    // Converte os documentos Firestore para objetos WatchlistItem
                    // e adiciona-os novamente à cache local
                    _items.addAll(
                        snapshot.documents.mapNotNull { doc ->  // Percorre todos os documentos devolvidos pela Firestore
                            // Converte cada documento para um objeto WatchlistItem
                            // Caso a conversão falhe, o documento é ignorado
                            doc.toObject(WatchlistItem::class.java)
                        }
                    )
                }
            }
    }

    /**
     * Obtém todos os conteúdos pertencentes a um determinado estado.
     *
     * A filtragem é efetuada localmente na cache sem necessidade
     * de realizar novas consultas à Firestore.
     *
     * @param status Estado pretendido.
     * @return Lista de conteúdos com o estado especificado.
     */
    fun getByStatus(status: WatchStatus): List<WatchlistItem> =
        // Percorre todos os elementos da cache local
        // e mantém apenas aqueles cujo estado coincide com o pretendido
        _items.filter { it.status == status }

    /**
     * Adiciona um novo conteúdo à watchlist ou atualiza um já existente.
     *
     * O identificador do documento é composto pelo ID do conteúdo e pelo
     * tipo de media para evitar colisões entre filmes e séries que possam
     * possuir o mesmo identificador numérico.
     *
     *
     * @param item Conteúdo a guardar.
     */
    fun addOrUpdate(item: WatchlistItem) {
        // Obtém o UID do utilizador autenticado
        // Se não existir sessão iniciada, termina imediatamente a função
        val uid = auth.currentUser?.uid ?: return
        // Cria um identificador único para o documento Firestore
        // combinando o ID do conteúdo com o seu tipo
        val docId = "${item.id}_${item.mediaType}"
        // Cria uma cópia do objeto associando-o ao utilizador atual
        val itemWithUid = item.copy(uid = uid)

        watchlistCollection() // Obtém a coleção watchlist
            .document(docId) // Seleciona o documento correspondente ao conteúdo
            // Guarda o objeto na Firestore
            // Caso o documento já exista, os dados serão atualizados
            .set(itemWithUid)
    }

    /**
     * Atualiza apenas o estado de visualização de um conteúdo.
     *
     * Utiliza uma operação parcial de update para modificar
     * exclusivamente o campo "status", preservando os restantes dados.
     *
     * @param id ID do conteúdo.
     * @param mediaType Tipo de conteúdo ("movie" ou "tv").
     * @param newStatus Novo estado pretendido.
     */
    fun updateStatus(id: Int, mediaType: String, newStatus: WatchStatus) {
        val docId = "${id}_${mediaType}" // Reconstrói o identificador único do documento
        watchlistCollection() // Obtém a coleção watchlist
            .document(docId) // Seleciona o documento pretendido
            // Atualiza apenas o campo "status"
            // O enum é convertido para texto através da propriedade name
            .update("status", newStatus.name)
    }

    /**
     * Atualiza simultaneamente a classificação pessoal e a crítica associadas a um conteúdo da watchlist.
     *
     * A operação é efetuada através de um update parcial, evitando a reescrita completa do documento.
     *
     * @param id ID do conteúdo.
     * @param mediaType Tipo de conteúdo.
     * @param rating Classificação atribuída pelo utilizador.
     * @param review Texto da crítica escrita pelo utilizador.
     */
    fun updateRatingAndReview(id: Int, mediaType: String, rating: Float, review: String) {
        val docId = "${id}_${mediaType}" // Reconstrói o identificador do documento
        watchlistCollection()  // Obtém a coleção watchlist
            .document(docId)  // Seleciona o documento correspondente
            .update( // Atualiza simultaneamente os campos rating e review
                mapOf(
                    "rating" to rating, // Nova classificação atribuída pelo utilizador
                    "review" to review // Novo texto da crítica
                )
            )
    }

    /**
     * Remove permanentemente um conteúdo da watchlist.
     *
     * Após a eliminação do documento, o Snapshot Listener detetará
     * automaticamente a alteração e atualizará a cache local.
     *
     * @param id ID do conteúdo.
     * @param mediaType Tipo de conteúdo.
     */
    fun remove(id: Int, mediaType: String) {
        val docId = "${id}_${mediaType}" // Reconstrói o identificador do documento
        watchlistCollection() // Obtém a coleção watchlist
            .document(docId) // Seleciona o documento correspondente
            .delete() // Elimina permanentemente o documento da Firestore
    }

    /**
     * Verifica se um conteúdo existe atualmente na watchlist
     * e devolve o respetivo estado de visualização.
     *
     * A pesquisa é realizada na cache local, tornando a operação
     * extremamente rápida e sem necessidade de comunicação com a Firestore.
     *
     * @param id ID do conteúdo.
     * @param mediaType Tipo de conteúdo.
     * @return Estado encontrado ou null caso o conteúdo não exista.
     */
    fun getStatus(id: Int, mediaType: String): WatchStatus? =
        // Procura o primeiro elemento da cache local que possua
        // simultaneamente o mesmo ID e o mesmo tipo de conteúdo
        _items.firstOrNull { it.id == id && // Verifica se o identificador coincide
                it.mediaType == mediaType // Verifica se o tipo de conteúdo coincide
            // Caso não exista nenhum elemento correspondente,
            // firstOrNull devolve null
        }?.status // Se o elemento existir, devolve apenas o seu estado
}