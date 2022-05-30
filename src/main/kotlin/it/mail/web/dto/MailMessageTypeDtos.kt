package it.mail.web.dto

import it.mail.domain.HtmlTemplateEngine
import it.mail.service.admin.MailMessageContentType

data class PagedMailMessageTypeResponseDto(
    val id: Long,
    val name: String,
    val description: String?,
    val maxRetriesCount: Int?,
    val contentType: MailMessageContentType,
)

data class SingleMailMessageTypeResponseDto(
    val id: Long,
    val name: String,
    val description: String?,
    val maxRetriesCount: Int?,
    val contentType: MailMessageContentType,
    /**
     * Not null if [contentType] is [MailMessageContentType.HTML]
     */
    val templateEngine: HtmlTemplateEngine?,
    /**
     * Not null if [contentType] is [MailMessageContentType.HTML]
     */
    val template: String?,
)

data class MailMessageTypeCreateDto(
    val name: String,
    val description: String?,
    val maxRetriesCount: Int?,
    val contentType: MailMessageContentType,
    /**
     * Not null if [contentType] is [MailMessageContentType.HTML]
     */
    val templateEngine: HtmlTemplateEngine? = null,
    /**
     * Not null if [contentType] is [MailMessageContentType.HTML]
     */
    val template: String? = null,
)

data class MailMessageTypeUpdateDto(
    val description: String?,
    val maxRetriesCount: Int?,
    val templateEngine: HtmlTemplateEngine? = null,
    val template: String? = null,
)
