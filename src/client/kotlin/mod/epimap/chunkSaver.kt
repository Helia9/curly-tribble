package mod.epimap

import net.fabricmc.loader.api.FabricLoader
import java.io.DataOutputStream
import java.io.FileOutputStream
import java.nio.file.Path
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

class chunkSaver {

    fun getModBaseDirectory(): Path {
        return FabricLoader.getInstance().configDir.resolve("my_mod_id")
    }

    fun getWorldOrServerDirectory(identifier: String): Path {
        val baseDir = getModBaseDirectory()
        return baseDir.resolve(identifier)
    }
    fun sanitizeIdentifier(identifier: String): String {
        return identifier.replace("[^a-zA-Z0-9_.-]".toRegex(), "_")
    }
    fun saveChunkData(data: Array<Array<Int>>, chunkX: Int, chunkZ: Int, directory: Path, blockHeightData: Array<Array<Int>>) {
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
        print("Chunk data saved to $chunkFile")
    }

    init {
    ClientTickEvents.END_CLIENT_TICK.register { client ->
        val currentServer = client.currentServerEntry
        val currentWorld = client.world

        if (currentServer != null) {
            val serverDir = getWorldOrServerDirectory(sanitizeIdentifier(currentServer.address))
            println("Server data directory: $serverDir")
        } else if (currentWorld != null) {
            val worldDir = getWorldOrServerDirectory(sanitizeIdentifier(client.world!!.registryKey.value.toString()))
            //println("World data directory: $worldDir")
        }
    }
}
}