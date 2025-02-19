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

    fun getTopLayer(chunk: Chunk): Pair<Array<Array<Int>>,Array<Array<Int>>> {
        val topLayer = Array(16) { Array(16) { 0 } }
        val topLayerHeight = Array(16) { Array (16) { 0 } }
        var found = 0
        for (x in 0..15) {
            for (z in 0..15) {
                for (y in 255 downTo 0) {
                    val blockState = chunk.getBlockState(BlockPos(x, y, z))
                    // if the block is not air or any transparent block, add it to the top layer
                    if (!blockState.isAir) {
                        if (found == 0) {
                            if (blockState.contains(Properties.WATERLOGGED) && blockState.get(Properties.WATERLOGGED)) {
                                topLayer[x][z] = 9
                            } else {
                                topLayer[x][z] = getBlockId(blockState.block)
                                found = 1
                            }
                        }
                        if (blockState.block != Blocks.WATER) {
                            topLayerHeight[x][z] = y
                            found = 0
                            break
                        }
                    }
                }
            }
        }
        return Pair(topLayer, topLayerHeight)
    }

    // test function that will simply display the top layer of the 0 0 chunk
    fun test() {
        val chunk = net.minecraft.client.MinecraftClient.getInstance().world!!.getChunk(-29, 21)
        val topLayerData = getTopLayer(chunk)
        val (topLayer, topLayerHeight) = topLayerData
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