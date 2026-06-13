package dam_a51568.screenly.data.model

data class Review(
    val id: String,
    val author: String,
    val content: String,
    val rating: Double?,
    val avatarUrl: String?,
    val formattedDate: String
)