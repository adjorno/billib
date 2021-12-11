package com.adjorno.billib.rest

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@EnableScheduling
@EnableAsync
@SpringBootApplication
open class BBRestApplication {
    @Bean
    open fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**")
            }
        }
    }
}

const val PASSWORD = "vtldtlm"
fun main(args: Array<String>) {
    SpringApplication.run(BBRestApplication::class.java, *args)
}
