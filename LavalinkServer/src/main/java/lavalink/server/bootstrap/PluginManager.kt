package lavalink.server.bootstrap

import org.springframework.boot.autoconfigure.SpringBootApplication


@SpringBootApplication
class PluginManager(config: PluginsConfig) {
    final val pluginManifests: MutableList<PluginManifest> = mutableListOf()
    var classLoader: ClassLoader = PluginManager::class.java.classLoader
}