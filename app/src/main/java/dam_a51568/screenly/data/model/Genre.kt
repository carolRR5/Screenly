package dam_a51568.screenly.data.model

/**
 * Modelo de domínio que representa um género cinematográfico ou televisivo.
 *
 * Esta classe é utilizada para categorizar conteúdos como filmes ou séries,
 * permitindo organizar e filtrar a informação apresentada ao utilizador
 * (ex: Ação, Drama, Comédia, Ficção Científica).
 *
 * O objetivo é simplificar a estrutura vinda da API e manter apenas
 * os dados essenciais para a camada de apresentação.
 *
 * @property id Identificador único do género na base de dados.
 * @property name Nome do género apresentado na interface
 */
data class Genre(
    val id: Int,
    val name: String
)