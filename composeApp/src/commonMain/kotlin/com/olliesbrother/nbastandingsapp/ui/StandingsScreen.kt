package com.olliesbrother.nbastandingsapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.olliesbrother.nbastandingsapp.data.StandingsRepository
import com.olliesbrother.nbastandingsapp.model.Conference
import com.olliesbrother.nbastandingsapp.model.ConferenceStandings
import com.olliesbrother.nbastandingsapp.model.TeamStanding

@Composable
fun StandingsScreen(
    repository: StandingsRepository,
    selectedConference: Conference,
    onConferenceSelected: (Conference) -> Unit,
    onRefreshRequested: () -> Unit,
    refreshVersion: Int,
    isRefreshing: Boolean
) {
    var loadError by remember { mutableStateOf<String?>(null) }

    val standingsMap by produceState<Map<Conference, ConferenceStandings>?>(initialValue = null, repository, refreshVersion) {
        loadError = null
        value = try {
            repository.getStandingsByConference()
        } catch (e: Exception) {
            loadError = e.message ?: "Failed to load standings"
            null
        }
    }

    val standings = standingsMap?.get(selectedConference)

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "NBA Standings",
            style = MaterialTheme.typography.headlineMedium
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(top = 12.dp, bottom = 16.dp)
        ) {
            ConferenceButton(
                label = "East",
                selected = selectedConference == Conference.EAST,
                onClick = { onConferenceSelected(Conference.EAST) }
            )

            ConferenceButton(
                label = "West",
                selected = selectedConference == Conference.WEST,
                onClick = { onConferenceSelected(Conference.WEST) }
            )

            OutlinedButton(
                onClick = onRefreshRequested,
                enabled = !isRefreshing
            ) {
                Text(if (isRefreshing) "Refreshing..." else "Refresh")
            }
        }

        when {
            standings == null && loadError == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            standings == null -> {
                Text(
                    text = loadError ?: "Failed to load standings",
                    color = MaterialTheme.colorScheme.error
                )
            }

            else -> {
                Text(
                    text = standings.conferenceName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        HeaderRow()

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(standings.teams) { team ->
                                TeamRow(team)
                            }
                        }
                    }
                }

                Text(
                    text = standings.updatedAt,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 14.dp)
                )
            }
        }
    }
}

@Composable
private fun ConferenceButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val colors = if (selected) {
        ButtonDefaults.buttonColors()
    } else {
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Button(
        onClick = onClick,
        colors = colors,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(label)
    }
}

@Composable
private fun HeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#",
            modifier = Modifier.width(28.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Team",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "W-L",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TeamRow(team: TeamStanding) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(28.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = team.seed.toString(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = team.abbreviation,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = team.teamName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "${team.wins}-${team.losses}",
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}