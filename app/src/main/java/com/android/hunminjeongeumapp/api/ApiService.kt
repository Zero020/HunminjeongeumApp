package com.android.hunminjeongeumapp.api

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    //1. 뜻 가져오기 (기본 요청)
    @GET("search")
    suspend fun getDefinition(
        @Query("key") apiKey: String,
        @Query("q") query: String
    ): WordResponse

    // 2. 예문 가져오기 (part=exam 추가)
    @GET("search")
    suspend fun getExample(
        @Query("key") apiKey: String,
        @Query("q") query: String,
        @Query("part") part: String = "exam"
    ): WordResponse
}
