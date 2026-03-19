package com.olliesbrother.nbastandingsapp.model

data class TeamStanding(
    val seed: Int,
    val abbreviation: String,
    val teamName: String,
    val wins: Int,
    val losses: Int
)