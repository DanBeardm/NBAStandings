package com.olliesbrother.nbastandingsapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.olliesbrother.nbastandingsapp.data.FakeStandingsRepository
import com.olliesbrother.nbastandingsapp.model.TeamStanding

@Composable
fun StandingsScreen() {
    val repository = remember { FakeStandingsRepository() }
    var showEast by remember { mutableStateOf(true) }

    val standings = if (showEast) {
        repository.getEasternStandings()
    } else {
        repository.getWesternStandings()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "NBA Standings",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = standings.conferenceName,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Button(onClick = { showEast = true }) {
                Text("East")
            }
            Button(onClick = { showEast = false }) {
                Text("West")
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            items(standings.teams) { team ->
                TeamRow(team)
            }
        }

        Text(
            text = standings.updatedAt,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun TeamRow(team: TeamStanding) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("${team.seed}. ${team.abbreviation}")
        Text("${team.wins}-${team.losses}")
    }
}