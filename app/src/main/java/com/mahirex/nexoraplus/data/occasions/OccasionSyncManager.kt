package com.mahirex.nexoraplus.data.occasions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Downloads the latest occasions calendar and applies it to ALL accounts
 * (occasions are stored once, shared by every account).
 *
 * Expected JSON shape:
 * {
 *   "version": 2,
 *   "occasions": [
 *     {"calType":0,"month":1,"day":10,"kind":0,"title":"عاشورای حسینی",
 *      "description":"...","isHoliday":true,"emoji":"🏴","colorHex":"#475569"}
 *   ]
 * }
 */
object OccasionSyncManager {

    /**
     * Point this to your hosted dataset (e.g. a GitHub raw file you maintain).
     * The core app never needs the network — this is the only optional online feature.
     */
    const val DEFAULT_URL: String =
        "https://raw.githubusercontent.com/MAHIREX/nexora-plus-data/main/occasions.json"

    @Serializable
    data class OccasionDto(
        val calType: Int,
        val month: Int,
        val day: Int,
        val kind: Int,
        val title: String,
        val description: String = "",
        val isHoliday: Boolean = false,
        val emoji: String = "🕌",
        val colorHex: String = "#5B3FE0"
    )

    @Serializable
    data class OccasionFile(val version: Int, val occasions: List<OccasionDto>)

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * Fetches and parses the remote file. Returns the parsed file or throws.
     * Suspending and always off the main thread — the HttpURLConnection I/O can never
     * leak onto the caller's dispatcher (NetworkOnMainThreadException guard).
     */
    suspend fun fetch(url: String = DEFAULT_URL): OccasionFile = withContext(Dispatchers.IO) {
        val connection = (java.net.URI(url).toURL().openConnection() as java.net.HttpURLConnection).apply {
            connectTimeout = 12_000
            readTimeout = 12_000
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "NexoraPlus/2.5")
        }
        try {
            val code = connection.responseCode
            if (code !in 200..299) throw IllegalStateException("HTTP $code")
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val parsed = json.decodeFromString<OccasionFile>(body)
            require(parsed.occasions.isNotEmpty()) { "Empty occasions list" }
            require(parsed.version > 0) { "Invalid version" }
            parsed
        } finally {
            connection.disconnect()
        }
    }
}
