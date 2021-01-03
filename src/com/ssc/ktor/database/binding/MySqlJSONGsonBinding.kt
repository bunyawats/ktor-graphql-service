package com.ssc.ktor.database.binding

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import org.jooq.*
import java.sql.SQLFeatureNotSupportedException
import java.sql.Types
import java.util.*


// We're binding <T> = Object (unknown database type), and <U> = JsonElement (user type)
class MySqlJSONGsonBinding : Binding<JSON, JsonElement> {

    override fun converter(): Converter<JSON, JsonElement> {

        return object : Converter<JSON, JsonElement> {

            override fun from(databaseObject: JSON?): JsonElement? {

                return if (databaseObject == null)
                    JsonNull.INSTANCE
                else Gson().fromJson(
                    "" + databaseObject,
                    JsonElement::class.java
                )
            }

            override fun to(userObject: JsonElement?): JSON? {

                return if (userObject == null || userObject === JsonNull.INSTANCE)
                    null
                else
                    JSON.valueOf(Gson().toJson(userObject))
            }

            override fun fromType(): Class<JSON> {
                return JSON::class.java
            }

            override fun toType(): Class<JsonElement> {
                return JsonElement::class.java
            }
        }
    }

    override fun sql(ctx: BindingSQLContext<JsonElement?>?) {
        ctx!!.render().sql("?")
    }

    override fun register(ctx: BindingRegisterContext<JsonElement?>?) {
        ctx!!.statement()
            .registerOutParameter(
                ctx.index(),
                Types.VARCHAR
            )
    }

    override fun set(ctx: BindingSetStatementContext<JsonElement>?) {
        ctx!!.statement()
            .setString(
                ctx.index(),
                Objects.toString(
                    ctx.convert(
                        converter()
                    ).value(),
                    null
                )
            )
    }

    override fun get(ctx: BindingGetResultSetContext<JsonElement?>?) {
        ctx!!.convert(converter())
            .value(
                JSON.valueOf(
                    ctx.resultSet().getString(
                        ctx.index()
                    )
                )
            )
    }

    override fun get(ctx: BindingGetStatementContext<JsonElement?>?) {
        ctx!!.convert(converter())
            .value(
                JSON.valueOf(
                    ctx.statement().getString(
                        ctx.index()
                    )
                )
            )
    }

    override fun set(ctx: BindingSetSQLOutputContext<JsonElement?>?) {
        throw SQLFeatureNotSupportedException()
    }

    override fun get(ctx: BindingGetSQLInputContext<JsonElement?>?) {
        throw SQLFeatureNotSupportedException()
    }

}
