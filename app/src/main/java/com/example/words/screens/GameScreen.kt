package com.example.words.screens

import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController

val wordGroups = listOf(
    listOf("apple", "banana", "cherry", "grape"),
    listOf("dog", "cat", "hamster", "goldfish"),
    listOf("red", "blue", "green", "yellow"),
    listOf("run", "jump", "swim", "fly"),
    listOf("happy", "sad", "angry", "excited"),
    listOf("sun", "moon", "stars", "sky")
)

@Composable
fun GameScreen(navController: NavController) {
    var words by remember { mutableStateOf<List<List<String>>>(emptyList()) }
    var completedRows by remember { mutableStateOf(emptySet<Int>()) }

    LaunchedEffect(Unit) {
        words = wordGroups.flatten().shuffled().chunked(4)
    }

    if (completedRows.size == wordGroups.size) {
        navController.navigate("win")
    }

    val buttonBounds = remember { mutableMapOf<Pair<Int, Int>, Rect>() }
    var draggedItem by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var dragTarget by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        words.forEachIndexed { rowIndex, row ->
            val isRowCompleted = completedRows.contains(rowIndex) ||
                wordGroups.any { group -> group.all { word -> row.contains(word) } }

            if (isRowCompleted && !completedRows.contains(rowIndex)) {
                completedRows = completedRows + rowIndex
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                row.forEachIndexed { colIndex, word ->
                    var offset by remember { mutableStateOf(Offset.Zero) }
                    val animatedOffset by animateOffsetAsState(targetValue = offset, label = "")
                    var zIndex by remember { mutableStateOf(1f) }
                    val isBeingDragged = draggedItem == (rowIndex to colIndex)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp)
                            .onGloballyPositioned { coordinates ->
                                buttonBounds[rowIndex to colIndex] = coordinates.boundsInRoot()
                            }
                            .zIndex(zIndex)
                            .graphicsLayer {
                                translationX = animatedOffset.x
                                translationY = animatedOffset.y
                            }
                            .pointerInput(rowIndex to colIndex) {
                                if (isRowCompleted) return@pointerInput
                                detectDragGestures(
                                    onDragStart = {
                                        draggedItem = rowIndex to colIndex
                                        zIndex = 2f // Bring to front
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        offset += dragAmount

                                        val currentButtonCenter = buttonBounds[rowIndex to colIndex]?.center ?: return@detectDragGestures
                                        val finalPosition = currentButtonCenter + offset

                                        dragTarget = buttonBounds.entries.find { (key, rect) ->
                                            val (targetRow, _) = key
                                            key != (rowIndex to colIndex) && rect.contains(finalPosition) && !completedRows.contains(targetRow)
                                        }?.key
                                    },
                                    onDragEnd = {
                                        zIndex = 1f
                                        dragTarget?.let { target ->
                                            val (targetRow, targetCol) = target
                                            val newWords = words.map { it.toMutableList() }.toMutableList()
                                            val temp = newWords[rowIndex][colIndex]
                                            newWords[rowIndex][colIndex] = newWords[targetRow][targetCol]
                                            newWords[targetRow][targetCol] = temp
                                            words = newWords.map { it.toList() }
                                        }
                                        offset = Offset.Zero
                                        draggedItem = null
                                        dragTarget = null
                                    }
                                )
                            }
                    ) {
                        Button(
                            onClick = { },
                            modifier = Modifier.fillMaxWidth(),
                            shape = if (isRowCompleted) CircleShape else ButtonDefaults.shape,
                            border = if (isRowCompleted) BorderStroke(2.dp, Color.Green) else null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when {
                                    isBeingDragged -> Color.Red
                                    dragTarget == (rowIndex to colIndex) -> Color.LightGray
                                    else -> ButtonDefaults.buttonColors().containerColor
                                }
                            )
                        ) {
                            Text(word)
                        }
                    }
                }
            }
        }
    }
}