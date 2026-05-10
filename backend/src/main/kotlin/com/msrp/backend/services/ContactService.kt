package com.msrp.backend.services

import com.msrp.backend.model.dto.ContactRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class ContactService(
    private val mailSender: JavaMailSender,
    @Value("\${spring.mail.username}") private val mailUsername: String,
) {
    private val log = LoggerFactory.getLogger(ContactService::class.java)

    companion object {
        private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }

    fun send(request: ContactRequest): ResponseEntity<Any> {
        return try {
            val name = request.name.trim().take(100)
            val email = request.email.trim().take(200)
            val subject = request.subject.trim().take(200)
            val message = request.message.trim().take(5000)

            require(name.isNotBlank()) { "Name is required" }
            require(email.isNotBlank() && EMAIL_REGEX.matches(email)) { "Valid email is required" }
            require(subject.isNotBlank()) { "Subject is required" }
            require(message.isNotBlank()) { "Message is required" }

            val mail = SimpleMailMessage()
            mail.setTo(mailUsername)
            mail.setFrom(mailUsername)
            mail.replyTo = email
            mail.subject = "[MSRP] $subject"
            mail.text =
                """
                From: $name <$email>

                $message
                """.trimIndent()

            mailSender.send(mail)
            log.info("Contact email sent from {} — subject: {}", email, subject)
            ResponseEntity.ok(mapOf("success" to true))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf("error" to "Failed to send message. Please try again later."))
        }
    }
}
