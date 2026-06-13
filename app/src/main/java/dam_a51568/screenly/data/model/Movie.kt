package dam_a51568.screenly.data.model

data class Movie(
    val id: Int,
    val title: String,
    val posterUrl: String,
    val overview: String,
    val year: String,
    val rating: Double,
    val genres: List<String>,
    val runtime: Int
)