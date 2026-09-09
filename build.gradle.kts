import korlibs.korge.gradle.*

plugins {
	alias(libs.plugins.korge)
	id("org.jmailen.kotlinter") version "4.5.0"
}

korge {
	id = "com.sample.demo"

// To enable all targets at once

	//targetAll()

// To enable targets based on properties/environment variables
	//targetDefault()

// To selectively enable targets
	
	targetJvm()
	targetJs()
    targetWasm()
	targetDesktop()
	targetIos()
	targetAndroid()

	serializationJson()
}


dependencies {
    add("commonMainApi", project(":deps"))
    add("jvmTestApi", "org.mockito.kotlin:mockito-kotlin:6.3.0")
    add("jvmTestApi", "org.assertj:assertj-core:3.27.7")
    add("jvmTestApi", "org.junit.jupiter:junit-jupiter:6.1.3")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.withType<org.jmailen.gradle.kotlinter.tasks.ConfigurableKtLintTask>().configureEach {
    doFirst {
        val buildDirAbs = project.layout.buildDirectory.get().asFile.absoluteFile
        val all = (this as SourceTask).source.files.filter { it.isFile }
        val filtered = all.filter { !it.absoluteFile.startsWith(buildDirAbs) }
        if (filtered.size != all.size) setSource(filtered)
    }
}

