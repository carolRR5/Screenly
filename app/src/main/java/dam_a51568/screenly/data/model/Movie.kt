package dam_a51568.screenly.data.model

/**
 * Modelo de domínio que representa um filme na aplicação.
 *
 * Esta data class contém apenas a informação essencial necessária
 * para a camada de apresentação, após transformação dos dados
 * provenientes da API TMDb.
 *
 * Serve como entidade principal para exibição de detalhes de filmes,
 * incluindo metadados como avaliação, géneros e duração.
 *
 * @property id Identificador único do filme na API.
 * @property title Título oficial do filme apresentado ao utilizador.
 * @property posterUrl URL completa do póster do filme.
 * @property overview  Sinopse/descrição do filme.
 * @property year Ano de lançamento do filme.
 * @property rating Avaliação média atribuída pelos utilizadores (0.0 a 10).
 * @property genres Lista de géneros associados ao filme. Ex: ["Action", "Drama", "Thriller"].
 * @property runtime Duração do filme em minutos.
 */
data class Movie(
    val id: Int,
    val title: String,
    val posterUrl: String, // Construída a partir da base de imagens da API + caminho relativo
    val overview: String,
    val year: String,
    val rating: Double,
    val genres: List<String>,
    val runtime: Int
)