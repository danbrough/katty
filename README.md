# Katty - Command shell for Kotlin Multiplatform based on the [mordant](https://github.com/ajalt/mordant) library.

Run [./demo/katty](./demo/katty) to run the native demo or [./demo/jkatty](./demo/jkatty) to run the JVM demo,
[./demo/jskatty](./demo/jskatty) for the javascript demo.

If you want a shell for interacting or configuring your application then this might be a start.

This is a kotlin multiplatform library providing an API for implementing "commands" that you can run interactively, 
from the command line .. or from some another configuration method, [TOML](https://github.com/orchestr7/ktoml) and/or 
environmental variables.

The shell delegates to the [CommandHandler](./core/src/commonMain/kotlin/io/github/danbrough/katty/CommandHandler.kt)
for which there is no implementation in the core package at present, but there is a simple example in the demo
at [DemoCommandHandler.kt](./demo/src/commonMain/kotlin/io/github/danbrough/katty/DemoCommandHandler.kt)

A [CliKT](https://ajalt.github.io/clikt/) based implementation of a CommandHandler is in the works 
in the [clikt](./clikt) package.


