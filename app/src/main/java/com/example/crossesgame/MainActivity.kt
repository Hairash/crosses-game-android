package com.example.crossesgame

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
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
import com.example.crossesgame.ui.theme.CrossesGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CrossesGameTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GameGrid(gridSize = 5)
                }
            }
        }
    }
}

enum class GameState {
    Playing,
    Ended
}

@Composable
fun GameGrid(gridSize: Int) {
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
                    row.map { mutableStateOf(it) }.toMutableList() // Wrap Boolean values back into MutableState
                }
            }
        )
    ) {
        List(gridSize) {
            MutableList(gridSize) { mutableStateOf(false) } // Initialize grid with MutableState<Boolean>
        }
    }

    // Center the grid and make it a square
    Box(
        modifier = Modifier
            .fillMaxSize() // Fill the entire screen to center the grid
            .clickable { // Handle touch anywhere
                if (gameState.value == GameState.Ended) {
                    startNewGame(grid, gameState)
                }
            },
        contentAlignment = Alignment.Center // Center the grid within the screen
    ) {
        // Square grid container
        Box(
            modifier = Modifier
                .size(gridSizeDp * 0.9f) // Slightly reduce size to add margins (90% of the smaller dimension)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp), // Space between rows
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                repeat(gridSize) { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f), // Makes all rows take equal vertical space
                        horizontalArrangement = Arrangement.spacedBy(4.dp) // Space between cells in each row
                    ) {
                        repeat(gridSize) { col ->
                            Cell(
                                value = grid[row][col].value,
                                onClick = { processClick(grid, row, col, gameState) },
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

fun startNewGame(grid: List<MutableList<MutableState<Boolean>>>, gameState: MutableState<GameState>) {
    Log.d("MainActivity", "Starting new game!")
    resetGrid(grid)
    gameState.value = GameState.Playing
}

fun resetGrid(grid: List<MutableList<MutableState<Boolean>>>) {
    grid.forEach { row ->
        row.forEach { cell ->
            cell.value = false
        }
    }
}

fun processClick(grid: List<MutableList<MutableState<Boolean>>>, row: Int, col: Int, gameState: MutableState<GameState>) {
    if (gameState.value == GameState.Ended) {
        startNewGame(grid, gameState)
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
            .background(
                color = Color.LightGray,
                shape = RoundedCornerShape(8.dp)
            )
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
