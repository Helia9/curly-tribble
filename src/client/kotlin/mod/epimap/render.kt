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
    var blockXOffset = 0
    var blockZOffset = 0
    var chunkZ15Height = IntArray(chunkSize) { 0 }
    private var accumulatedDeltaX = 0.0
    private var accumulatedDeltaY = 0.0
    private var zoom = 10
    private val mapPixelSize = 800
    private val colorHelper = getColor()

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
            if (accumulatedDeltaX >= 10 || accumulatedDeltaX <= -10) {
                blockXOffset -= (accumulatedDeltaX / 10).toInt()
                accumulatedDeltaX %= 10
                loadChunkData()
            }
            if (accumulatedDeltaY >= 10 || accumulatedDeltaY <= -10) {
                blockZOffset -= (accumulatedDeltaY / 10).toInt()
                accumulatedDeltaY %= 10
                loadChunkData()
            }
            return true
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        val oldZoom = zoom
        zoom = (zoom + verticalAmount.toInt()).coerceIn(4, 40)
        println("Zoom level: $zoom")
        if (zoom != oldZoom) {
            this.loadChunkData()
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
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
            blockZOffset--
            loadChunkData()
            return true
        }
        if (moveOffsetDownKey.matchesKey(keyCode, scanCode)) {
            blockZOffset++
            loadChunkData()
            return true
        }
        if (moveOffsetLeftKey.matchesKey(keyCode, scanCode)) {
            blockXOffset--
        loadChunkData()
            return true
        }
        if (moveOffsetRightKey.matchesKey(keyCode, scanCode)) {
            blockXOffset++
            loadChunkData()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    fun loadChunkData() {
        val client = MinecraftClient.getInstance()
        val player = client.player ?: return

        val arraySize = (mapPixelSize / zoom).coerceAtLeast(1)
        blockColors = Array(arraySize) { Array<Color?>(arraySize) { null } }

        val centerBlockX = player.blockX + blockXOffset
        val centerBlockZ = player.blockZ + blockZOffset

        val topLeftBlockX = centerBlockX - arraySize / 2
        val topLeftBlockZ = centerBlockZ - arraySize / 2

        val chunkStartX = Math.floorDiv(topLeftBlockX, chunkSize)
        val chunkStartZ = Math.floorDiv(topLeftBlockZ, chunkSize)
        val chunkEndX = Math.floorDiv(topLeftBlockX + arraySize - 1, chunkSize)
        val chunkEndZ = Math.floorDiv(topLeftBlockZ + arraySize - 1, chunkSize)

        for (chunkX in chunkStartX..chunkEndX) {
            for (chunkZ in chunkStartZ..chunkEndZ) {
                addChunkRender(chunkX, chunkZ, topLeftBlockX, topLeftBlockZ, arraySize)
            }
        }
    }

    private fun addChunkRender(chunkX: Int, chunkZ: Int, topLeftBlockX: Int, topLeftBlockZ: Int, arraySize: Int) {
        val chunksaver = chunkSaver()
        val chunkData = chunkLoader().loadChunkData(chunkX, chunkZ, chunksaver.getWorldOrServerDirectory(chunksaver.getWorldName()))
        if (chunkData != null) {
            val (data, blockHeight) = chunkData
            for (x in 0 until chunkSize) {
                for (z in 0 until chunkSize) {
                    val worldX = chunkX * chunkSize + x
                    val worldZ = chunkZ * chunkSize + z
                    val px = worldX - topLeftBlockX
                    val pz = worldZ - topLeftBlockZ
                    if (z == 15 && x in chunkZ15Height.indices) {
                        chunkZ15Height[x] = blockHeight[x][z]
                    }
                    if (px in 0 until arraySize && pz in 0 until arraySize) {
                        blockColors[px][pz] = colorHelper.getBlockColor(data[x][z], blockHeight, x, z, chunkZ15Height)
                    }
                }
            }
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)

        val client = MinecraftClient.getInstance()
        val screenWidth = client.window.scaledWidth
        val screenHeight = client.window.scaledHeight

        val renderWidth = mapPixelSize
        val renderHeight = mapPixelSize
        val startX = (screenWidth - renderWidth) / 2
        val startY = (screenHeight - renderHeight) / 2

        for (x in blockColors.indices) {
            for (z in blockColors[x].indices) {
                val color = blockColors[x][z] ?: Color.BLACK
                val pixelX = startX + x * zoom
                val pixelY = startY + z * zoom
                context.fill(pixelX, pixelY, pixelX + zoom, pixelY + zoom, color.rgb)
            }
        }

        val player = client.player
        if (player != null) {
            val arraySize = blockColors.size
            val centerBlockX = player.blockX + blockXOffset
            val centerBlockZ = player.blockZ + blockZOffset
            val topLeftBlockX = centerBlockX - arraySize / 2
            val topLeftBlockZ = centerBlockZ - arraySize / 2
            val playerArrayX = player.blockX - topLeftBlockX
            val playerArrayZ = player.blockZ - topLeftBlockZ

            if (playerArrayX in 0 until arraySize && playerArrayZ in 0 until arraySize) {
                val playerPixelX = startX + playerArrayX * zoom
                val playerPixelY = startY + playerArrayZ * zoom
                context.fill(playerPixelX, playerPixelY, playerPixelX + zoom, playerPixelY + zoom, Color.RED.rgb)
            }
        }
    }
}