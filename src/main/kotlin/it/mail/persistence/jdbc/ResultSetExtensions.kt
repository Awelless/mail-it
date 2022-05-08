package it.mail.persistence.jdbc

import it.mail.domain.MailMessage
import it.mail.domain.MailMessageStatus
import it.mail.domain.MailMessageType
import it.mail.domain.MailMessageTypeState
import java.sql.ResultSet
import java.time.Instant

/*
 * All these extensions require appropriate result set column names.
 *
 * Pattern for column name is: {entityShortName}_{columnNameSnakeCase}
 *
 * Entity shortnames:
 * | class            | entityShortName |
 * |------------------|-----------------|
 * | MailMessage      | m               |
 * | MailMessageType  | mt              |
 */

internal fun ResultSet.getMailMessageTypeFromRow(): MailMessageType {
    val typeId = getLong("mt_mail_message_type_id")
    val typeName = getString("mt_name")

    val typeDescriptionValue = getString("mt_description")
    val typeDescription = if (wasNull()) null else typeDescriptionValue

    val typeMaxRetriesCountValue = getInt("mt_max_retries_count")
    val typeMaxRetriesCount = if (wasNull()) null else typeMaxRetriesCountValue

    val typeState = MailMessageTypeState.valueOf(getString("mt_state"))

    return MailMessageType(
        id = typeId,
        name = typeName,
        description = typeDescription,
        maxRetriesCount = typeMaxRetriesCount,
        state = typeState,
    )
}

internal fun ResultSet.getMailMessageWithTypeFromRow(): MailMessage {
    val id = getLong("m_mail_message_id")
    val text = getString("m_text")
    val subject = getString("m_subject")
    val emailFrom = getString("m_email_from")
    val emailTo = getString("m_email_to")
    val createdAt = getObject("m_created_at", Instant::class.java)

    val sendingStartedAtValue = getObject("m_sending_started_at", Instant::class.java)
    val sendingStartedAt = if (wasNull()) null else sendingStartedAtValue

    val sentAtValue = getObject("m_sent_at", Instant::class.java)
    val sentAt = if (wasNull()) null else sentAtValue

    val status = MailMessageStatus.valueOf(getString("m_status"))
    val failedCount = getInt("m_failed_count")

    return MailMessage(
        id = id,
        text = text,
        subject = subject,
        emailFrom = emailFrom,
        emailTo = emailTo,
        type = getMailMessageTypeFromRow(),
        createdAt = createdAt,
        sendingStartedAt = sendingStartedAt,
        sentAt = sentAt,
        status = status,
        failedCount = failedCount,
    )
}