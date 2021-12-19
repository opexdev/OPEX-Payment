package co.nilin.opex.payment.utils

fun <T> T.equalsAny(vararg items: T): Boolean {
    for (i in items)
        if (i == this)
            return true
    return false
}