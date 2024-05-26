plugins {
    id("java")
}

group = "com.grand.marquis"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.logging.log4j:log4j-core:2.23.1")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.23.1")
    implementation("software.amazon.awssdk:sqs:2.25.45")
    implementation("io.smallrye.reactive:mutiny:2.6.0")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
val dockerComposeDir = projectDir.resolve("./infrastructure/localstack")

tasks.register<Exec>("localstack-up") {
    description = "Runs Docker Compose up"
    group = "docker"
    workingDir = dockerComposeDir
    commandLine("docker-compose", "up", "-d")
}

tasks.register<Exec>("localstack-down") {
    description = "Runs Docker Compose down"
    group = "docker"
    workingDir = dockerComposeDir
    commandLine("docker-compose", "down")
}

val terraformDir = projectDir.resolve("./infrastructure/localstack/terraform")

tasks.register<Exec>("terraform-create") {
    description = "Initializes and executes Terraform"
    group = "terraform"
    workingDir = terraformDir
    commandLine("terraform", "init")
    commandLine("terraform", "apply", "-auto-approve")
}

tasks.register<Exec>("terraform-destroy") {
    description = "Destroys terraform infrastructure"
    group = "terraform"
    workingDir = terraformDir
    commandLine("terraform", "init")
    commandLine("terraform" , "destroy", "-auto-approve")
}
