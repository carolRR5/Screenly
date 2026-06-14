package dam_a51568.screenly.data.model

data class User(
    val uid: String = "",
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    // Foto de perfil codificada em Base64, guardada no Firestore.
    val photoBase64: String? = null
)