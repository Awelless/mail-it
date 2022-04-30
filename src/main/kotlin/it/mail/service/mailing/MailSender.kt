package it.mail.service.mailing

import io.quarkus.mailer.Mail
import io.quarkus.mailer.reactive.ReactiveMailer
import io.smallrye.mutiny.coroutines.awaitSuspending
import it.mail.domain.MailMessage
import mu.KLogging

class MailSender(
    private val mailer: ReactiveMailer,
) {
    companion object : KLogging()

    suspend fun send(mailMessage: MailMessage) {
        // TODO add support for plain html and templates

        val mail = Mail.withText(mailMessage.emailTo, mailMessage.subject, mailMessage.text)
        if (mailMessage.emailFrom != null) {
            mail.from = mailMessage.emailFrom
        }

        mailer.send(mail).awaitSuspending()

        logger.debug { "Successfully sent message: ${mailMessage.id}" }
    }
}
