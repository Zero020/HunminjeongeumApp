package com.android.hunminjeongeumapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class QuizADBManager (context: Context?,
                      name: String?,
                      factory: SQLiteDatabase.CursorFactory?,
                      version: Int
) : SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase?) {

        db!!.execSQL("CREATE TABLE questions (id INTEGER, description text, question text, hint1 text, hint2 text, answer text)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }
}