import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    kotlin("plugin.serialization") version "1.9.21"
}

group = "com.hamzadamra.salesmanagement"
version = "2.1.0"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")

    // HTTP Client and Networking
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-cio:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    implementation("io.ktor:ktor-client-logging:2.3.7")
    implementation("io.ktor:ktor-client-auth:2.3.7")

    // JSON Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Preferences/Settings Storage
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Excel Export - Apache POI (consistent versions)
    implementation("org.apache.poi:poi:5.2.4")
    implementation("org.apache.poi:poi-ooxml:5.2.4")
    // Removed poi-ooxml-schemas as it's included in poi-ooxml 5.2.4

    // PDF Generation - iText
    implementation("com.itextpdf:itext7-core:7.2.5")
    implementation("com.itextpdf:html2pdf:4.0.5")

    // PDF Rendering - Apache PDFBox for PDF viewing
    implementation("org.apache.pdfbox:pdfbox:2.0.29")
    implementation("org.apache.pdfbox:pdfbox-tools:2.0.29")

    // File operations - Desktop integration for file dialogs

    // Logging dependencies to fix warnings
    implementation("org.slf4j:slf4j-simple:2.0.9")
    implementation("org.apache.logging.log4j:log4j-core:2.21.1")

    // Color Picker - Skydoves Compose Color Picker for Kotlin Multiplatform
    implementation("com.github.skydoves:colorpicker-compose:1.1.2")

    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.ktor:ktor-client-mock:2.3.7")
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.21")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Dmg, TargetFormat.Deb)
            packageName = "SalesManagementSystem"
            packageVersion = "2.1.0"
            description = "Professional Sales Management System with Enhanced Update Functionality"
            copyright = "© 2024 Hamza Damra. All rights reserved."
            vendor = "Hamza Damra"
            licenseFile.set(project.file("installer/LICENSE.txt"))

            // Application metadata
            modules("java.base", "java.desktop", "java.logging", "java.naming", "java.security.jgss", "java.sql")

            // Windows MSI specific configuration
            windows {
                // iconFile.set(project.file("installer/icons/app-icon.ico")) // TODO: Add icon file
                menuGroup = "Sales Management System"
                // Upgrade UUID for proper MSI upgrade handling
                upgradeUuid = "B8C9D0E1-F2A3-4B5C-6D7E-8F9A0B1C2D3E"

                // MSI installer properties
                msiPackageVersion = "2.1.0"
                dirChooser = true
                perUserInstall = false
                shortcut = true

                // Additional MSI properties
                console = false
            }

            // macOS DMG configuration
            macOS {
                // iconFile.set(project.file("installer/icons/app-icon.icns")) // TODO: Add icon file
                bundleID = "com.hamzadamra.salesmanagement"
                appCategory = "public.app-category.business"
                // entitlementsFile.set(project.file("installer/macOS/entitlements.plist"))
                // runtimeEntitlementsFile.set(project.file("installer/macOS/runtime-entitlements.plist"))
            }

            // Linux DEB configuration
            linux {
                // iconFile.set(project.file("installer/icons/app-icon.png")) // TODO: Add icon file
                packageName = "sales-management-system"
                debMaintainer = "hamza.damra@example.com"
                menuGroup = "Office"
                appRelease = "1"
                appCategory = "Office"
                shortcut = true
            }

            // Include additional resources
            includeAllModules = true
        }

        buildTypes.release.proguard {
            configurationFiles.from("installer/proguard-rules.pro")
        }
    }
}
