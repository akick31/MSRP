package com.msrp.backend.controllers

import com.msrp.backend.model.dto.ContactRequest
import com.msrp.backend.services.ContactService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("\${api.base-path}/contact")
class ContactController(private val contactService: ContactService) {
    @PostMapping("")
    fun contact(
        @RequestBody request: ContactRequest,
    ): ResponseEntity<Any> = contactService.send(request)
}
