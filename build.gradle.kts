import korlibs.korge.gradle.*

plugins {
	alias(libs.plugins.korge)
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
    add("jvmTestApi", "org.junit.jupiter:junit-jupiter:5.10.2")
    add("jvmTestApi", "junit:junit:4.13.2")
    add("jvmTestRuntimeOnly", "org.junit.vintage:junit-vintage-engine:5.10.2")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

