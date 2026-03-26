package com.olliesbrother.nbastandingsapp.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.olliesbrother.nbastandingsapp.data.EspnStandingsRepository
import com.olliesbrother.nbastandingsapp.data.StandingsCache

class WidgetRefreshWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val remoteRepository = EspnStandingsRepository()
        val cache = StandingsCache(applicationContext)

        return try {
            val standings = remoteRepository.getStandingsByConference()
            cache.write(standings)

            NbaStandingsWidget().updateAll(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        } finally {
            remoteRepository.close()
        }
    }
}