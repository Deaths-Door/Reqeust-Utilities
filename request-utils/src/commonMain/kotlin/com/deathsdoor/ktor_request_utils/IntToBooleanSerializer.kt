package com.deathsdoor.ktor_request_utils

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object IntToBooleanSerializer : KSerializer<Boolean> {
    override fun deserialize(decoder: Decoder): Boolean = decoder.decodeInt() != 0
    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor get() = buildSerialDescriptor("IntToBooleanSerializer", SerialKind.CONTEXTUAL) { element<Boolean>(Boolean.toString())}
    override fun serialize(encoder: Encoder, value: Boolean): Unit = encoder.encodeInt(if (value) 1 else 0)
}