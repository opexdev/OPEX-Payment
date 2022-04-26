package co.nilin.opex.payment.config

import co.nilin.opex.payment.utils.logger.CustomLogger
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.loadbalancer.LoadBalancerProperties
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

@Configuration
class WebClientConfig {

    @Bean
    @Primary
    fun webClient(): WebClient {
        val logger = CustomLogger(HttpClient::class.java)
        return WebClient.builder()
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient
                        .create()
                        .doOnRequest { _, connection ->
                            connection.addHandlerFirst(logger)
                        }
                )
            )
            .build()
    }

    @Bean
    @Qualifier("loadBalanced")
    fun loadBalancedWebClient(loadBalancerFactory: ReactiveLoadBalancer.Factory<ServiceInstance>): WebClient {
        val logger = CustomLogger(HttpClient::class.java)
        return WebClient.builder()
            .filter(
                ReactorLoadBalancerExchangeFilterFunction(
                    loadBalancerFactory,
                    LoadBalancerProperties(),
                    emptyList()
                )
            )
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient
                        .create()
                        .doOnRequest { _, connection ->
                            connection.addHandlerFirst(logger)
                        }
                )
            )
            .build()
    }

}
