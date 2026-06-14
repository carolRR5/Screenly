package dam_a51568.screenly.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import dam_a51568.screenly.data.model.User
import java.io.ByteArrayOutputStream
import androidx.core.graphics.scale

/**
 * Repositório responsável pela gestão de dados de utilizadores na Firestore.
 */
object UserRepository {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val usersCollection by lazy { firestore.collection("users") }

    /**
     * Guarda ou atualiza os dados de um utilizador na Firestore.
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

    /**
     * Converte os bytes de uma imagem para Base64, redimensionando-a primeiro
     * para garantir que não ultrapassa o limite de 1MB do Firestore.
     *
     * @param imageBytes Bytes da imagem original.
     * @return String Base64 da imagem redimensionada.
     */
    fun compressAndEncodeImage(imageBytes: ByteArray): String {
        // Descodifica a imagem original
        val originalBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

        // Redimensiona para no máximo 300x300 pixels mantendo a proporção
        val maxSize = 300
        val width = originalBitmap.width
        val height = originalBitmap.height
        val scale = minOf(maxSize.toFloat() / width, maxSize.toFloat() / height)
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        val resizedBitmap = originalBitmap.scale(newWidth, newHeight)

        // Comprime para JPEG com qualidade 80%
        val outputStream = ByteArrayOutputStream()
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)

        // Codifica em Base64
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    /**
     * Guarda a foto de perfil em Base64 no Firestore.
     *
     * @param uid UID do utilizador.
     * @param photoBase64 Foto de perfil codificada em Base64.
     * @param onSuccess Callback chamado em caso de sucesso.
     * @param onError Callback chamado em caso de erro.
     */
    fun updateProfilePhoto(
        uid: String,
        photoBase64: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        usersCollection.document(uid)
            .set(mapOf("photoBase64" to photoBase64), com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }
}