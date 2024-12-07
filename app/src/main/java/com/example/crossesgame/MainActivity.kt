package com.example.crossesgame

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun GameGrid(gridSize: Int) {
    // Get the screen dimensions
    val configuration = LocalConfiguration.current

    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    Log.d("GameGrid", "Screen Width: $screenWidth, Screen Height: $screenHeight")

    // Calculate the grid size as the smaller of width and height
    val gridSizeDp = if (screenWidth < screenHeight) screenWidth else screenHeight

    // 2D array to store the state of the grid
    val grid = remember {
        List(gridSize) { _ ->
            MutableList(gridSize) { _ -> mutableStateOf(false) }
        }
    }

    // Constrain the size of the grid container to be a square
    Box(
        modifier = Modifier
            .size(gridSizeDp) // Make the grid a square with the calculated size
//            .fillMaxWidth()
            .aspectRatio(1f) // Makes the container a square
            .padding(16.dp), // Padding for the grid container itself
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp), // Spacing between rows
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            repeat(gridSize) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // Makes all rows take equal vertical space
                    horizontalArrangement = Arrangement.spacedBy(10.dp) // Spacing between cells
                ) {
                    repeat(gridSize) { col ->
                        Cell(
                            isActive = grid[row][col].value,
                            onClick = { toggleCellState(grid, row, col) },
                            modifier = Modifier.weight(1f) // Makes all cells in a row take equal horizontal space
                        )
                    }
                }
            }
        }
    }
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
fun Cell(isActive: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
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
            painter = painterResource(id = if (isActive) R.drawable.croc_left else R.drawable.croc_up),
            contentDescription = "Cell Image",
            modifier = Modifier.fillMaxSize(), // Make the image fill the cell
            contentScale = ContentScale.Crop // Scale the image to fit nicely
        )
    }
}
