package bleach.mcosm.network;

import bleach.mcosm.McOSM;
import bleach.mcosm.api.API;
import bleach.mcosm.api.ApiDataHandler;
import bleach.mcosm.utils.GeoPos;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;
import java.net.URL;

public class StartOSMProcessHandler implements IMessageHandler<StartOSMProcess, IMessage> {
    @Override
    public IMessage onMessage(StartOSMProcess message, MessageContext ctx) {
        // This is the player the packet was sent to the server from
        EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
        // Execute the action on the main server thread by adding it as a scheduled task
        double[] latCoords = GeoPos.toLatLonBTE(serverPlayer.getPosition());
        String response;
        try {
            response = API.call(new URL(API.getApiLink(latCoords[0], latCoords[1], latCoords[2], latCoords[3], true)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(response);
        JsonObject jsonResponse = new JsonParser().parse(response).getAsJsonObject();

        ApiDataHandler apiData = new ApiDataHandler(jsonResponse, ApiDataHandler.Projection.BTE_00);
        apiData.addToInstance(McOSM.osmInst);
        // No response packet
        return null;
    }
}
