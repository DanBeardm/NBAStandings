package com.olliesbrother.nbastandingsapp.data

import com.olliesbrother.nbastandingsapp.model.Conference
import com.olliesbrother.nbastandingsapp.model.ConferenceStandings

interface StandingsRepository {
    suspend fun getStandingsByConference(): Map<Conference, ConferenceStandings>
}