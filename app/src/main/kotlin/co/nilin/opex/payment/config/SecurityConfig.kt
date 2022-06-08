package co.nilin.opex.payment.config

import co.nilin.opex.payment.utils.hasRoleWithAnyAuthorities
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain

@EnableWebFluxSecurity
class SecurityConfig {

    @Value("\${app.opex.cert-url}")
    private lateinit var jwkUrl: String

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
        http.csrf().disable()
            .authorizeExchange()
            .pathMatchers("/actuator/health").permitAll()
            .pathMatchers("/v1/payment/pay/**").permitAll()
            .pathMatchers("/v1/payment/**").hasRoleWithAnyAuthorities("user_kyc", "SCOPE_trust", "SCOPE_ipg")
            .pathMatchers("/v1/invoice/**").hasRoleWithAnyAuthorities("user_kyc", "SCOPE_trust")
            .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .anyExchange().authenticated()
            .and()
            .oauth2ResourceServer()
            .jwt()
        return http.build()
    }

    @Bean
    @Throws(Exception::class)
    fun reactiveJwtDecoder(): ReactiveJwtDecoder? {
        return NimbusReactiveJwtDecoder.withJwkSetUri(jwkUrl).build()
    }

}