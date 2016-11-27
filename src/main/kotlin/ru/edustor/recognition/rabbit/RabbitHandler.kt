package ru.edustor.recognition.rabbit

import org.springframework.amqp.core.ExchangeTypes
import org.springframework.amqp.rabbit.annotation.*
import org.springframework.stereotype.Component

@Component
open class RabbitHandler {
    @RabbitListener(bindings = arrayOf(QueueBinding(
            value = Queue("recognition.edustor.ru/incoming", durable = "true", arguments = arrayOf(
                    Argument(name = "x-dead-letter-exchange", value = "reject.edustor.ru")
            )),
            exchange = Exchange("internal.edustor.ru", type = ExchangeTypes.TOPIC,
                    ignoreDeclarationExceptions = "true",
                    durable = "true"),
            key = "uploaded.pdf.events"
    )))
    fun processFile(msg: ByteArray) {

    }
}