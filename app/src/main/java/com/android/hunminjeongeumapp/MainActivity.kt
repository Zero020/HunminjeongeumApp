package com.android.hunminjeongeumapp

import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.hunminjeongeumapp.quiz_a.QuizAActivity
import com.android.hunminjeongeumapp.quiz_b.QuizBActivity

class MainActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null  // 배경 음악
    private lateinit var soundPool: SoundPool  // 효과음 관리
    private var soundEffectA: Int = 0  // 액티비티 이동 효과음
    private var soundEffectB: Int = 0  // 힌트 버튼 효과음

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 🎵 배경 음악 설정
        mediaPlayer = MediaPlayer.create(this, R.raw.main_bgm)
        mediaPlayer?.isLooping = true // 무한 반복 재생
        mediaPlayer?.setVolume(1.0f, 1.0f) // 배경음악 볼륨 조정 (효과음과 밸런스 조정)
        mediaPlayer?.start()

        // 🎧 효과음 설정 (SoundPool)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5) // 동시에 5개까지 재생 가능
            .setAudioAttributes(audioAttributes)
            .build()

        // 효과음 로드
        soundEffectA = soundPool.load(this, R.raw.main_select_effect2, 1) // 액티비티 이동 효과음
        soundEffectB = soundPool.load(this, R.raw.main_hint_effect2, 1) // 힌트 버튼 효과음

        // 버튼 초기화
        val buttonToCGame = findViewById<Button>(R.id.button_to_C_Game)
        val buttonToUGame = findViewById<Button>(R.id.button_to_U_Game)
        val buttonToCraft = findViewById<Button>(R.id.button_to_Craft)

        // 버튼 클릭 시 효과음 재생 후 액티비티 이동
        buttonToCGame.setOnClickListener {
            playSoundEffectA { navigateToActivity(QuizAActivity::class.java) }
        }

        buttonToUGame.setOnClickListener {
            playSoundEffectA { navigateToActivity(QuizBActivity::class.java) }
        }

        buttonToCraft.setOnClickListener {
            playSoundEffectA { navigateToActivity(CraftActivity::class.java) }
        }

        // 힌트 버튼 및 텍스트뷰 초기화
        val textviewHint = findViewById<TextView>(R.id.textview_Hint)
        val buttonHintC = findViewById<Button>(R.id.button_Hint_C)
        val buttonHintU = findViewById<Button>(R.id.button_Hint_U)
        val buttonHintCraft = findViewById<Button>(R.id.button_Hint_Craft)

        // 버튼 클릭 시 힌트 변경 + 효과음 재생
        buttonHintC.setOnClickListener {
            playSoundEffectB()
            textviewHint.text = "주어진 초성과\n힌트를 보고\n무슨 단어인지\n맞추는 퀴즈이니라"
        }

        buttonHintU.setOnClickListener {
            playSoundEffectB()
            textviewHint.text = "주어진 단어의\n유의어를 맞추는\n게임이니라"
        }

        buttonHintCraft.setOnClickListener {
            playSoundEffectB()
            textviewHint.text = "초성 놀이와\n유의어 찾기 놀이의\n문제를 직접 등록\n할 수 있느니라"
        }
    }

    // 🎵 A 효과음 (버튼 클릭 후 이동)
    private fun playSoundEffectA(onComplete: () -> Unit) {
        soundPool.play(soundEffectA, 1.0f, 1.0f, 1, 0, 1.0f)
        val soundDurationMs = 500L // 효과음 지속 시간
        findViewById<Button>(R.id.button_to_C_Game).postDelayed({ onComplete() }, soundDurationMs)
    }

    // 🎵 B 효과음 (힌트 버튼 클릭 시)
    private fun playSoundEffectB() {
        soundPool.play(soundEffectB, 1.0f, 1.0f, 1, 0, 1.0f)
    }

    // 액티비티 이동 함수
    private fun navigateToActivity(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release() // 배경음악 해제
        mediaPlayer = null
        soundPool.release() // 효과음 해제
    }
}
