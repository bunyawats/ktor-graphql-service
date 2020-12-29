package com.ssc.ktor.graphql

import com.fasterxml.jackson.databind.SerializationFeature
import com.ssc.ktor.graphql.database.Database
import com.ssc.ktor.graphql.database.FlywayFeature
import com.ssc.ktor.graphql.domain.Pageable
import com.ssc.ktor.graphql.errors.statusPageConfiguration
import com.ssc.ktor.graphql.rest.channels
import com.ssc.ktor.graphql.rest.jackson.customJackson
import com.ssc.ktor.graphql.service.TvService
import freemarker.cache.ClassTemplateLoader
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.freemarker.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.routing.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)


fun Application.module(testing: Boolean = false) {
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }



    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        header("MyCustomHeader")
        allowCredentials = true
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }

    install(Authentication) {
        basic("myBasicAuth") {
            realm = "Ktor Server"
            validate { if (it.name == "test" && it.password == "password") UserIdPrincipal(it.name) else null }
        }
    }

    val database = Database(this)
    val tvService = TvService(database)

    install(Locations)
    install(StatusPages, statusPageConfiguration)
    install(FlywayFeature) { dataSource = database.connectionPool }
    install(ContentNegotiation) { customJackson { enable(SerializationFeature.INDENT_OUTPUT) } }

    routing {

        channels(tvService)

        get("/") {

            println("\n ${tvService.getChannels(Pageable(0, 5))}" )
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        get("/html-freemarker") {
            call.respond(FreeMarkerContent("index.ftl", mapOf("data" to IndexData(listOf(1, 2, 3))), ""))
        }

        get<MyLocation> {
            call.respondText("Location: name=${it.name}, arg1=${it.arg1}, arg2=${it.arg2}")
        }
        // Register nested routes
        get<Type.Edit> {
            call.respondText("Inside $it")
        }
        get<Type.List> {
            call.respondText("Inside $it")
        }

        authenticate("myBasicAuth") {
            get("/protected/route/basic") {
                val principal = call.principal<UserIdPrincipal>()!!
                call.respondText("Hello ${principal.name}")
            }
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

data class JsonSampleClass(val hello: String)

