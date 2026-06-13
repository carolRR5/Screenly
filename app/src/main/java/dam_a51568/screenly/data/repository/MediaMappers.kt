package dam_a51568.screenly.data.repository

import dam_a51568.screenly.data.model.CastMember
import dam_a51568.screenly.data.model.Country
import dam_a51568.screenly.data.model.CrewMember
import dam_a51568.screenly.data.model.Genre
import dam_a51568.screenly.data.model.MediaItem
import dam_a51568.screenly.data.model.Movie
import dam_a51568.screenly.data.model.Review
import dam_a51568.screenly.data.model.TvShow
import dam_a51568.screenly.data.remote.TmdbClient
import dam_a51568.screenly.data.remote.dto.TmdbCastMember
import dam_a51568.screenly.data.remote.dto.TmdbCountry
import dam_a51568.screenly.data.remote.dto.TmdbCrewMember
import dam_a51568.screenly.data.remote.dto.TmdbGenre
import dam_a51568.screenly.data.remote.dto.TmdbMediaItem
import dam_a51568.screenly.data.remote.dto.TmdbMovieDetails
import dam_a51568.screenly.data.remote.dto.TmdbReview
import dam_a51568.screenly.data.remote.dto.TmdbTvShowDetails

fun TmdbMovieDetails.toMovie(): Movie {
    return Movie(
        id = this.id,
        title = this.title,
        posterUrl = "${TmdbClient.IMAGE_BASE_URL}${this.posterPath}",
        overview = this.overview ?: "",
        year = this.releaseDate?.take(4) ?: "N/A",
        rating = this.voteAverage ?: 0.0,
        genres = this.genres?.map { it.name } ?: emptyList(),
        runtime = this.runtime ?: 0
    )
}

fun TmdbTvShowDetails.toTvShow(): TvShow {
    return TvShow(
        id = this.id,
        title = this.name,
        posterUrl = "${TmdbClient.IMAGE_BASE_URL}${this.posterPath}",
        overview = this.overview ?: "",
        year = this.firstAirDate?.take(4) ?: "N/A",
        rating = this.voteAverage ?: 0.0,
        genres = this.genres?.map { it.name } ?: emptyList(),
        episodeRuntime = this.typicalEpisodeRuntime
    )
}

fun TmdbGenre.toGenre(): Genre {
    return Genre(
        id = this.id,
        name = this.name
    )
}

fun TmdbCountry.toCountry(): Country {
    return Country(
        code = this.code,
        name = this.nativeName.ifEmpty { this.englishName },
        flag = getFlagEmoji(this.code)
    )
}

/**
 * Converte um código de país ISO 3166-1 alpha-2 num emoji de bandeira.
 * Ex: "PT" -> "🇵🇹"
 */
private fun getFlagEmoji(countryCode: String): String {
    if (countryCode.length != 2) return "🏳️"
    val firstLetter = Character.codePointAt(countryCode.uppercase(), 0) - 0x41 + 0x1F1E6
    val secondLetter = Character.codePointAt(countryCode.uppercase(), 1) - 0x41 + 0x1F1E6
    return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
}

fun TmdbMediaItem.toMediaItem(): MediaItem {
    return MediaItem(
        id = this.id,
        mediaType = this.mediaType,
        title = this.displayTitle,
        posterUrl = "${TmdbClient.IMAGE_BASE_URL}${this.posterPath}",
        year = this.displayYear
    )
}

fun TmdbCastMember.toCastMember(): CastMember {
    return CastMember(
        id = this.id,
        name = this.name,
        character = this.character,
        profileUrl = this.profilePath?.let { "${TmdbClient.IMAGE_BASE_URL}$it" },
        order = this.order
    )
}

fun TmdbCrewMember.toCrewMember(): CrewMember {
    return CrewMember(
        id = this.id,
        name = this.name,
        job = this.job,
        department = this.department,
        profileUrl = this.profilePath?.let { "${TmdbClient.IMAGE_BASE_URL}$it" }
    )
}

fun TmdbReview.toReview(): Review {
    return Review(
        id = this.id,
        author = this.author,
        content = this.content,
        rating = this.authorDetails.rating,
        avatarUrl = this.authorDetails.avatarPath?.let { path ->
            if (path.startsWith("/https")) path.removePrefix("/")
            else "${TmdbClient.IMAGE_BASE_URL}$path"
        },
        formattedDate = this.formattedDate
    )
}