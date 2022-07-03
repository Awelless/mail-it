package it.mail.persistence.common.serialization

internal interface MailMessageDataSerializer {

    fun write(data: Map<String, Any?>?): ByteArray

    fun read(bytes: ByteArray?): Map<String, Any?>?
}