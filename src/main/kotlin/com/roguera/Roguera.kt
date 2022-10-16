package com.roguera

import com.roguera.net.ServerRequests
import com.roguera.resources.GameResources
import com.roguera.view.Draw
import net.arikia.dev.drpc.DiscordEventHandlers
import net.arikia.dev.drpc.DiscordRPC
import net.arikia.dev.drpc.DiscordRichPresence
import net.arikia.dev.drpc.DiscordUser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.io.IOException
import java.net.URISyntaxException
import java.util.*

object Roguera {
    val log: Logger = LoggerFactory.getLogger(Roguera::class.java)

    @JvmField
    var isDebug = true
    var isSaveToLogFile = true

    @JvmField
    var isClearMap = false
    var isOnline = false
        private set

    @JvmField
    var terminals = ArrayList<Closeable>()
    private const val RESET_CODE = 0

    @JvmField
    var codeOfMenu = RESET_CODE

    @Throws(IOException::class, InterruptedException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        log.info("=================================== ROGUERA GAME START ===================================")
        checkCLI(args)
        if (checkConnection()) {
            log.info("[NETWORK][CONNECTION STATUS] connected")
            isOnline = true
        }
        log.info("[START_UP] CLI args: " + Arrays.toString(args))
        log.info(
            """SYSTEM PROPERTIES: 
	OS: ${System.getProperties().getProperty("os.name")}
	Architecture: ${System.getProperties().getProperty("os.arch")}
	System version: ${System.getProperties().getProperty("os.version")}
	Java version: ${System.getProperties().getProperty("java.version")}
	Java RE: ${System.getProperties().getProperty("java.runtime.version")}
	Java Specification Version: ${System.getProperties().getProperty("java.specification.version")}
	Java VM Version: ${System.getProperties().getProperty("java.vm.version")}
	Java Compiler Version: ${System.getProperties().getProperty("java.compiler")}
	Java Сlass version: ${System.getProperties().getProperty("java.class.version")}"""
        )
        log.info("[VERSION]" + GameResources.VERSION)
        Runtime.getRuntime().addShutdownHook(Thread {
            log.info("[DISCORD_RP]Closing Discord hook.")
            log.info("[DRAW_CALL] Count: " + Draw.DrawCallCount)
            log.info("[DRAW_RESET] Count: " + Draw.DrawResetCount)
            log.info("[DRAW_INIT] Count: " + Draw.DrawInitCount)
            DiscordRPC.discordShutdown()
        })
        startDRP()
        DiscordRPC.discordRunCallbacks()
        while (true) {
            MainMenu.start(0)
            when (codeOfMenu) {
                1 -> {
                    Main.enableNewGame()
                    Main.startSequence()
                }

                2 -> {
                    Main.disableNewGame()
                    Main.startSequence()
                }

                3 -> {
                    DiscordRPC.discordShutdown()
                    System.exit(0)
                }
            }
            log.info("[SYSTEM] Back to the main menu")
            codeOfMenu = RESET_CODE
        }
    }

    private fun checkCLI(args: Array<String>) {
        for (argument in args) {
            if (argument == "--debug") {
                isDebug = true
            }
            if (argument == "--savelog") {
                isSaveToLogFile = true
            }
            if (argument == "--clearmap") {
                isClearMap = true
            }
        }
    }

    private fun checkConnection(): Boolean {
        return try {
            ServerRequests.getConnection()
        } catch (e: URISyntaxException) {
            log.info("[NETWORK][CONNECTION STATUS] no connection")
            false
        } catch (e: IOException) {
            log.info("[NETWORK][CONNECTION STATUS] no connection")
            false
        } catch (e: InterruptedException) {
            log.info("[NETWORK][CONNECTION STATUS] no connection")
            false
        }
    }

    @JvmStatic
    fun tryToConnect(): Boolean {
        return checkConnection()
    }

    private fun startDRP() {
        val discordEventHandlers = DiscordEventHandlers.Builder().setReadyEventHandler { user: DiscordUser ->
            log.info("[DISCORD_RP] Start RP for user " + user.username + "#" + user.discriminator)
            val welcomePresence = DiscordRichPresence.Builder("hi")
            welcomePresence.setDetails("world")
            DiscordRPC.discordUpdatePresence(welcomePresence.build())
        }.build()
        DiscordRPC.discordInitialize("904706382639562823", discordEventHandlers, true)
        DiscordRPC.discordRegister("904706382639562823", "")
    }
}