package com.olliesbrother.nbastandingsapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.olliesbrother.nbastandingsapp.ui.StandingsScreen

@Composable
fun App() {
    MaterialTheme {
        Surface {
            StandingsScreen()
        }
    }
}

@Preview
@Composable
fun AppPreview() {
    App()
}