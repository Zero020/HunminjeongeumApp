package com.android.hunminjeongeumapp.quiz_b

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.database.sqlite.SQLiteDatabase
import android.database.Cursor
import android.widget.ImageButton
import com.android.hunminjeongeumapp.MainActivity
import com.android.hunminjeongeumapp.R

class RankingBActivity : AppCompatActivity() {

    private lateinit var rankingRecyclerView: RecyclerView
    private lateinit var nameInput: EditText // 유저 이름 입력
    private lateinit var inputButton: Button // 저장 버튼
    private lateinit var resultText: TextView // 점수 표시
    private lateinit var homeButton: ImageButton // 홈 버튼

    private var rankingList = mutableListOf<Rank>()
    private var receivedScore: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_branking)

        rankingRecyclerView = findViewById(R.id.rankingList)
        nameInput = findViewById(R.id.nameInput)
        inputButton = findViewById(R.id.inputButton)
        resultText = findViewById(R.id.ResultText)
        homeButton = findViewById(R.id.homeButton)

        rankingRecyclerView.layoutManager = LinearLayoutManager(this)

        // QuizBActivity에서 전달된 점수 가져오기
        receivedScore = intent.getIntExtra("score", 0)
        resultText.text = "당신의 점수: $receivedScore 점"

        // 랭킹 DB에서 데이터 로드
        loadRankingData()

        // 이름 입력 후 등록 버튼 클릭 시 랭킹 저장
        inputButton.setOnClickListener {
            val username = nameInput.text.toString()
            if (username.isNotEmpty()) {
                saveGameResult(username, receivedScore)
                Toast.makeText(this, "랭킹에 저장되었습니다!", Toast.LENGTH_SHORT).show()
                nameInput.isEnabled = false
                inputButton.isEnabled = false
            } else {
                Toast.makeText(this, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 홈 버튼 클릭 시 MainActivity로 이동
        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // 랭킹 데이터 로드 (점수 순으로 정렬)
    private fun loadRankingData() {
        val dbManager = QuizBDBManager(this, "quizB.db", null, 1)
        val sqlitedb: SQLiteDatabase = dbManager.readableDatabase

        val cursor: Cursor = sqlitedb.rawQuery("SELECT * FROM rankings ORDER BY score DESC LIMIT 5", null)

        rankingList.clear()
        while (cursor.moveToNext()) {
            val username = cursor.getString(cursor.getColumnIndexOrThrow("username"))
            val score = cursor.getInt(cursor.getColumnIndexOrThrow("score"))
            rankingList.add(Rank(rankingList.size + 1, username, score))
        }

        cursor.close()
        sqlitedb.close()

        rankingRecyclerView.adapter = RankingBAdapter(rankingList)
    }

    // 게임 결과 저장
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
    }
}
