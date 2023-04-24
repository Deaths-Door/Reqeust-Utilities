package com.deathsdoor.ktor_request_utils

fun Int.hasToBeInRange(from:Int, to:Int, action:() -> String) = require(this in from..to, lazyMessage = action)
fun Int.hasToBeInRange(from:Int, to:Int, variableName:String = this::class.simpleName!!) = require(this in from..to){ "$variableName has to be between $from to $to" }

fun Float.hasToBeInRange(from:Float, to:Float, action:() -> String) = require(this in from..to, lazyMessage = action)
fun Float.hasToBeInRange(from:Float, to:Float, variableName:String = this::class.simpleName!!) = require(this in from..to){ "$variableName has to be between $from to $to" }

fun Double.hasToBeInRange(from:Double, to:Double, action:() -> String) = require(this in from..to, lazyMessage = action)
fun Double.hasToBeInRange(from:Double, to:Double, variableName:String = this::class.simpleName!!) = require(this in from..to){ "$variableName has to be between $from to $to" }

fun Long.hasToBeInRange(from:Long, to:Long, action:() -> String) = require(this in from..to, lazyMessage = action)
fun Long.hasToBeInRange(from:Long, to:Long, variableName:String = this::class.simpleName!!) = require(this in from..to){ "$variableName has to be between $from to $to" }
