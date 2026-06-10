package dam_a51568.screenly.data.remote

import dam_a51568.screenly.data.models.TmdbCreditsResponse
import dam_a51568.screenly.data.models.TmdbGenreResponse
import dam_a51568.screenly.data.models.TmdbMovieDetails
import dam_a51568.screenly.data.models.TmdbReviewsResponse
import dam_a51568.screenly.data.models.TmdbSearchResponse
import dam_a51568.screenly.data.models.TmdbTvShowDetails
import dam_a51568.screenly.data.models.TmdbVideosResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interface Retrofit que define os endpoints da API do TMDb usados na aplicação.
 * O Retrofit gera automaticamente a implementação em tempo de execução.
 * Todos os métodos são suspend function para serem chamados dentro de coroutines.
 */
interface TmdbApiService {
    /**
     * Pesquisa filmes e séries em simultâneo pelo nome.
     *
     * @param query Texto introduzido pelo utilizador na barra de pesquisa.
     * @param apiKey Chave de autenticação da API do TMDb.
     * @param language Idioma dos resultados (por defeito português de Portugal).
     * @return Lista de resultados com filmes e séries correspondentes à pesquisa.
     */
    @GET("search/multi")
    suspend fun searchMulti(
        @Query("query") query: String,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "pt-PT"
    ): TmdbSearchResponse

    /**
     * Obtém os detalhes completos de um filme pelo seu ID do TMDb.
     *
     * @param id Identificador único do filme no TMDb.
     * @param apiKey Chave de autenticação da API do TMDb.
     * @param language Idioma dos dados devolvidos (por defeito português de Portugal).
     * @return Objeto com todos os detalhes do filme.
     */
    @GET("movie/{id}")
    suspend fun getMovieDetails(
        @Path("id") id: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "pt-PT"
    ): TmdbMovieDetails

    /**
     * Obtém os detalhes completos de uma série pelo seu ID do TMDb.
     *
     * @param id Identificador único da série no TMDb.
     * @param apiKey Chave de autenticação da API do TMDb.
     * @param language Idioma dos dados devolvidos (por defeito português de Portugal).
     * @return Objeto com todos os detalhes da série.
     */
    @GET("tv/{id}")
    suspend fun getTvShowDetails(
        @Path("id") id: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "pt-PT"
    ): TmdbTvShowDetails

    /**
     * Obtém os títulos em tendência da semana (filmes e séries).
     *
     * @param apiKey Chave de autenticação da API do TMDb.
     * @param language Idioma dos resultados (por defeito português de Portugal).
     * @return Lista de filmes e séries em tendência.
     */
    @GET("trending/all/week")
    suspend fun getTrending(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "pt-PT"
    ): TmdbSearchResponse

    /**
     * Obtém os filmes mais populares atualmente.
     * Endpoint: GET /movie/popular
     *
     * @param apiKey Chave de autenticação da API do TMDb.
     * @param language Idioma dos resultados (por defeito português de Portugal).
     * @return Lista de filmes populares.
     */
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "pt-PT"
    ): TmdbSearchResponse

    /**
     * Obtém as séries mais populares atualmente.
     * Endpoint: GET /tv/popular
     *
     * @param apiKey Chave de autenticação da API do TMDb.
     * @param language Idioma dos resultados (por defeito português de Portugal).
     * @return Lista de séries populares.
     */
    @GET("tv/popular")
    suspend fun getPopularTvShows(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "pt-PT"
    ): TmdbSearchResponse

    /**
     * Obtém o elenco e a crew de um filme.
     * Endpoint: GET /movie/{id}/credits
     *
     * @param id Identificador único do filme no TMDb.
     * @param apiKey Chave de autenticação da API do TMDb.
     * @param language Idioma dos resultados.
     * @return Objeto com listas de elenco e crew.
     */
    @GET("movie/{id}/credits")
    suspend fun getMovieCredits(
        @Path("id") id: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "pt-PT"
    ): TmdbCreditsResponse

    /**
     * Obtém o elenco e a crew de uma série.
     * Endpoint: GET /tv/{id}/credits
     *
     * @param id Identificador único da série no TMDb.
     * @param apiKey Chave de autenticação da API do TMDb.
     * @param language Idioma dos resultados.
     * @return Objeto com listas de elenco e crew.
     */
    @GET("tv/{id}/credits")
    suspend fun getTvCredits(
        @Path("id") id: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "pt-PT"
    ): TmdbCreditsResponse

    /**
     * Obtém os géneros disponíveis para filmes.
     * Endpoint: GET /genre/movie/list
     *
     * @param apiKey Chave de autenticação da API do TMDb.
     * @param language Idioma dos resultados.
     * @return Lista de géneros de filmes.
     */
    @GET("genre/movie/list")
    suspend fun getMovieGenres(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "pt-PT"
    ): TmdbGenreResponse

    /**
     * Obtém os géneros disponíveis para séries.
     * Endpoint: GET /genre/tv/list
     *
     * @param apiKey Chave de autenticação da API do TMDb.
     * @param language Idioma dos resultados.
     * @return Lista de géneros de séries.
     */
    @GET("genre/tv/list")
    suspend fun getTvGenres(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "pt-PT"
    ): TmdbGenreResponse

    /**
     * Descobre filmes com filtros avançados.
     * Endpoint: GET /discover/movie
     *
     * @param apiKey Chave de autenticação da API do TMDb.
     * @param language Idioma dos resultados.
     * @param sortBy Critério de ordenação (ex: "popularity.desc", "vote_average.desc").
     * @param withGenres ID do género pelo qual filtrar (opcional).
     * @param withOriginCountry Código do país pelo qual filtrar (opcional).
     * @param primaryReleaseDateGte Data mínima de lançamento (opcional).
     * @return Lista de filmes filtrados.
     */
    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "pt-PT",
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("with_genres") withGenres: String? = null,
        @Query("with_origin_country") withOriginCountry: String? = null,
        @Query("primary_release_date.gte") primaryReleaseDateGte: String? = null
    ): TmdbSearchResponse

    /**
     * Descobre séries com filtros avançados.
     * Endpoint: GET /discover/tv
     *
     * @param apiKey Chave de autenticação da API do TMDb.
     * @param language Idioma dos resultados.
     * @param sortBy Critério de ordenação.
     * @param withGenres ID do género pelo qual filtrar (opcional).
     * @param withOriginCountry Código do país pelo qual filtrar (opcional).
     * @param firstAirDateGte Data mínima de estreia (opcional).
     * @return Lista de séries filtradas.
     */
    @GET("discover/tv")
    suspend fun discoverTvShows(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "pt-PT",
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("with_genres") withGenres: String? = null,
        @Query("with_origin_country") withOriginCountry: String? = null,
        @Query("first_air_date.gte") firstAirDateGte: String? = null
    ): TmdbSearchResponse

    /**
     * Obtém os vídeos associados a um filme, incluindo trailers do YouTube.
     * Endpoint: GET /movie/{id}/videos
     *
     * @param id Identificador único do filme no TMDb.
     * @param apiKey Chave de autenticação da API do TMDb.
     * @param language Idioma dos resultados.
     * @return Lista de vídeos associados ao filme.
     */
    @GET("movie/{id}/videos")
    suspend fun getMovieVideos(
        @Path("id") id: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "pt-PT"
    ): TmdbVideosResponse

    /**
     * Obtém os vídeos associados a uma série, incluindo trailers do YouTube.
     * Endpoint: GET /tv/{id}/videos
     *
     * @param id Identificador único da série no TMDb.
     * @param apiKey Chave de autenticação da API do TMDb.
     * @param language Idioma dos resultados.
     * @return Lista de vídeos associados à série.
     */
    @GET("tv/{id}/videos")
    suspend fun getTvVideos(
        @Path("id") id: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "pt-PT"
    ): TmdbVideosResponse

    /**
     * Obtém as reviews de um filme paginadas.
     * Endpoint: GET /movie/{id}/reviews
     *
     * @param id Identificador único do filme no TMDb.
     * @param apiKey Chave de autenticação da API do TMDb.
     * @param language Idioma dos resultados.
     * @param page Número da página a carregar.
     * @return Lista paginada de reviews do filme.
     */
    @GET("movie/{id}/reviews")
    suspend fun getMovieReviews(
        @Path("id") id: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): TmdbReviewsResponse

    /**
     * Obtém as reviews de uma série paginadas.
     * Endpoint: GET /tv/{id}/reviews
     *
     * @param id Identificador único da série no TMDb.
     * @param apiKey Chave de autenticação da API do TMDb.
     * @param language Idioma dos resultados.
     * @param page Número da página a carregar.
     * @return Lista paginada de reviews da série.
     */
    @GET("tv/{id}/reviews")
    suspend fun getTvReviews(
        @Path("id") id: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): TmdbReviewsResponse
}
