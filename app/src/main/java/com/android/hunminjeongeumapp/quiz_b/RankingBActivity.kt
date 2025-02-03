package com.android.hunminjeongeumapp.quiz_b

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.AlphaAnimation
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.media.MediaPlayer
import android.media.SoundPool
import com.android.hunminjeongeumapp.MainActivity
import com.android.hunminjeongeumapp.R

class RankingBActivity : AppCompatActivity() {

    private lateinit var rankingRecyclerView: RecyclerView
    private lateinit var nameInput: EditText
    private lateinit var resultText: TextView
    private lateinit var homeButton: ImageButton

    private var rankingList = mutableListOf<Rank>()
    private var receivedScore: Int = 0

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var soundPool: SoundPool
    private var soundEffect1: Int = 0
    private var soundEffect2: Int = 0
    private var soundEffect3: Int = 0

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_branking)

        rankingRecyclerView = findViewById(R.id.rankingList)
        nameInput = findViewById(R.id.nameInput)
        resultText = findViewById(R.id.ResultText)
        homeButton = findViewById(R.id.homeButton)

        // 배경 음악 설정
        mediaPlayer = MediaPlayer.create(this, R.raw.aranking)
        mediaPlayer.setVolume(0.7f, 0.7f)
        mediaPlayer.isLooping = true
        mediaPlayer.start()

        // 효과음 로드
        soundPool = SoundPool.Builder().setMaxStreams(3).build()
        soundEffect1 = soundPool.load(this, R.raw.ashowlist, 1)
        soundEffect2 = soundPool.load(this, R.raw.agohome, 1)
        soundEffect3 = soundPool.load(this, R.raw.akeyboard, 1)

        // 애니메이션 설정
        val fadeInFast = AnimationUtils.loadAnimation(this, R.anim.basic_fade_in2)
        val fadeInSlow = AnimationUtils.loadAnimation(this, R.anim.basic_fade_in)

        resultText.startAnimation(fadeInFast)
        rankingRecyclerView.startAnimation(fadeInSlow)

        rankingRecyclerView.layoutManager = LinearLayoutManager(this)

        // 점수 가져오기
        receivedScore = intent.getIntExtra("score", 0)

        if (receivedScore == 0) {
            val resultString = "$receivedScore 점이구나 \n 더 열심히 해보자꾸나"
            nameInput.visibility = View.INVISIBLE
            animateText(resultString)
        } else {
            nameInput.visibility = View.VISIBLE
            animateText("$receivedScore 점이구나")
        }

        // 랭킹 데이터 로드
        loadRankingData()

        // 엔터 키 입력 시 자동 저장 + 키보드 효과음 추가
        nameInput.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {

                val username = nameInput.text.toString().trim()
                if (username.isNotEmpty()) {
                    saveGameResult(username, receivedScore)
                    Toast.makeText(this, "랭킹에 저장되었습니다!", Toast.LENGTH_SHORT).show()

                    // 키보드 숨기기
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(nameInput.windowToken, 0)

                    // 입력 필드 비활성화
                    nameInput.isEnabled = false
                    nameInput.translationY = 640f * resources.displayMetrics.density

                    // 효과음 재생
                    soundPool.play(soundEffect3, 1.0f, 1.0f, 0, 0, 1.0f)
                } else {
                    Toast.makeText(this, "이름을 입력하거라", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }

        // 홈 버튼 클릭 시 효과음 + 애니메이션 추가
        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            soundPool.play(soundEffect2, 1.0f, 1.0f, 0, 0, 1.0f)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }

    private fun animateText(text: String) {
        resultText.text = ""
        var index = 0
        val delay: Long = 100

        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if (index < text.length) {
                    resultText.text = resultText.text.toString() + text[index]
                    index++
                    handler.postDelayed(this, delay)
                }
            }
        }
        handler.post(runnable)
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer.pause()
    }

    override fun onResume() {
        super.onResume()
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    private fun loadRankingData() {
        val dbManager = QuizBDBManager(this, "quizB.db", null, 1)
        val sqlitedb: SQLiteDatabase = dbManager.readableDatabase

        val cursor: Cursor = sqlitedb.rawQuery("SELECT * FROM rankings ORDER BY score DESC", null)

        rankingList.clear()

        var rank = 1  // 현재 등수
        var prevScore = -1  // 이전 점수
        var sameRankCount = 0  // 공동 등수 개수

        while (cursor.moveToNext()) {
            val username = cursor.getString(cursor.getColumnIndexOrThrow("username"))
            val score = cursor.getInt(cursor.getColumnIndexOrThrow("score"))

            // 점수가 이전 점수와 같으면 공동 등수 처리
            if (score == prevScore) {
                sameRankCount++  // 공동 등수 개수 증가
            } else {
                // 새로운 점수라면, 공동 등수를 고려하여 새로운 등수 계산
                rank += sameRankCount
                sameRankCount = 1  // 현재 점수를 가진 사람 포함
            }

            // 리스트에 (등수, 이름, 점수) 추가
            rankingList.add(Rank(rank, username, score))

            // 현재 점수를 이전 점수로 업데이트
            prevScore = score
        }

        cursor.close()
        sqlitedb.close()

        // 어댑터에 랭킹 데이터 적용
        rankingRecyclerView.adapter = RankingBAdapter(rankingList)
    }


    private fun saveGameResult(username: String, score: Int) {
        val dbManager = QuizBDBManager(this, "quizB.db", null, 1)
        val sqlitedb = dbManager.writableDatabase

        val query = """
        INSERT INTO rankings (username, score)
        VALUES (?, ?)
    """

        sqlitedb.execSQL(query, arrayOf(username, score))
        sqlitedb.close()

        // 랭킹 데이터 갱신
        loadRankingData()

        // 입력 필드 비활성화
        nameInput.isEnabled = false
    }

}
