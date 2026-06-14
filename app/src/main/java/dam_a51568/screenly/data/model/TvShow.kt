package dam_a51568.screenly.data.model

/**
 * Modelo de domínio que representa uma série de televisão na aplicação.
 *
 * Esta data class contém a informação essencial de uma série após
 * transformação dos dados provenientes da API TMDb.
 *
 * É utilizada na camada de apresentação para exibir listas e detalhes
 * de séries, mantendo apenas os dados relevantes para a interface.
 *
 * @property id Identificador único da série na API.
 * @property title Título oficial da série apresentado ao utilizador.
 * @property posterUrl URL completa do póster da série.
 * @property overview  Sinopse/descrição da série.
 * @property year Ano de lançamento da série.
 * @property rating Avaliação média atribuída pelos utilizadores (0.0 a 10).
 * @property genres Lista de géneros associados à série. Ex: ["Drama", "Comedy"]
 * @property episodeRuntime Duração típica de um episódio em minutos
 */
data class TvShow(
    val id: Int,
    val title: String,
    val posterUrl: String,
    val overview: String,
    val year: String,
    val rating: Double,
    val genres: List<String>,
    val episodeRuntime: Int // Pode variar entre episódios, mas é normalizada para apresentação
)