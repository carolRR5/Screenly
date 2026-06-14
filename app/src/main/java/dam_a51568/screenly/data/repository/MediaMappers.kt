package dam_a51568.screenly.data.repository

import dam_a51568.screenly.data.model.CastMember
import dam_a51568.screenly.data.model.Country
import dam_a51568.screenly.data.model.CrewMember
import dam_a51568.screenly.data.model.Genre
import dam_a51568.screenly.data.model.MediaItem
import dam_a51568.screenly.data.model.Movie
import dam_a51568.screenly.data.model.Review
import dam_a51568.screenly.data.model.TvShow
import dam_a51568.screenly.data.remote.TmdbClient
import dam_a51568.screenly.data.remote.dto.TmdbCastMember
import dam_a51568.screenly.data.remote.dto.TmdbCountry
import dam_a51568.screenly.data.remote.dto.TmdbCrewMember
import dam_a51568.screenly.data.remote.dto.TmdbGenre
import dam_a51568.screenly.data.remote.dto.TmdbMediaItem
import dam_a51568.screenly.data.remote.dto.TmdbMovieDetails
import dam_a51568.screenly.data.remote.dto.TmdbReview
import dam_a51568.screenly.data.remote.dto.TmdbTvShowDetails

/**
 * Responsável pela conversão dos objetos recebidos da API para os modelos de domínio utilizados pela aplicação
 *
 * Estas funções de extensão atuam como uma camada de transformação entre a camada de dados remotos (DTOs) e
 * a camada de domínio, garantindo que os dados provenientes da APi são adaptados ao formato esperado
 * pelo resto da aplicação.
 *
 * Desta forma, a lógica de transformação fica centralizada num único local, promovendo reutilização de código,
 * manutenção simplificada e separação de responsabilidades entre as diferentes camadas da arquitetura da aplicação.
 */

/**
 * Converte os detalhes de um filme vindos da API [TmdbMovieDetails] para o modelo de domínio [Movie].
 *
 * @return Um objeto [Movie] formatado para a camada de apresentação.
 */
fun TmdbMovieDetails.toMovie(): Movie {
    // Instancia e devolve o modelo de domínio focado em Filmes
    return Movie(
        // Copia o identificador numérico direto do filme
        id = this.id,
        // Copia o título oficial
        title = this.title,
        // Concatena o URL base de imagens do TMDb com o caminho relativo do póster recebido
        posterUrl = "${TmdbClient.IMAGE_BASE_URL}${this.posterPath}",
        // Atribui a sinopse. Caso seja nula na API, converte para uma string vazia de segurança
        overview = this.overview ?: "",
        // Extrai os primeiros 4 caracteres da data (ano). Se for nula, assume "N/A"
        year = this.releaseDate?.take(4) ?: "N/A",
        // Atribui a nota média dos utilizadores. Se não houver avaliações, assume 0.0
        rating = this.voteAverage ?: 0.0,
        // Mapeia a lista de objetos de género extraindo apenas os seus nomes textuais (ex: "Ação")
        genres = this.genres?.map { it.name } ?: emptyList(),
        // Atribui o tempo de duração em minutos. Assume 0 se a informação não estiver disponível
        runtime = this.runtime ?: 0
    )
}

/**
 * Converte os detalhes de uma série vindos da API [TmdbTvShowDetails] para o modelo de domínio [TvShow].
 *
 * @return Um objeto [TvShow] formatado para a camada de apresentação.
 */
fun TmdbTvShowDetails.toTvShow(): TvShow {
    // Instancia e devolve o modelo de domínio focado em Séries de TV
    return TvShow(
        // Copia o identificador numérico direto da série
        id = this.id,
        // Mapeia o campo 'name' da API para a propriedade comum 'title' do domínio
        title = this.name,
        // Concatena o URL base de imagens com o caminho relativo do póster
        posterUrl = "${TmdbClient.IMAGE_BASE_URL}${this.posterPath}",
        // Garante uma string vazia caso a sinopse venha nula do servidor
        overview = this.overview ?: "",
        // Extrai de forma segura o ano de estreia através dos primeiros 4 dígitos da string de data
        year = this.firstAirDate?.take(4) ?: "N/A",
        // Mapeia a nota dos utilizadores com salvaguarda para zero
        rating = this.voteAverage ?: 0.0,
        // Transforma a lista de objetos TmdbGenre numa lista simples de Strings com os nomes dos géneros
        genres = this.genres?.map { it.name } ?: emptyList(),
        // Obtém a duração calculada pelo método utilitário customizado que criámos no DTO
        episodeRuntime = this.typicalEpisodeRuntime
    )
}

/**
 * Converte um género isolado da API [TmdbGenre] para o modelo de domínio [Genre].
 *
 * @return Um objeto [Genre] mapeado.
 */
fun TmdbGenre.toGenre(): Genre {
    return Genre(
        id = this.id, // Copia o ID único do género do TMDb
        name = this.name // Copia o nome traduzido do género
    )
}

/**
 * Converte um país da API [TmdbCountry] para o modelo de domínio [Country],
 * injetando dinamicamente a bandeira em formato emoji.
 *
 * @return Um objeto [Country] contendo o código, nome localizado e o emoji da bandeira.
 */
fun TmdbCountry.toCountry(): Country {
    return Country(
        // Mantém o código padrão ISO (ex: "PT")
        code = this.code,
        // Dá prioridade ao nome nativo do país. Se estiver vazio no servidor, usa o nome em inglês
        name = this.nativeName.ifEmpty { this.englishName },
        // Invoca a função utilitária local para gerar o emoji gráfico baseado nas duas letras do código
        flag = getFlagEmoji(this.code)
    )
}

/**
 * Converte um código de país ISO 3166-1 alpha-2 num emoji de bandeira real através de matemática Unicode.
 * Ex: "PT" -> "🇵🇹"
 *
 * @param countryCode O código de duas letras identificador do país (ex: "US", "FR", "PT").
 * @return Uma String contendo o emoji correspondente ou uma bandeira branca em caso de código inválido.
 */
private fun getFlagEmoji(countryCode: String): String {
    // Validação. Se não tiver exatamente 2 letras, devolve uma bandeira branca neutra
    if (countryCode.length != 2) return "🏳️"

    // Calcula o code point Unicode para a primeira letra do indicador regional
    val firstLetter = Character.codePointAt(countryCode.uppercase(), 0) - 0x41 + 0x1F1E6

    // Calcula o code point Unicode para a segunda letra do indicador regional
    val secondLetter = Character.codePointAt(countryCode.uppercase(), 1) - 0x41 + 0x1F1E6

    // Converte os code points calculados nos respetivos caracteres e junta-os para formar o emoji final
    return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
}

/**
 * Converte um item genérico de pesquisa da API [TmdbMediaItem] para o modelo polimórfico [MediaItem].
 * Usado para alimentar as listas de resultados mistos (pesquisa global).
 *
 * @return Um objeto [MediaItem] simplificado para listagens rápidas.
 */
fun TmdbMediaItem.toMediaItem(): MediaItem {
    return MediaItem(
        id = this.id, // Transpõe o ID do conteúdo
        mediaType = this.mediaType, // Identifica se é "movie" ou "tv"
        title = this.displayTitle, // Usa a propriedade customizada que resolve o conflito title/name
        posterUrl = "${TmdbClient.IMAGE_BASE_URL}${this.posterPath}", // Cria o link absoluto da imagem
        year = this.displayYear // Usa a propriedade customizada que resolve o ano
    )
}

/**
 * Converte um membro do elenco da API [TmdbCastMember] para o modelo de domínio [CastMember].
 *
 * @receiver O membro do elenco mapeado a partir do endpoint de créditos.
 * @return Um objeto [CastMember] com caminhos de imagem completos.
 */
fun TmdbCastMember.toCastMember(): CastMember {
    return CastMember(
        id = this.id, // Identificador do ator/atriz
        name = this.name, // Nome do ator/atriz
        character = this.character, // Nome da personagem interpretada
        // Se houver uma foto de perfil, gera o link completo. Caso contrário, devolve null
        profileUrl = this.profilePath?.let { "${TmdbClient.IMAGE_BASE_URL}$it" },
        order = this.order // Mantém a ordem de relevância no ecrã
    )
}

/**
 * Converte um membro da equipa técnica da API [TmdbCrewMember] para o modelo de domínio [CrewMember].
 *
 * @return Um objeto [CrewMember] formatado.
 */
fun TmdbCrewMember.toCrewMember(): CrewMember {
    return CrewMember(
        id = this.id, // Identificador do técnico
        name = this.name, // Nome do técnico
        job = this.job, // Cargo específico (ex: "Director", "Screenplay")
        department = this.department, // Departamento geral (ex: "Directing", "Writing")
        // Resolve o URL completo da foto de perfil recorrendo ao bloco let seguro para nulos
        profileUrl = this.profilePath?.let { "${TmdbClient.IMAGE_BASE_URL}$it" }
    )
}

/**
 * Converte uma crítica da API [TmdbReview] para o modelo de domínio [Review].
 * Trata anomalias conhecidas nos URLs de avatares enviados por utilizadores externos no TMDb.
 *
 * @return Um objeto [Review] com a data localizada e avatares validados.
 */
fun TmdbReview.toReview(): Review {
    return Review(
        id = this.id, // Identificador único da crítica
        author = this.author, // Autor do comentário
        content = this.content, // Texto corrido da review
        rating = this.authorDetails.rating, // Nota atribuída pelo autor de 1 a 10
        // Bloco corretor de URLs: Alguns avatares mais antigos do TMDb já vêm com link externo absoluto,
        // mas começam erradamente com uma barra invertida
        avatarUrl = this.authorDetails.avatarPath?.let { path ->
            if (path.startsWith("/https")) path.removePrefix("/") // Remove a barra se o URL já for absoluto
            else "${TmdbClient.IMAGE_BASE_URL}$path" // Caso contrário, cria o URL padrão da nossa CDN do TMDb
        },
        formattedDate = this.formattedDate // Transpõe a string de data que já foi traduzida pelo DTO
    )
}