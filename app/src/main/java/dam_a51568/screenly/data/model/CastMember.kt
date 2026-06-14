package dam_a51568.screenly.data.model

/**
 * Modelo de domínio que representa um membro do elenco (ator ou atriz) associado a um filme ou série.
 *
 * Esta classe é utilizada pela camada de apresentação para exibir informações sobre os participantes
 * de uma produção audiovisual, incluindo o nome do ator, a personagem interpretada e a respetiva
 * fotografia de perfil.
 *
 * Por ser uma data class, o Kotlin gera automaticamente métodos úteis
 * como equals(), hashCode(), toString() e copy().
 *
 * @property id Identificador único do ator/atriz fornecido pelo TMDb.
 * @property name Nome do membro do elenco.
 * @property character Nome da personagem interpretada na produção.
 * @property profileUrl URL completa da fotografia de perfil do ator/atriz.
 * Pode ser nula caso não exista imagem disponível.
 * @property order Posição de destaque do ator no elenco. Valores menores
 * representam normalmente maior relevância nos créditos.
 */
data class CastMember(
    val id: Int,
    val name: String,
    val character: String,
    val profileUrl: String?,
    val order: Int
)