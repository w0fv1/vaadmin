plugins {
    id("java-library")
    id("io.spring.dependency-management") version "1.1.6"
    id("maven-publish")
    id("signing")
    id("com.vaadin")
}


group = "dev.w0fv1"
version = "0.0.30"
val springBootVersion = "3.3.5" // 设置 Spring Boot 版本
val vaadinVersion = "24.5.7"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}
repositories {
    mavenCentral()
    mavenLocal() // 添加本地仓库
    maven { url = uri("https://maven.vaadin.com/vaadin-prereleases") }
    maven { url = uri("https://repo.spring.io/milestone") }
    maven { url = uri("https://maven.vaadin.com/vaadin-addons") }
}

buildscript {
    repositories {
        mavenCentral()
        maven { url = uri("https://maven.vaadin.com/vaadin-prereleases") }
        maven { url = uri("https://repo.spring.io/milestone") }
        maven {
            url = uri("https://maven.pkg.github.com/w0fv1/fampper")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}


dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
        mavenBom("com.vaadin:vaadin-bom:$vaadinVersion")
    }
}

dependencies {
    // https://mvnrepository.com/artifact/com.vaadin/vaadin-spring-boot-starter
    implementation("com.vaadin:vaadin-spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")

    implementation("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.reflections:reflections:0.10.2")

// https://mvnrepository.com/artifact/org.hibernate.validator/hibernate-validator
    implementation("org.hibernate.validator:hibernate-validator:8.0.1.Final")

    implementation("dev.w0fv1:fmapper:0.0.5") // 替换为实际的 group 和 version
    annotationProcessor("dev.w0fv1:fmapper:0.0.5") // 注解处理器依赖

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:$springBootVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")

    runtimeOnly("com.h2database:h2")
}

tasks.test {
    useJUnitPlatform()
}
tasks.jar {
    exclude("dev/w0fv1/vaadmin/test/**") // 忽略单个类
}


//import org.gradle.plugins.signing.Sign

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("Vaadmin")
                description.set("Vaadmin is a back-end management framework based on Vaadin.")
                url.set("https://github.com/w0fv1/vaadmin")

                developers {
                    developer {
                        id.set("w0fv1")
                        name.set("w0fv1")
                        email.set("hi@w0fv1.dev")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/w0fv1/vaadmin.git")
                    developerConnection.set("scm:git:ssh://github.com/w0fv1/vaadmin.git")
                    url.set("https://github.com/w0fv1/vaadmin")
                }
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/w0fv1/vaadmin")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
//        maven {
//            name = "local"
//            url = uri("${layout.buildDirectory}/repo")
//        }
    }
}

signing {
    // 如果在构建时手动输入密码，可以使用 `useGpgCmd()` 启用命令行 GPG
    useGpgCmd()
    sign(publishing.publications["mavenJava"])
}
