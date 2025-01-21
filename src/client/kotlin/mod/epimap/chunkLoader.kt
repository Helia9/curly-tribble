package mod.epimap
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.nio.file.Path

class chunkLoader {
    fun loadChunkData(chunkX: Int, chunkZ: Int, directory: Path): Pair<Array<IntArray>, Array<IntArray>>? {
        val chunkFile = directory.resolve("chunk_${chunkX}_${chunkZ}.bin").toFile()
        if (!chunkFile.exists()) return null

        return DataInputStream(FileInputStream(chunkFile)).use { input ->
            val fileChunkX = input.readInt()
            val fileChunkZ = input.readInt()

            if (fileChunkX != chunkX || fileChunkZ != chunkZ) {
                throw IllegalStateException("Chunk coordinates do not match!")
            }

            val blockIds = Array(16) { IntArray(16) { input.readInt() } }
            val blockHeights = Array(16) { IntArray(16) { input.readInt() } }

            Pair(blockIds, blockHeights)
        }
    }
}