package com.ssc.ktor.route

import com.ssc.ktor.graphql.GraphQLHandler
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import org.kodein.di.DI

fun Route.graphqlRoute(kodein: DI) {

    val graphQLHandler = GraphQLHandler(kodein)

    post("graphql") {
        graphQLHandler.handle(this.call)
    }

    get("playground") {
        this.call.respondText(buildPlaygroundHtml("graphql", "graphql"), ContentType.Text.Html)
    }
}


private fun buildPlaygroundHtml(graphQLEndpoint: String, subscriptionsEndpoint: String) =
    Application::class.java.classLoader.getResource("graphql-playground.html")?.readText()
        ?.replace("\${graphQLEndpoint}", graphQLEndpoint)
        ?.replace("\${subscriptionsEndpoint}", subscriptionsEndpoint)
        ?: throw IllegalStateException("graphql-playground.html cannot be found in the classpath")
