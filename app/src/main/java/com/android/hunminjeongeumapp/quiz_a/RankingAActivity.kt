package com.android.hunminjeongeumapp.quiz_a

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.hunminjeongeumapp.R
import android.database.sqlite.SQLiteDatabase
import android.database.Cursor
import android.os.Build
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText.*
import android.widget.ImageButton
import com.android.hunminjeongeumapp.MainActivity

class RankingAActivity : AppCompatActivity() {

    lateinit var rankingRecyclerView: RecyclerView
    lateinit var nameInput: EditText // 유저 이름 입력 받는 EditText

    lateinit var resultText: TextView // 게임 결과 표시할 텍스트뷰
    lateinit var homeButton: ImageButton

    val rankingList = mutableListOf<Rank>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_aranking)

        // findViewById로 뷰 연결
        rankingRecyclerView = findViewById(R.id.rankingList)
        nameInput = findViewById(R.id.nameInput)
        resultText = findViewById(R.id.ResultText)
        homeButton = findViewById(R.id.homeButton)


        // 리사이클러뷰 설정
        rankingRecyclerView.layoutManager = LinearLayoutManager(this)

        var receivedTime = intent.getLongExtra("totalTime", 0L)
        var receivedAccuracy = intent.getFloatExtra("accuracy", 0f)

        val minutes = receivedTime.toInt() / 60
        val seconds = receivedTime.toInt() % 60

        setFullScreen()
        // 랭킹 DB에서 가져오기
        loadRankingData()

        // 조건에 맞지 않으면 이름 입력을 비활성화
        if (receivedTime >= 60) {
            // 시간 초과 또는 정답률 0%인 경우
            Toast.makeText(this, "시간 초과로 순위에 등록하지 못하느리라.", Toast.LENGTH_SHORT).show()

            // 이름 입력 필드 및 버튼 비활성화
            nameInput.isEnabled = false
            nameInput.visibility = EditText.INVISIBLE
            resultText.text = "참으로 아쉽구나, \n 수고많았느리라"

        }else if(receivedAccuracy == 0f){
            Toast.makeText(this, "정답률 0%로 인해 순위에 등록하지 못하느리라.", Toast.LENGTH_SHORT).show()

            // 이름 입력 필드 및 버튼 비활성화
            nameInput.isEnabled = false
            nameInput.visibility = EditText.INVISIBLE
            resultText.text = "참으로 아쉽구나, 수고많았느리라"

        } else {
            // 조건을 만족하면 이름 입력 필드 및 버튼 활성화
            nameInput.isEnabled = true

            if (minutes != 1) {
                resultText.text = String.format("%02d초", seconds) + "동안, " +
                        " ${String.format("%.2f", receivedAccuracy * 100)}%군, \n 수고많았느리라"

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
                } else {
                    Toast.makeText(this, "이름을 입력하거라", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }

            homeButton.setOnClickListener {
                val intent : Intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // 현재 액티비티 종료
            }
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

            // 랭킹 데이터 갱신
            loadRankingData()
            nameInput.isEnabled = false
        }

    }
