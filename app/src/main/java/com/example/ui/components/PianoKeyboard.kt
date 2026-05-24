package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioSynth

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.GospelViewModel

@Composable
fun PianoKeyboard(
    modifier: Modifier = Modifier,
    viewModel: GospelViewModel? = null,
    labelsEnabled: Boolean = true
) {
    var localSelectedPresetName by remember { mutableStateOf("C Major") }

    // If viewModel is provided, synchronize state flows; else use local fallback
    val highlightedNotesState = viewModel?.highlightedNotes?.collectAsStateWithLifecycle()
    val selectedVoicingNameState = viewModel?.selectedVoicingName?.collectAsStateWithLifecycle()

    val highlightedNotes = remember(localSelectedPresetName, highlightedNotesState?.value) {
        highlightedNotesState?.value ?: AudioSynth.chordPresets[localSelectedPresetName] ?: emptyList()
    }

    val selectedVoicingName = selectedVoicingNameState?.value ?: localSelectedPresetName

    // Capture currently pressed note (temporary flash)
    var temporaryPressedNote by remember { mutableStateOf<String?>(null) }

    val whiteKeys = listOf("C", "D", "E", "F", "G", "A", "B", "C2", "D2", "E2")
    val blackKeyPositions = mapOf(
        "C#" to (1),
        "D#" to (2),
        "F#" to (4),
        "G#" to (5),
        "A#" to (6),
        "C#2" to (8),
        "D#2" to (9)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Preset selector bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Voicing",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Voicing:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        Button(
                            onClick = { expanded = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(selectedVoicingName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            AudioSynth.chordPresets.keys.forEach { preset ->
                                DropdownMenuItem(
                                    text = { Text(preset, fontWeight = FontWeight.Medium) },
                                    onClick = {
                                        if (viewModel != null) {
                                            viewModel.highlightNotes(AudioSynth.chordPresets[preset] ?: emptyList(), preset)
                                        } else {
                                            localSelectedPresetName = preset
                                        }
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        AudioSynth.playChord(highlightedNotes)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play Chord", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Strum", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // The Keyboard Box
        val keyWidth = 34.dp
        val whiteKeyHeight = 140.dp
        val blackKeyHeight = 85.dp
        val blackKeyWidth = 22.dp

        Box(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .shadow(6.dp, shape = RoundedCornerShape(8.dp))
                .border(2.dp, Color(0xFF2C1E14), RoundedCornerShape(8.dp)) // Rich wooden casing look
                .background(Color(0xFF2C1E14))
                .padding(top = 10.dp, bottom = 4.dp, start = 4.dp, end = 4.dp)
        ) {
            // 1. Render White Keys in a Row
            Row(modifier = Modifier.wrapContentSize()) {
                whiteKeys.forEach { note ->
                    val isHighlighted = highlightedNotes.contains(note)
                    val isPressed = temporaryPressedNote == note
                    
                    val keyColor by animateColorAsState(
                        targetValue = when {
                            isPressed -> MaterialTheme.colorScheme.primary
                            isHighlighted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                            else -> Color(0xFFFAFAF6) // Soft Cream Ivory
                        }, label = "WhiteKeyColor"
                    )

                    Box(
                        modifier = Modifier
                            .width(keyWidth)
                            .height(whiteKeyHeight)
                            .background(keyColor, RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                            .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                            .clickable {
                                temporaryPressedNote = note
                                AudioSynth.playNoteString(note)
                                // Release after short delay
                                scopePlayRelease { temporaryPressedNote = null }
                            },
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        if (labelsEnabled) {
                            Text(
                                text = note.replace("2", ""),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isHighlighted || isPressed) Color.Black else Color.Gray,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                }
            }

            // 2. Render Overlapping Black Keys with precise pixel/dp offsets
            val density = LocalDensity.current
            val keyWidthPx = with(density) { keyWidth.toPx() }
            val halfBlackWidthPx = with(density) { (blackKeyWidth / 2).toPx() }

            blackKeyPositions.forEach { entry ->
                val note = entry.key
                val position = entry.value
                val isHighlighted = highlightedNotes.contains(note)
                val isPressed = temporaryPressedNote == note

                val blackKeyColor by animateColorAsState(
                    targetValue = when {
                        isPressed -> MaterialTheme.colorScheme.primary
                        isHighlighted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                        else -> Color(0xFF1E1E24) // Obsidian Black
                    }, label = "BlackKeyColor"
                )

                // Calculate horizontal start offset
                val offsetDp = with(density) {
                    ((position * keyWidthPx) - halfBlackWidthPx).toDp()
                }

                Box(
                    modifier = Modifier
                        .offset(x = offsetDp)
                        .width(blackKeyWidth)
                        .height(blackKeyHeight)
                        .shadow(elevation = 3.dp, shape = RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
                        .background(blackKeyColor, RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
                        .border(1.dp, Color.Black, RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp))
                        .clickable {
                            temporaryPressedNote = note
                            AudioSynth.playNoteString(note)
                            scopePlayRelease { temporaryPressedNote = null }
                        },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    if (labelsEnabled) {
                        Text(
                            text = note.replace("2", ""),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isHighlighted || isPressed) Color.Black else Color.White,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
            }
        }

        Text(
            text = "💡 Tap keys to hear chords, or click 'Strum' to hear full Gospel voicings!",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

private fun scopePlayRelease(onRelease: () -> Unit) {
    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
        onRelease()
    }, 155)
}
