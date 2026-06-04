package dam_a51568.screenly.data.remote

import dam_a51568.screenly.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton responsável por configurar e fornecer a instância do cliente Retrofit para comunicação
 * com a API do TMDb.
 *
 * A instância de apiService é criada de forma lazy, ou seja, é inicializada apenas na primeira vez
 * que é acedida, evitando a criação desnecessária do cliente de rede caso nunca seja utilizado, e
 * reutilizada em todas as chamadas seguintes.
 */
object TmdbClient {
    // URL base de todos os endpoints da API do TMDb
    private const val BASE_URL = "https://api.themoviedb.org/3/"

    /**
     * URL base para construir os endereços completos das imagens de poster.
     * O sufixo "w500" define a largura da imagem em píxeis.
     */
    const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"

    /**
     * Chave de autenticação da API do TMDb.
     * Lida de forma segura a partir do BuildConfig, que por sua vez a obtém do ficheiro
     * local.properties.-
     */
    val API_KEY = BuildConfig.TMDB_API_KEY

    /**
     * Instância de "TmdbApiService" gerada pelo Retrofit.
     * Inicializada apenas na primeira utilização e partilhada por toda a aplicação.
     */
    val apiService: TmdbApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbApiService::class.java)
    }
}
