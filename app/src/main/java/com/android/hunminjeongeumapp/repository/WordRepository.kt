package com.android.hunminjeongeumapp.repository

import android.content.Context
import android.util.Log
import com.android.hunminjeongeumapp.api.ApiClient
import com.android.hunminjeongeumapp.api.ApiService
import com.android.hunminjeongeumapp.api.WordItem
import com.android.hunminjeongeumapp.api.SenseItem
import com.google.android.gms.common.util.JsonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder

class WordRepository(private val context: Context, private val apiKey: String) {
    private val apiService = ApiClient.retrofit.create(ApiService::class.java)

    suspend fun fetchWordData(): WordItem? {
        return withContext(Dispatchers.IO) {
            val randomWord = JsonUtils.getRandomWord(context) // 단어를 한 번만 가져옴
            Log.d("WordRepository", "선택된 단어: $randomWord")

            try {
                //같은 단어로 뜻 가져오기
                val definitionResponse = apiService.getDefinition(apiKey, randomWord)
                Log.d("WordRepository", "뜻 응답 원본: $definitionResponse")

                val wordItem = definitionResponse.items?.firstOrNull()
                if (wordItem == null) {
                    Log.e("WordRepository", "API에서 검색된 단어가 없음! (뜻 조회 실패)")
                    return@withContext null
                }

                // 첫 번째 뜻 가져오기
                val firstDefinition = wordItem.senses?.firstOrNull()?.definition ?: "뜻 없음"
                Log.d("WordRepository", "단어: ${wordItem.word}, 뜻: $firstDefinition")

                //같은 단어로 예문 가져오기
                val exampleResponse = apiService.getExample(apiKey, randomWord)
                Log.d("WordRepository", "예문 응답 원본: $exampleResponse")

                val exampleItem = exampleResponse.items?.firstOrNull()
                wordItem.example = exampleItem?.example ?: "예문 없음"
                Log.d("WordRepository", "예문: ${wordItem.example}")

                return@withContext wordItem.copy(senses = listOf(SenseItem(firstDefinition)))
            } catch (e: Exception) {
                Log.e("WordRepository", "API 요청 중 오류 발생: ${e.message}")
                return@withContext null
            }
        }
    }
}
