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
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
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
                            day = Color(0xFF0F1720),
                            night = Color(0xFF0F1720)
                        )
                    )
                    .padding(16.dp)
                    .clickable(launchApp)
            ) {
                HeaderSection(
                    title = "NBA Standings",
                    subtitle = standings.conferenceName,
                    updatedAt = standings.updatedAt
                )

                Spacer(modifier = GlanceModifier.height(12.dp))

                Column(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .background(
                            ColorProvider(
                                day = Color(0xFF17212B),
                                night = Color(0xFF17212B)
                            )
                        )
                        .padding(12.dp)
                ) {
                    TableHeader()

                    Spacer(modifier = GlanceModifier.height(8.dp))

                    standings.teams.take(8).forEachIndexed { index, team ->
                        TeamStandingRow(team)

                        if (index == 5) {
                            Spacer(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(
                                        ColorProvider(
                                            day = Color(0xFF314052),
                                            night = Color(0xFF314052)
                                        )
                                    )
                            )

                            Text(
                                text = "Play-In Line",
                                modifier = GlanceModifier.padding(vertical = 6.dp),
                                style = TextStyle(
                                    color = ColorProvider(
                                        day = Color(0xFF8FA2B7),
                                        night = Color(0xFF8FA2B7)
                                    ),
                                    fontSize = 11.sp
                                )
                            )
                        } else if (index < standings.teams.take(8).lastIndex) {
                            Spacer(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(
                                        ColorProvider(
                                            day = Color(0xFF233140),
                                            night = Color(0xFF233140)
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }
    }

    companion object {
        val SelectedConferenceKey = stringPreferencesKey("selected_conference")
    }
}

@Composable
private fun HeaderSection(
    title: String,
    subtitle: String,
    updatedAt: String
) {
    Text(
        text = title,
        style = TextStyle(
            color = ColorProvider(Color.White, Color.White),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    )

    Spacer(modifier = GlanceModifier.height(4.dp))

    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = subtitle,
            modifier = GlanceModifier.defaultWeight(),
            style = TextStyle(
                color = ColorProvider(
                    day = Color(0xFFB7C4D1),
                    night = Color(0xFFB7C4D1)
                ),
                fontSize = 13.sp
            )
        )

        Text(
            text = updatedAt,
            style = TextStyle(
                color = ColorProvider(
                    day = Color(0xFF8FA2B7),
                    night = Color(0xFF8FA2B7)
                ),
                fontSize = 11.sp
            )
        )
    }
}

@Composable
private fun TableHeader() {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#",
            modifier = GlanceModifier.width(28.dp),
            style = TextStyle(
                color = ColorProvider(
                    day = Color(0xFF8FA2B7),
                    night = Color(0xFF8FA2B7)
                ),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        )

        Text(
            text = "Team",
            modifier = GlanceModifier.defaultWeight(),
            style = TextStyle(
                color = ColorProvider(
                    day = Color(0xFF8FA2B7),
                    night = Color(0xFF8FA2B7)
                ),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        )

        Text(
            text = "W-L",
            style = TextStyle(
                color = ColorProvider(
                    day = Color(0xFF8FA2B7),
                    night = Color(0xFF8FA2B7)
                ),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
private fun TeamStandingRow(team: TeamStanding) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = GlanceModifier
                .width(28.dp)
                .background(
                    ColorProvider(
                        day = Color(0xFF243243),
                        night = Color(0xFF243243)
                    )
                )
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = team.seed.toString(),
                style = TextStyle(
                    color = ColorProvider(Color.White, Color.White),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = GlanceModifier.width(10.dp))

        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = team.abbreviation,
                style = TextStyle(
                    color = ColorProvider(Color.White, Color.White),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text = team.teamName,
                style = TextStyle(
                    color = ColorProvider(
                        day = Color(0xFF8FA2B7),
                        night = Color(0xFF8FA2B7)
                    ),
                    fontSize = 11.sp
                )
            )
        }

        Text(
            text = "${team.wins}-${team.losses}",
            style = TextStyle(
                color = ColorProvider(Color.White, Color.White),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}