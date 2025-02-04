package com.android.hunminjeongeumapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DataListAdapter(private val itemList: MutableList<DataItem>) :
    RecyclerView.Adapter<DataListAdapter.ViewHolder>() {

    private val selectedItems = mutableSetOf<DataItem>() // Set으로 변경하여 중복 방지

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textQuestion: TextView = view.findViewById(R.id.text_question)
        val textAnswer: TextView = view.findViewById(R.id.text_answer)
        val textExtra1: TextView = view.findViewById(R.id.text_extra1)
        val textExtra2: TextView = view.findViewById(R.id.text_extra2)
        val textExtra3: TextView = view.findViewById(R.id.text_extra3)
        val checkBox: CheckBox = view.findViewById(R.id.checkbox)

        fun bind(item: DataItem) {
            textQuestion.text = "문제: ${item.question}"
            textAnswer.text = "정답: ${item.answer}"

            if (item.extra3 != null) { // 초성 놀이 (힌트2가 있는 경우)
                textExtra1.text = item.extra1?.let { "뜻: $it" } ?: ""
                textExtra2.text = item.extra2?.let { "힌트1: $it" } ?: ""
                textExtra3.text = item.extra3?.let { "힌트2: $it" } ?: ""
            } else { // 유의어 놀이 (힌트2가 없음)
                textExtra1.text = item.extra1?.let { "선지1: $it" } ?: ""
                textExtra2.text = item.extra2?.let { "선지2: $it" } ?: ""
                textExtra3.text = "" // 유의어 놀이엔 힌트2가 없으므로 빈값
            }

            // 체크박스 상태 설정
            checkBox.isChecked = selectedItems.contains(item)

            // 체크박스 선택/해제 이벤트 리스너
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selectedItems.add(item) else selectedItems.remove(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_data, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int = itemList.size

    fun getSelectedItems(): List<DataItem> = selectedItems.toList() // 선택된 항목 반환
}
