package com.android.hunminjeongeumapp.quiz_a

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Rect
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.hunminjeongeumapp.R


class QuizAActivity : AppCompatActivity() {

    lateinit var sqlitedb: SQLiteDatabase
    lateinit var dbManager: QuizADBManager
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var soundPool: SoundPool
    private var soundEffect1: Int = 0
    private var soundEffect2: Int = 0
    private var soundEffect3: Int = 0

    lateinit var question: TextView
    lateinit var description: TextView
    lateinit var answer: EditText
    lateinit var timer: TextView
    lateinit var number: TextView
    lateinit var resultImage: ImageView // 정답/오답 이미지

    lateinit var resultImage2: ImageView //오답 이미지2
    lateinit var countdownText: TextView
    lateinit var darkBackground: FrameLayout
    lateinit var king_smile: ImageView
    lateinit var king_angry:ImageView

    lateinit var basicBackground: ImageView
    lateinit var mountain_appear: ImageView
    lateinit var moon_appear: ImageView

    lateinit var correct_answer: String
    lateinit var hint1: String
    lateinit var hint2: String

    var incorrectAttempts = 0
    var currentQuestionIndex = 0
    var questionsList = mutableListOf<Question>()

    var timeLimit: Long = 60000 // 1분
    lateinit var countDownTimer: CountDownTimer

    var totalCorrectAnswers = 0
    var totalAttempts = 0

    var isPaused = false // 타이머 상태 체크
    var timeLeftAtPause: Long = 0 // 타이머가 멈춘 시점
    var startTime: Long = 0

    //Edit 화면외에 클릭시 키보드 내리기
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val focusView = currentFocus
        if (focusView != null) {
            val rect = Rect()
            focusView.getGlobalVisibleRect(rect)
            val x = ev.x.toInt()
            val y = ev.y.toInt()
            if (!rect.contains(x, y)) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm?.hideSoftInputFromWindow(focusView.windowToken, 0)
                focusView.clearFocus()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_a)

        question = findViewById(R.id.a_question)
        description = findViewById(R.id.a_description)
        answer = findViewById(R.id.a_answer)
        timer = findViewById(R.id.a_timer)
        number = findViewById(R.id.a_number)
        resultImage = findViewById(R.id.a_resultImageView) // 결과 이미지

        //효과적인 요소들
        basicBackground = findViewById(R.id.a_basicImage)
        countdownText = findViewById(R.id.a_countdownText)
        darkBackground = findViewById(R.id.a_darkBackground)
        mountain_appear = findViewById(R.id.a_mountainView)
        moon_appear = findViewById(R.id.a_moonImage)
        king_angry = findViewById(R.id.a_king_angry)
        king_smile = findViewById(R.id.a_king_smile)
        resultImage2 = findViewById(R.id.a_resultImageView2)//다시푸세요

        resultImage2.visibility = ImageView.INVISIBLE
        king_angry.visibility = ImageView.INVISIBLE
        king_smile.visibility = ImageView.INVISIBLE

        dbManager = QuizADBManager(this, "quizA.db", null, 1)
        sqlitedb = dbManager.readableDatabase

        mediaPlayer = MediaPlayer.create(this, R.raw.aplay)
        mediaPlayer.setVolume(0.7f, 0.7f)
        mediaPlayer.isLooping = true
        //mediaPlayer.start()

        soundPool = SoundPool.Builder().setMaxStreams(3).build()
        soundEffect1 = soundPool.load(this, R.raw.acorrect, 1)
        soundEffect2 = soundPool.load(this, R.raw.aincorrect, 1)
        soundEffect3 = soundPool.load(this, R.raw.acountdown, 1)


        val mountainAnimation = AnimationUtils.loadAnimation(this, R.anim.a_mountain_apper)
        val sunAnimation = AnimationUtils.loadAnimation(this, R.anim.a_sun_appear)
        val slowFadeIn = AnimationUtils.loadAnimation(this, R.anim.slow_fade_in)

        answer.translationY = 566f * resources.displayMetrics.density

        findViewById<View>(R.id.a_mountainView).startAnimation(mountainAnimation)
        findViewById<View>(R.id.a_moonImage).startAnimation(sunAnimation)
        findViewById<View>(R.id.a_basicImage2_king_appear).startAnimation(slowFadeIn)

        setFullScreen()


        var cursor: Cursor? = sqlitedb.rawQuery("SELECT * FROM questions ORDER BY RANDOM() LIMIT 3", null)
        while (cursor?.moveToNext() == true) {
            val str_ques = cursor.getString(cursor.getColumnIndex("question"))
            val str_desc = cursor.getString(cursor.getColumnIndex("description"))
            val hint1 = cursor.getString(cursor.getColumnIndex("hint1"))
            val hint2 = cursor.getString(cursor.getColumnIndex("hint2"))
            val answer = cursor.getString(cursor.getColumnIndex("answer"))

            questionsList.add(Question(str_ques, str_desc, hint1, hint2, answer))
        }


        cursor?.close()
        sqlitedb.close()

        startTime = SystemClock.elapsedRealtime() // 게임 시작 시점 기록

        showCountdown()

        // 타이머 설정: 1분 후 showResult 호출
        //startCountdownTimer()

        showQuestion() // 첫 번째 문제 표시


        // EditText에 포커스가 있을 때 키보드 상태 체크
        answer.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // darkBackground를 표시
                darkBackground.visibility = FrameLayout.VISIBLE

                answer.translationY = 236f * resources.displayMetrics.density
            } else {
                // 키보드가 내려가면 darkBackground 숨기기
                darkBackground.visibility = FrameLayout.GONE

                // EditText 위치 원상복구
                answer.translationY = 566f * resources.displayMetrics.density

                // 키보드 숨기기
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(answer.windowToken, 0)
            }
        }

        // answer.text.clear()를 checkAnswer() 실행 이후에 실행하도록 변경
        answer.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(answer.windowToken, 0)

                answer.clearFocus()
                darkBackground.visibility = FrameLayout.GONE
                answer.translationY = 566f * resources.displayMetrics.density

                checkAnswer()

                answer.text.clear()  // ✅ 정답을 확인한 후에 텍스트를 지우도록 변경
                true
            } else {
                false
            }
        }


        answer.text.clear() // EditText 초기화
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
    // 1분 카운트다운 타이머
    fun startCountdownTimer() {
        countDownTimer = object : CountDownTimer(timeLimit, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (!isPaused) {
                    // 남은 시간 표시
                    timeLeftAtPause = millisUntilFinished // 남은 시간 저장
                    val seconds = (millisUntilFinished / 1000).toInt()
                    timer.text = String.format("%02d:%02d", seconds / 60, seconds % 60) // 분:초 형식으로 표시

                    if (seconds == 5) {
                        // 현재 재생 중인 음악 정지 및 자원 해제
                        mediaPlayer?.stop()
                        mediaPlayer?.release()
                        timer.setTextColor(ContextCompat.getColor(this@QuizAActivity, R.color.red))
                        // 새 음악 재생
                        mediaPlayer = MediaPlayer.create(this@QuizAActivity, R.raw.aplayover)
                        mediaPlayer?.isLooping = true
                        mediaPlayer?.start()
                    }
                }
            }

            override fun onFinish() {
                // 1분이 다 되면 showResult 호출
                showResult()
            }
        }
        countDownTimer.start()
        answer.isEnabled = true
    }

    fun showQuestion() {
        val currentQuestion = questionsList[currentQuestionIndex]

        question.text = currentQuestion.question
        description.text = currentQuestion.description
        correct_answer = currentQuestion.answer
        hint1 = currentQuestion.hint1
        hint2 = currentQuestion.hint2

        number.text = "${currentQuestionIndex + 1}/3" // 문제 번호 표시

        incorrectAttempts = 0 // 각 문제마다 틀린 횟수 초기화
    }

    fun checkAnswer() {

        val userAnswer = answer.text.toString().trim()
        totalAttempts++
        pauseTimer()
        if (userAnswer.equals(correct_answer, ignoreCase = true)) {
            // 정답 처리
            soundPool.play(soundEffect1, 1.0f, 1.0f, 0, 0, 1.0f)

            showCorrectAnswer()
            answer.text.clear()
        } else {
            // 오답 처리
            soundPool.play(soundEffect2, 1.0f, 1.0f, 0, 0, 1.0f)

            showIncorrectAnswer()
            answer.text.clear()
        }
    }

    // 정답 처리
    fun showCorrectAnswer() {

        resultImage.setImageResource(R.drawable.a_quiz_correct) // 정답 이미지

        //findViewById<View>(R.id.king_smile).startAnimation(fastFadeIn)
        king_smile.visibility = ImageView.VISIBLE
        val fastFadeIn = AnimationUtils.loadAnimation(this, R.anim.fast_fade_in)
        king_smile.startAnimation(fastFadeIn)

        Toast.makeText(this, "정답.", Toast.LENGTH_SHORT).show()

        totalCorrectAnswers++
        incorrectAttempts = 0
        currentQuestionIndex++

        //pauseTimer()

        resultImage.postDelayed({
            moveToNextQuestion()
            king_smile.visibility = ImageView.INVISIBLE
        }, 2000)

    }

    fun moveToNextQuestion() {
        answer.translationY = 566f * resources.displayMetrics.density
        resultImage.setImageResource(0) // 이미지 초기화
        if (currentQuestionIndex == 0) {
            // 첫 번째 문제에서만 카운트다운을 실행
            showCountdown()
        } else {
            // 첫 번째 문제가 아니면 바로 다음 문제를 표시
            if (currentQuestionIndex < 3) {
                showQuestion()
                answer.translationY = 566f * resources.displayMetrics.density
            } else {
                showResult()
            }
            resumeTimer() // 타이머 재시작
        }
    }

    //게임시작전 3, 2, 1 카운트다운
    fun showCountdown() {
        countdownText.visibility = TextView.VISIBLE
        answer.isEnabled = false
        answer.visibility = EditText.INVISIBLE
        question.visibility = TextView.INVISIBLE
        description.visibility = TextView.INVISIBLE

        var countdown = 3 // "3, 2, 1" 순으로 진행

        val countdownTimer = object : CountDownTimer(4000, 1000) { // 4초간 카운트다운
            override fun onTick(millisUntilFinished: Long) {

                when (countdown) {
                    3 -> countdownText.text = ""
                    2 -> {
                        countdownText.text = "3"
                        soundPool.play(soundEffect3, 1.0f, 1.0f, 0, 0, 1.0f)
                    }
                    1 -> {
                        countdownText.text = "2"
                        soundPool.play(soundEffect3, 1.0f, 1.0f, 0, 0, 1.0f)
                    }
                    0 -> {
                        countdownText.text = "1"
                        mediaPlayer.start()
                        soundPool.play(soundEffect3, 1.0f, 1.0f, 0, 0, 1.0f)
                    }
                    }
                countdown--
            }

            override fun onFinish() {
                answer.visibility = View.VISIBLE
                question.visibility = View.VISIBLE
                description.visibility = View.VISIBLE
                countdownText.visibility = TextView.INVISIBLE // 카운트다운 끝나면 텍스트 숨기기

                showQuestion()  // 문제 표시
                startCountdownTimer()
            }
        }
        countdownTimer.start()
    }

    fun showIncorrectAnswer() {
        resultImage.setImageResource(R.drawable.a_quiz_incorrect1)
        king_angry.visibility = ImageView.VISIBLE
        val fastFadeIn = AnimationUtils.loadAnimation(this, R.anim.fast_fade_in)
        king_angry.startAnimation(fastFadeIn)

        incorrectAttempts++
        totalAttempts++
        when (incorrectAttempts) {
            1 -> {
                question.text = hint1
                resultImage2.visibility = ImageView.VISIBLE
                resultImage2.startAnimation(fastFadeIn)

                Toast.makeText(this, "첫 번째 힌트를 주마", Toast.LENGTH_SHORT).show()
                resultImage.postDelayed({
                    resultImage.setImageResource(0)
                    resumeTimer()
                    resultImage2.visibility = ImageView.INVISIBLE
                    king_angry.visibility = ImageView.INVISIBLE
                }, 2000)
            }
            2 -> {
                question.text = hint2
                resultImage2.visibility = ImageView.VISIBLE
                resultImage2.startAnimation(fastFadeIn)

                Toast.makeText(this, "두 번째 힌트를 주마", Toast.LENGTH_SHORT).show()
                resultImage.postDelayed({
                    resultImage.setImageResource(0)
                    resumeTimer()
                    resultImage2.visibility = ImageView.INVISIBLE
                    king_angry.visibility = ImageView.INVISIBLE
                }, 2000)
            }
            3 ->  {
                question.text = correct_answer
                Toast.makeText(this, "다음문제로 넘어가마", Toast.LENGTH_SHORT).show()
                currentQuestionIndex++

                if (currentQuestionIndex < 3){
                    resultImage.postDelayed({
                        resultImage.setImageResource(0)
                        king_angry.visibility = ImageView.INVISIBLE
                        resumeTimer()
                        showQuestion()
                    }, 2000)
                }else {
                    resultImage.postDelayed({
                        resultImage.setImageResource(0)
                        resumeTimer()
                        showResult()
                    }, 1500)
                }
            }
        }
        answer.text.clear()
    }

    // 타이머 시작
    fun startTimer() {
        startTime = SystemClock.elapsedRealtime() // 게임 시작 시점 기록
        countDownTimer.start()
    }

    // 타이머를 멈추고 2초 후 재시작
    fun pauseTimer() {
        isPaused = true
        countDownTimer.cancel() // 타이머 멈추기
    }

    fun resumeTimer() {
        isPaused = false
        // 타이머가 멈춘 시점에서부터 다시 시작
        countDownTimer.cancel() // 기존 타이머를 취소하고
        countDownTimer = object : CountDownTimer(timeLeftAtPause, 1000) { // 남은 시간으로 새로 타이머 시작
            override fun onTick(millisUntilFinished: Long) {
                if (!isPaused) {
                    timeLeftAtPause = millisUntilFinished // 남은 시간 저장
                    val seconds = (millisUntilFinished / 1000).toInt()
                    timer.text = String.format("%02d:%02d", seconds / 60, seconds % 60) // 분:초 형식으로 표시
                }
            }

            override fun onFinish() {
                showResult()
            }
        }
        countDownTimer.start() // 타이머 시작
    }


    fun showResult() {
        // 총 걸린 시간 계산: SystemClock.elapsedRealtime()을 사용하여 경과 시간 계산
        val totalTime = (SystemClock.elapsedRealtime() - startTime) / 1000
        // 평균 정답률 계산
        val accuracy = if (questionsList.isNotEmpty()) totalCorrectAnswers.toFloat() / questionsList.size else 0f

        // 결과 화면으로 이동
        val intent: Intent = Intent(this, RankingAActivity::class.java)
        intent.putExtra("totalTime", totalTime) // 총 시간 전달
        intent.putExtra("accuracy", accuracy)  // 정답률 전달
        startActivity(intent) // 결과 화면으로 이동
        finish() // 현재 액티비티 종료
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer.cancel() // 타이머 종료
        mediaPlayer.release()
        soundPool.release()
    }
}

data class Question(
    val question: String,
    val description: String,
    val hint1: String,
    val hint2: String,
    val answer: String
)
