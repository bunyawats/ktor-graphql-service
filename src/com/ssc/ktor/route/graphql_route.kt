package com.ssc.ktor.route

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ssc.ktor.graphql.models.*
import graphql.ExceptionWhileDataFetching
import graphql.ExecutionInput
import graphql.ExecutionResult
import graphql.GraphQL
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.dataloader.DataLoaderRegistry
import org.kodein.di.DI
import org.kodein.di.instance
import java.io.IOException

fun Route.graphqlRoute(kodein: DI) {

    val graphQL by kodein.instance<GraphQL>()
    val graphQLHandler = GraphQLHandler(graphQL)

    post("graphql") {
        graphQLHandler.handle(this.call)
    }

    get("playground") {
        this.call.respondText(buildPlaygroundHtml("graphql", "graphql"), ContentType.Text.Html)
    }
}

data class AuthorizedContext(val authorizedUser: User? = null, var guestUUID: String? = null)

class GraphQLHandler(private val graphQL: GraphQL) {

    private val mapper = jacksonObjectMapper()
    private val dataLoaderRegistry = DataLoaderRegistry()

    init {
        dataLoaderRegistry.register(UNIVERSITY_LOADER_NAME, batchUniversityLoader)
        dataLoaderRegistry.register(COURSE_LOADER_NAME, batchCourseLoader)
        dataLoaderRegistry.register(BATCH_BOOK_LOADER_NAME, batchBookLoader)
    }

    /**
     * Get payload from the request.
     */
    private suspend fun getPayload(request: ApplicationRequest): Map<String, Any>? {
        return try {
            mapper.readValue<Map<String, Any>>(request.call.receiveText())
        } catch (e: IOException) {
            throw IOException("Unable to parse GraphQL payload.")
        }
    }

    /**
     * Get the variables passed in the request.
     */
    private fun getVariables(payload: Map<String, *>) =
        payload.getOrElse("variables") { emptyMap<String, Any>() } as Map<String, Any>

    /**
     * Find attache user to context (authentication would go here)
     */
    private fun getContext(request: ApplicationRequest): AuthorizedContext {
        val loggedInUser = User(
            email = "fake@site.com",
            firstName = "Someone",
            lastName = "You Don't know",
            universityId = 4
        )
        return AuthorizedContext(loggedInUser)
    }

    /**
     * Get any errors and data from [executionResult].
     */
    private fun getResult(executionResult: ExecutionResult): MutableMap<String, Any> {
        val result = mutableMapOf<String, Any>()

        if (executionResult.errors.isNotEmpty()) {
            // if we encounter duplicate errors while data fetching, only include one copy
            result["errors"] = executionResult.errors.distinctBy {
                if (it is ExceptionWhileDataFetching) {
                    it.exception
                } else {
                    it
                }
            }
        }

        try {
            // if data is null, get data will fail exceptionally
            result["data"] = executionResult.getData<Any>()
        } catch (e: Exception) {
        }

        return result
    }

    /**
     * Execute a query against schema
     */
    suspend fun handle(applicationCall: ApplicationCall) {
        val payload = getPayload(applicationCall.request)

        payload?.let {
            // Execute the query against the schema
            val executionResult = graphQL.executeAsync(
                ExecutionInput.Builder()
                    .query(payload["query"].toString())
                    .variables(getVariables(payload))
                    .dataLoaderRegistry(dataLoaderRegistry)
                    .context(getContext(applicationCall.request))
            ).get()
            val result = getResult(executionResult)

            // write response as json
            applicationCall.response.call.respond(mapper.writeValueAsString(result))
        }
    }
}


private fun buildPlaygroundHtml(graphQLEndpoint: String, subscriptionsEndpoint: String) =
    Application::class.java.classLoader.getResource("graphql-playground.html")?.readText()
        ?.replace("\${graphQLEndpoint}", graphQLEndpoint)
        ?.replace("\${subscriptionsEndpoint}", subscriptionsEndpoint)
        ?: throw IllegalStateException("graphql-playground.html cannot be found in the classpath")