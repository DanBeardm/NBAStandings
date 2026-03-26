package com.olliesbrother.nbastandingsapp.model

import kotlinx.serialization.Serializable

@Serializable
data class ConferenceStandings(
    val conferenceName: String,
    val updatedAt: String,
    val teams: List<TeamStanding>
)