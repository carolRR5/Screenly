package dam_a51568.screenly.data.models

import com.google.gson.annotations.SerializedName

// Representa a resposta da API do TMDb. Contém a lista de resultados devolvidos pela pesquisa.
data class TmdbSearchResponse(
    @SerializedName("results") val results: List<TmdbMediaItem>
)

/**
 * Representa a resposta da API ao endpoint de créditos de um filme ou série.
 * Contém separadamente o elenco (atores) e a crew (equipa técnica).
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
     * A API pode devolver uma lista com vários valores (episódios de durações diferentes), por isso
     * utiliza-se o primeiro como referência. Assume 45 minutos se a lista estiver vazia.
     */
    val typicalEpisodeRuntime: Int
        get() = episodeRunTime?.firstOrNull() ?: 45 // 45min por defeito se não houver dados
}

/**
 * Representa um género associado a um filme ou série (ex: Ação, Comédia, Drama).
 * Partilhado entre TmdbMovieDetails e TmdbTvShowDetails.
 */
data class TmdbGenre(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

/**
 * Representa a resposta da API ao endpoint de géneros.
 * Contém a lista de géneros disponíveis para filmes ou séries.
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
 * @param official Indica se o vídeo é oficial.
 */
data class TmdbVideo(
    @SerializedName("key") val key: String,
    @SerializedName("site") val site: String,
    @SerializedName("type") val type: String,
    @SerializedName("official") val official: Boolean
) {
    /**
     * URL completo do vídeo no YouTube.
     * Construído a partir da chave devolvida pela API do TMDb.
     */
    val youtubeUrl: String
        get() = "https://www.youtube.com/watch?v=$key"
}