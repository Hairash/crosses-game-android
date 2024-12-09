package com.example.crossesgame

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.crossesgame.helpers.PreferencesHelper
import com.example.crossesgame.ui.theme.CrossesGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CrossesGameTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GlobalGameLoop(context = this) // Pass the Activity's context
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            enableFullscreenMode()
        }
    }

    private fun enableFullscreenMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }
    }
}


enum class GameState {
    Playing,
    Ended
}


@Composable
fun GlobalGameLoop(context: Context) {
    // Track the current grid size (level progression)
    val gridSize = remember { mutableStateOf(PreferencesHelper.load(context, "grid_size_key", 1)) }
    val level = remember { mutableStateOf(PreferencesHelper.load(context, "level_key", 1)) }

    androidx.compose.runtime.key(gridSize.value, level.value) {
        GameGrid(
            gridSize = gridSize.value,
            level = level.value,
            onLevelEnd = {
                increaseLevel(level, gridSize)
                PreferencesHelper.save(context, "level_key", level.value)
                PreferencesHelper.save(context, "grid_size_key", gridSize.value)
            }
        )
    }
}

fun increaseLevel(level: MutableState<Int>, gridSize: MutableState<Int>) {
    level.value += 1
    if (level.value > gridSize.value * gridSize.value) {
        gridSize.value += 1
        level.value = 1
    }
}


@Composable
fun GameGrid(gridSize: Int, level: Int, onLevelEnd: () -> Unit) {
    Log.d("MainActivity", "Grid Size: $gridSize")
    Log.d("MainActivity", "Level: $level")
    // Get the screen dimensions
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // Calculate the grid size as the smaller of the screen's width and height
    val gridSizeDp = if (screenWidth < screenHeight) screenWidth else screenHeight
    Log.d("MainActivity", "Grid Size: $gridSizeDp")

    // Game state variable
    val gameState = rememberSaveable { mutableStateOf(GameState.Playing) }
    // 2D array to store the state of the grid
    val grid = rememberSaveable(
        saver = Saver(
            save = { grid: List<List<MutableState<Boolean>>> ->
                grid.flatten().map { it.value } // Extract Boolean values from MutableState
            },
            restore = { flatList: List<Boolean> ->
                flatList.chunked(gridSize).map { row ->
                    row.map { mutableStateOf(it) }
                        .toMutableList() // Wrap Boolean values back into MutableState
                }
            }
        )
    ) {
        List(gridSize) {
            MutableList(gridSize) { mutableStateOf(true) } // Initialize grid with MutableState<Boolean>
        }
    }

    if (gameState.value == GameState.Playing) {
        shuffleGrid(grid, level)
    }

    // Center the grid and make it a square
    Box(
        modifier = Modifier
            .fillMaxSize() // Fill the entire screen to center the grid
            .clickable { // Handle touch anywhere
                if (gameState.value == GameState.Ended) {
                    onLevelEnd()
//                    startNewGame(grid, gameState)
                }
            },
        contentAlignment = Alignment.Center // Center the grid within the screen
    ) {
        Image(
            painter = painterResource(id = R.drawable.background), // Replace with your background image resource
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Scale to cover the entire screen
        )
        // Square grid container
        Box(
            modifier = Modifier
                .size(gridSizeDp * 0.95f) // Slightly reduce size to add margins (90% of the smaller dimension)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
//                verticalArrangement = Arrangement.spacedBy(4.dp), // Space between rows
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                repeat(gridSize) { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f), // Makes all rows take equal vertical space
//                        horizontalArrangement = Arrangement.spacedBy(4.dp) // Space between cells in each row
                    ) {
                        repeat(gridSize) { col ->
                            Cell(
                                value = grid[row][col].value,
                                onClick = { processClick(grid, row, col, gameState, onLevelEnd) },
                                modifier = Modifier.weight(1f) // Ensures cells are equally sized
                            )
                        }
                    }
                }
            }
            if (gameState.value == GameState.Ended) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        color = Color(0, 32, 35),
                        topLeft = Offset(0f, size.height * 0.4f),
                        size = Size(size.width, size.height * 0.2f)
                    )
                }
                Image(
                    painter = painterResource(R.drawable.label_true_crocodile),
                    contentDescription = "Win Label",
                    modifier = Modifier.fillMaxSize(), // Make the image fill the cell
                    //                contentScale = ContentScale.Crop // Scale the image to fit nicely
                )
            }
        }
    }
}

fun shuffleGrid(grid: List<MutableList<MutableState<Boolean>>>, steps: Int) {
    Log.d("MainActivity", "Shuffling grid")
    Log.d("MainActivity", "steps: $steps")
    // Ensure grid starts as all inactive (false)
    while (grid.all { row -> row.all { it.value } }) {
        val cellSet = mutableSetOf<Pair<Int, Int>>()
        val gridSize = grid.size

        repeat(steps) {
            var cellX = (0 until gridSize).random()
            var cellY = (0 until gridSize).random()
            Log.d("MainActivity", "Shuffling cell: $cellX, $cellY")

            // Ensure the cell is not already toggled in this shuffle iteration
            while (cellSet.contains(cellX to cellY)) {
                cellX = (0 until gridSize).random()
                cellY = (0 until gridSize).random()
            }

            toggleCellState(grid, cellX, cellY) // Call toggle function
            cellSet.add(cellX to cellY)
        }
    }
    Log.d("MainActivity", "Grid shuffled")
}

fun processClick(
    grid: List<MutableList<MutableState<Boolean>>>,
    row: Int,
    col: Int,
    gameState: MutableState<GameState>,
    onLevelEnd: () -> Unit
) {
    if (gameState.value == GameState.Ended) {
        onLevelEnd()
//        startNewGame(grid, gameState)
        return
    }
    toggleCellState(grid, row, col)
    if (checkEndGame(grid)) {
        gameState.value = GameState.Ended
    }
}

fun checkEndGame(grid: List<MutableList<MutableState<Boolean>>>): Boolean {
    for (row in grid) {
        for (cell in row) {
            if (!cell.value) return false
        }
    }
    Log.d("MainActivity", "Game Over!")
    return true
}

fun toggleCellState(grid: List<MutableList<MutableState<Boolean>>>, row: Int, col: Int) {
    grid[row][col].value = !grid[row][col].value
    for (i in grid.indices) {
        if (i == col) continue
        grid[row][i].value = !grid[row][i].value
    }
    for (i in grid.indices) {
        if (i == row) continue
        grid[i][col].value = !grid[i][col].value
    }
}


@Composable
fun Cell(value: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1f) // Makes the cell square
//            .background(
//                color = Color.LightGray,
//                shape = RoundedCornerShape(8.dp)
//            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = if (value) R.drawable.croc_left else R.drawable.croc_up),
            contentDescription = "Cell Image",
            modifier = Modifier.fillMaxSize(), // Make the image fill the cell
            contentScale = ContentScale.Crop // Scale the image to fit nicely
        )
    }
}
