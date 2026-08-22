package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.RepeatMode
import com.example.data.model.Song
import com.example.ui.components.AudioVisualizerBars
import com.example.ui.components.PulsingMusicGlow
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    song: Song?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    repeatMode: RepeatMode,
    isShuffle: Boolean,
    sleepTimerMinutesLeft: Int?,
    onCollapse: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (song == null) return

    var isUserSeeking by remember { mutableStateOf(false) }
    var seekFraction by remember { mutableFloatStateOf(0f) }

    val actualDuration = if (durationMs > 0) durationMs else song.durationMs
    val currentPosition = if (isUserSeeking) {
        (seekFraction * actualDuration).toLong()
    } else {
        currentPositionMs
    }

    val progressFraction = if (actualDuration > 0) {
        (currentPosition.toFloat() / actualDuration.toFloat()).coerceIn(0f, 1f)
    } else 0f

    // Vinyl spinning animation
    val infiniteTransition = rememberInfiniteTransition(label = "now_playing_vinyl")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "vinyl_rotate"
    )

    fun formatTime(ms: Long): String {
        val totalSec = (ms / 1000).coerceAtLeast(0)
        val m = totalSec / 60
        val s = totalSec % 60
        return String.format("%02d:%02d", m, s)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        KadjaSurfaceVariant,
                        KadjaSurface,
                        KadjaBackground
                    )
                )
            )
            .testTag("now_playing_screen")
    ) {
        // Ambient background glow
        PulsingMusicGlow(
            isPlaying = isPlaying,
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .align(Alignment.TopCenter)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCollapse,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(KadjaSurfaceElevated.copy(alpha = 0.8f))
                        .testTag("collapse_now_playing")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse Player",
                        tint = KadjaTextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "جاري التشغيل",
                        color = KadjaPrimaryGlow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = song.album,
                        color = KadjaTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onOpenQueue,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(KadjaSurfaceElevated.copy(alpha = 0.8f))
                            .testTag("now_playing_queue")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = "Queue",
                            tint = KadjaSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Large Animated Album Art / Vinyl
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .shadow(32.dp, CircleShape, spotColor = KadjaPrimary.copy(alpha = 0.6f))
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                KadjaSurfaceElevated,
                                KadjaBackground,
                                Color.Black
                            )
                        )
                    )
                    .rotate(if (isPlaying) rotationAngle else 0f),
                contentAlignment = Alignment.Center
            ) {
                // Vinyl grooves rings
                Surface(
                    modifier = Modifier
                        .size(270.dp)
                        .clip(CircleShape),
                    color = Color(0xFF161224),
                    border = androidx.compose.foundation.BorderStroke(2.dp, KadjaCardBorder)
                ) {}

                Surface(
                    modifier = Modifier
                        .size(210.dp)
                        .clip(CircleShape),
                    color = Color(0xFF1E1833),
                    border = androidx.compose.foundation.BorderStroke(1.dp, KadjaCardBorder.copy(alpha = 0.6f))
                ) {}

                // Center Album Art Artwork
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(KadjaPrimary, KadjaSecondary, KadjaTertiary)
                            )
                        )
                        .padding(3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(KadjaBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        if (song.albumArtUri != null) {
                            AsyncImage(
                                model = song.albumArtUri,
                                contentDescription = "Album Art",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                error = painterResource(id = R.drawable.default_album_art)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = KadjaPrimaryGlow,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        // Center Vinyl Pin
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(2.dp, KadjaPrimary, CircleShape)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Song Info (Title, Artist, Like Heart)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        color = KadjaTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = song.artist,
                        color = KadjaTextSecondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (song.isFavorite) KadjaTertiary.copy(alpha = 0.2f) else KadjaSurfaceElevated)
                        .testTag("now_playing_fav_button")
                ) {
                    Icon(
                        imageVector = if (song.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (song.isFavorite) KadjaTertiary else KadjaTextSecondary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Seekbar Slider & Timestamps
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = if (isUserSeeking) seekFraction else progressFraction,
                    onValueChange = {
                        isUserSeeking = true
                        seekFraction = it
                    },
                    onValueChangeFinished = {
                        val targetMs = (seekFraction * actualDuration).toLong()
                        onSeek(targetMs)
                        isUserSeeking = false
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = KadjaPrimaryGlow,
                        activeTrackColor = KadjaPrimary,
                        inactiveTrackColor = KadjaSurfaceElevated
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("playback_seekbar")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(currentPosition),
                        color = KadjaTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    AudioVisualizerBars(
                        isPlaying = isPlaying,
                        barColor = KadjaPrimaryGlow,
                        barCount = 4
                    )

                    Text(
                        text = formatTime(actualDuration),
                        color = KadjaTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Controls Row: Shuffle | Prev | Play/Pause | Next | Repeat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle Button
                IconButton(
                    onClick = onToggleShuffle,
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("now_playing_shuffle")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffle) KadjaSecondary else KadjaTextTertiary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Previous Button
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(KadjaSurfaceElevated)
                        .testTag("now_playing_prev")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = KadjaTextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Main Play / Pause Button with Neon Gradient
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .shadow(20.dp, CircleShape, spotColor = KadjaPrimary)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(KadjaPrimary, KadjaPrimaryVariant, KadjaSecondary)
                            )
                        )
                        .clickable(onClick = onPlayPause)
                        .testTag("now_playing_main_play_pause"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Next Button
                IconButton(
                    onClick = onNext,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(KadjaSurfaceElevated)
                        .testTag("now_playing_next")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = KadjaTextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Repeat Mode Cycle Button
                IconButton(
                    onClick = onCycleRepeat,
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("now_playing_repeat")
                ) {
                    when (repeatMode) {
                        RepeatMode.OFF -> Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "Repeat Off",
                            tint = KadjaTextTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                        RepeatMode.ALL -> Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "Repeat All",
                            tint = KadjaPrimaryGlow,
                            modifier = Modifier.size(24.dp)
                        )
                        RepeatMode.ONE -> Icon(
                            imageVector = Icons.Default.RepeatOne,
                            contentDescription = "Repeat One",
                            tint = KadjaTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Tool Bar: Equalizer | Sleep Timer | Add to Playlist
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(KadjaSurfaceElevated.copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Equalizer button
                TextButton(
                    onClick = onOpenEqualizer,
                    modifier = Modifier.testTag("quick_eq_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = KadjaPrimaryGlow,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Equalizer", color = KadjaTextPrimary, fontSize = 13.sp)
                }

                // Sleep Timer button
                TextButton(
                    onClick = onOpenSleepTimer,
                    modifier = Modifier.testTag("quick_sleep_timer_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Bedtime,
                        contentDescription = null,
                        tint = if (sleepTimerMinutesLeft != null) KadjaAccentGold else KadjaTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (sleepTimerMinutesLeft != null) "$sleepTimerMinutesLeft د" else "مؤقت النوم",
                        color = if (sleepTimerMinutesLeft != null) KadjaAccentGold else KadjaTextPrimary,
                        fontSize = 13.sp
                    )
                }

                // Add to Playlist
                TextButton(
                    onClick = onAddToPlaylist,
                    modifier = Modifier.testTag("quick_add_playlist_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlaylistAdd,
                        contentDescription = null,
                        tint = KadjaSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إضافة لقائمة", color = KadjaTextPrimary, fontSize = 13.sp)
                }
            }
        }
    }
}
