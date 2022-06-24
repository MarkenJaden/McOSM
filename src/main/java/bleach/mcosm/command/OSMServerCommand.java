package bleach.mcosm.command;

import bleach.mcosm.network.McOSMPacketHandler;
import bleach.mcosm.network.StartOSMProcess;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

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
        McOSMPacketHandler.INSTANCE.sendToServer(new StartOSMProcess(0));
    }

}
