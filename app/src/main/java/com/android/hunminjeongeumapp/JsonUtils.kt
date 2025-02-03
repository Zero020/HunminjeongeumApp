package com.android.hunminjeongeumapp

import android.content.Context
import org.json.JSONObject

object JsonUtils {
    fun getRandomWord(context: Context): String {
        val jsonString = context.assets.open("search_words.json").bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)
        val wordsArray = jsonObject.getJSONArray("words")

        val randomIndex = (0 until wordsArray.length()).random()
        return wordsArray.getString(randomIndex)
    }
}
