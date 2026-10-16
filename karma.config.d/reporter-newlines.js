// Ends every test report message with a new line. The Kotlin reporter writes them all on a single line, which Gradle
// stops reading once it is longer than 1MB: the tests after that are neither reported nor fail the build.
const kotlinReporterPlugin = "kotlin-web-helpers/dist/karma-kotlin-reporter.js";
const kotlinReporter = require(kotlinReporterPlugin)["reporter:karma-kotlin-reporter"][1];

function KotlinReporterWithNewLines(baseReporterDecorator, config, emitter) {
    kotlinReporter.call(this, baseReporterDecorator, config, emitter);
    const write = this.write;
    this.write = function (message, ...args) {
        const line = typeof message === "string" && !message.endsWith("\n") ? message + "\n" : message;
        write.call(this, line, ...args);
    };
}
KotlinReporterWithNewLines.$inject = kotlinReporter.$inject;

config.plugins = config.plugins.map((plugin) =>
    plugin === kotlinReporterPlugin ? { "reporter:karma-kotlin-reporter": ["type", KotlinReporterWithNewLines] } : plugin,
);
