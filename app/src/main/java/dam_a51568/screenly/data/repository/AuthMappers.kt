package dam_a51568.screenly.data.repository

import com.google.firebase.auth.FirebaseUser
import dam_a51568.screenly.data.model.User

/**
 * Converte um [FirebaseUser] do Firebase Authentication para o modelo de domínio [User].
 * Esta função de extensão serve como um Mapper, desacoplando o resto da aplicação
 * das dependências diretas do SDK do Firebase.
 *
 * @return Um objeto [User] com os dados do utilizador autenticado.
 */
fun FirebaseUser.toUser(): User {
    // Instancia e devolve o modelo de dados User
    return User(
        // Mapeia o Identificador Único Universal (UID) gerado pelo Firebase
        uid = this.uid,
        // Mapeia o endereço de e-mail associado à conta do utilizador
        email = this.email,
        // Mapeia o nome do utilizador; se for nulo (ex: login por e-mail sem nome definido),
        // aplica o valor de salvaguarda (fallback) "Utilizador"
        displayName = this.displayName ?: "Utilizador",
        // Converte o objeto URI do Firebase para String (ou devolve null se o utilizador não tiver foto)
        photoUrl = this.photoUrl?.toString()
    )
}