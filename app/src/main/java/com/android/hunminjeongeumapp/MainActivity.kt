package com.android.hunminjeongeumapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
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
    }
}