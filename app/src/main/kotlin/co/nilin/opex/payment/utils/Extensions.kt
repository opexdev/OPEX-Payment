package co.nilin.opex.payment.utils

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import java.security.Principal

fun <T> T.equalsAny(vararg items: T): Boolean {
    for (i in items)
        if (i == this)
            return true
    return false
}

fun redirectTo(url: String) = ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
    .header(HttpHeaders.LOCATION, url)
    .build<String>()

fun <T> tryOrNull(action: () -> T): T? {
    return try {
        action()
    } catch (e: Exception) {
        return null
    }
}

fun SecurityContext.jwtAuthentication(): JwtAuthenticationToken {
    return authentication as JwtAuthenticationToken
}

fun Principal.jwtAuthentication(): JwtAuthenticationToken {
    return this as JwtAuthenticationToken
}

fun JwtAuthenticationToken.tokenValue(): String {
    return this.token.tokenValue
}