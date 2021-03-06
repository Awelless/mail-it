package it.mail.domain.core.mailing

import it.mail.domain.model.MailMessage
import it.mail.domain.model.MailMessageStatus.CANCELED
import it.mail.domain.model.MailMessageStatus.FAILED
import it.mail.domain.model.MailMessageStatus.PENDING
import it.mail.domain.model.MailMessageStatus.RETRY
import it.mail.domain.model.MailMessageStatus.SENDING
import it.mail.domain.model.MailMessageStatus.SENT
import it.mail.exception.NotFoundException
import it.mail.persistence.api.MailMessageRepository
import mu.KLogging
import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class MailMessageService(
    private val mailMessageRepository: MailMessageRepository,
) {
    companion object : KLogging()

    private val hungMessageStatuses = listOf(SENDING)
    private val hungMessageDuration = 2.minutes.toJavaDuration()

    private val possibleToSendMessageStatuses = listOf(PENDING, RETRY)

    suspend fun getAllHungMessages(): List<MailMessage> =
        mailMessageRepository.findAllWithTypeByStatusesAndSendingStartedBefore(hungMessageStatuses, Instant.now().minus(hungMessageDuration))

    suspend fun getAllIdsOfPossibleToSentMessages(): List<Long> =
        mailMessageRepository.findAllIdsByStatusIn(possibleToSendMessageStatuses)

    suspend fun getMessageForSending(messageId: Long): MailMessage {
        if (mailMessageRepository.updateMessageStatusAndSendingStartedTimeByIdAndStatusIn(messageId, possibleToSendMessageStatuses, SENDING, Instant.now()) == 0) {
            throw NotFoundException("MailMessage, id: $messageId for delivery is not found")
        }

        return mailMessageRepository.findOneWithTypeById(messageId)
            ?: throw NotFoundException("MailMessage, id: $messageId for delivery is not found")
    }

    suspend fun processSuccessfulDelivery(mailMessage: MailMessage) {
        mailMessage.apply {
            status = SENT
            sentAt = Instant.now()
        }

        mailMessageRepository.updateMessageStatusAndSentTime(mailMessage.id, mailMessage.status, mailMessage.sentAt!!)
    }

    suspend fun processFailedDelivery(mailMessage: MailMessage) {
        mailMessage.apply {
            val maxRetries = type.maxRetriesCount ?: Int.MAX_VALUE

            if (failedCount >= maxRetries) {
                status = FAILED
                logger.error { "Max number of retries exceeded. Marking MailMessage: $id as failed" }
            } else {
                failedCount++
                status = RETRY
                logger.info { "Failed MailMessage: $id is scheduled for another delivery" }
            }

            sendingStartedAt = null
        }

        mailMessageRepository.updateMessageStatusFailedCountAndSendingStartedTime(mailMessage.id, mailMessage.status, mailMessage.failedCount, null)
    }

    suspend fun processMessageTypeForceDeletion(mailMessage: MailMessage) {
        mailMessage.status = CANCELED

        mailMessageRepository.updateMessageStatus(mailMessage.id, mailMessage.status)
    }
}
