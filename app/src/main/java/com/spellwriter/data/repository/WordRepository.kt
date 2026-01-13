package com.spellwriter.data.repository

import com.spellwriter.data.models.Word
import java.util.Locale

object WordRepository {

    // German words (default)
    private val germanWords = mapOf(
        1 to Pair(
            // Star 1: 3-letter + 4-letter
            listOf("OHR", "ARM", "EIS", "HUT", "ZUG", "TAG", "TOR", "RAD", "ROT", "NUS"),
            listOf("BAUM", "HAUS", "BALL", "BUCH", "HUND", "MOND", "BROT", "KOPF", "NASE", "HAND")
        ),
        2 to Pair(
            // Star 2: 4-letter + 5-letter
            listOf("BEIN", "TIER", "SOFA", "HASE", "BERG", "EULE", "MAUS", "GRAS", "ROSE", "BOOT"),
            listOf("APFEL", "KATZE", "MILCH", "TISCH", "VOGEL", "FISCH", "PFERD", "STUHL", "WOLKE", "BLUME")
        ),
        3 to Pair(
            // Star 3: 5-letter + 6-letter
            listOf("BIRNE", "LAMPE", "BIENE", "TIGER", "SONNE", "STERN", "REGEN", "NACHT", "ZEBRA", "GABEL"),
            listOf("ORANGE", "BANANE", "GARTEN", "DRACHE", "BUTTER", "SCHULE", "KUCHEN", "FROSCH", "STRAND", "TOMATE")
        )
    )

    // English words
    private val englishWords = mapOf(
        1 to Pair(
            // Star 1: 3-letter + 4-letter
            listOf("CAT", "DOG", "SUN", "HAT", "BED", "CUP", "EGG", "PIG", "BUS", "BOX"),
            listOf("TREE", "FISH", "BIRD", "CAKE", "MOON", "BOOK", "FROG", "STAR", "DUCK", "BALL")
        ),
        2 to Pair(
            // Star 2: 4-letter + 5-letter
            listOf("BEAR", "DOOR", "MILK", "RAIN", "SHOE", "BOAT", "LION", "HAND", "NOSE", "LAMP"),
            listOf("APPLE", "HORSE", "HOUSE", "WATER", "CHAIR", "CLOUD", "BREAD", "TIGER", "PLANT", "TRAIN")
        ),
        3 to Pair(
            // Star 3: 5-letter + 6-letter
            listOf("SNAKE", "BEACH", "LEMON", "TRUCK", "QUEEN", "SHEEP", "SPOON", "DRESS", "MOUSE", "STORM"),
            listOf("RABBIT", "GARDEN", "ORANGE", "CHEESE", "FLOWER", "MONKEY", "BANANA", "PENCIL", "DRAGON", "CASTLE")
        )
    )

    fun getWordsForStar(star: Int, locale: Locale = Locale.getDefault()): List<String> {
        val words = if (locale.language == "en") englishWords else germanWords
        val (shortWords, longWords) = words[star] ?: return emptyList()
        return shortWords + longWords
    }

    fun getShortWordsForStar(star: Int, locale: Locale = Locale.getDefault()): List<String> {
        val words = if (locale.language == "en") englishWords else germanWords
        return words[star]?.first ?: emptyList()
    }

    fun getLongWordsForStar(star: Int, locale: Locale = Locale.getDefault()): List<String> {
        val words = if (locale.language == "en") englishWords else germanWords
        return words[star]?.second ?: emptyList()
    }

    fun getTTSLocale(locale: Locale = Locale.getDefault()): Locale {
        return if (locale.language == "en") Locale.US else Locale.GERMANY
    }
}
