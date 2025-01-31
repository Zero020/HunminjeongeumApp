package com.android.hunminjeongeumapp.quiz_a

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.hunminjeongeumapp.R

data class Rank(
    val position: Int,
    val username: String,
    val timeTaken: Int,
    val accuracy: Float
)

class RankingAAdapter(private val rankingList: List<Rank>) : RecyclerView.Adapter<RankingAAdapter.RankingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ranking_a, parent, false)
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
        private val timeTaken: TextView = itemView.findViewById(R.id.timeTaken)
        private val accuracy: TextView = itemView.findViewById(R.id.accuracy)

        fun bind(rank: Rank) {
            rankPosition.text = rank.position.toString()
            username.text = rank.username

            timeTaken.text = "${rank.timeTaken}s"

            accuracy.text = "(${String.format("%.2f", rank.accuracy * 100)}%)"
        }
    }
}
