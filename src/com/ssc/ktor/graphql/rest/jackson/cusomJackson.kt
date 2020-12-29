package com.ssc.ktor.graphql.rest.jackson

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ssc.ktor.graphql.exceptions.BadRequestBodyException
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import io.ktor.utils.io.*
import io.ktor.utils.io.errors.*
import io.ktor.utils.io.jvm.javaio.*
import kotlin.reflect.jvm.jvmErasure


@KtorExperimentalAPI
fun ContentNegotiation.Configuration.customJackson(
    contentType: ContentType = ContentType.Application.Json,
    block: ObjectMapper.() -> Unit = {}
) {
    val mapper = jacksonObjectMapper()
    mapper.apply {
        setDefaultPrettyPrinter(DefaultPrettyPrinter().apply {
            indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
            indentObjectsWith(DefaultIndenter("  ", "\n"))
        })
    }
    mapper.apply(block)
    val converter = JacksonConverter(mapper)
    register(contentType, converter)
}

@KtorExperimentalAPI
class JacksonConverter(private val objectmapper: ObjectMapper = jacksonObjectMapper()) : ContentConverter {
    override suspend fun convertForSend(
        context: PipelineContext<Any, ApplicationCall>,
        contentType: ContentType,
        value: Any
    ): Any? {
        return TextContent(
            objectmapper.writeValueAsString(value),
            contentType.withCharset(context.call.suitableCharset())
        )
    }

    override suspend fun convertForReceive(context: PipelineContext<ApplicationReceiveRequest, ApplicationCall>): Any? {
        try {
            val request = context.subject
            val type = request.typeInfo.jvmErasure
            val value = request.value as? ByteReadChannel ?: return null
            val reader = value.toInputStream().reader(context.call.request.contentCharset() ?: Charsets.UTF_8)
            return objectmapper.readValue(reader, type.javaObjectType)
        } catch (exception: IOException) {
            throw BadRequestBodyException(exception.localizedMessage)
        }
    }
}
