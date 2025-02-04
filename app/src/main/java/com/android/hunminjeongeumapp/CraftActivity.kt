package com.android.hunminjeongeumapp

import android.content.ContentValues
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.hunminjeongeumapp.quiz_a.QuizADBManager
import com.android.hunminjeongeumapp.quiz_b.QuizBDBManager

class CraftActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var isChosungGameSelected = true // 초성 놀이 기본 선택

    private lateinit var soundPool: SoundPool
    private var cEffect = 0
    private var uEffect = 0
    private var backButtonEffect = 0
    private var registButtonEffect = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_craft)

        // 배경 음악 설정
        mediaPlayer = MediaPlayer.create(this, R.raw.craft_bgm)
        mediaPlayer?.isLooping = true // 무한 반복 재생
        mediaPlayer?.setVolume(1.0f, 1.0f) // 좌우 볼륨 50%로 설정
        mediaPlayer?.start()


        // SoundPool 초기화
        soundPool = SoundPool.Builder()
            .setMaxStreams(4) // 최대 동시 재생 개수
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()

        // 효과음 로드
        cEffect = soundPool.load(this, R.raw.craft_c_game2, 1)
        uEffect = soundPool.load(this, R.raw.craft_u_game2, 1)
        backButtonEffect = soundPool.load(this, R.raw.craft_back2, 1)
        registButtonEffect = soundPool.load(this, R.raw.craft_regist2, 1)

        // 버튼
        val buttonCraftCGame: Button = findViewById(R.id.button_Craft_C_Game)
        val buttonCraftUGame: Button = findViewById(R.id.button_Craft_U_Game)
        val buttonCraftApply: Button = findViewById(R.id.button_Craft_apply)
        val buttonCraftToMain: Button = findViewById(R.id.button_Craft_to_Main)

        // 초성 놀이 관련 요소
        val textViewCraftC1 = findViewById<View>(R.id.textView_Craft_C_1)
        val textViewCraftC2 = findViewById<View>(R.id.textView_Craft_C_2)
        val textViewCraftC3 = findViewById<View>(R.id.textView_Craft_C_3)
        val editTextCraftC1 = findViewById<EditText>(R.id.editTextText_Craft_C_1)
        val editTextCraftC2 = findViewById<EditText>(R.id.editTextText_Craft_C_2)
        val editTextCraftC3 = findViewById<EditText>(R.id.editTextText_Craft_C_3)

        // 유의어 놀이 관련 요소
        val textViewCraftU1 = findViewById<View>(R.id.textView_Craft_U_1)
        val textViewCraftU2 = findViewById<View>(R.id.textView_Craft_U_2)
        val textViewCraftU3 = findViewById<View>(R.id.textView_Craft_U_3)
        val editTextCraftU1 = findViewById<EditText>(R.id.editTextText_Craft_U_1)
        val editTextCraftU2 = findViewById<EditText>(R.id.editTextText_Craft_U_2)
        val editTextCraftU3 = findViewById<EditText>(R.id.editTextText_Craft_U_3)

        // 초기 버튼 투명도 설정
        buttonCraftCGame.alpha = 1.0f // 기본 선택
        buttonCraftUGame.alpha = 0.6f

        // 초성 놀이 UI를 보이게 설정 (처음 실행 시)
        setVisibilityForGameType(true, textViewCraftC1, textViewCraftC2, textViewCraftC3,
            editTextCraftC1, editTextCraftC2, editTextCraftC3,
            textViewCraftU1, textViewCraftU2, textViewCraftU3,
            editTextCraftU1, editTextCraftU2, editTextCraftU3)

        // 초성 놀이 버튼 클릭 이벤트
        buttonCraftCGame.setOnClickListener {
            soundPool.play(cEffect, 1.0f, 1.0f, 0, 0, 1.0f) // 🎵 초성 놀이 효과음 재생
            isChosungGameSelected = true

            buttonCraftCGame.alpha = 1.0f
            buttonCraftUGame.alpha = 0.6f

            // 유의어 입력 필드 초기화
            editTextCraftU1.text.clear()
            editTextCraftU2.text.clear()
            editTextCraftU3.text.clear()

            setVisibilityForGameType(true, textViewCraftC1, textViewCraftC2, textViewCraftC3, editTextCraftC1, editTextCraftC2, editTextCraftC3,
                textViewCraftU1, textViewCraftU2, textViewCraftU3, editTextCraftU1, editTextCraftU2, editTextCraftU3)
        }

        // 유의어 놀이 버튼 클릭 이벤트
        buttonCraftUGame.setOnClickListener {
            soundPool.play(uEffect, 1.0f, 1.0f, 0, 0, 1.0f) // 🎵 유의어 놀이 효과음 재생
            isChosungGameSelected = false

            buttonCraftUGame.alpha = 1.0f
            buttonCraftCGame.alpha = 0.6f

            // 초성 입력 필드 초기화
            editTextCraftC1.text.clear()
            editTextCraftC2.text.clear()
            editTextCraftC3.text.clear()

            setVisibilityForGameType(false, textViewCraftC1, textViewCraftC2, textViewCraftC3, editTextCraftC1, editTextCraftC2, editTextCraftC3,
                textViewCraftU1, textViewCraftU2, textViewCraftU3, editTextCraftU1, editTextCraftU2, editTextCraftU3)
        }

        // 데이터 저장 버튼 클릭 이벤트
        buttonCraftApply.setOnClickListener {
            soundPool.play(registButtonEffect, 1.0f, 1.0f, 0, 0, 1.0f) // 🎵 데이터 저장 효과음 재생
            if (isChosungGameSelected) {
                // 초성 놀이 데이터 저장
                val questionText = editTextCraftC1.text.toString().trim()
                val descriptionText = editTextCraftC3.text.toString().trim()
                val answerText = editTextCraftC2.text.toString().trim()

                if (questionText.isEmpty() || descriptionText.isEmpty() || answerText.isEmpty()) {
                    Toast.makeText(this, "모든 항목을 채워주거라", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val hint1 = answerText.first() + questionText.drop(1)
                val hint2 = questionText.dropLast(1) + answerText.last()

                val dbManager = QuizADBManager(this, "quizA.db", null, 1)
                val sqlitedb = dbManager.writableDatabase

                val values = ContentValues().apply {
                    put("description", descriptionText)
                    put("question", questionText)
                    put("hint1", hint1)
                    put("hint2", hint2)
                    put("answer", answerText)
                }
                sqlitedb.insert("questions", null, values)
                sqlitedb.close()

                Toast.makeText(this, "초성 놀이 데이터가 저장됐노라", Toast.LENGTH_SHORT).show()

                editTextCraftC1.text.clear()
                editTextCraftC2.text.clear()
                editTextCraftC3.text.clear()
            } else {
                // 유의어 놀이 데이터 저장
                val questionText = editTextCraftU1.text.toString().trim()
                val item1Text = editTextCraftU2.text.toString().trim()
                val item2Text = editTextCraftU3.text.toString().trim()

                if (questionText.isEmpty() || item1Text.isEmpty() || item2Text.isEmpty()) {
                    Toast.makeText(this, "모든 항목을 채워주거라", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val dbManager = QuizBDBManager(this, "quizB.db", null, 1)
                val sqlitedb = dbManager.writableDatabase

                val values = ContentValues().apply {
                    put("question", questionText)
                    put("item1", item1Text)
                    put("item2", item2Text)
                    put("answer", 0) // 정답 인덱스는 항상 0
                }
                sqlitedb.insert("questions", null, values)
                sqlitedb.close()

                Toast.makeText(this, "유의어 놀이 데이터가 저장됐노라", Toast.LENGTH_SHORT).show()

                editTextCraftU1.text.clear()
                editTextCraftU2.text.clear()
                editTextCraftU3.text.clear()
            }
        }

        // 메인 화면으로 이동
        buttonCraftToMain.setOnClickListener {
            soundPool.play(backButtonEffect, 1.0f, 1.0f, 0, 0, 1.0f) // 🎵 메인으로 가기 효과음 재생
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 버튼 초기화
        val buttonToDataList: Button = findViewById(R.id.button_to_datalist)

        // 버튼 클릭 시 DataListActivity로 이동
        buttonToDataList.setOnClickListener {
            val intent = Intent(this, DataListActivity::class.java)
            startActivity(intent)

        }

    }

    private fun setVisibilityForGameType(
        isChosungGame: Boolean,
        textViewCraftC1: View, textViewCraftC2: View, textViewCraftC3: View,
        editTextCraftC1: View, editTextCraftC2: View, editTextCraftC3: View,
        textViewCraftU1: View, textViewCraftU2: View, textViewCraftU3: View,
        editTextCraftU1: View, editTextCraftU2: View, editTextCraftU3: View
    ) {
        if (isChosungGame) {
            // 초성 놀이 관련 요소 보이기
            textViewCraftC1.visibility = View.VISIBLE
            textViewCraftC2.visibility = View.VISIBLE
            textViewCraftC3.visibility = View.VISIBLE
            editTextCraftC1.visibility = View.VISIBLE
            editTextCraftC2.visibility = View.VISIBLE
            editTextCraftC3.visibility = View.VISIBLE

            // 유의어 놀이 관련 요소 숨기기
            textViewCraftU1.visibility = View.INVISIBLE
            textViewCraftU2.visibility = View.INVISIBLE
            textViewCraftU3.visibility = View.INVISIBLE
            editTextCraftU1.visibility = View.INVISIBLE
            editTextCraftU2.visibility = View.INVISIBLE
            editTextCraftU3.visibility = View.INVISIBLE
        } else {
            // 유의어 놀이 관련 요소 보이기
            textViewCraftU1.visibility = View.VISIBLE
            textViewCraftU2.visibility = View.VISIBLE
            textViewCraftU3.visibility = View.VISIBLE
            editTextCraftU1.visibility = View.VISIBLE
            editTextCraftU2.visibility = View.VISIBLE
            editTextCraftU3.visibility = View.VISIBLE

            // 초성 놀이 관련 요소 숨기기
            textViewCraftC1.visibility = View.INVISIBLE
            textViewCraftC2.visibility = View.INVISIBLE
            textViewCraftC3.visibility = View.INVISIBLE
            editTextCraftC1.visibility = View.INVISIBLE
            editTextCraftC2.visibility = View.INVISIBLE
            editTextCraftC3.visibility = View.INVISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release() // SoundPool 해제
        mediaPlayer?.release() // 배경음악 해제
        mediaPlayer = null
    }

}
