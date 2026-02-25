package com.ifochka.m14n.rest

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@EnableAsync
@SpringBootApplication
open class M14nApplication

fun main(args: Array<String>) {
    SpringApplication.run(M14nApplication::class.java, *args)
}
