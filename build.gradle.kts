import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import nu.studer.gradle.jooq.JooqEdition
import org.jooq.meta.jaxb.Property
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    application
    kotlin("jvm") version "1.4.21"
    id("nu.studer.jooq") version "5.2"
    id("com.github.johnrengelman.shadow") version "5.1.0"
    id("org.flywaydb.flyway") version "7.3.2"
}

group = "com.ssc.ktor.graphql"
version = "0.0.1"

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

repositories {
    mavenLocal()
    jcenter()
    maven { url = uri("https://kotlin.bintray.com/ktor") }
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-freemarker:$ktor_version")
    implementation("io.ktor:ktor-locations:$ktor_version")
    implementation("io.ktor:ktor-metrics:$ktor_version")
    implementation("io.ktor:ktor-auth:$ktor_version")
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-apache:$ktor_version")
    implementation("io.ktor:ktor-client-auth-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-json-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-gson:$ktor_version")
    implementation("io.ktor:ktor-client-logging-jvm:$ktor_version")
    implementation("io.ktor:ktor-jackson:$ktor_version")

    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("org.flywaydb:flyway-core:7.3.2")
    implementation("com.zaxxer:HikariCP:3.4.5")
    implementation("mysql:mysql-connector-java:8.0.22")

    implementation("com.expediagroup:graphql-kotlin-schema-generator:4.0.0-alpha.9")
    implementation("org.kodein.di:kodein-di:7.2.0")
    implementation("org.kodein.di:kodein-di-framework-ktor-server-jvm:7.1.0")

    jooqGenerator("mysql:mysql-connector-java:8.0.22")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
}


kotlin.sourceSets["main"].kotlin.srcDirs("src")
kotlin.sourceSets["test"].kotlin.srcDirs("test")

sourceSets["main"].resources.srcDirs("resources")
sourceSets["test"].resources.srcDirs("testresources")

val db_url: String by project
val db_username: String by project
val db_password: String by project

flyway {
    url = db_url
    user = db_username
    password = db_password
    baselineOnMigrate = true
    locations = arrayOf("filesystem:resources/db/migrations")
}

jooq {
    version.set("3.14.4")
    edition.set(JooqEdition.OSS)

    configurations {
        create("main") {
            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.WARN
                jdbc.apply {
                    driver = "com.mysql.cj.jdbc.Driver"
                    url = db_url
                    user = db_username
                    password = db_password
                    properties.add(Property().withKey("PAGE_SIZE").withValue("2048"))
                }
                generator.apply {
                    name = "org.jooq.codegen.DefaultGenerator"
                    database.apply {
                        name = "org.jooq.meta.mysql.MySQLDatabase"
                        inputSchema = "ktorgraphql"
                        includes = ".*"
                        excludes = "flyway_schema_history"

                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                        isRelations = true
                        isJavaTimeTypes = true
                    }
                    target.apply {
                        packageName = "com.ssc.jooq.db"
                        directory = "build/generated/jooq"
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
}


tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("joo-ktor")
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to application.mainClassName))
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}


