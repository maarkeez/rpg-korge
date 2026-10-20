package dependencyreport

object KotlinSourceFileMother {
    const val BASE_PACKAGE = "com.mkz.rpg"

    fun kotlinFile(
        packageName: String,
        imports: List<String> = emptyList(),
        name: String = "File${packageName.hashCode().toUInt()}${imports.hashCode().toUInt()}",
    ) = KotlinSourceFile(
        path = "${packageName.replace(".", "/")}/$name.kt",
        content =
            buildString {
                appendLine("package $packageName")
                appendLine()
                imports.forEach { appendLine("import $it") }
                appendLine()
                appendLine("class $name")
            },
    )

    // A file of the `<subdomain>.<layer>` package that imports the given names, relative to the base package (e.g.: `battle.domain.Battle`).
    fun subdomainFile(
        subdomain: String,
        imports: List<String> = emptyList(),
        layer: String = "domain",
        name: String = "File${subdomain.hashCode().toUInt()}${imports.hashCode().toUInt()}",
    ) = kotlinFile(packageName = "$BASE_PACKAGE.$subdomain.$layer", imports = imports.map { "$BASE_PACKAGE.$it" }, name = name)
}
