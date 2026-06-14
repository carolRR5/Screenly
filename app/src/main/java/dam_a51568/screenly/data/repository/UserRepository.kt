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
 * Repositório responsável pela persistência e recuperação de dados de utilizadores
 * na base de dados Firebase Firestore.
 *
 * Esta camada abstrai o acesso direto à Firestore, centralizando operações
 * relacionadas com perfis de utilizador, tais como:
 * - Criação e atualização de documentos de utilizador;
 * - Consulta de dados em tempo real através de listeners;
 * - Armazenamento de fotografias de perfil;
 * - Compressão e codificação de imagens para formatos compatíveis com a Firestore.
 *
 * A utilização do padrão Repository permite separar a lógica de acesso aos dados
 * da restante lógica da aplicação, promovendo reutilização, manutenção e testabilidade.
 */
object UserRepository {
    // Instância única da Firestore criada apenas quando necessária
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    // Referência para a coleção users onde são armazenados documentos dos utilizadores
    private val usersCollection by lazy { firestore.collection("users") }

    /**
     * Cria ou atualiza o documento correspondente a um utilizador na Firestore.
     *
     * Caso o documento já exista, os seus campos serão substituídos pelos
     * valores presentes no objeto [user]. Caso contrário, será criado um novo
     * documento utilizando o UID como identificador.
     *
     * @param user Modelo de domínio contendo todos os dados do utilizador
     * @return Uma [Task] que representa a operação assíncrona de escrita.
     */
    fun saveUser(user: User): Task<Void> {
        return usersCollection.document(user.uid).set(user)
    }

    /**
     * Obtém os dados de um utilizador e mantém a sincronização em tempo real
     * com a Firestore através de um Snapshot Listener.
     *
     * Sempre que o documento for alterado na base de dados, o callback
     * [onResult] será automaticamente invocado com os novos dados.
     *
     * @param uid Identificador único do utilizador.
     * @param onResult Função chamada quando existem novos dados disponíveis.
     */
    fun getUser(uid: String, onResult: (User?) -> Unit) {
        usersCollection.document(uid).addSnapshotListener { snapshot, _ ->
            // Converte automaticamente o documento Firestore para um objeto User
            val user = snapshot?.toObject(User::class.java)
            // Devolve o resultado ao componente que efetuou o pedido
            onResult(user)
        }
    }

    /**
     * Redimensiona, comprime e converte uma imagem para Base64.
     *
     * Esta operação é necessária porque a Firestore possui limites de tamanho para documentos
     * (aproximadamente 1 MB). Ao reduzir a resolução da imagem e aplicar compressão JPEG,
     * minimiza-se o espaço ocupado sem comprometer significativamente a qualidade visual.
     *
     * Fluxo de processamento:
     * 1. Conversão dos bytes para Bitmap;
     * 2. Redimensionamento proporcional;
     * 3. Compressão JPEG;
     * 4. Codificação Base64 para armazenamento textual.
     *
     * @param imageBytes Conteúdo binário da imagem original.
     * @return String Base64 pronta para armazenamento na Firestore.
     */
    fun compressAndEncodeImage(imageBytes: ByteArray): String {
        // Converte o array de bytes recebido numa imagem Bitmap manipulável
        val originalBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

        // Define a dimensão máxima permitida para largura ou altura
        val maxSize = 300

        // Obtém as dimensões originais da imagem
        val width = originalBitmap.width
        val height = originalBitmap.height

        // Calcula o fator de escala mantendo a proporção original
        val scale = minOf(maxSize.toFloat() / width, maxSize.toFloat() / height)

        // Calcula as novas dimensões após redimensionamento
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        // Cria uma nova imagem redimensionada sem distorcer o conteúdo
        val resizedBitmap = originalBitmap.scale(newWidth, newHeight)

        // Cria um buffer de memória onde será armazenada a imagem comprimida
        val outputStream = ByteArrayOutputStream()

        // Comprime a imagem para JPEG com qualidade de 80%
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)

        // Converte os bytes comprimidos para Base64 para armazenamento textual
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }

    /**
     * Guarda a foto de perfil em Base64 no Firestore.
     *
     * É utilizada a opção Merge para garantir que apenas o campo
     * "photoBase64" é atualizado, preservando todos os restantes campos
     * existentes no documento do utilizador.
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
            // Atualiza apenas o campo da fotografia mantendo o resto do documento
            .set(
                mapOf("photoBase64" to photoBase64),
                com.google.firebase.firestore.SetOptions.merge()
            )
            // Executado quando a operação termina com sucesso
            .addOnSuccessListener { onSuccess() }

            // Executado quando ocorre algum erro durante a escrita
            .addOnFailureListener { onError(it) }
    }
}