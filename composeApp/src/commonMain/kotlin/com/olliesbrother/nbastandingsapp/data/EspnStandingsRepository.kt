package com.olliesbrother.nbastandingsapp.data

import com.olliesbrother.nbastandingsapp.model.Conference
import com.olliesbrother.nbastandingsapp.model.ConferenceStandings
import com.olliesbrother.nbastandingsapp.model.TeamStanding
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class EspnStandingsRepository : StandingsRepository {

    private val client = HttpClient {
        expectSuccess = true
    }

    override suspend fun getStandingsByConference(): Map<Conference, ConferenceStandings> {
        val rawText = client
            .get("https://site.api.espn.com/apis/v2/sports/basketball/nba/standings")
            .bodyAsText()

        val root = Json.parseToJsonElement(rawText).jsonObject

        val eastNode = findConferenceNode(root, Conference.EAST)
            ?: error("Could not find Eastern Conference in ESPN standings response.")

        val westNode = findConferenceNode(root, Conference.WEST)
            ?: error("Could not find Western Conference in ESPN standings response.")

        val eastStandings = parseConferenceStandings(eastNode, "Eastern Conference")
        val westStandings = parseConferenceStandings(westNode, "Western Conference")

        return mapOf(
            Conference.EAST to eastStandings,
            Conference.WEST to westStandings
        )
    }

    fun close() {
        client.close()
    }
}

private data class ParsedConferenceRow(
    val sourceOrder: Int,
    val abbreviation: String,
    val teamName: String,
    val wins: Int,
    val losses: Int
)

private fun findConferenceNode(
    root: JsonObject,
    conference: Conference
): JsonObject? {
    val children = root["children"]
        ?.asJsonArrayOrNull()
        .orEmpty()
        .mapNotNull { it as? JsonObject }

    return children.firstOrNull { child ->
        val abbreviation = child.string("abbreviation")?.lowercase().orEmpty()
        val name = child.string("name")?.lowercase().orEmpty()
        val displayName = child.string("displayName")?.lowercase().orEmpty()

        when (conference) {
            Conference.EAST ->
                abbreviation == "east" ||
                        name.contains("eastern") ||
                        displayName.contains("eastern")

            Conference.WEST ->
                abbreviation == "west" ||
                        name.contains("western") ||
                        displayName.contains("western")
        }
    }
}

private fun parseConferenceStandings(
    conferenceNode: JsonObject,
    conferenceName: String
): ConferenceStandings {
    val standings = conferenceNode["standings"]?.asJsonObjectOrNull()
        ?: error("Conference standings object missing for $conferenceName.")

    val seasonDisplayName = standings.string("seasonDisplayName") ?: "Current Season"

    val entries = standings["entries"]
        ?.asJsonArrayOrNull()
        .orEmpty()
        .mapNotNull { it as? JsonObject }

    if (entries.isEmpty()) {
        error("No entries found for $conferenceName.")
    }

    val parsedRows = entries.mapIndexedNotNull { index, entry ->
        parseConferenceRow(entry, index)
    }

    val sortedRows = parsedRows.sortedWith(
        compareByDescending<ParsedConferenceRow> { it.wins }
            .thenBy { it.losses }
            .thenBy { it.sourceOrder }
    )

    val teams = sortedRows.mapIndexed { index, row ->
        TeamStanding(
            seed = index + 1,
            abbreviation = row.abbreviation,
            teamName = row.teamName,
            wins = row.wins,
            losses = row.losses
        )
    }

    return ConferenceStandings(
        conferenceName = conferenceName,
        updatedAt = seasonDisplayName,
        teams = teams
    )
}

private fun parseConferenceRow(
    entry: JsonObject,
    sourceOrder: Int
): ParsedConferenceRow? {
    val team = entry["team"]?.asJsonObjectOrNull() ?: return null
    val stats = entry["stats"]
        ?.asJsonArrayOrNull()
        .orEmpty()
        .mapNotNull { it as? JsonObject }

    val wins = findStatInt(stats, "wins", "w") ?: return null
    val losses = findStatInt(stats, "losses", "l") ?: return null

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

    return ParsedConferenceRow(
        sourceOrder = sourceOrder,
        abbreviation = abbreviation,
        teamName = teamName,
        wins = wins,
        losses = losses
    )
}

private fun findStatInt(
    stats: List<JsonObject>,
    vararg names: String
): Int? {
    val wanted = names.map { it.lowercase() }.toSet()

    val stat = stats.firstOrNull { item ->
        val name = item.string("name")?.lowercase()
        val abbreviation = item.string("abbreviation")?.lowercase()
        wanted.contains(name) || wanted.contains(abbreviation)
    } ?: return null

    return stat.numberToInt("value")
        ?: stat.string("displayValue")?.toDoubleOrNull()?.toInt()
}

private fun JsonObject.string(key: String): String? {
    return this[key]?.jsonPrimitive?.contentOrNull
}

private fun JsonObject.numberToInt(key: String): Int? {
    val primitive = this[key] as? JsonPrimitive ?: return null
    return primitive.contentOrNull?.toDoubleOrNull()?.toInt()
}

private fun JsonElement?.asJsonObjectOrNull(): JsonObject? = this as? JsonObject
private fun JsonElement?.asJsonArrayOrNull(): JsonArray? = this as? JsonArray