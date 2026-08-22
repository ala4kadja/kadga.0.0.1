package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerSheet(
    selectedPreset: String,
    availablePresets: List<String>,
    onSelectPreset: (String) -> Unit,
    bassBoostStrength: Float,
    onBassBoostChange: (Float) -> Unit,
    playbackSpeed: Float,
    onPlaybackSpeedChange: (Float) -> Unit,
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = KadjaSurface,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = KadjaCardBorder)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = KadjaPrimaryGlow
                    )
                    Text(
                        text = "معدل الصوت والمؤثرات (Equalizer)",
                        color = KadjaTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = KadjaTextSecondary
                    )
                }
            }

            // Presets Section
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "أنماط الصوت الجاهزة (Presets)",
                    color = KadjaTextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availablePresets) { preset ->
                        val isSelected = preset == selectedPreset
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectPreset(preset) }
                                .testTag("preset_$preset"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) KadjaPrimary else KadjaSurfaceElevated,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, KadjaCardBorder)
                        ) {
                            Text(
                                text = preset,
                                color = if (isSelected) Color.White else KadjaTextSecondary,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            Divider(color = KadjaCardBorder.copy(alpha = 0.5f))

            // Bass Boost Slider
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "تضخيم الباس (Bass Boost)",
                        color = KadjaTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${(bassBoostStrength * 100).toInt()}%",
                        color = KadjaTertiary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = bassBoostStrength,
                    onValueChange = onBassBoostChange,
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = KadjaTertiary,
                        activeTrackColor = KadjaTertiary,
                        inactiveTrackColor = KadjaSurfaceElevated
                    ),
                    modifier = Modifier.testTag("bass_boost_slider")
                )
            }

            // Playback Speed
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "سرعة التشغيل",
                    color = KadjaTextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    speeds.forEach { speed ->
                        val isCurrent = (playbackSpeed == speed)
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isCurrent) KadjaSecondary else KadjaSurfaceElevated)
                                .clickable { onPlaybackSpeedChange(speed) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${speed}x",
                                color = if (isCurrent) Color.Black else KadjaTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Volume Control
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = if (volume == 0f) Icons.Default.VolumeOff else if (volume < 0.5f) Icons.Default.VolumeDown else Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = KadjaPrimaryGlow,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "مستوى الصوت",
                            color = KadjaTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "${(volume * 100).toInt()}%",
                        color = KadjaPrimaryGlow,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = volume,
                    onValueChange = onVolumeChange,
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = KadjaPrimaryGlow,
                        activeTrackColor = KadjaPrimary,
                        inactiveTrackColor = KadjaSurfaceElevated
                    ),
                    modifier = Modifier.testTag("volume_slider")
                )
            }
        }
    }
}
