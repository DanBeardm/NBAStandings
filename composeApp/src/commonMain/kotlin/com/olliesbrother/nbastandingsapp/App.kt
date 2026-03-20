package com.olliesbrother.nbastandingsapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.olliesbrother.nbastandingsapp.data.FakeStandingsRepository
import com.olliesbrother.nbastandingsapp.data.StandingsRepository
import com.olliesbrother.nbastandingsapp.model.Conference
import com.olliesbrother.nbastandingsapp.ui.StandingsScreen

@Composable
fun App(
    repository: StandingsRepository = FakeStandingsRepository(),
    selectedConference: Conference = Conference.EAST,
    onConferenceSelected: (Conference) -> Unit = {}
) {
    MaterialTheme {
        Surface {
            StandingsScreen(
                repository = repository,
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