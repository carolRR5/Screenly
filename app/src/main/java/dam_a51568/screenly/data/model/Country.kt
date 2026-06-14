package dam_a51568.screenly.data.model

/**
 * Modelo de domínio que representa um país associado a conteúdos.
 *
 * Esta classe é utilizada na camada de apresentação para normalizar
 * a informação vinda da API, garantindo que cada país possui:
 * - um código ISO padronizado;
 * - um nome legível para o utilizador;
 * - uma representação visual da bandeira em formato emoji.
 *
 * Este modelo simplifica os dados da API e torna-os mais adequados
 * para utilização direta na interface.
 *
 * @property code Identificador único do país (ex: "PT", "US", "GB")
 * @property name Nome do país apresentado na interface do utilizador
 * @property flag // Representação visual da bandeira do país em formato emoji
 */
data class Country(
    val code: String, // ex: "PT", "US", "GB"
    val name: String, // ex: "Portugal", "United States"
    val flag: String // ex:  "🇵🇹", "🇬🇧"
)