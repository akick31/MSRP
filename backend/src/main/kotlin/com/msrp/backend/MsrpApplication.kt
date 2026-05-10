package com.msrp.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
open class MsrpApplication

fun main(args: Array<String>) {
    runApplication<MsrpApplication>(*args)
}
