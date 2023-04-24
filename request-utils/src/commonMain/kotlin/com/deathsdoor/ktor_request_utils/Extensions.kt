@file:Suppress("UNUSED")

package com.deathsdoor.ktor_request_utils

import kotlinx.serialization.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import kotlin.reflect.KClass


inline fun <reified T> Encoder.encodeValue(value : T) = this.encodeSerializableValue(JsonObject.serializer(),Json.encodeToJsonElement(value).jsonObject)
inline fun <reified T> Encoder.encodeValue(serializer: SerializationStrategy<T>,value : T) = this.encodeSerializableValue(serializer,value)
@OptIn(InternalSerializationApi::class) fun <T : Any> Encoder.encodeValue(value : T, clazz:KClass<T>) = this.encodeSerializableValue(JsonObject.serializer(),Json.encodeToJsonElement(clazz.serializer(),value).jsonObject)

inline fun<reified T> deserializeFromJsonElement(value : JsonElement) = RequestHandlerWrapper.json.decodeFromJsonElement<T>(value)
inline fun<reified T> deserializeFromString(value : String) = RequestHandlerWrapper.json.decodeFromString<T>(value)

inline fun<reified T> JsonElement.deserialize() = RequestHandlerWrapper.json.decodeFromJsonElement<T>(this)
inline fun<reified T> JsonObject.deserialize() = RequestHandlerWrapper.json.decodeFromJsonElement<T>(this)
inline fun<reified T> JsonPrimitive.deserialize() = RequestHandlerWrapper.json.decodeFromJsonElement<T>(this)

fun<T : Any> JsonElement.deserialize(deserializer: DeserializationStrategy<T>) = RequestHandlerWrapper.json.decodeFromJsonElement<T>(deserializer,this)
inline fun<reified T> String.deserialize() = RequestHandlerWrapper.json.decodeFromString<T>(this)

inline fun <reified T> T.encodeAsJsonElement(): JsonElement = Json.encodeToJsonElement(this)

inline fun <reified T> List<T>.encodeAsJsonElement(): List<JsonElement> = this.map { it.encodeAsJsonElement() }
inline fun <reified T> List<T>.encodeAsJsonElement(action:(T) -> JsonElement): List<JsonElement> = this.map { action(it) }

val Decoder.asJsonElement: JsonElement get() = decodeSerializableValue(JsonElement.serializer())
val Decoder.asJsonObject: JsonObject get() = asJsonElement.jsonObject
val Decoder.asJsonArray: JsonArray get() = asJsonElement.jsonArray
val Decoder.asJsonPrimitive: JsonPrimitive get() = asJsonElement.jsonPrimitive
val Decoder.asJsonNull: JsonNull get() = asJsonElement.jsonNull

val JsonElement.isJsonObject: Boolean get() = this is JsonObject
val JsonElement.isJsonArray: Boolean get() = this is JsonArray
val JsonElement.isJsonPrimitive: Boolean get() = this is JsonPrimitive
val JsonElement.isJsonNull: Boolean get() = this is JsonNull

val JsonObject.isEntriesEmpty: Boolean get() = this.entries.isEmpty()
val JsonObject.isEntriesNotEmpty: Boolean get() = this.entries.isNotEmpty()

val JsonElement.print : JsonElement
    get() {
        println("jsonElement = $this")
        return this
    }

val JsonElement.printIsJsonObject: JsonElement
    get() {
        println("jsonElement is JsonObject = ${this.isJsonObject}")
        return this
    }
val JsonElement.printIsJsonArray: JsonElement
    get() {
        println("jsonElement is JsonArray = ${this.isJsonArray}")
        return this
    }
val JsonElement.printIsPrimitive: JsonElement
    get() {
        println("jsonElement is JsonPrimitive = ${this.isJsonPrimitive}")
        return this
    }
val JsonElement.printIsJsonNull : JsonElement
    get() {
        println("jsonElement is JsonNull = ${this.isJsonNull}")
        return this
    }