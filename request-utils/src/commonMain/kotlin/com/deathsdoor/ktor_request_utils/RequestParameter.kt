package com.deathsdoor.ktor_request_utils

import kotlin.jvm.JvmName

//TODO update to add type so can check what type it is
open class RequestParameter(val asString:String){
    object ApiKey : RequestParameter("apikey")

    protected companion object {
        fun generateObjects(parameters:String, separator:Char): String = generateObjects(*parameters.split(separator).toTypedArray())
        fun generateObjects(vararg parameter:String): String =
            parameter.distinct().joinToString() {
                "object ${it.split("_").joinToString words@ { word -> return@words if(word.isEmpty()) "" else "${word[0].uppercaseChar()}${word.drop(1)}" }.replace(",","").replace(" ","")} : RequestParameter(\"$it\") \n"
                }.replace(",","")
        fun generateClass(vararg parameter:String): String = "private abstract class ApiRequestParameter {\n ${generateObjects(*parameter)} }"
        fun generateClass(parameters:String, separator:Char): String = generateClass(*parameters.split(separator).toTypedArray())
    }
}

