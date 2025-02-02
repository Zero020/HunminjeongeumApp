package com.android.hunminjeongeumapp.quiz_a

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.hunminjeongeumapp.R
import android.database.sqlite.SQLiteDatabase
import android.database.Cursor
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.ImageView
import com.android.hunminjeongeumapp.MainActivity

class RankingAActivity : AppCompatActivity() {

    lateinit var rankingRecyclerView: RecyclerView
    lateinit var nameInput: EditText // 유저 이름 입력 받는 EditText

    lateinit var resultText: TextView // 게임 결과 표시할 텍스트뷰
    lateinit var homeButton: ImageButton
    lateinit var rankText: ImageView
    lateinit var paper : ImageView
    lateinit var rankingking: ImageView

    val rankingList = mutableListOf<Rank>()
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var soundPool: SoundPool
    private var soundEffect1: Int = 0
    private var soundEffect2: Int = 0
    private var soundEffect3: Int = 0

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_aranking)

        // findViewById로 뷰 연결
        rankingRecyclerView = findViewById(R.id.a_rankingList)
        nameInput = findViewById(R.id.a_nameInput)
        resultText = findViewById(R.id.a_ResultText)
        homeButton = findViewById(R.id.a_homeButton)
        rankText = findViewById(R.id.a_rankText)
        paper = findViewById(R.id.a_paperImage)
        rankingking = findViewById(R.id.a_rankKing)

        mediaPlayer = MediaPlayer.create(this, R.raw.aranking)
        mediaPlayer.setVolume(0.7f, 0.7f)
        mediaPlayer.isLooping = true  // 음악을 무한 반복 설정
        mediaPlayer.start()
        soundPool = SoundPool.Builder().setMaxStreams(3).build()
        soundEffect1 = soundPool.load(this, R.raw.ashowlist, 1)
        soundEffect2 = soundPool.load(this, R.raw.agohome, 1)
        soundEffect3 = soundPool.load(this, R.raw.akeyboard, 1)
        
        //애니메이션
        val basicFadeIn2 = AnimationUtils.loadAnimation(this, R.anim.basic_fade_in2)
        val basicFadeIn = AnimationUtils.loadAnimation(this, R.anim.basic_fade_in)

        findViewById<View>(R.id.a_ResultText).startAnimation(basicFadeIn2)
        findViewById<View>(R.id.a_rankKing).startAnimation(basicFadeIn)
        findViewById<View>(R.id.a_paperImage).startAnimation(basicFadeIn)

        // 리사이클러뷰 설정
        rankingRecyclerView.layoutManager = LinearLayoutManager(this)
        nameInput.translationY = 640f * resources.displayMetrics.density

        var receivedTime = intent.getLongExtra("totalTime", 0L)
        var receivedAccuracy = intent.getFloatExtra("accuracy", 0f)

        val minutes = receivedTime.toInt() / 60
        val seconds = receivedTime.toInt() % 60

        setFullScreen()
        // 랭킹 DB에서 가져오기
        loadRankingData()
        soundPool.setOnLoadCompleteListener { soundPool, sampleId, status ->
            if (status == 0) {  // 로드 성공 시
                soundPool.play(soundEffect1, 1.0f, 1.0f, 1, 0, 1.0f)
            }
        }

        // 조건에 맞지 않으면 이름 입력을 비활성화
        if (receivedTime >= 60) {
            // 시간 초과 또는 정답률 0%인 경우
            Toast.makeText(this, "시간 초과로 순위에 등록하지 못하느리라.", Toast.LENGTH_SHORT).show()

            // 이름 입력 필드 및 버튼 비활성화
            nameInput.isEnabled = false
            nameInput.visibility = EditText.INVISIBLE
            val resultString = "참으로 아쉽구나, \n 또 보자꾸나."
            animateText(resultString)

        }else if(receivedAccuracy == 0f){
            Toast.makeText(this, "정답률 0%로 인해 순위에 등록하지 못하느리라.", Toast.LENGTH_SHORT).show()

            // 이름 입력 필드 및 버튼 비활성화
            nameInput.isEnabled = false
            nameInput.visibility = EditText.INVISIBLE
            val resultString = "참으로 아쉽구나, \n 또 보자꾸나."
            animateText(resultString)

        } else {
            // 조건을 만족하면 이름 입력 필드 및 버튼 활성화
            nameInput.isEnabled = true

            if (minutes != 1) {
                val resultString = String.format("%02d초", seconds) + "동안, \n" +
                        " ${String.format("%.2f", receivedAccuracy * 100)}%맞혔구나."
                animateText(resultString)

            }
        }
        // EditText에 포커스가 있을 때 키보드 상태 체크
        nameInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {

                nameInput.translationY = 270f * resources.displayMetrics.density
            } else {

                // EditText 위치 원상복구
                nameInput.translationY = 640f * resources.displayMetrics.density

                // 키보드 숨기기
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(nameInput.windowToken, 0)
            }
        }

        nameInput.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                val username = nameInput.text.toString().trim() // 입력값 가져오기
                if (username.isNotEmpty()) {
                    saveGameResult(username, receivedAccuracy, receivedTime.toInt(), "")
                    Toast.makeText(this, "랭킹에 저장됐느리라.", Toast.LENGTH_SHORT).show()

                    // 키보드 숨기기
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(nameInput.windowToken, 0)

                    // 입력 필드 비활성화
                    nameInput.isEnabled = false
                    nameInput.translationY = 640f * resources.displayMetrics.density
                } else {
                    Toast.makeText(this, "이름을 입력하거라", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }
        nameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 텍스트 변경 전
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 텍스트가 변경될 때 효과음 재생
                if (count > 0) { // 실제로 새로운 텍스트가 입력되었을 때만 사운드 재생
                    soundPool.play(soundEffect3, 1.0f, 1.0f, 0, 0, 1.0f)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // 텍스트 변경 후
            }
        })

        homeButton.setOnClickListener {
            val intent : Intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            soundPool.play(soundEffect2, 1.0f, 1.0f, 0, 0, 1.0f)
            finish() // 현재 액티비티 종료
        }
    }

    private fun animateText(text: String) {
        resultText.text = "" // 초기 텍스트를 비워둠
        var index = 0
        val delay: Long = 150 // 각 글자가 표시되는 시간 간격

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
        mediaPlayer.pause()  // 액티비티가 일시 중지되면 음악도 일시 중지
    }

    override fun onResume() {
        super.onResume()
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()  // 액티비티가 다시 시작되면 음악도 재시작
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()  // 액티비티가 종료되면 MediaPlayer 자원 해제
    }

    // 랭킹 데이터 로드
    private fun loadRankingData() {

        val dbManager = QuizADBManager(this, "quizA.db", null, 1)
        val sqlitedb: SQLiteDatabase = dbManager.readableDatabase

        val cursor: Cursor = sqlitedb.rawQuery("SELECT * FROM rankings ORDER BY time_taken ASC, accuracy DESC LIMIT 5", null)

        rankingList.clear()
        while (cursor.moveToNext()) {
            val username = cursor.getString(cursor.getColumnIndex("username"))
            val timeTaken = cursor.getInt(cursor.getColumnIndex("time_taken"))
            val accuracy = cursor.getFloat(cursor.getColumnIndex("accuracy"))
            rankingList.add(Rank(rankingList.size + 1, username, timeTaken, accuracy))
        }

        cursor.close()
        sqlitedb.close()

        // 어댑터에 랭킹 데이터 적용
        rankingRecyclerView.adapter = RankingAAdapter(rankingList)
    }

    fun setFullScreen() {
        var uiOption = window.decorView.systemUiVisibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            uiOption = uiOption or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            uiOption = uiOption or View.SYSTEM_UI_FLAG_FULLSCREEN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            uiOption = uiOption or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = uiOption
    }
    // 게임 결과 저장
    fun saveGameResult(username: String, accuracy: Float, timeTaken: Int, incorrectWords: String?) {
        val dbManager = QuizADBManager(this, "quizA.db", null, 1)
        val sqlitedb = dbManager.writableDatabase

        val query = """
            INSERT INTO rankings (username, accuracy, time_taken, is_registered, incorrect_words)
            VALUES (?, ?, ?, ?, ?)
        """

        // 게임이 정상적으로 종료되었으므로 isRegistered는 true로 설정
        val isRegistered = true

        // 게임 결과를 rankings 테이블에 저장
        sqlitedb.execSQL(query, arrayOf(username, accuracy, timeTaken, isRegistered, incorrectWords))
        sqlitedb.close()
        soundPool.play(soundEffect3, 1.0f, 1.0f, 0, 0, 1.0f)

        // 랭킹 데이터 갱신
        loadRankingData()
        nameInput.isEnabled = false
    }

}
