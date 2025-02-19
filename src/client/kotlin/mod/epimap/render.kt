package mod.epimap

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.option.KeyBinding
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW
import java.awt.Color
import net.minecraft.client.util.InputUtil

class CustomScreen(title: Text) : Screen(title) {

    private val chunkSize = 16
    private var renderRadius = 4
    private lateinit var blockColors: Array<Array<Color?>>
    var chunkXOffset = 0
    var chunkZOffset = 0
    var chunkZ15Height = IntArray(chunkSize) { 0 }
    private var accumulatedDeltaX = 0.0
    private var accumulatedDeltaY = 0.0

    companion object {
        val moveOffsetUpKey = KeyBinding(
            "key.setting-up.move_offset_up",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UP,
            "category.setting-up"
        )
        val moveOffsetDownKey = KeyBinding(
            "key.setting-up.move_offset_down",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_DOWN,
            "category.setting-up"
        )
        val moveOffsetLeftKey = KeyBinding(
            "key.setting-up.move_offset_left",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT,
            "category.setting-up"
        )
        val moveOffsetRightKey = KeyBinding(
            "key.setting-up.move_offset_right",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT,
            "category.setting-up"
        )
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (button == 1) {
            accumulatedDeltaX += deltaX
            accumulatedDeltaY += deltaY
            if (accumulatedDeltaX >= 40 || accumulatedDeltaX <= -40) {
                chunkXOffset -= (accumulatedDeltaX / 40).toInt()
                accumulatedDeltaX = 0.0
                loadChunkData()
            }
            if (accumulatedDeltaY >= 40 || accumulatedDeltaY <= -40) {
                chunkZOffset -= (accumulatedDeltaY / 40).toInt()
                accumulatedDeltaY = 0.0
                loadChunkData()
            }
            return true
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun init() {
        var chunk_list = ChunkHandler().getChunksInRadius(4)
        for (chunk in chunk_list) {
            ChunkHandler().handleChunkLoad(chunk)
        }
        loadChunkData()

    }

    override fun shouldPause(): Boolean {
        return false
    }
    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {

        if (moveOffsetUpKey.matchesKey(keyCode, scanCode)) {
            chunkZOffset--
            loadChunkData()
            return true
        }
        if (moveOffsetDownKey.matchesKey(keyCode, scanCode)) {
            chunkZOffset++
            loadChunkData()
            return true
        }
        if (moveOffsetLeftKey.matchesKey(keyCode, scanCode)) {
            chunkXOffset--
        loadChunkData()
            return true
        }
        if (moveOffsetRightKey.matchesKey(keyCode, scanCode)) {
            chunkXOffset++
            loadChunkData()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    fun loadChunkData() {
        val client = MinecraftClient.getInstance()
        val player = client.player ?: return

        // how many chunks we wanna save
        val arraySize = (renderRadius * 2 + 1) * chunkSize
        blockColors = Array(arraySize) { Array<Color?>(arraySize) { null } }

        // we center the map around this point, if we could move it we could move the map in the menu
        println("real chunk pos = ${player.blockX / chunkSize}, ${player.blockZ / chunkSize}")
        println("chunk offset = $chunkXOffset, $chunkZOffset")
        val playerChunkX = (player.blockX / chunkSize) + chunkXOffset
        val playerChunkZ = (player.blockZ / chunkSize) + chunkZOffset

        for (dx in -renderRadius..renderRadius) {
            for (dz in -renderRadius..renderRadius) {
                val chunkX = playerChunkX + dx
                val chunkZ = playerChunkZ + dz
                addChunkRender(chunkX, chunkZ, dx, dz)
            }
        }
    }

    private fun addChunkRender(chunkX: Int, chunkZ: Int, dx: Int, dz: Int) {
        val chunkData = chunkLoader().loadChunkData(chunkX, chunkZ, chunkSaver().getWorldOrServerDirectory(chunkSaver().getWorldName()))
        if (chunkData != null) {
            val (data, blockHeight) = chunkData
            val offsetX = (dx + renderRadius) * chunkSize
            val offsetZ = (dz + renderRadius) * chunkSize
            for (x in 0 until chunkSize) {
                for (z in 0 until chunkSize) {
                    val rawId = data[x][z]
                    if (z == 15) {
                        chunkZ15Height[x] = blockHeight[x][z]
                    }
                    blockColors[offsetX + x][offsetZ + z] = getColor().getBlockColor(rawId, blockHeight, x, z, chunkZ15Height)
                }
            }
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)

        val client = MinecraftClient.getInstance()
        val screenWidth = client.window.scaledWidth
        val screenHeight = client.window.scaledHeight

        val renderWidth = blockColors.size * 10
        val renderHeight = blockColors[0].size * 10
        val startX = (screenWidth - renderWidth) / 2
        val startY = (screenHeight - renderHeight) / 2

        for (x in blockColors.indices) {
            for (z in blockColors[x].indices) {
                val color = blockColors[x][z] ?: Color.BLACK
                val pixelX = startX + x * 10
                val pixelY = startY + z * 10
                context.fill(pixelX, pixelY, pixelX + 10, pixelY + 10, color.rgb)
            }
        }
    }
}