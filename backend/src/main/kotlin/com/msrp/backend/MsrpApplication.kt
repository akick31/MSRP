package com.msrp.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = ["com.msrp.backend"])
open class MsrpApplication

fun main(args: Array<String>) {
    runApplication<MsrpApplication>(*args)
}
