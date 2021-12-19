package co.nilin.opex.payment

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@ComponentScan("co.nilin.opex")
class PaymentApplication

fun main(args: Array<String>) {
	runApplication<PaymentApplication>(*args)
}
