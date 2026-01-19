package com.spellwriter.data.network

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API service interface for Random Word API.
 * Fetches random words from https://random-word-api.herokuapp.com
 */
interface RandomWordApiService {
    /**
     * Fetch random words from the API.
     *
     * @param number Number of words to fetch
     * @param length Word length (3, 4, 5, or 6 letters)
     * @param lang Language code ("de" for German, "en" for English)
     * @return List of random words
     */
    @GET("/word")
    suspend fun getWords(
        @Query("number") number: Int,
        @Query("length") length: Int,
        @Query("lang") lang: String
    ): List<String>
}
