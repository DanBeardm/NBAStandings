package com.olliesbrother.nbastandingsapp.data

import com.olliesbrother.nbastandingsapp.model.Conference
import com.olliesbrother.nbastandingsapp.model.ConferenceStandings
import com.olliesbrother.nbastandingsapp.model.TeamStanding

class FakeStandingsRepository : StandingsRepository {

    override suspend fun getStandingsByConference(): Map<Conference, ConferenceStandings> {
        val east = ConferenceStandings(
            conferenceName = "Eastern Conference",
            updatedAt = "Mock data",
            teams = listOf(
                TeamStanding(1, "BOS", "Boston Celtics", 52, 18),
                TeamStanding(2, "MIL", "Milwaukee Bucks", 48, 22),
                TeamStanding(3, "CLE", "Cleveland Cavaliers", 47, 23),
                TeamStanding(4, "NYK", "New York Knicks", 45, 25),
                TeamStanding(5, "ORL", "Orlando Magic", 42, 28),
                TeamStanding(6, "IND", "Indiana Pacers", 41, 29),
                TeamStanding(7, "MIA", "Miami Heat", 39, 31),
                TeamStanding(8, "PHI", "Philadelphia 76ers", 38, 32)
            )
        )

        val west = ConferenceStandings(
            conferenceName = "Western Conference",
            updatedAt = "Mock data",
            teams = listOf(
                TeamStanding(1, "DEN", "Denver Nuggets", 54, 16),
                TeamStanding(2, "OKC", "Oklahoma City Thunder", 50, 20),
                TeamStanding(3, "MIN", "Minnesota Timberwolves", 49, 21),
                TeamStanding(4, "LAC", "LA Clippers", 47, 23),
                TeamStanding(5, "DAL", "Dallas Mavericks", 44, 26),
                TeamStanding(6, "PHX", "Phoenix Suns", 43, 27),
                TeamStanding(7, "SAC", "Sacramento Kings", 40, 30),
                TeamStanding(8, "LAL", "Los Angeles Lakers", 39, 31)
            )
        )

        return mapOf(
            Conference.EAST to east,
            Conference.WEST to west
        )
    }
}