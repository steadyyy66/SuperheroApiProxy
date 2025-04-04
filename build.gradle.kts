import com.google.protobuf.gradle.* // ⬅️ 关键导入

plugins {
    kotlin("jvm") version "2.0.10"
    id("com.google.protobuf") version "0.9.4"
}

group = "com.kody"
version = "1.0-SNAPSHOT"

val grpcVersion = "1.63.0"
val grpcKotlinVersion = "1.4.1"
val protobufVersion = "3.25.3"

repositories {
    mavenCentral()
}

dependencies {
//    testImplementation(kotlin("test"))

    implementation("io.ktor:ktor-server-metrics-micrometer:2.3.7")
    implementation("io.ktor:ktor-server-netty:2.3.7")
    implementation("com.typesafe:config:1.4.2")

    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.23")
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("ch.qos.logback:logback-classic:1.2.11")

    // gRPC Kotlin
    implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")
    implementation("io.grpc:grpc-netty-shaded:$grpcVersion")
    implementation(kotlin("stdlib"))


    implementation("io.ktor:ktor-server-core-jvm:2.3.7")

    // 协程
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
        create("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
        }
    }
}

sourceSets {
    main {
        proto.srcDir("src/main/proto") // 放你的 .proto 文件
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}
tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("**/*.proto")
}