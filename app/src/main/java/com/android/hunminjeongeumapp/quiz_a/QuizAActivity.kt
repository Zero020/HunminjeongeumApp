package com.android.hunminjeongeumapp.quiz_a

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Rect
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.hunminjeongeumapp.R


class QuizAActivity : AppCompatActivity() {

    lateinit var sqlitedb: SQLiteDatabase
    lateinit var dbManager: QuizADBManager

    lateinit var question: TextView
    lateinit var description: TextView
    lateinit var answer: EditText
    lateinit var timer: TextView
    lateinit var number: TextView
    lateinit var resultImage: ImageView // 정답/오답 이미지
    lateinit var countdownText: TextView
    lateinit var darkBackground: FrameLayout

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

        question = findViewById(R.id.question)
        description = findViewById(R.id.description)
        answer = findViewById(R.id.answer)
        timer = findViewById(R.id.timer)
        number = findViewById(R.id.number)
        resultImage = findViewById(R.id.resultImageView) // 결과 이미지
        countdownText = findViewById(R.id.countdownText)
        darkBackground = findViewById(R.id.darkBackground)

        dbManager = QuizADBManager(this, "quizA.db", null, 1)
        sqlitedb = dbManager.readableDatabase

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

        // 타이머 설정: 2분 후 showResult 호출
        //startCountdownTimer()

        showQuestion() // 첫 번째 문제 표시
        // EditText에 포커스가 있을 때 키보드 상태 체크
        answer.setOnFocusChangeListener { _, hasFocus ->

            darkBackground.visibility = FrameLayout.VISIBLE

            if (hasFocus) {

            } else {
                // 키보드가 내려가면 darkBackground 숨기기
                darkBackground.visibility = FrameLayout.GONE

                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(answer.windowToken, 0)
            }
        }

        // 엔터키 누를 때 정답 비교
        answer.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_DONE) {
                // 엔터 키 -> 키보드 내리기
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(answer.windowToken, 0)

                answer.clearFocus()

                darkBackground.visibility = FrameLayout.GONE

                checkAnswer()
                true
            } else {
                false
            }
        }



        answer.text.clear() // EditText 초기화
    }


    // 2분 카운트다운 타이머
    fun startCountdownTimer() {
        countDownTimer = object : CountDownTimer(timeLimit, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (!isPaused) {
                    // 남은 시간 표시
                    timeLeftAtPause = millisUntilFinished // 남은 시간 저장
                    val seconds = (millisUntilFinished / 1000).toInt()
                    timer.text = String.format("%02d:%02d", seconds / 60, seconds % 60) // 분:초 형식으로 표시
                }
            }

            override fun onFinish() {
                // 2분이 다 되면 showResult 호출
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
            //totalCorrectAnswers++
            showCorrectAnswer()
            answer.text.clear()
        } else {
            // 오답 처리
            showIncorrectAnswer()
            answer.text.clear()
        }
    }

    // 정답 처리
    fun showCorrectAnswer() {
        resultImage.setImageResource(R.drawable.sun) // 정답 이미지
        Toast.makeText(this, "정답입니다!", Toast.LENGTH_SHORT).show()

        totalCorrectAnswers++
        incorrectAttempts = 0
        currentQuestionIndex++

        //pauseTimer()

        resultImage.postDelayed({
            moveToNextQuestion()

        }, 2000)

    }

    fun moveToNextQuestion() {
        resultImage.setImageResource(0) // 이미지 초기화
        if (currentQuestionIndex == 0) {
            // 첫 번째 문제에서만 카운트다운을 실행
            showCountdown()
        } else {
            // 첫 번째 문제가 아니면 바로 다음 문제를 표시
            if (currentQuestionIndex < 3) {
                showQuestion()
            } else {
                showResult()
            }
            resumeTimer() // 타이머 재시작
        }
    }

    fun showCountdown() {
        countdownText.visibility = TextView.VISIBLE
        answer.isEnabled = false


        var countdown = 3 // "3, 2, 1, go!" 순으로 진행

        val countdownTimer = object : CountDownTimer(4000, 1000) { // 4초간 카운트다운
            override fun onTick(millisUntilFinished: Long) {
                when (countdown) {
                    3 -> countdownText.text = ""
                    2 -> countdownText.text = "3"
                    1 -> countdownText.text = "2"
                    0 -> countdownText.text = "1"
                }
                countdown--
            }

            override fun onFinish() {
                countdownText.visibility = TextView.INVISIBLE // 카운트다운 끝나면 텍스트 숨기기
                showQuestion()  // 문제 표시
                startCountdownTimer()
            }
        }
        countdownTimer.start()
    }

    fun showIncorrectAnswer() {
        resultImage.setImageResource(R.drawable.night)
        incorrectAttempts++
        totalAttempts++
        when (incorrectAttempts) {
            1 -> {
                question.text = hint1
                Toast.makeText(this, "첫 번째 힌트", Toast.LENGTH_SHORT).show()
                resultImage.postDelayed({
                    resultImage.setImageResource(0)
                    resumeTimer()
                }, 1500)
            }
            2 -> {
                question.text = hint2
                Toast.makeText(this, "두 번째 힌트", Toast.LENGTH_SHORT).show()
                resultImage.postDelayed({
                    resultImage.setImageResource(0)
                    resumeTimer()
                }, 1500)
            }
            3 ->  {
                question.text = correct_answer
                Toast.makeText(this, "3번째 오답", Toast.LENGTH_SHORT).show()
                currentQuestionIndex++

                if (currentQuestionIndex < 3){
                    resultImage.postDelayed({
                        resultImage.setImageResource(0)
                        resumeTimer()
                        showQuestion()
                    }, 1500)
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
        val accuracy = if (totalAttempts > 0) totalCorrectAnswers.toFloat() / totalAttempts else 0f

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
    }
}

data class Question(
    val question: String,
    val description: String,
    val hint1: String,
    val hint2: String,
    val answer: String
)
