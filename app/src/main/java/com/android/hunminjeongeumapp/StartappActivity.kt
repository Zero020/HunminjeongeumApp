package com.android.hunminjeongeumapp

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class StartappActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var soundEffect: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startapp)

        // 배경 음악 설정
        mediaPlayer = MediaPlayer.create(this, R.raw.startapp_bgm)
        mediaPlayer?.isLooping = true // 무한 반복 재생
        mediaPlayer?.setVolume(1.0f, 1.0f) // 볼륨 설정
        mediaPlayer?.start()

        // button_StartApp 클릭 시 효과음 재생 후 MainActivity로 이동
        val buttonStartApp = findViewById<ImageButton>(R.id.button_StartApp)
        buttonStartApp.setOnClickListener {
            playButtonSoundAndGoToMain()
        }
    }

    private fun playButtonSoundAndGoToMain() {
        // 효과음 재생
        soundEffect = MediaPlayer.create(this, R.raw.startapp_buttonsound)
        soundEffect?.setVolume(100.0f, 100.0f) // 볼륨 설정
        soundEffect?.setOnCompletionListener {
            // 효과음이 끝난 후 MainActivity로 이동
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // 현재 액티비티 종료
        }
        soundEffect?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release() // 배경음악 해제
        mediaPlayer = null

        soundEffect?.release() // 효과음 해제
        soundEffect = null
    }
}
