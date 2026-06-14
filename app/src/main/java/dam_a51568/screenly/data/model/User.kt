package dam_a51568.screenly.data.model

/**
 * Modelo de domínio que representa um utilizador da aplicação.
 *
 * Esta classe é utilizada para armazenar e sincronizar a informação
 * do utilizador autenticado, tanto proveniente do Firebase Authentication
 * como da base de dados Firestore.
 *
 * Permite gerir dados de perfil e personalização da conta.
 *
 * @property uid Identificador único do utilizador no Firebase Authentication.
 * @property email Email associado à conta do utilizador (pode ser nulo dependendo do provider).
 * @property displayName Nome de apresentação do utilizador na aplicação.
 * @property photoUrl URL da imagem de perfil fornecida pelo provider.
 * @property photoBase64 Fotografia de perfil codificada em Base64 e armazenada na Firestore
 */
data class User(
    val uid: String = "", // Serve como chave principal para associar dados na Firestore
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    // Foto de perfil codificada em Base64, guardada no Firestore.
    val photoBase64: String? = null // Utilizada como alternativa ao photoUrl para persistência personalizada
)