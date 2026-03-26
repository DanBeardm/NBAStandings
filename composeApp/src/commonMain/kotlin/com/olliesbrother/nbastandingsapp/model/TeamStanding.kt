package com.olliesbrother.nbastandingsapp.model

import kotlinx.serialization.Serializable

@Serializable
data class TeamStanding(
    val seed: Int,
    val abbreviation: String,
    val teamName: String,
    val wins: Int,
    val losses: Int
)