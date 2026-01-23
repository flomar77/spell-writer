package com.spellwriter.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spellwriter.data.repository.WordRepository
import com.spellwriter.data.repository.WordsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val context: Context): ViewModel() {
    private val _currentLanguage = MutableStateFlow(WordRepository.getSystemLanguage())
    val wordsRepository = WordsRepository(context)
    init {
        viewModelScope.launch {
            fetchNewWords()
        }
    }
    private suspend fun fetchNewWords() {
        wordsRepository.fetchAndCacheNewWords(_currentLanguage.value)
    }
}