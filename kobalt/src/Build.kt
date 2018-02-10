import com.beust.kobalt.plugin.application.application
import com.beust.kobalt.plugin.packaging.assemble
import com.beust.kobalt.plugin.publish.bintray
import com.beust.kobalt.project

object Version {
    val main = "0.0.1"
    val retrofit = "2.0.0-beta2"
}

val p = project {
    name = "hostScanner"
    group = "com.beust"
    artifactId = name
    version = Version.main

    dependencies {
        compile("com.squareup.retrofit:retrofit:${Version.retrofit}",
                "com.squareup.retrofit:converter-gson:${Version.retrofit}",
                "com.squareup.okhttp3:okhttp:3.9.1",
                "org.jetbrains.kotlin:kotlin-stdlib:1.2.21")
    }

    assemble {
        mavenJars {}
    }

    bintray {
        publish = true
    }

    application {
        mainClass = "com.beust.hostScanner.HostScannerKt"
    }
}
