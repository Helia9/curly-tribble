package mod.epimap

import com.fasterxml.jackson.databind.annotation.JsonAppend.Prop
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.fluid.WaterFluid
import net.minecraft.registry.tag.BlockTags
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.world.chunk.Chunk


class analyzeChunk {

    fun getBlockId(block: net.minecraft.block.Block): Int {
        return net.minecraft.block.Block.getRawIdFromState(block.defaultState)
    }

    fun getTopLayer(chunk: Chunk): Array<Array<Int>> {
        val topLayer = Array(16) { Array(16) { 0 } }
        for (x in 0..15) {
            for (z in 0..15) {
                for (y in 255 downTo 0) {
                    // if the block is not air or any transparent block, add it to the top layer
                    if (!chunk.getBlockState(BlockPos(x, y, z)).isAir) {
                        if (chunk.getBlockState(BlockPos(x, y, z)).contains(Properties.WATERLOGGED) && chunk.getBlockState(BlockPos(x, y, z)).get(Properties.WATERLOGGED)) {
                            topLayer[x][z] = 9
                        } else {
                            topLayer[x][z] = getBlockId(chunk.getBlockState(BlockPos(x, y, z)).block)
                            break
                        }
                    }
                }
            }
        }
        return topLayer
    }

    fun getTopLayerHeight(chunk: Chunk): Array<Array<Int>> {
        val topLayerHeight = Array(16) { Array (16) { 0 } }
        for (x in 0..15) {
            for (z in 0..15) {
                for (y in 255 downTo 0) {
                    val blockState = chunk.getBlockState(BlockPos(x, y, z))

                    // Check if the block is not air
                    if (!blockState.isAir) {
                        // Check if the block is not water, seagrass, kelp, or tall seagrass
                        if (blockState.block != Blocks.WATER) {
                            topLayerHeight[x][z] = y
                            break
                        }
                    }
                }
            }
        }
        return topLayerHeight
    }
    // test function that will simply display the top layer of the 0 0 chunk
    fun test() {
        val chunk = net.minecraft.client.MinecraftClient.getInstance().world!!.getChunk(-29, 21)
        val topLayer = getTopLayer(chunk)
        for (x in 0..15) {
            for (z in 0..15) {
                val id = topLayer[x][z]
                val convertdedid = net.minecraft.block.Block.getStateFromRawId(id)
                //print(convertdedid.block)
                //print(" ")
            }
            //println()
        }
    }
    // function that will listen ensure that the world is loaded before calling the test function
    fun testcheck() {
        print("testing")
        val client = net.minecraft.client.MinecraftClient.getInstance()
        while (client.world == null) {
            println("waiting for world to load")
            continue
        }
        test()
    }

}