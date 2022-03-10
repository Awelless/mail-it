package it.mail.service.external

import it.mail.domain.MailMessage
import it.mail.repository.MailMessageRepository
import mu.KLogging
import java.time.Instant
import java.util.UUID.randomUUID
import javax.enterprise.context.ApplicationScoped
import javax.transaction.Transactional

@ApplicationScoped
class MailMessageService(
    private val mailMessageRepository: MailMessageRepository,
    private val mailMessageTypeService: ExternalMailMessageTypeService
) {
    companion object: KLogging()

    @Transactional
    fun createNewMail(text: String, subject: String?, emailFrom: String, emailTo: String, messageTypeName: String): MailMessage {
        val messageType = mailMessageTypeService.getTypeByName(messageTypeName)

        val externalId = randomUUID().toString()

        val message = MailMessage(
            text = text,
            subject = subject,
            emailFrom = emailFrom,
            emailTo = emailTo,
            externalId = externalId,
            type = messageType,
            createdAt = Instant.now(),
        )

        mailMessageRepository.persist(message)

        logger.debug { "Persisted message: $externalId" }

        return message
    }
}