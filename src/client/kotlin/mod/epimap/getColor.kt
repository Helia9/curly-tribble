package mod.epimap
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import java.awt.Color

class getColor {
    fun getBlockColor(rawId: Int, blockHeight: Array<IntArray>, x: Int, z: Int, chunkZ15Height: IntArray): Color? {
        val blockState: BlockState = Block.getStateFromRawId(rawId)
        val mapColor = blockState.block.defaultMapColor ?: return null

        val baseColor = Color(mapColor.color)

        // Calculate a darkening factor based on blockHeight (e.g., higher = darker)
        // Clamp the blockHeight between 0 and 255 for safety
        var darkeningFactor = 1f
        if (blockHeight[x][z] > 62) {
            if (z == 0) {
                if (chunkZ15Height[x] < blockHeight[x][z]) {
                    darkeningFactor = 1.15f
                } else if (chunkZ15Height[x] > blockHeight[x][z]) {
                    darkeningFactor = 0.85f
                }
            }
            if (z != 0) {
                if (blockHeight[x][z - 1] < blockHeight[x][z]) {
                    darkeningFactor = 1.15f
                } else if (blockHeight[x][z - 1] > blockHeight[x][z]) {
                    darkeningFactor = 0.85f
                }
            }
        } else {
            if (blockHeight[x][z] < 57) {
                darkeningFactor = 0.75f
            } else {
                darkeningFactor = 1 - (0.05f * (62 - blockHeight[x][z]))
            }
        }
        // Darken the color
        val red = (baseColor.red * darkeningFactor).toInt().coerceIn(0, 255)
        val green = (baseColor.green * darkeningFactor).toInt().coerceIn(0, 255)
        val blue = (baseColor.blue * darkeningFactor).toInt().coerceIn(0, 255)

        return Color(red, green, blue)
    }
}