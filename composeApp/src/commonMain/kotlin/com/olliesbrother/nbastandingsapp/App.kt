package com.olliesbrother.nbastandingsapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.olliesbrother.nbastandingsapp.model.Conference
import com.olliesbrother.nbastandingsapp.ui.StandingsScreen

@Composable
fun App(
    selectedConference: Conference = Conference.EAST,
    onConferenceSelected: (Conference) -> Unit = {}
) {
    MaterialTheme {
        Surface {
            StandingsScreen(
                selectedConference = selectedConference,
                onConferenceSelected = onConferenceSelected
            )
        }
    }
}

@Preview
@Composable
fun AppPreview() {
    App()
}