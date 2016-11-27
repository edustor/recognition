package ru.edustor.recognition.exception

import org.springframework.amqp.AmqpRejectAndDontRequeueException

class PdfNotFoundException(msg: String) : AmqpRejectAndDontRequeueException(msg)