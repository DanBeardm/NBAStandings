package com.olliesbrother.nbastandingsapp.data

import com.olliesbrother.nbastandingsapp.model.Conference
import com.olliesbrother.nbastandingsapp.model.ConferenceStandings

class CachedStandingsRepository(
    private val remoteRepository: StandingsRepository,
    private val cache: StandingsCache,
    private val maxAgeMillis: Long = 15 * 60 * 1000L
) : StandingsRepository {

    override suspend fun getStandingsByConference(): Map<Conference, ConferenceStandings> {
        val cached = cache.read()

        if (cached != null && !isExpired(cached.savedAtMillis)) {
            return cached.standings
        }

        return try {
            val fresh = remoteRepository.getStandingsByConference()
            cache.write(fresh)
            fresh
        } catch (e: Exception) {
            cached?.standings ?: throw e
        }
    }

    suspend fun warmCache() {
        val cached = cache.read()

        if (cached != null && !isExpired(cached.savedAtMillis)) {
            return
        }

        val fresh = remoteRepository.getStandingsByConference()
        cache.write(fresh)
    }

    private fun isExpired(savedAtMillis: Long): Boolean {
        return System.currentTimeMillis() - savedAtMillis > maxAgeMillis
    }
}