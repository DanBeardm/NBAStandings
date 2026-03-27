package com.olliesbrother.nbastandingsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import com.olliesbrother.nbastandingsapp.data.CachedStandingsRepository
import com.olliesbrother.nbastandingsapp.data.EspnStandingsRepository
import com.olliesbrother.nbastandingsapp.data.StandingsCache
import com.olliesbrother.nbastandingsapp.model.Conference
import com.olliesbrother.nbastandingsapp.widget.NbaStandingsWidget
import com.olliesbrother.nbastandingsapp.widget.WidgetPreferences
import com.olliesbrother.nbastandingsapp.widget.WidgetRefreshWorker
import kotlinx.coroutines.launch
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private lateinit var prefs: WidgetPreferences
    private lateinit var standingsCache: StandingsCache

    private val remoteRepository by lazy {
        EspnStandingsRepository()
    }

    private val repository by lazy {
        CachedStandingsRepository(
            remoteRepository = remoteRepository,
            cache = standingsCache
        )
    }

    private var selectedConferenceState = mutableStateOf(Conference.EAST)
    private var refreshVersionState = mutableIntStateOf(0)
    private var isRefreshingState = mutableStateOf(false)
    private var statusMessageState = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        prefs = WidgetPreferences(applicationContext)
        standingsCache = StandingsCache(applicationContext)
        selectedConferenceState.value = prefs.getSelectedConference()

        scheduleBackgroundRefresh()

        lifecycleScope.launch {
            val hadCache = standingsCache.read() != null

            val result = runCatching {
                repository.warmCache()
            }

            if (result.isSuccess) {
                refreshVersionState.intValue++
                statusMessageState.value = null
            } else {
                statusMessageState.value = if (hadCache) {
                    "Offline — showing cached standings"
                } else {
                    "Could not load standings"
                }
            }

            NbaStandingsWidget().updateAll(applicationContext)
        }

        setContent {
            val selectedConference by selectedConferenceState
            val refreshVersion = refreshVersionState.intValue
            val isRefreshing by isRefreshingState
            val statusMessage by statusMessageState

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
                },
                onRefreshRequested = {
                    if (isRefreshingState.value) return@App

                    lifecycleScope.launch {
                        isRefreshingState.value = true
                        val hadCache = standingsCache.read() != null

                        val result = runCatching {
                            repository.forceRefresh()
                        }

                        if (result.isSuccess) {
                            refreshVersionState.intValue++
                            statusMessageState.value = null
                            NbaStandingsWidget().updateAll(applicationContext)
                        } else {
                            statusMessageState.value = if (hadCache) {
                                "Refresh failed — showing cached standings"
                            } else {
                                "Refresh failed"
                            }
                        }

                        isRefreshingState.value = false
                    }
                },
                refreshVersion = refreshVersion,
                isRefreshing = isRefreshing,
                statusMessage = statusMessage
            )
        }
    }

    private fun scheduleBackgroundRefresh() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(
            6, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            WIDGET_REFRESH_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    override fun onDestroy() {
        remoteRepository.close()
        super.onDestroy()
    }

    companion object {
        private const val WIDGET_REFRESH_WORK_NAME = "nba_widget_refresh_work"
    }
}