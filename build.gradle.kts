plugins {
    `java-library`
}

group = "com.github.jjooxz" // MUITO IMPORTANTE pro JitPack
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar() // opcional, mas bonito
}

repositories {
    mavenCentral()
}

dependencies {
    api("uk.co.caprica:vlcj:4.8.2")
    api("net.java.dev.jna:jna:5.13.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}