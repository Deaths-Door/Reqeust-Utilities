package com.deathsdoor.request_utils

import io.ktor.client.engine.*
import io.ktor.client.engine.js.*

actual val httpClientEngine: HttpClientEngine by lazy{ Js.create() }