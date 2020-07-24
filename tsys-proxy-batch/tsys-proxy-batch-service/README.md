# tsys-proxy-batch
This microservice is responsible for settlement request processing.

# Logback configuration
By default logback is configured to encode logs as JSON. While this is a requirement to support SUMO
logic, it is not great for development.To disable logback JSON encoding set the active spring profile
to `dev` by adding the following JVM option: `-Dspring.profiles.active=dev`.

## Install Lombok plugin
A plugin that adds first-class support for Project Lombok .Project Lombok is a java library that
automatically plugs into your editor and build tools and auto generates the getters,setters,equals,
hashcode and toString methods with the annotations.

Important: Lombok Requires Annotation Processing. For the plugin to function correctly, please enable
it under "Settings > Build > Compiler > Annotation Processors".

To install the Lombok plugin
* Go to File > Settings > Plugins
* Click on Browse repositories
* Search for Lombok Plugin
* Click on Install plugin
* Restart IntelliJ IDEA