package com.deathsdoor.request_utils

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import kotlinx.serialization.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import kotlin.reflect.KClass

val GlobalJson by lazy { Json }

/// Trait to provide some basic info about API
interface RequestInfo {
    val BASE_URL: String

    // Returns the OkHttpClient instance associated with the API client.
    // The client is used to send
    val client : HttpClient by lazy { HttpClient(httpClientEngine) }

    val json by lazy { Json }
}

interface RequestModifiers : RequestInfo {

    // Joins the given endpoint with the base URL.
    // Returns the joined URL as a String.
    fun createEndpoint(endpoint: String): String = "${BASE_URL.trimEnd('/')}/$endpoint".trimEnd('/')

    // Conditionally adds a header to the given HttpRequestBuilder
    // based on the result of a closure.
    // If the closure returns true, the specified header with the provided key and value
    // will be added to the request. If the closure returns false, no changes will be made
    // to the request.
    fun addHeaderIf(
        requestBuilder: HttpRequestBuilder,
        key: String,
        value: String,
        closure: () -> Boolean
    ): HttpRequestBuilder = closure() ? requestBuilder.header(key, value) : requestBuilder
}

interface RequestDefaults : RequestModifiers {

    // Modifies the provided HttpRequestBuilder with default headers.
    // Returns the modified HttpRequestBuilder with default headers set.
    fun defaultHeaders(requestBuilder: HttpRequestBuilder): HttpRequestBuilder = requestBuilder

    // Modifies the provided HttpRequestBuilder with default parameters.
    // Returns the modified HttpRequestBuilder with default parameters set.
    fun defaultParameters(requestBuilder: HttpRequestBuilder): HttpRequestBuilder = requestBuilder

    // Modifies the provided HttpRequestBuilder with default settings for a post request.
    // Returns the modified HttpRequestBuilder with default settings applied.
    fun defaultPostRequestor(endpoint: String, json: String): HttpRequestBuilder {
        return defaultParameters(defaultHeaders(client.post(createEndpoint(endpoint)))).apply {
            body = json
        }
    }

    // Modifies the provided HttpRequestBuilder with default settings for a get request.
    // Returns the modified HttpRequestBuilder with default settings applied.
    fun defaultGetRequestor(endpoint: String, parameters: Parameters): HttpRequestBuilder {
        return defaultParameters(defaultHeaders(client.get(endpoint))).apply {
            url {
                parameters.forEach { (key, values) ->
                    appendAll(key, values)
                }
            }
        }
    }
}

sealed class RequestResult<out O, out E>

data class Success<out O>(val value: O) : RequestResult<O, Nothing>()
data class Failure<out E>(val error: E) : RequestResult<Nothing, E>()

interface RequestHandler<T : Any, O : Any, E : Any> : RequestDefaults {

    // Sends an HTTP request, processes the response, and maps it using the provided closure.
    // This asynchronous function sends an HTTP request using the given HttpRequestBuilder,
    // processes the response, and maps it using the provided closure. It returns the mapped
    // result if the request is successful, or a Failure if the
    // request fails.
    suspend fun requestMap(
        requestBuilder: HttpRequestBuilder,
        map: suspend (T) -> O
    ): RequestResult<O, E> {
        return try {
            val response = client().request(requestBuilder)
            val status = response.status

            val body = response.receive<String>()

            if (status.isSuccess()) {
                val json = json.decodeFromString(serializer<T>(), body)
                Success(map(json))
            } else {
                val json = json.decodeFromString(serializer<T>(), body)
                Failure(RequestError.ErrorPayload(json))
            }
        } catch (e: Exception) {
            Failure(RequestError.ErrorPayload(null))
        }
    }

    // Resolves the error in the response and returns an option containing the value or None.
    // An option containing the value if the response is successful, otherwise None.
    fun resolveError(
        response: RequestResult<O, E>,
        errorHandler: (RequestError<E>) -> Unit
    ): O? {
        return when (response) {
            is Success -> response.value
            is Failure -> {
                errorHandler(response.error)
                null
            }
        }
    }

    // This asynchronous function constructs (by default) a GET request using the defaultGetRequestor method
    // with the given endpoint and parameters. It then sends the request using the request method, expecting
    // a response of type T or an error of type E. The error is resolved using the resolveError method
    // and returns an Option<T> representing the response data if successful, or None if an error occurred.
    suspend fun getRequestHandler(
        endpoint: String,
        parameters: Parameters,
        map: suspend (T) -> O,
        errorHandler: (RequestError<E>) -> Unit
    ): O? {
        val request = defaultGetRequestor(endpoint, parameters)
        val response = requestMap(request, map)
        return resolveError(response, errorHandler)
    }

    // Handles a POST request to the specified endpoint with the provided JSON payload and returns the response data of type T.
    // This asynchronous function constructs a POST request using the defaultPostRequestor method with the given endpoint
    // and JSON payload. It then sends the request using the request method, expecting a response of type T or an error of type E.
    // The error is resolved using the resolveError method and returns an Option<T> representing the response data if successful,
    // or None if an error occurred.
    suspend fun postRequestHandler(
        endpoint: String,
        json: String,
        map: suspend (T) -> O,
        errorHandler: (RequestError<E>) -> Unit
    ): O? {
        val request = defaultPostRequestor(endpoint, json)
        val response = requestMap(request, map)
        return resolveError(response, errorHandler)
    }
}

inline fun <reified T> Encoder.encodeValue(value : T) = this.encodeSerializableValue(JsonObject.serializer(),Json.encodeToJsonElement(value).jsonObject)
inline fun <reified T> Encoder.encodeValue(serializer: SerializationStrategy<T>,value : T) = this.encodeSerializableValue(serializer,value)
@OptIn(InternalSerializationApi::class) fun <T : Any> Encoder.encodeValue(value : T, clazz:KClass<T>) = this.encodeSerializableValue(JsonObject.serializer(),Json.encodeToJsonElement(clazz.serializer(),value).jsonObject)

inline fun<reified T> deserializeFromJsonElement(value : JsonElement) = GlobalJson.decodeFromJsonElement<T>(value)
inline fun<reified T> deserializeFromString(value : String) = GlobalJson.decodeFromString<T>(value)

inline fun<reified T> JsonElement.deserialize() = GlobalJson.decodeFromJsonElement<T>(this)
inline fun<reified T> JsonObject.deserialize() = GlobalJson.decodeFromJsonElement<T>(this)
inline fun<reified T> JsonPrimitive.deserialize() = GlobalJson.decodeFromJsonElement<T>(this)

fun<T : Any> JsonElement.deserialize(deserializer: DeserializationStrategy<T>) = GlobalJson.decodeFromJsonElement<T>(deserializer,this)
inline fun<reified T> String.deserialize() = GlobalJson.decodeFromString<T>(this)

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
