package co.nilin.opex.payment.config

import co.nilin.opex.payment.gateway.vandar.proxy.VandarProxy
import co.nilin.opex.payment.utils.hasRoleAndLevel
import co.nilin.opex.payment.utils.hasRoleWithAnyAuthorities
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
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
    private val logger = LoggerFactory.getLogger(SecurityConfig::class.java)

    @Profile("!otc")
    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
        http.csrf().disable()
            .authorizeExchange()
            .pathMatchers("/actuator/health").permitAll()
            .pathMatchers("/v1/payment/pay/**").permitAll()
            .pathMatchers("/v1/payment/verify/**").permitAll()
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
    @Profile("otc")
    fun otcSpringSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
        http.csrf().disable()
            .authorizeExchange()
            .pathMatchers("/actuator/health").permitAll()
            .pathMatchers("/v1/payment/pay/**").permitAll()
            .pathMatchers("/v1/payment/verify/**").permitAll()
            .pathMatchers("/v1/payment/**").hasRoleAndLevel("user", "Trusted")
            .pathMatchers("/v1/invoice/**").hasRoleAndLevel("user", "Trusted")
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