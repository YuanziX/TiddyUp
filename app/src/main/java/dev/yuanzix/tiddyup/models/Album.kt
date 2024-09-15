package dev.yuanzix.tiddyup.models

import android.net.Uri

data class Album(
    val imageCount: Int,
    val name: String,
    val id: Long,
    val thumbnailUri: Uri,
    val filterCriteria: FilterCriteria = FilterCriteria.ALBUM,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Album) return false
        if (this.id == other.id) return true
        return false
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
