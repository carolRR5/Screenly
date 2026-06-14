package dam_a51568.screenly.data.model

/**
 * Enumeração que representa o estado de visualização de um conteúdo
 * na watchlist do utilizador.
 *
 * Este enum é utilizado para classificar cada item guardado na watchlist,
 * permitindo ao utilizador organizar os conteúdos consoante o seu progresso.
 *
 * Também facilita operações de filtragem e consulta na aplicação,
 * tanto na camada de dados como na interface.
 */
enum class WatchStatus {
    TO_WATCH,
    WATCHING,
    WATCHED
}