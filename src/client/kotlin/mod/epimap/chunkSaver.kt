package mod.epimap

import net.fabricmc.loader.api.FabricLoader
import java.io.DataOutputStream
import java.io.FileOutputStream
import java.nio.file.Path
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient

class chunkSaver {

    fun getModBaseDirectory(): Path {
        return FabricLoader.getInstance().configDir.resolve("epimap")
    }

    fun getWorldOrServerDirectory(identifier: String): Path {
        val name = getWorldName()
        val baseDir = getModBaseDirectory()
        return baseDir.resolve(name)
    }

    fun sanitizeIdentifier(identifier: String): String {
        return identifier.replace("[^a-zA-Z0-9_.-]".toRegex(), "_")
    }

    fun saveChunkData(
        data: Array<Array<Int>>,
        chunkX: Int,
        chunkZ: Int,
        directory: Path,
        blockHeightData: Array<Array<Int>>
    ) {
        val chunkFile = directory.resolve("chunk_${chunkX}_${chunkZ}.bin").toFile()
        chunkFile.parentFile.mkdirs() // Ensure directory exists

        DataOutputStream(FileOutputStream(chunkFile)).use { out ->
            out.writeInt(chunkX)
            out.writeInt(chunkZ)

            for (row in data) {
                for (blockId in row) {
                    out.writeInt(blockId)
                }
            }
            for (row in blockHeightData) {
                for (height in row) {
                    out.writeInt(height)
                }
            }
        }
        //print("Chunk data saved to $chunkFile")
    }

    init {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            val currentServer = client.currentServerEntry
            val currentWorld = client.world

            if (currentServer != null) {
                val serverDir = getWorldOrServerDirectory(sanitizeIdentifier(currentServer.address))
                //println("Server data directory: $serverDir")
            } else if (currentWorld != null) {
                val worldDir =
                    getWorldOrServerDirectory(sanitizeIdentifier(client.world!!.registryKey.value.toString()))
                //println("World data directory: $worldDir")
            }
        }
    }

    fun getWorldName(): String {
        val client = MinecraftClient.getInstance()
        val world = client.world
        val server = client.currentServerEntry

        return when {
            server != null -> {
                // Multiplayer server: Use the server's name or address
                sanitizeIdentifier(server.name.ifEmpty { server.address })
            }

            client.isInSingleplayer && world != null -> {
                // Singleplayer world: Use the save name from the integrated server
                val integratedServer = client.server
                if (integratedServer != null) {
                    sanitizeIdentifier(integratedServer.saveProperties.levelName)
                } else {
                    "singleplayer_unknown"
                }
            }

            world != null -> {
                // Fallback for client-side dimension key (e.g., overworld, nether, end)
                sanitizeIdentifier(world.registryKey.value.path)
            }

            else -> {
                "unknown"
            }
        }
    }
}