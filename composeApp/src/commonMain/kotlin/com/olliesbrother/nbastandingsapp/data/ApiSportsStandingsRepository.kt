package com.olliesbrother.nbastandingsapp.data

import com.olliesbrother.nbastandingsapp.model.Conference
import com.olliesbrother.nbastandingsapp.model.ConferenceStandings
import com.olliesbrother.nbastandingsapp.model.TeamStanding
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ApiSportsStandingsRepository(
    private val apiKey: String,
    private val leagueId: Int,
    private val season: String = "2022-2023",
    private val stage: String? = "NBA - Regular Season"
) : StandingsRepository {

    private val client = HttpClient {
        expectSuccess = true

        defaultRequest {
            url.takeFrom("https://v1.basketball.api-sports.io")
            header("x-apisports-key", apiKey)
        }

        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                }
            )
        }
    }

    override suspend fun getStandingsByConference(): Map<Conference, ConferenceStandings> {
        val rawText = client.get("/standings") {
            parameter("league", leagueId)
            parameter("season", season)
            stage?.let { parameter("stage", it) }
        }.body<String>()

        println("API-BASKETBALL RAW STANDINGS: $rawText")

        val root = Json.parseToJsonElement(rawText).jsonObject
        val rawRows = extractRows(root)
        val parsedRows = rawRows.mapNotNull(::parseStandingRow)

        val eastTeams = parsedRows
            .filter { it.conference == Conference.EAST }
            .sortedBy { it.rank }
            .map { it.toTeamStanding() }

        val westTeams = parsedRows
            .filter { it.conference == Conference.WEST }
            .sortedBy { it.rank }
            .map { it.toTeamStanding() }

        if (eastTeams.isEmpty() && westTeams.isEmpty()) {
            error("API-Sports returned no standings rows. Raw response: $rawText")
        }

        return mapOf(
            Conference.EAST to ConferenceStandings(
                conferenceName = "Eastern Conference",
                updatedAt = "Updated recently",
                teams = eastTeams
            ),
            Conference.WEST to ConferenceStandings(
                conferenceName = "Western Conference",
                updatedAt = "Updated recently",
                teams = westTeams
            )
        )
    }
    fun close() {
        client.close()
    }
}

private data class ParsedStandingRow(
    val conference: Conference,
    val rank: Int,
    val abbreviation: String,
    val teamName: String,
    val wins: Int,
    val losses: Int
) {
    fun toTeamStanding(): TeamStanding {
        return TeamStanding(
            seed = rank,
            abbreviation = abbreviation,
            teamName = teamName,
            wins = wins,
            losses = losses
        )
    }
}

private fun extractRows(root: JsonObject): List<JsonObject> {
    val responseRows = root["response"]
        ?.asJsonArrayOrNull()
        ?.mapNotNull { it as? JsonObject }

    if (!responseRows.isNullOrEmpty()) {
        return responseRows
    }

    val legacyRows = root["api"]
        ?.asJsonObjectOrNull()
        ?.get("standings")
        ?.asJsonArrayOrNull()
        ?.mapNotNull { it as? JsonObject }

    if (!legacyRows.isNullOrEmpty()) {
        return legacyRows
    }

    return emptyList()
}

private fun parseStandingRow(row: JsonObject): ParsedStandingRow? {
    val conference = parseConference(row) ?: return null
    val rank = parseRank(row) ?: return null
    val wins = parseWins(row) ?: return null
    val losses = parseLosses(row) ?: return null

    val teamObject = row["team"]?.asJsonObjectOrNull()

    val abbreviation =
        teamObject?.string("code")
            ?: teamObject?.string("abbreviation")
            ?: teamObject?.string("name")
            ?: "UNK"

    val teamName =
        teamObject?.string("name")
            ?: teamObject?.string("fullName")
            ?: buildString {
                val city = teamObject?.string("city").orEmpty()
                val nickname = teamObject?.string("nickname").orEmpty()
                append(city)
                if (city.isNotBlank() && nickname.isNotBlank()) append(" ")
                append(nickname)
            }.ifBlank { abbreviation }

    return ParsedStandingRow(
        conference = conference,
        rank = rank,
        abbreviation = abbreviation,
        teamName = teamName,
        wins = wins,
        losses = losses
    )
}

private fun parseConference(row: JsonObject): Conference? {
    val conferenceValue =
        row["conference"]?.asJsonObjectOrNull()?.string("name")
            ?: row.string("conference")
            ?: row.string("conferenceName")
            ?: row["group"]?.asJsonObjectOrNull()?.string("name")

    return when (conferenceValue?.trim()?.lowercase()) {
        "east", "eastern" -> Conference.EAST
        "west", "western" -> Conference.WEST
        else -> null
    }
}

private fun parseRank(row: JsonObject): Int? {
    return row["conference"]?.asJsonObjectOrNull()?.int("rank")
        ?: row["group"]?.asJsonObjectOrNull()?.int("rank")
        ?: row.int("conferenceRank")
        ?: row.int("position")
        ?: row.int("rank")
}

private fun parseWins(row: JsonObject): Int? {
    return row["win"].extractRecordValue()
        ?: row.int("win")
        ?: row.int("wins")
}

private fun parseLosses(row: JsonObject): Int? {
    return row["loss"].extractRecordValue()
        ?: row.int("loss")
        ?: row.int("losses")
}

private fun JsonElement?.extractRecordValue(): Int? {
    return when (this) {
        is JsonPrimitive -> this.intOrNull
        else -> this?.asJsonObjectOrNull()?.int("total")
    }
}

private fun JsonObject.string(key: String): String? {
    return this[key]?.jsonPrimitive?.contentOrNull
}

private fun JsonObject.int(key: String): Int? {
    return this[key]?.jsonPrimitive?.intOrNull
}

private fun JsonElement.asJsonObjectOrNull(): JsonObject? {
    return this as? JsonObject
}

private fun JsonElement.asJsonArrayOrNull(): JsonArray? {
    return this as? JsonArray
}