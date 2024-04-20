rootProject.name = "Lavalink-Parent"

include(":Lavalink-Server")
include(":Testbot")


project(":Lavalink-Server").projectDir = file("$rootDir/LavalinkServer")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        mavenCentral()
        maven("https://jitpack.io") // build projects directly from GitHub
        maven ("https://maven.aliyun.com/repository/jcenter")
        maven("https://m2.dv8tion.net/releases")
        maven("https://maven.lavalink.dev/releases")
        maven("https://maven.lavalink.dev/snapshots")

    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            spring()
            voice()
            metrics()
            common()
            other()
        }
    }
}

fun VersionCatalogBuilder.spring() {
    version("spring-boot", "3.1.0")

    library("spring-websocket", "org.springframework", "spring-websocket").version("6.0.9")

    library("spring-boot",          "org.springframework.boot", "spring-boot").versionRef("spring-boot")
    library("spring-boot-web",      "org.springframework.boot", "spring-boot-starter-web").versionRef("spring-boot")
    library("spring-boot-undertow", "org.springframework.boot", "spring-boot-starter-undertow") .versionRef("spring-boot")
    library("spring-boot-test",     "org.springframework.boot", "spring-boot-starter-test") .versionRef("spring-boot")

    bundle("spring", listOf("spring-websocket", "spring-boot-web", "spring-boot-undertow"))
}

fun VersionCatalogBuilder.voice() {
    version("lavaplayer", "0eaeee195f0315b2617587aa3537fa202df07ddc-SNAPSHOT")

    library("lavaplayer",            "dev.arbjerg", "lavaplayer").versionRef("lavaplayer")
    //library("lavaplayer-ip-rotator", "dev.arbjerg", "lavaplayer-ext-youtube-rotator").versionRef("lavaplayer")
    //library("lavaplayer",            "com.github.Kamilake.lavaplayer", "lavaplayer").versionRef("lavaplayer")
    library("lavaplayer-ip-rotator", "dev.arbjerg", "lavaplayer-ext-youtube-rotator").version("2.1.1")
    library("lavadsp",               "dev.arbjerg", "lavadsp").version("0.7.8")

    library("koe",          "moe.kyokobot.koe", "core").version("2.0.0-rc2")
    library("koe-udpqueue", "moe.kyokobot.koe", "ext-udpqueue").version("2.0.0-rc2")

    version("udpqueue", "0.2.7")
    val platforms = listOf("linux-x86-64", "linux-x86", "linux-aarch64", "linux-arm", "win-x86-64", "win-x86", "darwin")
    platforms.forEach {
        library("udpqueue-native-$it", "club.minnced", "udpqueue-native-$it").versionRef("udpqueue")
    }

    bundle("udpqueue-natives", platforms.map { "udpqueue-native-$it" })
}

fun VersionCatalogBuilder.metrics() {
    version("prometheus", "0.16.0")

    library("metrics",         "io.prometheus", "simpleclient").versionRef("prometheus")
    library("metrics-hotspot", "io.prometheus", "simpleclient_hotspot").versionRef("prometheus")
    library("metrics-logback", "io.prometheus", "simpleclient_logback").versionRef("prometheus")
    library("metrics-servlet", "io.prometheus", "simpleclient_servlet").versionRef("prometheus")

    bundle("metrics", listOf("metrics", "metrics-hotspot", "metrics-logback", "metrics-servlet"))
}

fun VersionCatalogBuilder.common() {
    version("kotlin", "1.8.22")

    library("kotlin-reflect",     "org.jetbrains.kotlin", "kotlin-reflect").versionRef("kotlin")
    library("kotlin-stdlib-jdk8", "org.jetbrains.kotlin", "kotlin-stdlib-jdk8").versionRef("kotlin")

    library("oshi",           "com.github.oshi",      "oshi-core").version("6.4.11")
    library("json",           "org.json",             "json").version("20180813")
    library("gson",           "com.google.code.gson", "gson").version("2.8.5")

    library("spotbugs", "com.github.spotbugs", "spotbugs-annotations").version("3.1.6")
}


fun VersionCatalogBuilder.other() {
    library("jda",             "net.dv8tion",         "JDA").version("4.1.1_135")
    library("lavalink-client", "com.github.FredBoat", "Lavalink-Client").version("8d9b660")
}
