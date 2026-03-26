package com.olliesbrother.nbastandingsapp.data

import android.content.Context
import com.olliesbrother.nbastandingsapp.model.Conference
import com.olliesbrother.nbastandingsapp.model.ConferenceStandings
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StandingsCache(context: Context) {

    private val prefs = context.getSharedPreferences("nba_standings_cache", Context.MODE_PRIVATE)

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun read(): CachedStandingsSnapshot? {
        val raw = prefs.getString(KEY_STANDINGS, null) ?: return null

        val payload = runCatching {
            json.decodeFromString<CachedStandingsPayload>(raw)
        }.getOrNull() ?: return null

        val formattedUpdatedAt = formatUpdatedAt(payload.savedAtMillis)

        return CachedStandingsSnapshot(
            standings = mapOf(
                Conference.EAST to payload.east.copy(updatedAt = formattedUpdatedAt),
                Conference.WEST to payload.west.copy(updatedAt = formattedUpdatedAt)
            ),
            savedAtMillis = payload.savedAtMillis
        )
    }

    fun write(standings: Map<Conference, ConferenceStandings>) {
        val east = standings[Conference.EAST] ?: return
        val west = standings[Conference.WEST] ?: return

        val payload = CachedStandingsPayload(
            east = east,
            west = west,
            savedAtMillis = System.currentTimeMillis()
        )

        prefs.edit()
            .putString(KEY_STANDINGS, json.encodeToString(payload))
            .apply()
    }

    private fun formatUpdatedAt(savedAtMillis: Long): String {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        return "Updated ${formatter.format(Date(savedAtMillis))}"
    }

    companion object {
        private const val KEY_STANDINGS = "standings_json"
    }
}

data class CachedStandingsSnapshot(
    val standings: Map<Conference, ConferenceStandings>,
    val savedAtMillis: Long
)

@Serializable
private data class CachedStandingsPayload(
    val east: ConferenceStandings,
    val west: ConferenceStandings,
    val savedAtMillis: Long
)