package bleach.mcosm.operation.block;

import bleach.mcosm.network.StartOSMProcessHandler;
import bleach.mcosm.operation.OperationThread;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.ArrayList;
import java.util.List;

public class StretchBlocksOperation extends AbstractBlockOperation {

    private List<ChunkPos> chunks = new ArrayList<>();
    private List<ChunkPos> reloaded = new ArrayList<>();
    private int reloadH = 1;

    public StretchBlocksOperation(List<BlockPos> poses, int height) {
        if (poses.isEmpty()) return;

        for (BlockPos b : poses) {
            ChunkPos c = new ChunkPos(b);
            if (!chunks.contains(c)) chunks.add(c);
        }

        this.thread = new OperationThread<Void>() {

            public void run() {
                if (poses.isEmpty() || height == 0) return;

                World world = DimensionManager.getWorld(0);

                double i = 0;
                for (BlockPos b : poses) {
                    IBlockState state = world.getBlockState(b);
                    for (int h = 1; h < height; h++) {
                        setBlock(b.up(h), world, state);
                        setProgress((i / (double) (poses.size() * (height - 1) - 1)) / 2);
                        i += 0.5;
                    }
                }

                World clWorld = DimensionManager.getWorld(0);
                while (reloaded.size() < chunks.size()) {
                    ChunkPos cp = chunks.get(reloaded.size());
                    int newReload = Math.min(3, height - reloadH);

                    for (BlockPos b : poses) {
                        if (b.getX() >> 4 == cp.x && b.getZ() >> 4 == cp.z) {
                            for (int h = reloadH; h <= reloadH + newReload; h++) {
                                BlockPos nb = b.up(h);
                                validateBlock(nb, world, clWorld);
                            }
                        }
                    }

                    reloadH += newReload + 1;
                    if (reloadH == height + 1) {
                        reloaded.add(cp);
                        reloadH = 1;
                    }

                    setProgress(0.5 + ((double) reloaded.size() / (double) chunks.size()) / 2d);
                }
            }
        };
    }
}
