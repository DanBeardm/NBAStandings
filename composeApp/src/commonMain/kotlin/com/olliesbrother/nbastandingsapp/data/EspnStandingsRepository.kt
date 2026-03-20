package com.olliesbrother.nbastandingsapp.data

import com.olliesbrother.nbastandingsapp.model.Conference
import com.olliesbrother.nbastandingsapp.model.ConferenceStandings
import com.olliesbrother.nbastandingsapp.model.TeamStanding
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

class EspnStandingsRepository : StandingsRepository {

    private val client = HttpClient {
        expectSuccess = true

        defaultRequest {
            url.takeFrom("https://site.api.espn.com/apis/site/v2")
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
        val root: JsonObject = client
            .get("sports/basketball/nba/standings")
            .body()

        val eastTeams = parseConferenceTeams(root, Conference.EAST)
        val westTeams = parseConferenceTeams(root, Conference.WEST)

        if (eastTeams.isEmpty() && westTeams.isEmpty()) {
            error("ESPN standings loaded, but no conference rows were parsed.")
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

private fun parseConferenceTeams(
    root: JsonObject,
    conference: Conference
): List<TeamStanding> {
    val conferenceNode = findConferenceNode(root, conference) ?: return emptyList()

    val entries = extractEntries(conferenceNode)

    return entries.mapNotNull { entry ->
        parseTeamStanding(entry)
    }.sortedBy { it.seed }
}

private fun findConferenceNode(
    root: JsonObject,
    conference: Conference
): JsonObject? {
    val children = root["children"]?.asJsonArrayOrNull().orEmpty()

    return children
        .mapNotNull { it as? JsonObject }
        .firstOrNull { child ->
            val text = listOfNotNull(
                child.string("name"),
                child.string("displayName"),
                child.string("shortDisplayName"),
                child["abbreviation"]?.jsonPrimitive?.contentOrNull
            ).joinToString(" ").lowercase()

            when (conference) {
                Conference.EAST -> text.contains("east")
                Conference.WEST -> text.contains("west")
            }
        }
}

private fun extractEntries(node: JsonObject): List<JsonObject> {
    node["standings"]
        ?.asJsonObjectOrNull()
        ?.get("entries")
        ?.asJsonArrayOrNull()
        ?.mapNotNull { it as? JsonObject }
        ?.let { return it }

    node["entries"]
        ?.asJsonArrayOrNull()
        ?.mapNotNull { it as? JsonObject }
        ?.let { return it }

    return emptyList()
}

private fun parseTeamStanding(entry: JsonObject): TeamStanding? {
    val team = entry["team"]?.asJsonObjectOrNull() ?: return null
    val stats = entry["stats"]?.asJsonArrayOrNull().orEmpty()

    val statMap = stats
        .mapNotNull { it as? JsonObject }
        .associateBy(
            keySelector = { stat ->
                stat.string("name")?.lowercase()
                    ?: stat.string("abbreviation")?.lowercase()
                    ?: ""
            },
            valueTransform = { stat ->
                stat["value"]
            }
        )

    val seed = statMap["playoffseed"]?.asInt()
        ?: statMap["rank"]?.asInt()
        ?: statMap["seed"]?.asInt()
        ?: return null

    val wins = statMap["wins"]?.asInt()
        ?: statMap["w"]?.asInt()
        ?: return null

    val losses = statMap["losses"]?.asInt()
        ?: statMap["l"]?.asInt()
        ?: return null

    val abbreviation =
        team.string("abbreviation")
            ?: team.string("shortDisplayName")
            ?: team.string("name")
            ?: "UNK"

    val teamName =
        team.string("displayName")
            ?: team.string("shortDisplayName")
            ?: team.string("name")
            ?: abbreviation

    return TeamStanding(
        seed = seed,
        abbreviation = abbreviation,
        teamName = teamName,
        wins = wins,
        losses = losses
    )
}

private fun JsonObject.string(key: String): String? {
    return this[key]?.jsonPrimitive?.contentOrNull
}

private fun JsonElement?.asJsonObjectOrNull(): JsonObject? {
    return this as? JsonObject
}

private fun JsonElement?.asJsonArrayOrNull(): JsonArray? {
    return this as? JsonArray
}

private fun JsonElement?.asInt(): Int? {
    return when (this) {
        is JsonPrimitive -> this.intOrNull
        else -> null
    }
}