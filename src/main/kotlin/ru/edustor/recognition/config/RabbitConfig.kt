package ru.edustor.recognition.config

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.interceptor.RetryOperationsInterceptor

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

    @Autowired fun configureContainer(factory: SimpleRabbitListenerContainerFactory) {
        factory.setAdviceChain(
                interceptor()
        )
    }

    @Bean
    open fun interceptor(): RetryOperationsInterceptor {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(1000, 5.0, 10000)
                .recoverer(RejectAndDontRequeueRecoverer())
                .build()
    }
}