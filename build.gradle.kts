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
}

