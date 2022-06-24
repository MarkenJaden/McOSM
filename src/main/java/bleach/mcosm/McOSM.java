package bleach.mcosm;

import bleach.mcosm.command.*;
import bleach.mcosm.network.McOSMPacketHandler;
import bleach.mcosm.network.StartOSMProcess;
import bleach.mcosm.network.StartOSMProcessHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

import java.util.LinkedList;
import java.util.Queue;

@Mod(modid = McOSM.MODID, name = McOSM.NAME, version = McOSM.VERSION)
public class McOSM {

    public static final String MODID = "mcosm";
    public static final String NAME = "McOSM";
    public static final String VERSION = "0.4.2";

    public static OSMInstance osmInst = new OSMInstance();

    public static Queue<GuiScreen> guiQueue = new LinkedList<>();

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);

        McOSMPacketHandler.INSTANCE.registerMessage(StartOSMProcessHandler.class, StartOSMProcess.class, 1, Side.SERVER);

        if (event.getSide() == Side.CLIENT) {
            ClientCommandHandler.instance.registerCommand(new OSMCommand());
            ClientCommandHandler.instance.registerCommand(new OSMServerCommand());
            ClientCommandHandler.instance.registerCommand(new OSMFileCommand());
            ClientCommandHandler.instance.registerCommand(new OSMApiCommand());
            ClientCommandHandler.instance.registerCommand(new OSMShellCommand());
            ClientCommandHandler.instance.registerCommand(new OSMCoordsCommand());
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == Phase.END && !guiQueue.isEmpty()) {
            Minecraft.getMinecraft().displayGuiScreen(guiQueue.poll());
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == Phase.END) return;
        osmInst.tick();
    }
}
