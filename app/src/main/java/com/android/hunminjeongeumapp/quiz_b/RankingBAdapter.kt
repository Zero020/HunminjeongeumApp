package com.android.hunminjeongeumapp.quiz_b

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.hunminjeongeumapp.R

// 랭킹 데이터 모델 클래스
data class Rank(
    val position: Int,
    val username: String,
    val score: Int
)

class RankingBAdapter(private val rankingList: List<Rank>) : RecyclerView.Adapter<RankingBAdapter.RankingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ranking_b, parent, false)
        return RankingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        val rank = rankingList[position]
        holder.bind(rank)
    }

    override fun getItemCount(): Int {
        return rankingList.size
    }

    inner class RankingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rankPosition: TextView = itemView.findViewById(R.id.rankPosition)
        private val username: TextView = itemView.findViewById(R.id.username)
        private val score: TextView = itemView.findViewById(R.id.score)

        fun bind(rank: Rank) {
            rankPosition.text = rank.position.toString()
            username.text = rank.username
            score.text = "${rank.score} 점"
        }
    }
}