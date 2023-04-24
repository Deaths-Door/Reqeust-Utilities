package com.deathsdoor.ktor_request_utils

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

interface JsonDeserializer<T:Any> : KSerializer<T> {
    val clazz:KClass<T>
    val arrayName: String? get() = null
    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor get() = buildSerialDescriptor(clazz::class.simpleName!!, SerialKind.CONTEXTUAL)
    @OptIn(InternalSerializationApi::class)
    fun deserializeList(jsonElement: JsonElement?) : List<T> = (if(jsonElement?.isJsonObject == true) jsonElement.jsonObject[arrayName] else jsonElement)?.jsonArray?.mapNotNull { it.deserialize(clazz.serializer()) } ?: emptyList()
    override fun serialize(encoder: Encoder, value: T) = encoder.encodeValue(value,clazz)
}