package mod.epimap

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.GameModeSelectionScreen
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.toast.SystemToast
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Text
import java.awt.Color

class CustomScreen(title: Text) : Screen(title) {

    private val chunkSize = 16
    private var renderRadius = 4
    private lateinit var blockColors: Array<Array<Color?>>
    public var chunkXOffset = 0
    public var chunkZOffset = 0

    override fun init() {
        loadChunkData()
        val buttonRight = ButtonWidget.builder(Text.of("right")) { btn ->


            chunkXOffset += 1

            loadChunkData()
        }.dimensions(40, 40, 120, 20).build()
        this.addDrawableChild(buttonRight)
        val buttonLeft = ButtonWidget.builder(Text.of("left")) { btn ->
            chunkXOffset -= 1
            loadChunkData()
        }.dimensions(40, 70, 120, 20).build()
        this.addDrawableChild(buttonLeft)
        val buttonUp = ButtonWidget.builder(Text.of("up")) { btn ->
            chunkZOffset -= 1
            loadChunkData()
        }.dimensions(40, 100, 120, 20).build()
        this.addDrawableChild(buttonUp)
        val buttonDown = ButtonWidget.builder(Text.of("down")) { btn ->
            chunkZOffset += 1
            loadChunkData()
        }.dimensions(40, 130, 120, 20).build()
        this.addDrawableChild(buttonDown)
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
        val chunkData = chunkLoader().loadChunkData(chunkX, chunkZ, chunkSaver().getWorldOrServerDirectory("test"))
        if (chunkData != null) {
            val (data, blockHeight) = chunkData
            val offsetX = (dx + renderRadius) * chunkSize
            val offsetZ = (dz + renderRadius) * chunkSize
            for (x in 0 until chunkSize) {
                for (z in 0 until chunkSize) {
                    val rawId = data[x][z]
                    blockColors[offsetX + x][offsetZ + z] = getColor().getBlockColor(rawId, blockHeight[x][z])
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