package bleach.mcosm.network;

import bleach.mcosm.McOSM;
import bleach.mcosm.api.API;
import bleach.mcosm.api.ApiDataHandler;
import bleach.mcosm.utils.GeoPos;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;

public class StartOSMProcessHandler implements IMessageHandler<StartOSMProcess, IMessage> {
    public static BlockPos pos;
    public static EntityPlayerMP playerMP;

    @Override
    public IMessage onMessage(StartOSMProcess message, MessageContext ctx) {
        // Execute the action on the main server thread by adding it as a scheduled task

        String[] coords = message.coords.split(";");
        pos = new BlockPos(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
        playerMP = ctx.getServerHandler().player;

        double[] d = GeoPos.toLatLonBTE(pos);
        String response;
        try {
            response = API.call(new URL(API.getApiLink(new BigDecimal(d[0] - 0.001).setScale(6, RoundingMode.HALF_UP).doubleValue(),
                    new BigDecimal(d[1] - 0.001).setScale(6, RoundingMode.HALF_UP).doubleValue(),
                    new BigDecimal(d[0] + 0.001).setScale(6, RoundingMode.HALF_UP).doubleValue(),
                    new BigDecimal(d[1] + 0.001).setScale(6, RoundingMode.HALF_UP).doubleValue(), true)));
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
