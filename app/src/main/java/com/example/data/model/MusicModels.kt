package com.example.data.model

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val contentUri: Uri,
    val albumArtUri: Uri? = null,
    val dateAdded: Long = 0L,
    val folderName: String = "Internal",
    val isFavorite: Boolean = false,
    val sizeBytes: Long = 0L,
    val bitRateKbps: Int = 320,
    val isSample: Boolean = false
) {
    val durationFormatted: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val remainingSeconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, remainingSeconds)
        }
}

data class Artist(
    val name: String,
    val songCount: Int,
    val albumsCount: Int = 1
)

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val songCount: Int,
    val albumArtUri: Uri? = null,
    val year: Int = 2026
)

data class Folder(
    val name: String,
    val path: String,
    val songCount: Int
)

data class Playlist(
    val id: Long,
    val name: String,
    val songCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val coverSongId: Long? = null
)

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

enum class SortOption {
    TITLE_ASC,
    ARTIST_ASC,
    DURATION_DESC,
    DATE_ADDED_DESC
}
