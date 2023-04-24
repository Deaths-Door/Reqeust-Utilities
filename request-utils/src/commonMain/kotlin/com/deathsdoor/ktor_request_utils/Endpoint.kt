package com.deathsdoor.ktor_request_utils

open class Endpoint(val asString: String){
    protected open fun asEndPoint(vararg params: Any?): Endpoint = Endpoint("$asString/${params.joinToString(separator = "/") { it.toString() }}")

    protected companion object {
        fun generateObjects(endpoints:String, separator:Char): String = generateObjects(*endpoints.split(separator).toTypedArray())
        //TODO update it do handle more cases
        fun generateObjects(vararg endpoints:String): String =
            endpoints.distinct().joinToString() {
                "object ${it.split(".").joinToString words@ { word -> return@words if(word.isEmpty()) "" else "${word[0].uppercaseChar()}${word.drop(1)}" }.replace(",","").replace(" ","")} : Endpoint(\"$it\") \n"
            }.replace(",","")
        fun generateClass(vararg endpoints:String): String = "private abstract class ApiEndpoints {\n ${generateObjects(*endpoints)} }"
        fun generateClass(endpoints:String, separator:Char): String = generateClass(*endpoints.split(separator).toTypedArray())
    }
}

