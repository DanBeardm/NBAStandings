package com.olliesbrother.nbastandingsapp.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.olliesbrother.nbastandingsapp.MainActivity
import com.olliesbrother.nbastandingsapp.data.FakeStandingsRepository
import com.olliesbrother.nbastandingsapp.model.Conference
import com.olliesbrother.nbastandingsapp.model.TeamStanding

class NbaStandingsWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val launchApp = actionStartActivity(
            Intent(context, MainActivity::class.java)
        )

        provideContent {
            val prefs = currentState<Preferences>()
            val conferenceValue = prefs[SelectedConferenceKey] ?: Conference.EAST.name

            val repository = FakeStandingsRepository()
            val standings = when (Conference.valueOf(conferenceValue)) {
                Conference.EAST -> repository.getEasternStandings()
                Conference.WEST -> repository.getWesternStandings()
            }

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(
                        ColorProvider(
                            day = Color(0xFF101418),
                            night = Color(0xFF101418)
                        )
                    )
                    .padding(16.dp)
                    .clickable(launchApp)
            ) {
                Text(
                    text = "NBA Standings",
                    style = TextStyle(
                        color = ColorProvider(Color.White, Color.White),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = standings.conferenceName,
                    style = TextStyle(
                        color = ColorProvider(
                            Color(0xFFB8C1CC),
                            Color(0xFFB8C1CC)
                        ),
                        fontSize = 14.sp
                    )
                )

                Spacer(modifier = GlanceModifier.height(12.dp))

                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    Text(
                        text = "Team",
                        modifier = GlanceModifier.defaultWeight(),
                        style = TextStyle(
                            color = ColorProvider(
                                Color(0xFF8E99A8),
                                Color(0xFF8E99A8)
                            ),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Text(
                        text = "W-L",
                        style = TextStyle(
                            color = ColorProvider(
                                Color(0xFF8E99A8),
                                Color(0xFF8E99A8)
                            ),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                standings.teams.take(8).forEach { team ->
                    TeamStandingRow(team)
                }

                Spacer(modifier = GlanceModifier.height(12.dp))

                Text(
                    text = standings.updatedAt,
                    style = TextStyle(
                        color = ColorProvider(
                            Color(0xFF8E99A8),
                            Color(0xFF8E99A8)
                        ),
                        fontSize = 11.sp
                    )
                )
            }
        }
    }

    companion object {
        val SelectedConferenceKey = stringPreferencesKey("selected_conference")
    }
}

@Composable
private fun TeamStandingRow(team: TeamStanding) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "${team.seed}. ${team.abbreviation}",
            modifier = GlanceModifier.defaultWeight(),
            style = TextStyle(
                color = ColorProvider(Color.White, Color.White),
                fontSize = 14.sp
            )
        )

        Text(
            text = "${team.wins}-${team.losses}",
            style = TextStyle(
                color = ColorProvider(Color.White, Color.White),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}