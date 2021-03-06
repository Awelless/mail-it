package it.mail.domain.core.external

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import it.mail.domain.external.api.CreateMailCommand
import it.mail.domain.model.MailMessage
import it.mail.domain.model.MailMessageStatus.PENDING
import it.mail.domain.model.MailMessageType
import it.mail.exception.ValidationException
import it.mail.persistence.api.MailMessageRepository
import it.mail.persistence.api.MailMessageTypeRepository
import it.mail.test.createMailMessage
import it.mail.test.createPlainMailMessageType
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

@ExtendWith(MockKExtension::class)
class ExternalMailMessageServiceImplTest {

    @RelaxedMockK
    lateinit var mailMessageRepository: MailMessageRepository

    @RelaxedMockK
    lateinit var mailMessageTypeRepository: MailMessageTypeRepository

    @InjectMockKs
    lateinit var mailMessageService: ExternalMailMessageServiceImpl

    val mailMessageSlot = slot<MailMessage>()

    lateinit var mailType: MailMessageType

    @BeforeEach
    fun setUp() {
        mailType = createPlainMailMessageType()
    }

    @Test
    fun `createNewMail - when everything is correct - creates`() = runTest {
        // given
        val command = CreateMailCommand(
            text = "Some message",
            data = mapOf("name" to "john"),
            subject = "subject",
            emailFrom = "from@gmail.com",
            emailTo = "to@mail.com",
            mailMessageTypeId = mailType.id
        )

        coEvery { mailMessageTypeRepository.findById(mailType.id) }.returns(mailType)
        coEvery { mailMessageRepository.create(capture(mailMessageSlot)) }.returns(createMailMessage(mailType))

        // when
        mailMessageService.createNewMail(command)

        // then
        coVerify(exactly = 1) { mailMessageRepository.create(any()) }

        val savedMailMessage = mailMessageSlot.captured
        assertEquals(command.text, savedMailMessage.text)
        assertEquals(command.subject, savedMailMessage.subject)
        assertEquals(command.emailFrom, savedMailMessage.emailFrom)
        assertEquals(command.emailTo, savedMailMessage.emailTo)
        assertEquals(mailType, savedMailMessage.type)
        assertEquals(PENDING, savedMailMessage.status)
    }

    @Test
    fun `createNewMail - when message type is invalid - throws exception`() = runTest {
        val command = CreateMailCommand(
            text = "Some message",
            data = mapOf("name" to "john"),
            subject = "subject",
            emailFrom = "from@gmail.com",
            emailTo = "to@mail.com",
            mailMessageTypeId = 999,
        )

        coEvery { mailMessageTypeRepository.findById(command.mailMessageTypeId) }.returns(null)

        assertThrows<ValidationException> { mailMessageService.createNewMail(command) }

        coVerify(exactly = 0) { mailMessageRepository.create(any()) }
    }

    @ParameterizedTest
    @MethodSource("invalidDataForCreation")
    fun `createNewMail - with invalid data - throws exception`(
        subject: String?,
        emailFrom: String?,
        emailTo: String,
        expectedMessage: String
    ) = runTest {
        val command = CreateMailCommand(
            text = "123",
            data = emptyMap(),
            subject = subject,
            emailFrom = emailFrom,
            emailTo = emailTo,
            mailMessageTypeId = 1,
        )

        val exception = assertThrows<ValidationException> {
            mailMessageService.createNewMail(command)
        }

        assertEquals(expectedMessage, exception.message)

        coVerify(exactly = 0) { mailMessageRepository.create(any()) }
    }

    companion object {
        @JvmStatic
        private fun invalidDataForCreation(): List<Arguments> = listOf(
            arguments("subject", "email.email.com", "email@gmail.com", "emailFrom is incorrect"),
            arguments("subject", "email@email.com", "", "emailTo shouldn't be blank"),
            arguments("subject", null, "email.email.com", "emailTo is incorrect"),
        )
    }
}
