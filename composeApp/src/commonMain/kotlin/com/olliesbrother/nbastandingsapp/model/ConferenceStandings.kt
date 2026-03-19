package com.olliesbrother.nbastandingsapp.model

data class ConferenceStandings(
    val conferenceName: String,
    val updatedAt: String,
    val teams: List<TeamStanding>
)