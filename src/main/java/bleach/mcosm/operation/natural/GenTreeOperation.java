package bleach.mcosm.operation.natural;

import bleach.mcosm.network.StartOSMProcessHandler;
import bleach.mcosm.operation.Operation;
import bleach.mcosm.operation.OperationThread;
import bleach.mcosm.utils.gen.OSMGenCanopyTree;
import bleach.mcosm.utils.gen.OSMGenHedge;
import bleach.mcosm.utils.gen.TreeType;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.*;

import java.util.Random;

public class GenTreeOperation extends Operation {

    /**
     * just here so i can extend this without calling stuff
     **/
    protected GenTreeOperation() {
    }

    public GenTreeOperation(BlockPos pos) {
        this(pos, TreeType.BIG_OAK);
    }

    public GenTreeOperation(BlockPos pos, TreeType type) {
        this.thread = new OperationThread<Void>() {

            public void run() {
                generateTree(type, pos);
                setProgress(1);
            }
        };
    }

    protected void generateTree(TreeType type, BlockPos pos) {
        World world = StartOSMProcessHandler.playerMP.world;

        Random rand = new Random();
        WorldGenAbstractTree gen;

        if (type == null) {
            gen = new OSMGenCanopyTree(true);
        } else {
            switch (type) {
                case BIG_OAK:
                    gen = new WorldGenBigTree(true);
                    break;
                case BIRCH:
                    gen = new WorldGenBirchTree(true, true);
                    break;
                case SPRUCE:
                    gen = new WorldGenTaiga2(true);
                    break;
                case ACACIA:
                    gen = new WorldGenSavannaTree(true);
                    break;
                case DARK_OAK:
                    gen = new WorldGenCanopyTree(true);
                    break;
                case JUNGLE:
                    IBlockState iblockstate = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE);
                    IBlockState iblockstate1 = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE)
                            .withProperty(BlockLeaves.CHECK_DECAY, false);
                    gen = new WorldGenTrees(true, 4 + rand.nextInt(7), iblockstate, iblockstate1, false);
                    break;
                case CANOPY:
                    gen = new OSMGenCanopyTree(true);
                    break;
                case HEDGE:
                    gen = new OSMGenHedge(true, 1);
                default:
                    return;
            }
        }

        gen.generate(world, rand, pos);
    }

    protected boolean useMainThread() {
        return true;
    }

}
