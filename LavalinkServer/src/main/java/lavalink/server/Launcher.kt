/*
 * Copyright (c) 2021 Freya Arbjerg and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package lavalink.server

import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary
import lavalink.server.bootstrap.PluginManager
import lavalink.server.util.ConsoleLogging
import org.springframework.boot.Banner
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent
import org.springframework.boot.context.event.ApplicationFailedEvent
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import java.util.*

@Suppress("SpringBootApplicationSetup", "SpringComponentScan")
@SpringBootApplication
@ComponentScan(
    value = ["\${componentScan}"],
    excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = [PluginManager::class])]
)
class LavalinkApplication

object Launcher {

    val startTime = System.currentTimeMillis()

    private fun getVersionInfo(indentation: String = "\t"): String {
        val version = "NinkyNonk/Lavalink@1.7.0"

        return buildString {
            append("${indentation}Version:        "); appendln(version)
            append("${indentation}JVM:            "); appendln(System.getProperty("java.version"))
            append("${indentation}Lavaplayer      "); appendln(PlayerLibrary.VERSION)
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val parent = launchPluginBootstrap()
        launchMain(parent, args)
    }

    private fun launchPluginBootstrap() = SpringApplication(PluginManager::class.java).run {
        setBannerMode(Banner.Mode.OFF)
        webApplicationType = WebApplicationType.NONE
        run()
    }

    private fun launchMain(parent: ConfigurableApplicationContext, args: Array<String>) {
        val properties = Properties()
        properties["componentScan"] = listOf("lavalink.server")

        SpringApplicationBuilder()
            .sources(LavalinkApplication::class.java)
            .properties(properties)
            .web(WebApplicationType.SERVLET)
            .bannerMode(Banner.Mode.OFF)
            .listeners(
                ApplicationListener { event: Any ->
                    when (event) {
                        is ApplicationEnvironmentPreparedEvent ->
                            ConsoleLogging.Log(getVersionInfo())
                        is ApplicationReadyEvent ->
                            ConsoleLogging.LogInfo("Lavalink is ready to accept connections.")
                        is ApplicationFailedEvent ->
                            ConsoleLogging.LogError("Application failed" + event.exception);
                    }
                }
            ).parent(parent)
            .run(*args)
    }
}
