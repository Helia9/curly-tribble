package mod.epimap
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import java.awt.Color

class getColor {
    fun getBlockColor(rawId: Int, blockHeight: Int): Color? {
        val blockState: BlockState = Block.getStateFromRawId(rawId)
        val mapColor = blockState.block.defaultMapColor ?: return null

        val baseColor = Color(mapColor.color)

        // Calculate a darkening factor based on blockHeight (e.g., higher = darker)
        // Clamp the blockHeight between 0 and 255 for safety
        val darkeningFactor = ((blockHeight * 3) / 255.0f).coerceIn(0.4F, 1F)

        // Darken the color
        val red = (baseColor.red * darkeningFactor).toInt().coerceIn(0, 255)
        val green = (baseColor.green * darkeningFactor).toInt().coerceIn(0, 255)
        val blue = (baseColor.blue * darkeningFactor).toInt().coerceIn(0, 255)

        return Color(red, green, blue)
    }
}