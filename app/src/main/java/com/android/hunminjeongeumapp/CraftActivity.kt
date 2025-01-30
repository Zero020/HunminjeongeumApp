package com.android.hunminjeongeumapp

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.hunminjeongeumapp.quiz_a.QuizADBManager

class CraftActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_craft)

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

        // 처음 액티비티가 실행될 때 모든 입력 필드 숨기기
        val allViews = listOf(
            textViewCraftC1, textViewCraftC2, textViewCraftC3,
            editTextCraftC1, editTextCraftC2, editTextCraftC3,
            textViewCraftU1, textViewCraftU2, textViewCraftU3,
            editTextCraftU1, editTextCraftU2, editTextCraftU3
        )
        allViews.forEach { it.visibility = View.INVISIBLE }

        // 초성 놀이 버튼 클릭 이벤트
        buttonCraftCGame.setOnClickListener {
            // 유의어 입력 필드 초기화
            editTextCraftU1.text.clear()
            editTextCraftU2.text.clear()
            editTextCraftU3.text.clear()

            // 초성 놀이 관련 요소 보이기, 유의어 놀이 관련 요소 숨기기
            setVisibilityForGameType(true, textViewCraftC1, textViewCraftC2, textViewCraftC3, editTextCraftC1, editTextCraftC2, editTextCraftC3,
                textViewCraftU1, textViewCraftU2, textViewCraftU3, editTextCraftU1, editTextCraftU2, editTextCraftU3)
        }

        // 유의어 놀이 버튼 클릭 이벤트
        buttonCraftUGame.setOnClickListener {
            // 초성 입력 필드 초기화
            editTextCraftC1.text.clear()
            editTextCraftC2.text.clear()
            editTextCraftC3.text.clear()

            // 유의어 놀이 관련 요소 보이기, 초성 놀이 관련 요소 숨기기
            setVisibilityForGameType(false, textViewCraftC1, textViewCraftC2, textViewCraftC3, editTextCraftC1, editTextCraftC2, editTextCraftC3,
                textViewCraftU1, textViewCraftU2, textViewCraftU3, editTextCraftU1, editTextCraftU2, editTextCraftU3)
        }

        // 데이터 저장 버튼 클릭 이벤트
        buttonCraftApply.setOnClickListener {
            val questionText = editTextCraftC1.text.toString().trim() // 초성 입력
            val descriptionText = editTextCraftC3.text.toString().trim() // 단어 뜻 입력
            val answerText = editTextCraftC2.text.toString().trim() // 정답 단어 입력

            // 필수 입력값 체크
            if (questionText.isEmpty() || descriptionText.isEmpty() || answerText.isEmpty()) {
                Toast.makeText(this, "모든 항목을 채워주거라", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // hint1: 단어의 첫 글자 + 초성의 첫 글자 제거 후 결합
            val hint1 = answerText.first() + questionText.drop(1)

            // hint2: 초성의 마지막 글자 제거 + 단어의 마지막 글자 결합
            val hint2 = questionText.dropLast(1) + answerText.last()

            // 데이터베이스 저장
            val dbManager = QuizADBManager(this, "quizA.db", null, 1)
            val sqlitedb = dbManager.writableDatabase

            val values = ContentValues().apply {
                put("description", descriptionText) // 뜻
                put("question", questionText) // 초성
                put("hint1", hint1) // 힌트1
                put("hint2", hint2) // 힌트2
                put("answer", answerText) // 정답
            }
            sqlitedb.insert("questions", null, values)
            sqlitedb.close()

            // 저장 완료 메시지
            Toast.makeText(this, "저장되었느니라", Toast.LENGTH_SHORT).show()

            // 입력 필드 초기화
            editTextCraftC1.text.clear()
            editTextCraftC2.text.clear()
            editTextCraftC3.text.clear()
        }

        // 메인 화면으로 이동
        buttonCraftToMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // 현재 액티비티 종료 (선택 사항)
        }
    }

    // 초성 놀이 & 유의어 놀이 전환 함수
    private fun setVisibilityForGameType(
        isChosungGame: Boolean,
        textViewCraftC1: View, textViewCraftC2: View, textViewCraftC3: View,
        editTextCraftC1: View, editTextCraftC2: View, editTextCraftC3: View,
        textViewCraftU1: View, textViewCraftU2: View, textViewCraftU3: View,
        editTextCraftU1: View, editTextCraftU2: View, editTextCraftU3: View
    ) {
        if (isChosungGame) {
            // 초성 놀이 보이기
            textViewCraftC1.visibility = View.VISIBLE
            textViewCraftC2.visibility = View.VISIBLE
            textViewCraftC3.visibility = View.VISIBLE
            editTextCraftC1.visibility = View.VISIBLE
            editTextCraftC2.visibility = View.VISIBLE
            editTextCraftC3.visibility = View.VISIBLE

            // 유의어 놀이 숨기기
            textViewCraftU1.visibility = View.INVISIBLE
            textViewCraftU2.visibility = View.INVISIBLE
            textViewCraftU3.visibility = View.INVISIBLE
            editTextCraftU1.visibility = View.INVISIBLE
            editTextCraftU2.visibility = View.INVISIBLE
            editTextCraftU3.visibility = View.INVISIBLE
        } else {
            // 유의어 놀이 보이기
            textViewCraftU1.visibility = View.VISIBLE
            textViewCraftU2.visibility = View.VISIBLE
            textViewCraftU3.visibility = View.VISIBLE
            editTextCraftU1.visibility = View.VISIBLE
            editTextCraftU2.visibility = View.VISIBLE
            editTextCraftU3.visibility = View.VISIBLE

            // 초성 놀이 숨기기
            textViewCraftC1.visibility = View.INVISIBLE
            textViewCraftC2.visibility = View.INVISIBLE
            textViewCraftC3.visibility = View.INVISIBLE
            editTextCraftC1.visibility = View.INVISIBLE
            editTextCraftC2.visibility = View.INVISIBLE
            editTextCraftC3.visibility = View.INVISIBLE
        }
    }
}
