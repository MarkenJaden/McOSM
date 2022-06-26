package bleach.mcosm.operation.nodes;

import bleach.mcosm.network.StartOSMProcessHandler;
import bleach.mcosm.operation.Operation;
import bleach.mcosm.operation.OperationThread;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AlignToGroundOperation extends Operation {

    public static final List<Block> SOLID = Arrays.asList(
            Blocks.GRASS, Blocks.STONE, Blocks.DIRT, Blocks.COAL_ORE, Blocks.IRON_ORE, Blocks.GOLD_ORE, Blocks.LAPIS_ORE,
            Blocks.REDSTONE_ORE, Blocks.DIAMOND_ORE, Blocks.EMERALD_ORE, Blocks.WATER, Blocks.FLOWING_WATER,
            Blocks.LAVA, Blocks.FLOWING_LAVA, Blocks.BEDROCK, Blocks.SAND, Blocks.SANDSTONE);

    public AlignToGroundOperation(List<BlockPos> poses) {
        this.thread = new OperationThread<List<BlockPos>>() {
            public void run() {
                World world = DimensionManager.getWorld(0);
                List<BlockPos> aligned = new ArrayList<>();

                for (BlockPos b : poses) {
                    //MutableBlockPos curPos = new MutableBlockPos(b.getX(), 1, b.getZ());
                    BlockPos lowestPos = b;

                    // Way of getting the highest block on a coordinate with cubicchunks infinite height
					/*for (int i = 255; i < 9200; i += 255) {
						curPos.setY(i);
						Block bl = world.getBlockState(curPos).getBlock();
						
						if (!SOLID.contains(bl)) {
							int airStreak = 0;
							
							for (int j = 0; j < 255; j++) {
								if (SOLID.contains(world.getBlockState(curPos.down(j)).getBlock())) {
									lowestPos = curPos.down(j - 1);
									break;
								} else {
									airStreak++;
								}
							}
							
							for (int j = 1; airStreak <= 255; j++) {
								if (SOLID.contains(world.getBlockState(curPos.up(j)).getBlock())) {
									lowestPos = curPos.up(j);
									break;
								} else {
									airStreak++;
								}
							}
							
							if (airStreak <= 255) {
								i = lowestPos.getY();
							}
						}
					}*/

                    int ground = world.getHeight(b.getX(), b.getZ());
                    for (int h = ground; h > ground - 255; h--) {
                        BlockPos curPos = b.down(ground - h);

                        if (SOLID.contains(world.getBlockState(curPos).getBlock())) {
                            lowestPos = curPos.up();
                            break;
                        }
                    }

                    aligned.add(lowestPos);
                    setProgress((double) aligned.size() / (double) poses.size());
                }

                setOutput(aligned);
            }
        };
    }

    protected boolean useMainThread() {
        return true;
    }
}
