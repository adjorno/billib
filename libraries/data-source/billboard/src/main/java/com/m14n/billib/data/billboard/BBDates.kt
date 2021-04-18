package com.m14n.billib.data.billboard

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

fun generateBillboardDateSequence(startDate: Date, endDate: Date) =
    generateSequence(startDate) { currentDate ->
        Calendar.getInstance().apply {
            time = currentDate
            add(Calendar.DATE, weekIncrement(currentDate))
        }.time.takeUnless { it.after(endDate) }
    }

private fun weekIncrement(currentDate: Date) = CHART_DATE_FORMAT.format(currentDate).let { strDate ->
    when (strDate) {
        "2018-01-03" -> 3
        "2017-12-30" -> 4
        else -> 7
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = DateSerializer::class)
object DateSerializer : KSerializer<Date> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeString(value.time.toString())
    }

    override fun deserialize(decoder: Decoder): Date {
        return Date(decoder.decodeString().toLong())
    }
}

internal val CHART_DATE_FORMAT: DateFormat = SimpleDateFormat("yyyy-MM-dd")
val String.date: Date
    get()= CHART_DATE_FORMAT.parse(this)

val Date.text: String
    get() = CHART_DATE_FORMAT.format(this)
