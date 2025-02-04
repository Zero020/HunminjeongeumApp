package com.android.hunminjeongeumapp.quiz_b

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.android.hunminjeongeumapp.R
import kotlinx.coroutines.*

class QuizBActivity : AppCompatActivity() {

    private lateinit var scoreTextView: TextView
    private lateinit var questionTextView: TextView
    private lateinit var item1Button: Button
    private lateinit var item2Button: Button
    private lateinit var resultImageView: ImageView
    private lateinit var resultImageView2: ImageView
    private lateinit var king_smile: ImageView
    private lateinit var king_angry: ImageView

    private lateinit var dbManager: QuizBDBManager
    private lateinit var sqlitedb: SQLiteDatabase
    private var questions = mutableListOf<Question>()
    private var currentIndex = 0
    private var currentAttempts = 0
    private var score = 0
    private var isReattempted = false

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var soundPool: SoundPool
    private var soundEffect1: Int = 0
    private var soundEffect2: Int = 0
    private var soundEffect3: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_b)

        // UI 초기화
        scoreTextView = findViewById(R.id.score)
        questionTextView = findViewById(R.id.question)
        item1Button = findViewById(R.id.item1)
        item2Button = findViewById(R.id.item2)
        resultImageView = findViewById(R.id.b_resultImageView)

        king_angry = findViewById(R.id.b_king_angry)
        king_smile = findViewById(R.id.b_king_smile)
        resultImageView2 = findViewById(R.id.b_resultImageView2)//다시푸세요

        resultImageView2.visibility = ImageView.INVISIBLE
        king_angry.visibility = ImageView.INVISIBLE
        king_smile.visibility = ImageView.INVISIBLE

        // 배경 음악 설정
        mediaPlayer = MediaPlayer.create(this, R.raw.aplay)
        mediaPlayer.setVolume(0.7f, 0.7f)
        mediaPlayer.isLooping = true

        soundPool = SoundPool.Builder().setMaxStreams(3).build()
        soundEffect1 = soundPool.load(this, R.raw.acorrect, 1)
        soundEffect2 = soundPool.load(this, R.raw.aincorrect, 1)
        soundEffect3 = soundPool.load(this, R.raw.acountdown, 1)

        setFullScreen()

        // 데이터베이스 설정 및 문제 가져오기
        dbManager = QuizBDBManager(this, "quizB.db", null, 1)
        sqlitedb = dbManager.readableDatabase
        loadQuestionsFromDB()

        // 첫 번째 문제 설정
        loadQuestion()

        item1Button.setOnClickListener { checkAnswer(0) }
        item2Button.setOnClickListener { checkAnswer(1) }

        // 페이드인 애니메이션 추가 (1초 후에 UI 요소 표시)
        val elementsToAnimate = listOf(scoreTextView, questionTextView, item1Button, item2Button)
        elementsToAnimate.forEach { it.visibility = View.INVISIBLE } // 처음엔 숨김

        // 1초 후에 페이드인 애니메이션 적용
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            val fadeIn = android.view.animation.AlphaAnimation(0f, 1f).apply {
                duration = 1000  // 1초 동안 서서히 나타남
                fillAfter = true // 애니메이션 종료 후 상태 유지
            }
            elementsToAnimate.forEach {
                it.visibility = View.VISIBLE
                it.startAnimation(fadeIn)
            }
        }, 1000) // 1초(1000ms) 후 실행
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
            findViewById<TextView>(R.id.number).text = (currentIndex + 1).toString() // 문제 번호 업데이트

            currentAttempts = 0  //  현재 문제 시작 시 다시 0으로 초기화
            isReattempted = false //  재시도 여부도 초기화

        } else {
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
            val gainedScore = if (currentAttempts == 0) 10 else (10.0 / (currentAttempts + 1)).toInt()
            score += gainedScore  // ⬅ 점수 반영
            soundPool.play(soundEffect1, 1.0f, 1.0f, 0, 0, 1.0f)
            showCorrectAnswer()
        } else {
            soundPool.play(soundEffect2, 1.0f, 1.0f, 0, 0, 1.0f)
            score -= 1  // ⬅ 틀릴 때 점수 감소 (예: 1점씩 깎음)
            showIncorrectAnswer()
            isReattempted = true
            currentAttempts++  // ⬅ 틀린 횟수 증가
        }
    }



    // 정답 처리
    fun showCorrectAnswer() {

        resultImageView.setImageResource(R.drawable.b_quiz_correct) // 정답 이미지

        //findViewById<View>(R.id.king_smile).startAnimation(fastFadeIn)
        king_smile.visibility = ImageView.VISIBLE
        val fastFadeIn = AnimationUtils.loadAnimation(this, R.anim.fast_fade_in)
        king_smile.startAnimation(fastFadeIn)

        scoreTextView.text = score.toString()
        currentIndex++ // 다음 문제로 이동

        //pauseTimer()

        resultImageView.postDelayed({
            resultImageView.setImageResource(0) // 이미지 초기화
            king_smile.visibility = ImageView.INVISIBLE
            loadQuestion()
        }, 2000)

    }

    // 오답처리
    fun showIncorrectAnswer() {
        resultImageView.setImageResource(R.drawable.b_quiz_incorrect)
        king_angry.visibility = ImageView.VISIBLE
        val fastFadeIn = AnimationUtils.loadAnimation(this, R.anim.fast_fade_in)
        king_angry.startAnimation(fastFadeIn)

        resultImageView2.visibility = ImageView.VISIBLE
        resultImageView2.startAnimation(fastFadeIn)

        resultImageView.postDelayed({
            resultImageView.setImageResource(0) // 이미지 초기화
            king_angry.visibility = ImageView.INVISIBLE
            resultImageView2.visibility = ImageView.INVISIBLE
            loadQuestion()
        }, 2000)


    }

    fun setFullScreen(){
        var uiOption = window.decorView.systemUiVisibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            uiOption = uiOption or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            uiOption = uiOption or View.SYSTEM_UI_FLAG_FULLSCREEN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            uiOption = uiOption or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = uiOption
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer.pause()  // 액티비티가 일시 중지되면 음악도 일시 중지
    }

    override fun onResume() {
        super.onResume()
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()  // 액티비티가 다시 시작되면 음악도 재시작
        }
    }
}


data class Question(
    val question: String,
    val item1: String,
    val item2: String,
    val answer: Int
)
