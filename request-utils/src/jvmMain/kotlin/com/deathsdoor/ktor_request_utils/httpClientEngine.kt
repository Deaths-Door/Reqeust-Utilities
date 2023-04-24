package com.deathsdoor.ktor_request_utils

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*

actual val httpClientEngine: HttpClientEngine by lazy { CIO.create() }