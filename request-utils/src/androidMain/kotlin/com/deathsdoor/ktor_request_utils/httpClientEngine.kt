package com.deathsdoor.ktor_request_utils

import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*

actual val httpClientEngine: HttpClientEngine by lazy{ OkHttp.create() }