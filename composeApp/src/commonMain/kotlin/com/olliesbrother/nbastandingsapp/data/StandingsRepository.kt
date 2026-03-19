package com.olliesbrother.nbastandingsapp.data

import com.olliesbrother.nbastandingsapp.model.ConferenceStandings

interface StandingsRepository {
    fun getEasternStandings(): ConferenceStandings
    fun getWesternStandings(): ConferenceStandings
}