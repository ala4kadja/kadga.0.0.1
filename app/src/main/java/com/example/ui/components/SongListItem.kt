package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.Song
import com.example.ui.theme.*

@Composable
fun SongListItem(
    song: Song,
    isCurrentSong: Boolean,
    isPlaying: Boolean,
    onSongClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onAddToPlaylistClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    val cardBg by animateColorAsState(
        targetValue = if (isCurrentSong) KadjaSurfaceElevated else KadjaSurface.copy(alpha = 0.7f),
        label = "card_bg"
    )

    val borderColor = if (isCurrentSong) KadjaPrimary.copy(alpha = 0.8f) else Color.Transparent

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onSongClick)
            .testTag("song_item_${song.id}"),
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album Art or Thumbnail
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(KadjaPrimaryVariant, KadjaSecondaryVariant)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (song.albumArtUri != null) {
                    AsyncImage(
                        model = song.albumArtUri,
                        contentDescription = "Album art for ${song.title}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.default_album_art)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                if (isCurrentSong) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        AudioVisualizerBars(
                            isPlaying = isPlaying,
                            barColor = KadjaPrimaryGlow,
                            barCount = 3
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Song Info (Title & Artist)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.title,
                    color = if (isCurrentSong) KadjaPrimaryGlow else KadjaTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = if (isCurrentSong) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = song.artist,
                        color = KadjaTextSecondary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = " • ",
                        color = KadjaTextTertiary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = song.durationFormatted,
                        color = KadjaTextTertiary,
                        fontSize = 12.sp
                    )
                }
            }

            // Favorite Button
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("fav_button_${song.id}")
            ) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (song.isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (song.isFavorite) KadjaTertiary else KadjaTextTertiary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // 3-Dots Menu
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("more_menu_${song.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = KadjaTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.background(KadjaSurfaceElevated)
                ) {
                    DropdownMenuItem(
                        text = { Text("إضافة لقائمة تشغيل", color = KadjaTextPrimary) },
                        leadingIcon = {
                            Icon(Icons.Default.PlaylistAdd, contentDescription = null, tint = KadjaSecondary)
                        },
                        onClick = {
                            menuExpanded = false
                            onAddToPlaylistClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (song.isFavorite) "إزالة من المفضلة" else "إضافة للمفضلة", color = KadjaTextPrimary) },
                        leadingIcon = {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = KadjaTertiary)
                        },
                        onClick = {
                            menuExpanded = false
                            onFavoriteClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("معلومات الأغنية", color = KadjaTextPrimary) },
                        leadingIcon = {
                            Icon(Icons.Default.Info, contentDescription = null, tint = KadjaPrimary)
                        },
                        onClick = {
                            menuExpanded = false
                            showInfoDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text(song.title, fontWeight = FontWeight.Bold, color = KadjaTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("الفنان: ${song.artist}", color = KadjaTextSecondary)
                    Text("الألبوم: ${song.album}", color = KadjaTextSecondary)
                    Text("المجلد: ${song.folderName}", color = KadjaTextSecondary)
                    Text("المدة: ${song.durationFormatted}", color = KadjaTextSecondary)
                    if (song.sizeBytes > 0) {
                        val mb = String.format("%.2f MB", song.sizeBytes / (1024.0 * 1024.0))
                        Text("الحجم: $mb", color = KadjaTextSecondary)
                    }
                    Text("الجودة: 320 kbps HD Audio", color = KadjaPrimaryGlow)
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("إغلاق", color = KadjaPrimary)
                }
            },
            containerColor = KadjaSurfaceElevated
        )
    }
}
