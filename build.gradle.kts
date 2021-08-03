import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.3.3.RELEASE"
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
    kotlin("jvm") version "1.3.72"
    kotlin("plugin.spring") version "1.3.72"
    kotlin("kapt") version "1.5.10"

}

group = "com.pecpwee"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenCentral()

    //import for binance-future api
    maven {
        setUrl("https://jitpack.io")
    }

}

val VERSION_RETROFIT = "2.9.0"
val XCHANGE_VERSION = "5.0.8"
val TELEGRAM_VERSION = "5.2.0"

dependencies {

    implementation(files("lib/dingtalk-taobao-sdk-java-auto_1479188381469-20210603.jar"))
    implementation("org.springframework.boot:spring-boot-starter-web")

    kapt("org.springframework.boot:spring-boot-configuration-processor")
//    implementation("")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
//    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("com.squareup.okhttp3:okhttp:4.8.1")
    implementation("com.squareup.retrofit2:retrofit:$VERSION_RETROFIT")
    implementation("com.squareup.retrofit2:converter-gson:$VERSION_RETROFIT")
    implementation("commons-codec:commons-codec:1.10")
    implementation("com.alibaba:fastjson:1.2.47")
    implementation("org.apache.commons:commons-lang3:3.0")
    implementation("com.squareup.retrofit2:converter-jackson:2.9.0")
    implementation("org.ta4j:ta4j-core:0.14")
    implementation("org.jfree:jfreechart:1.5.0")
    implementation("org.jfree:jcommon:1.0.24")


    implementation("org.telegram:telegrambots-spring-boot-starter:${TELEGRAM_VERSION}")
    implementation("org.telegram:telegrambotsextensions:${TELEGRAM_VERSION}")


//    //binance-future api https://jitpack.io/#binance-exchange/binance-java-api
//    implementation("com.github.binance-exchange:binance-java-api:8d38e8e63c")
//    //binance-现货 api https://jitpack.io/#binance-exchange/binance-java-api
//    implementation("com.github.Binance-docs:Binance_Futures_Java:48dffefe04")

    //

    implementation("org.knowm.xchange:xchange-core:$XCHANGE_VERSION")
    implementation("org.knowm.xchange:xchange-binance:$XCHANGE_VERSION")
    implementation("org.knowm.xchange:xchange-stream-binance:$XCHANGE_VERSION")
    implementation("org.knowm.xchange:xchange-deribit:$XCHANGE_VERSION")
    implementation("org.knowm.xchange:xchange-coinbasepro:$XCHANGE_VERSION")

    implementation("io.contek.invoker:invoker-ftx-api:2.6.2")
    implementation("io.contek.invoker:invoker-commons:2.6.2")
    implementation("io.contek.invoker:commons:2.4.0")

    //yahoofinance
    implementation("com.yahoofinance-api:YahooFinanceAPI:3.15.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}
