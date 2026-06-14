package dam_a51568.screenly.data.model

/**
 * Modelo de domínio que representa um membro da equipa técnica (crew)
 * associado a um filme ou série.
 *
 * Esta classe é utilizada para representar profissionais que não fazem
 * parte do elenco (atores), mas sim da equipa de produção, como:
 * realizadores, argumentistas, produtores, entre outros.
 *
 * Serve para simplificar os dados provenientes da API e adaptá-los
 * à camada de apresentação da aplicação.
 *
 * @property id Identificador único do membro da equipa técnica na base de dados.
 * @property name Nome do membro da equipa técnica.
 * @property job Função específica desempenhada na produção.
 * @property department Departamento geral ao qual pertence dentro da produção.
 * @property profileUrl URL da imagem de perfil do membro da equipa técnica (Pode ser null caso
 * o TMDb não forneça imagem associada)
 */
data class CrewMember(
    val id: Int,
    val name: String,
    val job: String,
    val department: String,
    val profileUrl: String?
)