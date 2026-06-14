package dam_a51568.screenly.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Representa a resposta da API do TMDb para pesquisas multi-conteúdo.
 * Contém a lista de resultados devolvidos pela pesquisa.
 *
 * @param results Lista de itens de média (filmes ou séries) encontrados.
 */
data class TmdbSearchResponse(
    @SerializedName("results") val results: List<TmdbMediaItem>
)

/**
 * Representa a resposta da API ao endpoint de créditos de um filme ou série.
 * Contém separadamente o elenco (atores) e a crew (equipa técnica).
 *
 * @param cast Lista de membros do elenco.
 * @param crew Lista de membros da equipa técnica.
 */
data class TmdbCreditsResponse(
    @SerializedName("cast") val cast: List<TmdbCastMember>,
    @SerializedName("crew") val crew: List<TmdbCrewMember>
)

/**
 * Representa um actor ou atriz no elenco de um título.
 *
 * @param id Identificador único do actor no TMDb.
 * @param name Nome real do actor.
 * @param character Personagem que interpreta no título.
 * @param profilePath Caminho da foto de perfil do actor (pode ser null).
 * @param order Ordem de importância no elenco (0 = protagonista).
 */
data class TmdbCastMember(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("character") val character: String,
    @SerializedName("profile_path") val profilePath: String?,
    @SerializedName("order") val order: Int
)

/**
 * Representa um membro da equipa técnica de um título.
 *
 * @param id Identificador único do membro no TMDb.
 * @param name Nome do membro da crew.
 * @param job Função desempenhada (ex: "Diretor", "Producer").
 * @param department Departamento a que pertence (ex: "Directing", "Production").
 * @param profilePath Caminho da foto de perfil (pode ser null).
 */
data class TmdbCrewMember(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("job") val job: String,
    @SerializedName("department") val department: String,
    @SerializedName("profile_path") val profilePath: String?
)

/**
 * Representa um item individual (filme ou série) devolvido pela pesquisa.
 * A API do TMDb utiliza campos diferentes consoante o tipo de conteúdo:
 * - Para filmes é usado "title" e "release_date"
 * - Para séries é usado "name" e "first_air_date"
 * O campo "media_type" indica qual dos dois tipos é.
 *
 * @param id Identificador único do conteúdo no TMDb.
 * @param mediaType Tipo de conteúdo ("movie" para filmes, "tv" para séries).
 * @param title Título do filme (null se for uma série).
 * @param name Nome da série (null se for um filme).
 * @param overview Sinopse ou resumo do conteúdo.
 * @param posterPath Caminho para a imagem do póster (pode ser null).
 * @param releaseDate Data de lançamento do filme (null se for uma série).
 * @param firstAirDate Data de estreia da série (null se for um filme).
 * @param voteAverage Classificação média dos utilizadores.
 */
data class TmdbMediaItem(
    @SerializedName("id") val id: Int,
    @SerializedName("media_type") val mediaType: String, // "movie" para filmes, "tv" para séries
    @SerializedName("title") val title: String?, // Usado para filmes
    @SerializedName("name") val name: String?,   // Usado para séries
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("release_date") val releaseDate: String?, // Filmes
    @SerializedName("first_air_date") val firstAirDate: String?, // Séries
    @SerializedName("vote_average") val voteAverage: Double?
) {
    // Devolve o título a apresentar na UI, independentemente de ser filme ou série.
    val displayTitle: String
        get() = title ?: name ?: "Título Desconhecido"

    // Devolve apenas o ano de lançamento a partir da data completa (ex: "2023-04-15" corresponde a "2023").
    // Funciona tanto para filmes como para séries
    val displayYear: String
        get() {
            val date = releaseDate ?: firstAirDate ?: return "N/A"
            return if (date.length >= 4) date.substring(0, 4) else "N/A"
        }
}

/**
 * Representa os detalhes completos de um filme.
 * Utilizado no ecrã de detalhes após o utilizador selecionar um filme nos resultados de pesquisa.
 *
 * @param id Identificador único do filme no TMDb.
 * @param title Título oficial do filme.
 * @param overview Sinopse detalhada do filme.
 * @param posterPath Caminho para a imagem do póster (pode ser null).
 * @param releaseDate Data de lançamento oficial.
 * @param runtime Duração total do filme em minutos (pode ser null).
 * @param genres Lista de géneros associados ao filme.
 * @param voteAverage Classificação média atribuída pelos utilizadores.
 */
data class TmdbMovieDetails(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("runtime") val runtime: Int?, // Duração total do filme em minutos
    @SerializedName("genres") val genres: List<TmdbGenre>?,
    @SerializedName("vote_average") val voteAverage: Double?
)

/**
 * Representa os detalhes completos de uma série.
 * Utilizado no ecrã de detalhes após o utilizador selecionar uma série nos resultados de pesquisa.
 *
 * @param id Identificador único da série no TMDb.
 * @param name Nome oficial da série.
 * @param overview Sinopse detalhada da série.
 * @param posterPath Caminho para a imagem do póster (pode ser null).
 * @param firstAirDate Data em que a série estreou.
 * @param episodeRunTime Lista com as durações possíveis de episódios em minutos.
 * @param genres Lista de géneros associados à série.
 * @param voteAverage Classificação média atribuída pelos utilizadores.
 */
data class TmdbTvShowDetails(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("first_air_date") val firstAirDate: String?,
    @SerializedName("episode_run_time") val episodeRunTime: List<Int>?, // Durações possíveis de episódios em minutos
    @SerializedName("genres") val genres: List<TmdbGenre>?,
    @SerializedName("vote_average") val voteAverage: Double?
) {
    /**
     * Devolve a duração típica de um episódio em minutos.
     * A API pode devolver uma lista com vários valores (episódios de durações diferentes), utilizando
     * o primeiro como referência. Assume 45 minutos se a lista estiver vazia.
     */
    val typicalEpisodeRuntime: Int
        get() = episodeRunTime?.firstOrNull() ?: 45 // 45min por defeito se não houver dados
}

/**
 * Representa um género associado a um filme ou série (ex: Ação, Comédia, Drama).
 * Partilhado entre TmdbMovieDetails e TmdbTvShowDetails.
 *
 * @param id Identificador único do género.
 * @param name Nome do género na língua solicitada.
 */
data class TmdbGenre(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

/**
 * Representa um país de origem conforme as configurações da API do TMDb.
 *
 * @param code Código do país no padrão ISO 3166-1 (ex: "PT", "US").
 * @param englishName Nome do país em inglês.
 * @param nativeName Nome do país na sua língua nativa.
 */
data class TmdbCountry(
    @SerializedName("iso_3166_1") val code: String,  // ex: "PT", "US"
    @SerializedName("english_name") val englishName: String,
    @SerializedName("native_name") val nativeName: String
)

/**
 * Representa a resposta da API ao endpoint de géneros.
 * Contém a lista de géneros disponíveis para filmes ou séries.
 *
 * @param genres Lista de géneros devolvidos pela API.
 */
data class TmdbGenreResponse(
    @SerializedName("genres") val genres: List<TmdbGenre>
)

/**
 * Representa a resposta da API ao endpoint de vídeos de um título.
 *
 * @param results Lista de vídeos disponíveis.
 */
data class TmdbVideosResponse(
    @SerializedName("results") val results: List<TmdbVideo>
)

/**
 * Representa um vídeo associado a um filme ou série no TMDb.
 *
 * @param key Chave do vídeo no YouTube (usada para construir o URL).
 * @param site Plataforma onde o vídeo está alojado (ex: "YouTube").
 * @param type Tipo de vídeo (ex: "Trailer", "Teaser", "Clip").
 * @param official Indica se o vídeo é um conteúdo oficial de produção.
 */
data class TmdbVideo(
    @SerializedName("key") val key: String,
    @SerializedName("site") val site: String,
    @SerializedName("type") val type: String,
    @SerializedName("official") val official: Boolean
) {
    /**
     * URL completo do vídeo no YouTube para reprodução direta ou intent externa.
     * Construído dinamicamente a partir da chave devolvida pela API do TMDb.
     */
    val youtubeUrl: String
        get() = "https://www.youtube.com/watch?v=$key"
}

/**
 * Representa a resposta da API ao endpoint de reviews de um título.
 *
 * @param results Lista de reviews da página atual.
 * @param totalPages Total de páginas de reviews disponíveis para paginação.
 * @param totalResults Número total de reviews existentes no servidor para este título.
 */
data class TmdbReviewsResponse(
    @SerializedName("results") val results: List<TmdbReview>,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_results") val totalResults: Int
)

/**
 * Representa uma review de um filme ou série no TMDb.
 *
 * @param id Identificador único da review (em formato String da API).
 * @param author Nome visível do autor da crítica.
 * @param authorDetails Objeto com detalhes adicionais do autor (avatar e nota).
 * @param content Texto completo da review.
 * @param createdAt Data e hora de publicação original enviada pela API.
 */
data class TmdbReview(
    @SerializedName("id") val id: String,
    @SerializedName("author") val author: String,
    @SerializedName("author_details") val authorDetails: TmdbReviewAuthor,
    @SerializedName("content") val content: String,
    @SerializedName("created_at") val createdAt: String
) {
    /**
     * Data de criação formatada para apresentação amigável na UI (ex: "Janeiro 2024").
     */
    val formattedDate: String
        get() = try {
            val inputFormat = SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                Locale.getDefault()
            )
            val outputFormat = SimpleDateFormat(
                "MMMM yyyy",
                Locale("pt", "PT")
            )
            val date = inputFormat.parse(createdAt)
            if (date != null) outputFormat.format(date) else ""
        } catch (e: Exception) {
            ""
        }
}

/**
 * Representa os detalhes do autor de uma review.
 *
 * @param avatarPath Caminho do avatar do autor (pode ser null).
 * @param rating Classificação de 1 a 10 atribuída pelo autor ao título (pode ser null).
 */
data class TmdbReviewAuthor(
    @SerializedName("avatar_path") val avatarPath: String?,
    @SerializedName("rating") val rating: Double?
)