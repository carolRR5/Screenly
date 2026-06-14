package dam_a51568.screenly.data.model

/**
 * Modelo de domínio que representa uma review (crítica) de um utilizador
 * sobre um filme ou série.
 *
 * Esta classe é utilizada na camada de apresentação para exibir comentários,
 * avaliações e informação associada ao autor da review.
 *
 * Inclui também dados já tratados e formatados pela camada de dados,
 * como a data e o URL do avatar.
 *
 * @property id Identificador único da review na API.
 * @property author Nome do autor da review.
 * @property content Conteúdo textual da review (comentário completo).
 * @property rating Avaliação atribuída pelo autor (pode ser null se não existir rating).
 * @property avatarUrl URL do avatar do autor da review.
 * @property formattedDate Data da review já formatada para apresentação na UI.
 */
data class Review(
    val id: String,
    val author: String,
    val content: String,
    val rating: Double?,
    val avatarUrl: String?, // Pode ser null caso o utilizador não tenha imagem associada
    val formattedDate: String
)