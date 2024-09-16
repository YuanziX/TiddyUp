package dev.yuanzix.tiddyup.navigation

import dev.yuanzix.tiddyup.models.FilterCriteria
import kotlinx.serialization.Serializable

@Serializable
object Base

@Serializable
object Home

@Serializable
data class Cleanup(
    val filterCriteria: FilterCriteria,
    val albumId: Long = -1,
    val albumName: String? = null,
    val month: String? = null,
)