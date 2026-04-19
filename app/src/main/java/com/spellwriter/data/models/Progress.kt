package com.spellwriter.data.models

data class Progress(
    val stars: Int = 0
) {
    init {
        require(stars in 0..3) { "stars must be between 0 and 3, got: $stars" }
    }

    fun getCurrentStar(): Int = (stars + 1).coerceIn(1, 3)

    fun isStarEarned(star: Int): Boolean {
        require(star in 1..3) { "star must be between 1 and 3, got: $star" }
        return star <= stars
    }

    fun earnStar(star: Int): Progress {
        require(star in 1..3) { "star must be between 1 and 3, got: $star" }
        require(!isStarEarned(star)) { "star $star already earned" }
        require(star == stars + 1) { "Must earn stars in order. Current: $stars, trying to earn: $star" }
        return copy(stars = stars + 1)
    }

    fun isComplete(): Boolean = stars == 3
}
