package com.deathsdoor.ktor_request_utils

import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*
import io.ktor.client.engine.ios.*

actual val httpClientEngine: HttpClientEngine by lazy { Darwin.create() }