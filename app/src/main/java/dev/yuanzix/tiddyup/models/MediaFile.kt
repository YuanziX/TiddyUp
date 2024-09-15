package dev.yuanzix.tiddyup.models

import android.net.Uri

data class MediaFile(
    val uri: Uri,
    val name: String,
    val type: MediaType,
)
