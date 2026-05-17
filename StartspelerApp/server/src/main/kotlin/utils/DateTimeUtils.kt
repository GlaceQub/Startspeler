package utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.Clock

fun instantToUtcDateTime(instant: Instant): LocalDateTime =
    instant.toLocalDateTime(TimeZone.UTC)

fun UtcNow(): LocalDateTime =
    Clock.System.now().toLocalDateTime(TimeZone.UTC)

fun LocalDateTime.toUtcIsoInstantString(): String =
    this.toInstant(TimeZone.UTC).toString()

fun DbUtcNow(): LocalDateTime =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
