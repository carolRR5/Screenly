package dam_a51568.screenly.data.remote

import dam_a51568.screenly.BuildConfig
import dam_a51568.screenly.data.remote.api.TmdbApiService
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
    // Define o URL base e imutável para todos os pedidos HTTP direcionados à versão 3 da API do TMDb
    private const val BASE_URL = "https://api.themoviedb.org/3/"

    /**
     * URL base para construir os endereços completos das imagens de póster.
     * O sufixo "w500" define a largura da imagem em píxeis.
     */
    const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"

    /**
     * Chave de autenticação da API do TMDb.
     * Lida de forma segura a partir do BuildConfig, que por sua vez a obtém do ficheiro local.properties.
     */
    const val API_KEY = BuildConfig.TMDB_API_KEY

    /**
     * Instância de "TmdbApiService" gerada pelo Retrofit.
     * Utiliza o delegado 'by lazy' para garantir uma inicialização segura em termos de concorrência (thread-safe)
     * apenas quando for chamada pela primeira vez no código.
     */
    val apiService: TmdbApiService by lazy {
        // Inicia o processo de configuração e construção do cliente HTTP do Retrofit
        Retrofit.Builder()
            // Define o ponto de partida (URL principal) para as rotas da API
            .baseUrl(BASE_URL)
            // Configura o motor do Gson para desserializar automaticamente as respostas JSON em objetos Kotlin
            .addConverterFactory(GsonConverterFactory.create())
            // Finaliza as configurações e cria a instância base do Retrofit
            .build()
            // Instancia e implementa em tempo de execução os métodos abstratos declarados na interface TmdbApiService
            .create(TmdbApiService::class.java)
    }
}
