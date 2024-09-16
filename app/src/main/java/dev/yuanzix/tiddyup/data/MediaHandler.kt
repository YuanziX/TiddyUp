package dev.yuanzix.tiddyup.data

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.yuanzix.tiddyup.models.Album
import dev.yuanzix.tiddyup.models.MediaFile
import dev.yuanzix.tiddyup.models.MediaType
import dev.yuanzix.tiddyup.models.Month
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject


class MediaHandler @Inject constructor(
    @ApplicationContext private val ctx: Context,
) {
    private val imageQueryUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        MediaStore.Images.Media.getContentUri("external")
    }

    suspend fun haveImages(): Boolean = withContext(Dispatchers.IO) {
        if (!hasReadPermission()) return@withContext false

        ctx.contentResolver.query(
            imageQueryUri,
            arrayOf(MediaStore.Images.Media._ID),
            "${MediaStore.Images.Media.MIME_TYPE} LIKE ?",
            arrayOf("image/%"),
            null
        )?.use { cursor ->
            cursor.moveToFirst()
        } ?: false
    }

    suspend fun getImageAlbums(): Flow<Album> = flow {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.SIZE
        )

        ctx.contentResolver.query(
            imageQueryUri,
            projection,
            "${MediaStore.Images.Media.MIME_TYPE} LIKE ?",
            arrayOf("image/%"),
            "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val bucketIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
            val bucketNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
            val imageCountColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val bucketId = cursor.getLong(bucketIdColumn)
                val bucketName = cursor.getString(bucketNameColumn)
                val imageCount = cursor.getInt(imageCountColumn)

                if (bucketName != null) {
                    emit(
                        Album(
                            id = bucketId,
                            name = bucketName,
                            imageCount = imageCount,
                            thumbnailUri = ContentUris.withAppendedId(imageQueryUri, id)
                        )
                    )
                }
            }
        }
    }

    suspend fun getImagesInAlbum(bucketId: Long): Flow<MediaFile> = flow {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.MIME_TYPE,
        )

        val selection =
            "${MediaStore.Images.Media.BUCKET_ID} = ? AND ${MediaStore.Images.Media.MIME_TYPE} LIKE ?"
        val selectionArgs = arrayOf(bucketId.toString(), "image/%")
        val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"

        ctx.contentResolver.query(
            imageQueryUri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val mimeType = cursor.getString(mimeTypeColumn)

                if (name != null && mimeType != null) {
                    emit(
                        MediaFile(
                            uri = ContentUris.withAppendedId(imageQueryUri, id),
                            name = name,
                            type = MediaType.IMAGE,
                        )
                    )
                }
            }
        }
    }

    fun getMonthLabels(): Flow<Month> = flow {
        if (!hasReadPermission()) {
            return@flow
        }

        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val seenMonths = mutableSetOf<String>()

        val projection = arrayOf(
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media._ID
        )

        ctx.contentResolver.query(
            imageQueryUri,
            projection,
            "${MediaStore.Images.Media.MIME_TYPE} LIKE ?",
            arrayOf("image/%"),
            "${MediaStore.Images.Media.DATE_TAKEN} DESC"
        )?.use { cursor ->
            val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                var dateTaken = cursor.getLong(dateTakenColumn)

                if (dateTaken == 0L) {
                    dateTaken = cursor.getLong(dateAddedColumn) * 1000L
                }

                val formattedDate = dateFormat.format(Date(dateTaken))

                if (formattedDate !in seenMonths) {
                    seenMonths.add(formattedDate)
                    emit(
                        Month(
                            month = formattedDate,
                            thumbnailUri = ContentUris.withAppendedId(imageQueryUri, id)
                        )
                    )
                }
            }
        }
    }

    fun getImagesForMonth(monthLabel: String): Flow<MediaFile> = flow {
        if (!hasReadPermission()) {
            emitAll(emptyFlow())
            return@flow
        }

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
        )

        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val parsedDate = dateFormat.parse(monthLabel) ?: run {
            emitAll(emptyFlow())
            return@flow
        }

        val calendar = Calendar.getInstance().apply { time = parsedDate }

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        val endOfMonth = calendar.timeInMillis

        val selection = """
        (${MediaStore.Images.Media.DATE_TAKEN} BETWEEN ? AND ? OR 
        ${MediaStore.Images.Media.DATE_ADDED} BETWEEN ? AND ?) AND 
        ${MediaStore.Images.Media.MIME_TYPE} LIKE ?
    """.trimIndent().replace("\n", " ")
        val selectionArgs = arrayOf(
            startOfMonth.toString(), endOfMonth.toString(),
            (startOfMonth / 1000).toString(), (endOfMonth / 1000).toString(),
            "image/%"
        )

        ctx.contentResolver.query(
            imageQueryUri,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Images.Media.DATE_TAKEN} DESC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: continue

                emit(
                    MediaFile(
                        uri = ContentUris.withAppendedId(imageQueryUri, id),
                        name = name,
                        type = MediaType.IMAGE
                    )
                )
            }
        } ?: run {
            emitAll(emptyFlow())
        }
    }

    fun createDeleteRequest(
        mediaFiles: List<MediaFile>,
        launcher: ActivityResultLauncher<IntentSenderRequest>,
    ) {
        val contentResolver = ctx.contentResolver

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pi = MediaStore.createDeleteRequest(contentResolver, mediaFiles.map {
                it.uri
            })
            val request = IntentSenderRequest.Builder(pi.intentSender).build()
            launcher.launch(request)
        } else {
            mediaFiles.forEach { mediaFile ->
                ctx.contentResolver.delete(mediaFile.uri, null, null)
            }
        }
    }

    private fun hasReadPermission(): Boolean {
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            ContextCompat.checkSelfPermission(
                ctx, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else true
    }
}