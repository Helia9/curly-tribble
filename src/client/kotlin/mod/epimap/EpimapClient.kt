package mod.epimap

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.world.chunk.Chunk
import org.slf4j.LoggerFactory

object EpimapClient : ClientModInitializer {

	override fun onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		//call testcheck on world load, not on startup:
		KeyBindingHelper.registerKeyBinding(CustomScreen.moveOffsetUpKey)
		KeyBindingHelper.registerKeyBinding(CustomScreen.moveOffsetDownKey)
		KeyBindingHelper.registerKeyBinding(CustomScreen.moveOffsetLeftKey)
		KeyBindingHelper.registerKeyBinding(CustomScreen.moveOffsetRightKey)

		ClientChunkEvents.CHUNK_LOAD.register { world, chunk ->
			if (world === MinecraftClient.getInstance().world) {
				ChunkHandler().handleChunkLoad(chunk)
			}
		}

		keybindListener()
		ClientPlayConnectionEvents.JOIN.register(ClientPlayConnectionEvents.Join { handler, sender, client ->
			analyzeChunk().testcheck()
		})
		ClientPlayConnectionEvents.DISCONNECT.register(ClientPlayConnectionEvents.Disconnect { handler, client ->
			analyzeChunk().testcheck()
			chunkSaver()
			chunkSaver().saveChunkData(
				analyzeChunk().getTopLayer(client.world!!.getChunk(-29, 21)).first,
				-29,
				21,
				chunkSaver().getWorldOrServerDirectory("test"),
				analyzeChunk().getTopLayer(client.world!!.getChunk(-29, 21)).second
			)
		})
	}
}