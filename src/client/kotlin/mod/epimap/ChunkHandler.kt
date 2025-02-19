package mod.epimap
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.ChunkStatus
import org.slf4j.LoggerFactory

class ChunkHandler {
    public fun handleChunkLoad(chunk: Chunk) {
        val topLayerData = analyzeChunk().getTopLayer(chunk)
        val (topLayer, topLayerHeight) = topLayerData
        chunkSaver().saveChunkData(topLayer, chunk.pos.x, chunk.pos.z, chunkSaver().getWorldOrServerDirectory("test"), topLayerHeight)
        //println("Chunk data saved, chunk at : ${chunk.pos.x}, ${chunk.pos.z}")
    }

    fun getChunksInRadius(radius: Int): List<Chunk> {
        val client = MinecraftClient.getInstance()
        val world: ClientWorld = client.world ?: return emptyList()
        val player = client.player ?: return emptyList()


        val playerPos = player.blockPos


        val playerChunkX = playerPos.x / 16
        val playerChunkZ = playerPos.z / 16

        val chunksInRadius = mutableListOf<Chunk>()


        for (dx in -radius..radius) {
            for (dz in -radius..radius) {
                val chunkX = playerChunkX + dx
                val chunkZ = playerChunkZ + dz
                val chunkPos = ChunkPos(chunkX, chunkZ)

                val chunk = world.chunkManager.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, false)
                chunk?.let { chunksInRadius.add(it) }
            }
        }

        return chunksInRadius
    }
}