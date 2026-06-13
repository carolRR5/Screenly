package dam_a51568.screenly.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import dam_a51568.screenly.data.model.User

/**
 * Repositório responsável pela gestão de dados de utilizadores na Firestore.
 */
object UserRepository {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val usersCollection by lazy { firestore.collection("users") }

    /**
     * Guarda ou atualiza os dados de um utilizador na Firestore.
     * O ID do documento será o UID do utilizador.
     *
     * @param user Objeto User com os dados a guardar.
     * @return Uma [Task] que representa a operação assíncrona.
     */
    fun saveUser(user: User): Task<Void> {
        return usersCollection.document(user.uid).set(user)
    }

    /**
     * Obtém os dados de um utilizador em tempo real.
     *
     * @param uid UID do utilizador a pesquisar.
     * @param onResult Callback que devolve o objeto [User] ou null se não existir.
     */
    fun getUser(uid: String, onResult: (User?) -> Unit) {
        usersCollection.document(uid).addSnapshotListener { snapshot, _ ->
            val user = snapshot?.toObject(User::class.java)
            onResult(user)
        }
    }
}
