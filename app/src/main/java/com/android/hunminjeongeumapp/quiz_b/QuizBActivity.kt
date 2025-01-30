package com.android.hunminjeongeumapp.quiz_b

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.hunminjeongeumapp.R

class QuizBActivity : AppCompatActivity() {

    private lateinit var scoreTextView: TextView
    private lateinit var questionTextView: TextView
    private lateinit var item1Button: Button
    private lateinit var item2Button: Button
    private lateinit var resultImageView: ImageView

    private lateinit var dbManager: QuizBDBManager
    private lateinit var sqlitedb: SQLiteDatabase
    private var questions = mutableListOf<Question>()
    private var currentIndex = 0
    private var currentAttempts = 0
    private var score = 0
    private var isReattempted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_b)

        // UI 초기화
        scoreTextView = findViewById(R.id.score)
        questionTextView = findViewById(R.id.question)
        item1Button = findViewById(R.id.item1)
        item2Button = findViewById(R.id.item2)
        resultImageView = findViewById(R.id.resultImageView)

        // 데이터베이스 설정 및 문제 가져오기
        dbManager = QuizBDBManager(this, "quizB.db", null, 1)
        sqlitedb = dbManager.readableDatabase
        loadQuestionsFromDB()

        // 첫 번째 문제 설정
        loadQuestion()

        item1Button.setOnClickListener { checkAnswer(0) }
        item2Button.setOnClickListener { checkAnswer(1) }
    }

    // 데이터베이스에서 문제 로드
    private fun loadQuestionsFromDB() {
        val cursor = sqlitedb.rawQuery("SELECT * FROM questions ORDER BY RANDOM() LIMIT 3", null)

        while (cursor.moveToNext()) {
            val question = cursor.getString(cursor.getColumnIndexOrThrow("question"))
            val item1 = cursor.getString(cursor.getColumnIndexOrThrow("item1"))
            val item2 = cursor.getString(cursor.getColumnIndexOrThrow("item2"))
            val answer = cursor.getInt(cursor.getColumnIndexOrThrow("answer"))

            questions.add(Question(question, item1, item2, answer))
        }
        cursor.close()
        sqlitedb.close()
    }

    // 문제 표시
    private fun loadQuestion() {
        if (currentIndex < questions.size) {
            val currentQuestion = questions[currentIndex]
            questionTextView.text = currentQuestion.question
            item1Button.text = currentQuestion.item1
            item2Button.text = currentQuestion.item2
            currentAttempts = 0
            isReattempted = false
        } else {
            // 모든 문제를 다 풀었을 경우 랭킹 화면으로 이동
            val intent = Intent(this, RankingBActivity::class.java)
            intent.putExtra("score", score)
            startActivity(intent)
            finish()
        }
    }

    // 사용자의 정답을 확인하고 점수 계산
    private fun checkAnswer(selectedIndex: Int) {
        val correctIndex = questions[currentIndex].answer

        if (selectedIndex == correctIndex) {
            Toast.makeText(this, "정답", Toast.LENGTH_SHORT).show()
            val gainedScore = if (currentAttempts == 0) 10 else 10 / (currentAttempts + 1)
            score += gainedScore
            scoreTextView.text = score.toString()
            currentIndex++ // 다음 문제로 이동
            loadQuestion()
        } else {
            Toast.makeText(this, "오답", Toast.LENGTH_SHORT).show()
            isReattempted = true
            currentAttempts++
        }
    }
}

data class Question(
    val question: String,
    val item1: String,
    val item2: String,
    val answer: Int
)
