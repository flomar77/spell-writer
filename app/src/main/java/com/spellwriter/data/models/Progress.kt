package com.spellwriter.data.models

const val MAX_STARS = 6

data class Progress(
    val stars: Int = 0
) {
    init {
        require(stars in 0..MAX_STARS) { "stars must be between 0 and $MAX_STARS, got: $stars" }
    }

    fun getCurrentStar(): Int = (stars + 1).coerceIn(1, MAX_STARS)

    fun isStarEarned(star: Int): Boolean {
        require(star in 1..MAX_STARS) { "star must be between 1 and $MAX_STARS, got: $star" }
        return star <= stars
    }

    fun earnStar(star: Int): Progress {
        require(star in 1..MAX_STARS) { "star must be between 1 and $MAX_STARS, got: $star" }
        require(!isStarEarned(star)) { "star $star already earned" }
        require(star == stars + 1) { "Must earn stars in order. Current: $stars, trying to earn: $star" }
        return copy(stars = stars + 1)
    }

    fun isComplete(): Boolean = stars == MAX_STARS
}
