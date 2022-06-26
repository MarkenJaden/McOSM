package bleach.mcosm.command;

import bleach.mcosm.network.McOSMPacketHandler;
import bleach.mcosm.network.StartOSMProcess;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class OSMServerCommand extends CommandBase {

    @Override
    public String getName() {
        return "osmserver";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/osmserver";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        BlockPos pos = sender.getPosition();
        McOSMPacketHandler.INSTANCE.sendToServer(new StartOSMProcess(pos.getX(), pos.getY(), pos.getZ()));
    }

}
