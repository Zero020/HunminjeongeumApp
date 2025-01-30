package com.android.hunminjeongeumapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.hunminjeongeumapp.quiz_a.QuizAActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 버튼 초기화
        val buttonToCGame = findViewById<Button>(R.id.button_to_C_Game)
        val buttonToCraft = findViewById<Button>(R.id.button_to_Craft)

        // 버튼 클릭 시 액티비티 이동
        buttonToCGame.setOnClickListener {
            val intent = Intent(this, QuizAActivity::class.java)
            startActivity(intent)
        }

        buttonToCraft.setOnClickListener {
            val intent = Intent(this, CraftActivity::class.java)
            startActivity(intent)
        }


        // 힌트 버튼 및 텍스트뷰 초기화
        val textviewHint = findViewById<TextView>(R.id.textview_Hint)
        val buttonHintC = findViewById<Button>(R.id.button_Hint_C)
        val buttonHintU = findViewById<Button>(R.id.button_Hint_U)
        val buttonHintCraft = findViewById<Button>(R.id.button_Hint_Craft)

        // 버튼 클릭 시 힌트 변경
        buttonHintC.setOnClickListener {
            textviewHint.text = "주어진 초성과\n힌트를 보고\n무슨 단어인지\n맞추는 퀴즈이니라"
        }

        buttonHintU.setOnClickListener {
            textviewHint.text = "주어진 단어의\n유의어를 맞추는\n게임이니라"
        }

        buttonHintCraft.setOnClickListener {
            textviewHint.text = "초성 놀이와\n유의어 찾기 놀이의\n문제를 직접 등록\n할 수 있느니라"
        }
    }
}