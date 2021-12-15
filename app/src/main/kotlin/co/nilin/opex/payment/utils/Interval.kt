package co.nilin.opex.payment.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit

class Interval(val duration: Long, val unit: TimeUnit) {

    fun offsetTime() = unit.toMillis(duration)

    fun getDate() = Date(Date().time - offsetTime())

    fun getLocalDateTime(): LocalDateTime = with(Instant.ofEpochMilli(getDate().time)) {
        LocalDateTime.ofInstant(this, ZoneId.systemDefault())
    }

}