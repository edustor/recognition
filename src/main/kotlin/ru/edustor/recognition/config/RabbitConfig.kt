package ru.edustor.recognition.config

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class RabbitConfig {
    @Bean
    open fun rabbitRejectedExchange(): TopicExchange {
        return TopicExchange("reject.edustor.ru", true, false)
    }

    @Bean
    open fun rabbitRejectedQueue(): Queue {
        return Queue("rejected.edustor.ru", true, false, false)
    }

    @Bean
    open fun rabbitRejectedBinding(): Binding {
        return Binding("rejected.edustor.ru", Binding.DestinationType.QUEUE, "reject.edustor.ru", "#", null)
    }
}