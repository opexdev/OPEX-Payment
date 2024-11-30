package co.nilin.opex.payment.utils

import com.nimbusds.jose.shaded.json.JSONArray
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.oauth2.jwt.Jwt
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

fun ServerHttpSecurity.AuthorizeExchangeSpec.Access.hasRoleWithAnyAuthorities(
    role: String,
    vararg authorities: String
): ServerHttpSecurity.AuthorizeExchangeSpec = access { mono, _ ->
    mono.map { auth ->
        val hasAuthority = auth.authorities.map { it.authority }.containsAny(*authorities)
        val hasRole = ((auth.principal as Jwt).claims["roles"] as JSONArray?)?.contains(role) == true
        AuthorizationDecision(hasAuthority && hasRole)
    }
}

fun <T> Collection<T>.containsAny(vararg elements: T): Boolean {
    elements.forEach {
        if (this.contains(it))
            return true
    }
    return false
}

fun ServerHttpSecurity.AuthorizeExchangeSpec.Access.hasRoleAndLevel(
    role: String? = null,
    level: String? = null
): ServerHttpSecurity.AuthorizeExchangeSpec = access { mono, _ ->
    mono.map { auth ->
        val hasLevel = level?.let { ((auth.principal as Jwt).claims["level"] as String?)?.equals(level) == true }
            ?: true
        val hasRole = ((auth.principal as Jwt).claims["roles"] as JSONArray?)?.contains(role) == true
        AuthorizationDecision(hasLevel && hasRole)
    }
}
