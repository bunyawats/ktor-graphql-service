package com.ssc.ktor.graphql.rest

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.freemarker.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.sampleRoute() {

    get("/") {
        call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
    }

    get("/html-freemarker") {
        call.respond(FreeMarkerContent("index.ftl", mapOf("data" to IndexData(listOf(1, 2, 3))), ""))
    }

    // ../location/ggg?arg1=500
    get<MyLocation> {
        call.respondText("Location: name=${it.name}, arg1=${it.arg1}, arg2=${it.arg2}")
    }

    // ../type/car/edit
    get<Type.Edit> { typeEdit ->
        call.respondText("Inside $typeEdit")
    }

    // ../type/book/list/200
    get<Type.List> { typeList ->
        call.respondText("Inside $typeList")
    }

    authenticate("myBasicAuth") {
        get("/protected/route/basic") {
            val principal = call.principal<UserIdPrincipal>()!!
            call.respondText("Hello ${principal.name}")
        }
    }
}

data class IndexData(val items: List<Int>)

@Location("/location/{name}")
class MyLocation(val name: String, val arg1: Int = 42, val arg2: String = "default")

@Location("/type/{name}")
data class Type(val name: String) {
    @Location("/edit")
    data class Edit(val type: Type)

    @Location("/list/{page}")
    data class List(val type: Type, val page: Int)
}