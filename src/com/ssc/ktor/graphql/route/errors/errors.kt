package com.ssc.ktor.graphql.errors

import com.ssc.ktor.graphql.exceptions.BadRequestBodyException
import com.ssc.ktor.graphql.route.ErrorResp
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.*
import org.slf4j.Logger

val statusPageConfiguration: StatusPages.Configuration.() -> Unit = {
    exception<Throwable> { cause ->
        when (cause) {
            is BadRequestBodyException -> call.respond(ErrorResp.of(cause))
            else -> call.respond(HttpStatusCode.InternalServerError, unhandledError(application.log, cause))
        }
    }
}

private fun unhandledError(log: Logger, cause: Throwable): ErrorResp {
    log.error(cause)
    return ErrorResp.of(
        HttpStatusCode.InternalServerError,
        "Internal Server Error",
        "${cause.javaClass}: ${cause.localizedMessage}"
    )
}