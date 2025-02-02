package com.android.hunminjeongeumapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class StartappActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startapp)

        // button_StartApp 클릭 시 MainActivity로 이동
        val buttonStartApp = findViewById<ImageButton>(R.id.button_StartApp)
        buttonStartApp.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // 현재 액티비티 종료
        }
    }
}
