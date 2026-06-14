package dam_a51568.screenly.data.model

/**
 * Modelo de domínio genérico que representa um conteúdo multimédia
 * (filme ou série) de forma simplificada.
 *
 * Esta classe é utilizada principalmente em listagens e resultados de pesquisa,
 * onde não é necessário expor todos os detalhes do conteúdo, apenas
 * a informação essencial para apresentação na interface.
 *
 * Permite também tratar filmes e séries de forma polimórfica através do campo mediaType.
 *
 * @property id Identificador único do conteúdo (filme ou série) na API.
 * @property mediaType Tipo de conteúdo multimédia.
 * @property title Título do conteúdo apresentado ao utilizador.
 * @property posterUrl // URL completa do poster do conteúdo
 * @property year // Ano de lançamento ou estreia do conteúdo
 */
data class MediaItem(
    val id: Int,
    val mediaType: String, // Permite distinguir e tratar corretamente diferentes tipos de media
    val title: String, // Pode representar "title" (filmes) ou "name" (séries)
    val posterUrl: String, // Construída a partir da base URL da API + caminho relativo
    val year: String // Formatado como String para simplificação na UI (ex: "2024")
)