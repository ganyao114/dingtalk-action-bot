plugins {
    kotlin("jvm") version "1.9.23"
    application
}

group = "com.swift"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(files("libs/taobao-sdk-java-auto.jar"))
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("commons-logging:commons-logging:1.3.1")
    implementation("com.aliyun.oss:aliyun-sdk-oss:3.8.0")
    implementation("net.lingala.zip4j:zip4j:2.11.5")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("com.swift.Main")
}

tasks.jar {
    // 设置主类，导出的jar可以直接运行
    manifest {
        attributes["Main-Class"] = "com.swift.Main" // 格式为包名+类名+“Kt”（因为kotlin编译后生成的java类会自动加上kt）
    }

    // 下方的依赖打包可能会有重复文件，设置排除掉重复文件
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // 将依赖一起打包进jar
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
}