package mod.epimap
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.world.chunk.Chunk
import org.slf4j.LoggerFactory

class ChunkHandler {
    public fun handleChunkLoad(chunk: Chunk) {
        val topLayer = analyzeChunk().getTopLayer(chunk)
        val topLayerHeight = analyzeChunk().getTopLayerHeight(chunk)
        chunkSaver().saveChunkData(topLayer, chunk.pos.x, chunk.pos.z, chunkSaver().getWorldOrServerDirectory("test"), topLayerHeight)
        println("Chunk data saved, chunk at : ${chunk.pos.x}, ${chunk.pos.z}")
    }
}