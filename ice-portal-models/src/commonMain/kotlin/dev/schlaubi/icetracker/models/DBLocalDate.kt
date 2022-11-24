package dev.schlaubi.icetracker.models

import kotlinx.datetime.LocalDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * [LocalDate] which gets serialized as `YYYY-MM-DD`.
 */
public typealias DBLocalDate = @Serializable(with = DBLocalDateSerializer::class) LocalDate

public class DBLocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("DBLocalDate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate): Unit =
        encoder.encodeString("${value.year}-${value.month}-${value.dayOfMonth}")

    override fun deserialize(decoder: Decoder): LocalDate {
        val (year, month, day) = decoder.decodeString().split("-")

        return LocalDate(year.toInt(), month.toInt(), day.toInt())
    }
}
