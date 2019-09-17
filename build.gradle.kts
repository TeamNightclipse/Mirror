import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import groovy.util.ConfigObject
import groovy.util.ConfigSlurper
import net.minecraftforge.gradle.user.ReobfMappingType
import org.gradle.jvm.tasks.Jar
import java.io.File
import java.util.Properties

plugins {
    scala
    //We apply these to get pretty build script
    java
    idea
    maven
    signing
    id("com.github.johnrengelman.shadow").version("2.0.4")
    id("net.minecraftforge.gradle.forge").version("2.3-SNAPSHOT")
}

val scaladoc: ScalaDoc by tasks
val compileJava: JavaCompile by tasks
val compileScala: ScalaCompile by tasks
val shadowJar: ShadowJar by tasks

val config = parseConfig(file("build.properties"))

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

compileJava.options.encoding = "UTF-8"
compileScala.scalaCompileOptions.additionalParameters = listOf("-Xexperimental")
scaladoc.scalaDocOptions.additionalParameters = listOf("-Xexperimental")

version = "${config["mc_version"]}-${config["version"]}"
group = "net.katsstuff.teamnightclipse"
base.archivesBaseName = "mirror"

sourceSets["main"].apply {
    java {
        setSrcDirs(listOf<File>())
    }
    withConvention(ScalaSourceSet::class) {
        scala {
            srcDir("src/main/java")
        }
    }
}

minecraft {
    version = "${config["mc_version"]}-${config["forge_version"]}"
    runDir = if (file("../run1.12").exists()) "../run1.12" else "run"
    mappings = "stable_39"
    // makeObfSourceJar = false // an Srg named sources jar is made by default. uncomment this to disable.

    //Not working anymore, set it manually
    replace("@VERSION@", project.version)
    replaceIn("Mirror.scala")
}

dependencies {
    compile("com.chuusai:shapeless_2.11:2.3.3") {
        exclude(group = "org.scala-lang")
    }
    compile("org.scala-lang:scala-library:2.11.4") //Gets ourself a better compiler

    testCompile("junit:junit:4.12")
    testCompile("org.scalatest:scalatest_2.11:3.0.1")
    testCompile("org.scalacheck:scalacheck_2.11:1.13.4")
}

shadowJar.apply {
    classifier = "shaded"
    dependencies {
        include(dependency("com.chuusai:shapeless_2.11:2.3.3"))
        exclude(dependency("org.scala-lang:scala-library:2.11.1"))
        exclude(dependency("org.scala-lang:scala-library:2.11.4"))
    }
    exclude("dummyThing")
    relocate("shapeless", "net.katsstuff.teamnightclipse.mirror.shade.shapeless")
}

tasks.withType<ProcessResources> {
    inputs.property("version", project.version)
    inputs.property("mcversion", minecraft.version)

    from(sourceSets["main"].resources.srcDirs) {
        include("mcmod.info")
        expand(mapOf("version" to project.version, "mcversion" to minecraft.version))
    }

    from(sourceSets["main"].resources.srcDirs) {
        exclude("mcmod.info")
    }
}

fun parseConfig(config: File): ConfigObject {
    val prop = Properties()
    prop.load(config.reader())
    return ConfigSlurper().parse(prop)
}

idea.module.inheritOutputDirs = true

tasks["build"].dependsOn(shadowJar)

reobf.create("shadowJar") {
    mappingType = ReobfMappingType.SEARGE
}

tasks["reobfShadowJar"].mustRunAfter(shadowJar)
tasks["build"].dependsOn("reobfShadowJar")

val deobfJar by tasks.creating(Jar::class) {
    classifier = "dev"
    from(sourceSets["main"].output)
}

val sourcesJar by tasks.creating(Jar::class) {
    classifier = "sources"
    from(sourceSets["main"].allSource)
}

val javadocJar by tasks.creating(Jar::class) {
    classifier = "javadoc"
    dependsOn(scaladoc)
    from(scaladoc.destinationDir)
}

artifacts {
    add("archives", shadowJar)
    add("archives", sourcesJar)
    add("archives", javadocJar)
    add("archives", deobfJar)
}

signing {
    useGpgCmd()
    sign(configurations.archives)
}

tasks {
    "uploadArchives"(Upload::class) {
        repositories {
            withConvention(MavenRepositoryHandlerConvention::class) {
                mavenDeployer {
                    beforeDeployment {
                        signing.signPom(this)
                    }

                    withGroovyBuilder {
                        val releasesUri = """https://api.bintray.com/maven/team-nightclipse/maven/Mirror/;publish=1"""
                        "repository"("url" to uri(releasesUri)) {
                            "authentication"("userName" to properties["bintray.user"], "password" to properties["bintray.apikey"])
                        }
                        /*
                        "snapshotRepository"("url" to uri("TODO")) {
                            "authentication"("userName" to properties["bintray.user"], "password" to properties["bintray.apikey"])
                        }
                        */
                    }

                    pom.project {
                        withGroovyBuilder {
                            "description"("A Minecraft rendering library")

                            "licenses" {
                                "license" {
                                    "name"("MIT")
                                    "url"("http://opensource.org/licenses/MIT")
                                    "distribution"("repo")
                                }
                            }

                            "scm" {
                                "url"("https://github.com/TeamNightclipse/Mirror")
                                "connection"("scm:git:github.com/TeamNightclipse/Mirror")
                                "developerConnection"("scm:git:github.com/TeamNightclipse/Mirror")
                            }

                            "issueManagement" {
                                "system"("github")
                                "url"("https://github.com/TeamNightclipse/Mirror/issues")
                            }

                            "developers" {
                                "developer" {
                                    "id"("Nikolai Frid")
                                    "email"("katrix97@hotmail.com")
                                    "url"("http://katsstuff.net/")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
