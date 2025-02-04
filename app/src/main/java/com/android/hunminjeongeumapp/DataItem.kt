package com.android.hunminjeongeumapp

data class DataItem(
    val id: Int,       // 고유 ID
    val question: String,
    val answer: String,
    val extra1: String?,
    val extra2: String?,
    val extra3: String?
) {
    override fun equals(other: Any?): Boolean {
        return other is DataItem && this.id == other.id // ID가 같을 때만 동일 객체로 판단
    }

    override fun hashCode(): Int {
        return id.hashCode() // ID 기반으로 해시 값 반환
    }
}
