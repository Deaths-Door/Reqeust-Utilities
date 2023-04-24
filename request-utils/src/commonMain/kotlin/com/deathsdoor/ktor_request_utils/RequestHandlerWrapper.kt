package com.deathsdoor.ktor_request_utils

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.reflect.KClass

abstract class RequestHandlerWrapper {
    class RequestFailedException(message: String) : Exception(message)
    class InvalidJsonResponseException(expectedType: KClass<*>, jsonResponse: String, override val cause: Throwable?) : RuntimeException( "Error converting JSON to type ${expectedType.simpleName} due to ${cause?.message}. \nActual JSON: $jsonResponse"){
        constructor(expectedType: KClass<*>, jsonElement: JsonElement) : this(expectedType,jsonElement.toString())
        constructor(expectedType: KClass<*>, jsonResponse: String): this(expectedType,jsonResponse,null)
    }

    companion object {
        val json by lazy { Json }
    }

    protected open val client : HttpClient by lazy { HttpClient(httpClientEngine) }
    protected open val json : Json by lazy { Json }
    protected open val baseURL : String? = null
    protected open val apiKey : String? = null
    protected open val apiKeyAsString : String? = null

    protected open val clientID : String? = null
    protected open val clientSecret : String? = null


    protected open suspend fun HttpResponse.responseMessage(): JsonElement? = json.decodeFromString<JsonObject>(this.bodyAsText())
    protected open suspend fun HttpResponse.responseHeader(): JsonElement? = this.responseMessage()?.jsonObject?.get("header")
    protected open suspend fun HttpResponse.responseStatusCode(): Int = this.responseHeader()?.jsonObject?.get("status_code").toString().toInt()
    protected open suspend fun HttpResponse.responseBody(): JsonObject? = this.responseMessage()?.jsonObject?.get("body")?.jsonObject

    protected open fun concentrateBaseUndEndpoint(endpoint: Endpoint) = "${baseURL}${endpoint.asString}?"
    protected open suspend fun requestHttpRequest(endpoint: Endpoint, parameters: Map<RequestParameter, Any?>, extraHeaders: Map<RequestParameter, Any?>): HttpResponse {
        return client.get(concentrateBaseUndEndpoint(endpoint)){
            parameter(if(apiKeyAsString == null) RequestParameter.ApiKey.asString else apiKeyAsString!!,apiKey)
            parameters.forEach { parameter(it.key.asString,it.value) }
            extraHeaders.forEach { header(it.key.asString,it.value) }
        }
    }
    protected open suspend fun handleErrors(response: HttpResponse){
        throw RequestFailedException("RequestFailed with status code of ${response.responseStatusCode()} and a response body of ${response.bodyAsText()}")
    }
    protected open suspend fun HttpResponse.isRequestSuccessful(action : suspend (response:HttpResponse) -> Boolean = { it.responseStatusCode() == 200 }) = action(this)
    protected open suspend fun <T> performDefaultAction(endpoint: Endpoint, parameters: Map<RequestParameter,Any?> = mapOf(), extraHeaders: Map<RequestParameter, Any?> = mapOf(), parameterCheck:(parameters : Map<RequestParameter,Any?>) -> Unit = {}, deserializer:(jsonObject: JsonObject?) -> T): T? {
        parameterCheck(parameters)
        val response = requestHttpRequest(endpoint,parameters,extraHeaders)
        if(!response.isRequestSuccessful()){
            handleErrors(response)
            return null
        }
        return deserializer(response.responseBody())
    }
}