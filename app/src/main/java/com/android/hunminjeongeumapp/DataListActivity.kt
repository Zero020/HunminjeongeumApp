package com.android.hunminjeongeumapp

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.hunminjeongeumapp.quiz_a.QuizADBManager
import com.android.hunminjeongeumapp.quiz_b.QuizBDBManager

class DataListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DataListAdapter
    private var dataList = mutableListOf<DataItem>()
    private var isChosungSelected = true // 기본 초성 놀이 선택

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_list)

        // UI 요소 초기화
        recyclerView = findViewById(R.id.recycler_view)
        val buttonShowChosung: Button = findViewById(R.id.button_show_chosung)
        val buttonShowSynonym: Button = findViewById(R.id.button_show_synonym)
        val buttonDeleteSelected: Button = findViewById(R.id.button_delete_selected)
        val buttonBackToCraft: Button = findViewById(R.id.button_back_to_craft)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = DataListAdapter(dataList)
        recyclerView.adapter = adapter

        // 초기 데이터 로드 (초성 놀이)
        loadChosungData()
        updateButtonTransparency(buttonShowChosung, buttonShowSynonym)

        // 초성 놀이 버튼 클릭 시
        buttonShowChosung.setOnClickListener {
            isChosungSelected = true
            loadChosungData()
            updateButtonTransparency(buttonShowChosung, buttonShowSynonym)
        }

        // 유의어 놀이 버튼 클릭 시
        buttonShowSynonym.setOnClickListener {
            isChosungSelected = false
            loadSynonymData()
            updateButtonTransparency(buttonShowChosung, buttonShowSynonym)
        }

        // 선택된 데이터 삭제 버튼 클릭 시
        buttonDeleteSelected.setOnClickListener {
            deleteSelectedItems()
        }

        // 등록 화면으로 이동 버튼 클릭 시
        buttonBackToCraft.setOnClickListener {
            val intent = Intent(this, CraftActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // 버튼 투명도 조절 함수
    private fun updateButtonTransparency(buttonChosung: Button, buttonSynonym: Button) {
        if (isChosungSelected) {
            buttonChosung.alpha = 1.0f // 선택된 버튼: 불투명
            buttonSynonym.alpha = 0.6f // 비활성 버튼: 투명
        } else {
            buttonChosung.alpha = 0.6f
            buttonSynonym.alpha = 1.0f
        }
    }

    // 초성 놀이 데이터 로드 (모든 컬럼 포함)
    private fun loadChosungData() {
        dataList.clear()
        val dbManager = QuizADBManager(this, "quizA.db", null, 1)
        val sqlitedb: SQLiteDatabase = dbManager.readableDatabase
        val cursor = sqlitedb.rawQuery("SELECT * FROM questions", null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val question = cursor.getString(cursor.getColumnIndexOrThrow("question"))
            val answer = cursor.getString(cursor.getColumnIndexOrThrow("answer"))
            val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
            val hint1 = cursor.getString(cursor.getColumnIndexOrThrow("hint1"))
            val hint2 = cursor.getString(cursor.getColumnIndexOrThrow("hint2")) // hint2 추가

            dataList.add(DataItem(id, question, answer, description, hint1, hint2))
        }
        cursor.close()
        sqlitedb.close()

        adapter.notifyDataSetChanged()
    }

    // 유의어 놀이 데이터 로드 (모든 컬럼 포함)
    private fun loadSynonymData() {
        dataList.clear()
        val dbManager = QuizBDBManager(this, "quizB.db", null, 1)
        val sqlitedb: SQLiteDatabase = dbManager.readableDatabase
        val cursor = sqlitedb.rawQuery("SELECT * FROM questions", null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val question = cursor.getString(cursor.getColumnIndexOrThrow("question"))
            val item1 = cursor.getString(cursor.getColumnIndexOrThrow("item1"))
            val item2 = cursor.getString(cursor.getColumnIndexOrThrow("item2"))

            dataList.add(DataItem(id, question, item1, item1, item2, null))
        }
        cursor.close()
        sqlitedb.close()

        adapter.notifyDataSetChanged()
    }

    // 선택된 데이터 삭제
    private fun deleteSelectedItems() {
        val selectedItems = adapter.getSelectedItems()

        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "삭제할 항목을 선택해주거라", Toast.LENGTH_SHORT).show()
            return
        }

        val dbManager = if (isChosungSelected) QuizADBManager(this, "quizA.db", null, 1) else QuizBDBManager(this, "quizB.db", null, 1)
        val sqlitedb = dbManager.writableDatabase

        for (item in selectedItems) {
            sqlitedb.delete("questions", "id = ?", arrayOf(item.id.toString()))
        }

        sqlitedb.close()
        Toast.makeText(this, "삭제하였노라!", Toast.LENGTH_SHORT).show()

        if (isChosungSelected) loadChosungData() else loadSynonymData()
    }
}
