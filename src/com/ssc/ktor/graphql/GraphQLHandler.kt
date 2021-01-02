package com.ssc.ktor.graphql

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ssc.ktor.graphql.schema.models.User
import graphql.ExceptionWhileDataFetching
import graphql.ExecutionInput
import graphql.ExecutionResult
import graphql.GraphQL
import graphql.schema.GraphQLSchema
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import org.dataloader.DataLoaderRegistry
import org.kodein.di.DI
import org.kodein.di.instance
import java.io.IOException


data class AuthorizedContext(val authorizedUser: User? = null, var guestUUID: String? = null)

class GraphQLHandler(kodein: DI) {

    private val graphQLSchema by kodein.instance<GraphQLSchema>()
    private val dataLoaderRegistry by kodein.instance<DataLoaderRegistry>()

    private val mapper = jacksonObjectMapper()
    private val graphQL = GraphQL.newGraphQL(graphQLSchema).build()!!

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
            e.printStackTrace()
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
            val resultMap = getResult(executionResult)

            applicationCall.respond(resultMap)
        }
    }
}