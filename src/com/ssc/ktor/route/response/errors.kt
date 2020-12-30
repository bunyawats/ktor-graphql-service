package com.ssc.ktor.route.response

import com.ssc.ktor.route.ErrorResp
import com.ssc.ktor.route.exceptions.BadRequestBodyException
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