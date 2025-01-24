package com.android.hunminjeongeumapp

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.widget.Chronometer
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class QuizAActivity : AppCompatActivity() {

    lateinit var sqlitedb: SQLiteDatabase

    lateinit var question: TextView
    lateinit var description: TextView
    lateinit var answer: EditText

    lateinit var str_ques: String
    lateinit var str_desc: String

    lateinit var timer: Chronometer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_a)

        question = findViewById(R.id.question)
        description = findViewById(R.id.description)
        answer = findViewById(R.id.answer)

        timer = findViewById(R.id.timer) // 타이머 사용
        timer.start()

        // 퀴즈 문제들 저장해놓은 DB 파일을 읽기 전용으로 열기
        sqlitedb = openOrCreateDatabase("quizA.db", MODE_PRIVATE, null)

        var cursor: Cursor? = null
            //랜덤으로 문제출제
        cursor = sqlitedb.rawQuery("SELECT * FROM questions ORDER BY RANDOM() LIMIT 1", null)

        if (cursor != null && cursor.moveToNext()) {
            str_ques = cursor.getString(cursor.getColumnIndex("question"))
            str_desc = cursor.getString(cursor.getColumnIndex("description"))
        }

        cursor?.close()
        sqlitedb.close()  // DB 연결 종료

        question.text = str_ques
        description.text = str_desc
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.stop()  // 타이머 종료
    }
}

