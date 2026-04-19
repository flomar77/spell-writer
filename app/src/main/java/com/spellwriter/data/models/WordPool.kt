package com.spellwriter.data.models

/**
 * Static word pool for spell-writing game.
 * Provides word lists organized by star level and language.
 *
 * Letter progression per star:
 * - Star 1: 3–4 letters
 * - Star 2: 4–5 letters
 * - Star 3: 5–6 letters
 * - Star 4: 6–7 letters
 * - Star 5: 7–8 letters
 * - Star 6: 8–9 letters
 */
object WordPool {
    // German word lists
    private val germanStar1 = listOf(
        // 3-letter words (5)
        "ARM", "BAD", "BUS", "EIS", "HOF",
        // 4-letter words (5)
        "HAUS", "BAUM", "HUND", "BUCH", "GRAS"
    )

    private val germanStar2 = listOf(
        // 4-letter words (5)
        "BERG", "DACH", "GELD", "KERN", "LOCH",
        // 5-letter words (5)
        "TISCH", "STUHL", "LAMPE", "BLUME", "KATZE"
    )

    private val germanStar3 = listOf(
        // 5-letter words (5)
        "SCHAF", "PFERD", "VOGEL", "FUCHS", "REGEN",
        // 6-letter words (5)
        "SCHULE", "GARTEN", "MUTTER", "BUTTER", "KUCHEN"
    )

    private val germanStar4 = listOf(
        // 6-letter words (5)
        "KINDER", "SOMMER", "WINTER", "ABENDS", "STUNDE",
        // 7-letter words (5)
        "FENSTER", "SCHRANK", "DRUCKER", "STEMPEL", "WOHNUNG"
    )

    private val germanStar5 = listOf(
        // 7-letter words (5)
        "STIEFEL", "GEDICHT", "KNOCHEN", "SCHWANZ", "WUERFEL",
        // 8-letter words (5)
        "ABENDROT", "EISVOGEL", "FLUGZEUG", "SCHULBUS", "ZEITPLAN"
    )

    private val germanStar6 = listOf(
        // 8-letter words (5)
        "ERDBEERE", "GEBAEUDE", "KALENDER", "NAECHSTE", "SCHLANGE",
        // 9-letter words (5)
        "BUECHEREI", "FEUERWEHR", "HANDSCHUH", "SPIELZEUG", "STADTPLAN"
    )

    // English word lists
    private val englishStar1 = listOf(
        // 3-letter words (5)
        "ANT", "BEE", "CAT", "DOG", "EGG",
        // 4-letter words (5)
        "BIRD", "FISH", "FROG", "BEAR", "WOLF"
    )

    private val englishStar2 = listOf(
        // 4-letter words (5)
        "BOAT", "CAGE", "DRUM", "KITE", "LAMP",
        // 5-letter words (5)
        "CHAIR", "TABLE", "PLANT", "CLOCK", "BRUSH"
    )

    private val englishStar3 = listOf(
        // 5-letter words (5)
        "TIGER", "EAGLE", "SNAKE", "SHEEP", "HORSE",
        // 6-letter words (5)
        "SCHOOL", "GARDEN", "BUTTER", "FINGER", "WINTER"
    )

    private val englishStar4 = listOf(
        // 6-letter words (5)
        "CASTLE", "BRIDGE", "FLOWER", "SILVER", "FOREST",
        // 7-letter words (5)
        "CHICKEN", "WHISPER", "LANTERN", "BLANKET", "JOURNEY"
    )

    private val englishStar5 = listOf(
        // 7-letter words (5)
        "BROTHER", "CAPTAIN", "DIAMOND", "EVENING", "SILENCE",
        // 8-letter words (5)
        "BACKPACK", "CALENDAR", "DAUGHTER", "ELEPHANT", "FOOTBALL"
    )

    private val englishStar6 = listOf(
        // 8-letter words (5)
        "BIRTHDAY", "CHEMICAL", "FOUNTAIN", "GRATEFUL", "HOSPITAL",
        // 9-letter words (5)
        "ADVENTURE", "BUTTERFLY", "CHOCOLATE", "DANGEROUS", "EDUCATION"
    )

    init {
        validateWordPool()
    }

    fun validateWordPool() {
        val g = GameConstants.WORDS_PER_DIFFICULTY_GROUP
        validateWordList("German Star 1",   germanStar1,  3 to g, 4 to g)
        validateWordList("German Star 2",   germanStar2,  4 to g, 5 to g)
        validateWordList("German Star 3",   germanStar3,  5 to g, 6 to g)
        validateWordList("German Star 4",   germanStar4,  6 to g, 7 to g)
        validateWordList("German Star 5",   germanStar5,  7 to g, 8 to g)
        validateWordList("German Star 6",   germanStar6,  8 to g, 9 to g)
        validateWordList("English Star 1",  englishStar1, 3 to g, 4 to g)
        validateWordList("English Star 2",  englishStar2, 4 to g, 5 to g)
        validateWordList("English Star 3",  englishStar3, 5 to g, 6 to g)
        validateWordList("English Star 4",  englishStar4, 6 to g, 7 to g)
        validateWordList("English Star 5",  englishStar5, 7 to g, 8 to g)
        validateWordList("English Star 6",  englishStar6, 8 to g, 9 to g)
    }

    private fun validateWordList(
        name: String,
        words: List<String>,
        shortRequirement: Pair<Int, Int>,
        longRequirement: Pair<Int, Int>
    ) {
        val (shortLength, shortCount) = shortRequirement
        val (longLength, longCount) = longRequirement

        val shortWords = words.filter { it.length == shortLength }
        val longWords = words.filter { it.length == longLength }

        check(words.size >= shortCount + longCount) {
            "$name: Expected ${shortCount + longCount} words, got ${words.size}"
        }
        check(shortWords.size >= shortCount) {
            "$name: Expected $shortCount $shortLength-letter words, got ${shortWords.size}"
        }
        check(longWords.size >= longCount) {
            "$name: Expected $longCount $longLength-letter words, got ${longWords.size}"
        }
        check(words.all { it == it.uppercase() }) {
            "$name: All words must be uppercase"
        }
    }

    fun getWordsForStar(starNumber: Int, language: String): List<String> {
        val lang = if (language.startsWith("de")) "de" else "en"
        return shuffleByLength(getStaticWords(starNumber, lang))
    }

    fun getStaticWords(starNumber: Int, lang: String): List<String> {
        return when (lang) {
            "de" -> when (starNumber) {
                1 -> germanStar1
                2 -> germanStar2
                3 -> germanStar3
                4 -> germanStar4
                5 -> germanStar5
                6 -> germanStar6
                else -> germanStar1
            }
            else -> when (starNumber) {
                1 -> englishStar1
                2 -> englishStar2
                3 -> englishStar3
                4 -> englishStar4
                5 -> englishStar5
                6 -> englishStar6
                else -> englishStar1
            }
        }
    }

    fun shuffleByLength(words: List<String>): List<String> {
        return words
            .groupBy { it.length }
            .toSortedMap()
            .flatMap { (_, wordGroup) ->
                wordGroup.shuffled().take(GameConstants.WORDS_PER_DIFFICULTY_GROUP)
            }
    }
}
