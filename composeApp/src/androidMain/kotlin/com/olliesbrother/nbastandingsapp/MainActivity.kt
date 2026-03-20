package com.olliesbrother.nbastandingsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import com.olliesbrother.nbastandingsapp.data.EspnStandingsRepository
import com.olliesbrother.nbastandingsapp.model.Conference
import com.olliesbrother.nbastandingsapp.widget.NbaStandingsWidget
import com.olliesbrother.nbastandingsapp.widget.WidgetPreferences
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var prefs: WidgetPreferences
    private val repository by lazy {
        EspnStandingsRepository()
    }

    private var selectedConferenceState = mutableStateOf(Conference.EAST)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        prefs = WidgetPreferences(applicationContext)
        selectedConferenceState.value = prefs.getSelectedConference()

        setContent {
            val selectedConference by selectedConferenceState

            App(
                repository = repository,
                selectedConference = selectedConference,
                onConferenceSelected = { conference ->
                    if (conference == selectedConferenceState.value) return@App

                    selectedConferenceState.value = conference
                    prefs.setSelectedConference(conference)

                    lifecycleScope.launch {
                        val widget = NbaStandingsWidget()
                        val manager = GlanceAppWidgetManager(applicationContext)
                        val glanceIds = manager.getGlanceIds(NbaStandingsWidget::class.java)

                        glanceIds.forEach { glanceId ->
                            updateAppWidgetState(applicationContext, glanceId) { state ->
                                state[NbaStandingsWidget.SelectedConferenceKey] = conference.name
                            }
                            widget.update(applicationContext, glanceId)
                        }
                    }
                }
            )
        }
    }

    override fun onDestroy() {
        repository.close()
        super.onDestroy()
    }
}