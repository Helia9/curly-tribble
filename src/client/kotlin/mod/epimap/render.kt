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
import net.minecraft.util.Identifier
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.render.GameRenderer
import net.minecraft.util.math.RotationAxis
import net.minecraft.client.render.RenderLayer

class CustomScreen(title: Text) : Screen(title) {

    private val chunkSize = 16 // Default chunk size
    private var renderRadius = 4
    private lateinit var blockColors: Array<Array<Color?>> // Store the map basically
    var blockXOffset = 0 // The offsets so that you can drag
    var blockZOffset = 0
    var chunkZ15Height = IntArray(chunkSize) { 0 } // Gets the height of the 15th row in the chunk as it wouldn't usually manage as we take the row + 1's height to calculate it, thus row + 1 is out of range, thus we need to use another chunk to calculate it
    private var accumulatedDeltaX = 0.0
    private var accumulatedDeltaY = 0.0
    val guiScale = MinecraftClient.getInstance().window.scaleFactor // Gets the GUI scale
    val mapPixelSize = (800 / guiScale).toInt() // Sets the size of the map depending on the GUI scale
    private val colorHelper = getColor() // For optimization, only gets one instance of getColor rather than creating a new one every time
    private var needsLoading = false // For optimization, only loads one chunk per frame
    private val chunkCache = mutableMapOf<Pair<Int, Int>, Pair<Array<IntArray>, Array<IntArray>>>()
    private var lastCenterBlockX = 0
    private var lastCenterBlockZ = 0
    private var lastZoom = 0
    private var zoom = (16 / guiScale).toInt() // Default zoom
    private val ARROW_TEXTURE = Identifier.of("epimap", "textures/gui/arrow.png")
    // prints the current path to the console

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
                needsLoading = true
            }
            if (accumulatedDeltaY >= 10 || accumulatedDeltaY <= -10) {
                blockZOffset -= (accumulatedDeltaY / 10).toInt()
                accumulatedDeltaY %= 10
                needsLoading = true
            }
            return true
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        val oldZoom = zoom
        zoom = (zoom + verticalAmount.toInt()).coerceIn((4 / guiScale).toInt(), (40 * guiScale).toInt())
        if (zoom == 0) {
            zoom = 1 // Prevents division by zero
        }
        println("Zoom level: $zoom")
        if (zoom != oldZoom) {
            this.needsLoading = true
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun init() {
        var chunk_list = ChunkHandler().getChunksInRadius(4)
        for (chunk in chunk_list) {
            ChunkHandler().handleChunkLoad(chunk)
        }
        chunkCache.clear()
        needsLoading = true

    }

    override fun shouldPause(): Boolean {
        return false
    }
    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {

        if (moveOffsetUpKey.matchesKey(keyCode, scanCode)) {
            blockZOffset--
            needsLoading = true
            return true
        }
        if (moveOffsetDownKey.matchesKey(keyCode, scanCode)) {
            blockZOffset++
            needsLoading = true
            return true
        }
        if (moveOffsetLeftKey.matchesKey(keyCode, scanCode)) {
            blockXOffset--
            needsLoading = true
            return true
        }
        if (moveOffsetRightKey.matchesKey(keyCode, scanCode)) {
            blockXOffset++
            needsLoading = true
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    fun loadChunkData() {
        val client = MinecraftClient.getInstance()
        val player = client.player ?: return

        val arraySize = (mapPixelSize / zoom).coerceAtLeast(1)
        val centerBlockX = player.blockX + blockXOffset
        val centerBlockZ = player.blockZ + blockZOffset

        if (centerBlockX == lastCenterBlockX && centerBlockZ == lastCenterBlockZ && zoom == lastZoom) return

        lastCenterBlockX = centerBlockX
        lastCenterBlockZ = centerBlockZ
        lastZoom = zoom

        blockColors = Array(arraySize) { Array<Color?>(arraySize) { null } }
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
        val chunkKey = Pair(chunkX, chunkZ)
        val chunkData = chunkCache.getOrPut(chunkKey) {
            val chunksaver = chunkSaver()
            chunkLoader().loadChunkData(chunkX, chunkZ, chunksaver.getWorldOrServerDirectory(chunksaver.getWorldName()))
                ?: return
        }
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

    fun draw_player(startX: Int, startY: Int, context: DrawContext) {
        val player = client?.player
        if (player != null) {
            val arraySize = blockColors.size
            val centerBlockX = player.blockX + blockXOffset
            val centerBlockZ = player.blockZ + blockZOffset
            val topLeftBlockX = centerBlockX - arraySize / 2
            val topLeftBlockZ = centerBlockZ - arraySize / 2
            val playerArrayX = player.blockX - topLeftBlockX
            val playerArrayZ = player.blockZ - topLeftBlockZ

            val textureManager = MinecraftClient.getInstance().textureManager
            val texture = textureManager.getTexture(ARROW_TEXTURE)
            if (texture == null) {
                // It's generally better to log an error than to crash if a texture is missing after initial load.
                // For now, keeping the error as in your code.
                error("Texture not loaded: $ARROW_TEXTURE")
            }
            // else {
            //    println("texture is : $texture") // Debug print
            // }

            if (playerArrayX in 0 until arraySize && playerArrayZ in 0 until arraySize) {
                val playerPixelX = startX + playerArrayX * zoom
                val playerPixelY = startY + playerArrayZ * zoom
                // this is false btw, the actual size is 360 but doesnt matter
                val ACTUAL_ARROW_TEXTURE_WIDTH = (32 / guiScale).toInt()
                val ACTUAL_ARROW_TEXTURE_HEIGHT = (32 / guiScale).toInt()

                context.matrices.push()
                context.matrices.translate(
                    (playerPixelX + zoom / 2).toDouble(), // Center of the player's grid cell
                    (playerPixelY + zoom / 2).toDouble(), // Center of the player's grid cell
                    0.0
                )
                context.matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(player.yaw)) // Rotate around this center

                // Adjust translation for centering if origin is bottom-right.
                // The arrow's display size on screen will be 'zoom' x 'zoom'.
                context.matrices.translate(
                    (-ACTUAL_ARROW_TEXTURE_HEIGHT / 2).toDouble(), // Offset to center the arrow
                    (-ACTUAL_ARROW_TEXTURE_HEIGHT / 2).toDouble(), // offset to center the arrow
                    0.0
                )

                RenderSystem.setShaderTexture(0, ARROW_TEXTURE)
                RenderSystem.enableBlend()
                RenderSystem.defaultBlendFunc()

                context.drawTexture(
                    { RenderLayer.getGuiTextured(ARROW_TEXTURE) },
                    ARROW_TEXTURE,
                    0, 0,
                    0f, 0f,
                    ACTUAL_ARROW_TEXTURE_HEIGHT, ACTUAL_ARROW_TEXTURE_HEIGHT,
                    ACTUAL_ARROW_TEXTURE_WIDTH, ACTUAL_ARROW_TEXTURE_HEIGHT // Actual dimensions of the texture file
                )
                context.matrices.pop()
            }
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        if (needsLoading) {
            loadChunkData()
            needsLoading = false
        }
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

        draw_player(startX, startY, context)
    }
}