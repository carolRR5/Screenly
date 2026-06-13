package dam_a51568.screenly.data.repository

import com.google.firebase.auth.FirebaseUser
import dam_a51568.screenly.data.model.User

/**
 * Converte um [FirebaseUser] do Firebase Authentication para o modelo de domínio [User].
 *
 * @return Um objeto [User] com os dados do utilizador autenticado.
 */
fun FirebaseUser.toUser(): User {
    return User(
        uid = this.uid,
        email = this.email ?: "",
        displayName = this.displayName ?: "Utilizador",
        photoUrl = this.photoUrl?.toString()
    )
}