package com.ifochka.m14n.rest.notification

interface FcmService {
    fun sendToTopic(
        topic: String,
        title: String,
        body: String,
    )
}
