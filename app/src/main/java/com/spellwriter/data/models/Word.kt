package com.spellwriter.data.models

data class Word(
    val text: String,
    val length: Int = text.length
)

data class WordList(
    val star: Int,
    val shortWords: List<Word>,
    val longWords: List<Word>
)
