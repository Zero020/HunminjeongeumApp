package com.android.hunminjeongeumapp.quiz_b

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class QuizBDBManager(
    context: Context?,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase?) {
        // questions 테이블 생성
        db!!.execSQL("CREATE TABLE IF NOT EXISTS questions (id INTEGER PRIMARY KEY, question TEXT, item1 TEXT, item2 TEXT, answer INTEGER)")

        // rankings 테이블 생성
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS rankings (
                game_id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT DEFAULT '-',
                score DOUBLE DEFAULT 0.0,
                time_taken INTEGER DEFAULT 0,
                is_registered BOOLEAN DEFAULT 0,
                incorrect_words TEXT DEFAULT ''
            )
        """)

        // 데이터 삽입
        insertInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS questions")
        db?.execSQL("DROP TABLE IF EXISTS rankings")
        onCreate(db)
    }

    // 초기 데이터 삽입
    private fun insertInitialData(db: SQLiteDatabase) {
        db.execSQL("INSERT INTO questions (question, item1, item2, answer) VALUES ('늑골','갈비뼈','구멍', 0)")
        db.execSQL("INSERT INTO questions (question, item1, item2, answer) VALUES ('침잠','침범','몰입', 1)")
        db.execSQL("INSERT INTO questions (question, item1, item2, answer) VALUES ('두루미','학','왜가리', 0)")
    }
}
