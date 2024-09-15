package dev.yuanzix.tiddyup.models

import android.net.Uri

data class Month(
    val month: String, // MMMM yyyy
    val thumbnailUri: Uri,
    val filterCriteria: FilterCriteria = FilterCriteria.MONTH,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Month) return false
        if (this.month == other.month) return true
        return false
    }

    override fun hashCode(): Int {
        return month.hashCode()
    }
}
