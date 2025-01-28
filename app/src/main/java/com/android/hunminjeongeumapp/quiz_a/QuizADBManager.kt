package com.android.hunminjeongeumapp.quiz_a

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class QuizADBManager(
    context: Context?,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase?) {
        // questions 테이블 생성
        db!!.execSQL("CREATE TABLE IF NOT EXISTS questions (id INTEGER PRIMARY KEY, description TEXT, question TEXT, hint1 TEXT, hint2 TEXT, answer TEXT)")

        // rankings 테이블 생성
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS rankings (
                game_id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT DEFAULT '-',
                accuracy REAL DEFAULT 0.0,
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
        db.execSQL("INSERT INTO questions (description, question, hint1, hint2, answer) VALUES ('논쟁되는 핵심 문제','ㅈㅈ', '재ㅈ', '쟁ㅈ', '쟁점')")
        db.execSQL("INSERT INTO questions (description, question, hint1, hint2, answer) VALUES ('나쁜 일에 또 나쁜 일', 'ㅅㅅㄱㅅ', '서ㅅㄱ상', '서사ㄱ상', '설상가상')")
        db.execSQL("INSERT INTO questions (description, question, hint1, hint2, answer) VALUES ('국가 비상시 군사 통치', 'ㄱㅇ', 'ㄱ어', 'ㄱ엄', '계엄')")
        db.execSQL("INSERT INTO questions (description, question, hint1, hint2, answer) VALUES ('오늘을 의미함', 'ㄱㅇ', 'ㄱ이', '그이', '금일')")
        db.execSQL("INSERT INTO questions (description, question, hint1, hint2, answer) VALUES ('대수롭지 않게 넘김', 'ㄱㄱ', '가ㄱ', '간ㄱ', '간과')")
        db.execSQL("INSERT INTO questions (description, question, hint1, hint2, answer) VALUES ('권리로 요구함', 'ㅊㄱ', '처ㄱ', '청ㄱ', '청구')")
        db.execSQL("INSERT INTO questions (description, question, hint1, hint2, answer) VALUES ('삼일간의 기간', 'ㅅㅎ', 'ㅅ흐', '사흐', '사흘')")
        db.execSQL("INSERT INTO questions (description, question, hint1, hint2, answer) VALUES ('임시로 정한 제목', 'ㄱㅈ', '가ㅈ', '가저', '가제')")
        db.execSQL("INSERT INTO questions (description, question, hint1, hint2, answer) VALUES ('공직자 책임 추궁 절차', 'ㅌㅎ', '타ㅎ', '탄ㅎ', '탄핵')")
        db.execSQL("INSERT INTO questions (description, question, hint1, hint2, answer) VALUES ('나이를 높여 부름', 'ㅇㅅ', '여ㅅ', '연ㅅ', '연세')")


    }
}
