package com.startspeler.util

import kotlin.js.Date

private val utcTimestampWithoutZoneRegex =
    Regex("""^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(?::\d{2}(?:\.\d+)?)?$""")

fun formatUtcTimestampForDisplay(value: String?, emptyFallback: String = "-"): String {
    if (value.isNullOrBlank()) return emptyFallback

    val normalized = if (utcTimestampWithoutZoneRegex.matches(value)) "${value}Z" else value
    val parsed = Date(normalized)

    if (parsed.getTime().isNaN()) return value

    val day = parsed.getDate().toString().padStart(2, '0')
    val month = (parsed.getMonth() + 1).toString().padStart(2, '0')
    val year = parsed.getFullYear().toString()
    val hours = parsed.getHours().toString().padStart(2, '0')
    val minutes = parsed.getMinutes().toString().padStart(2, '0')

    return "$hours:$minutes $day-$month-$year"
}
