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
import java.util.stream.Collectors;

public class StartOSMProcessHandler implements IMessageHandler<StartOSMProcess, IMessage> {
    public static BlockPos pos;
    public static EntityPlayerMP playerMP;

    @Override
    public IMessage onMessage(StartOSMProcess message, MessageContext ctx) {
        String[] coords = message.coords.split(";");
        playerMP = ctx.getServerHandler().player;

        String response = "";
        try {
            response = API.call(new URL(API.getApiLink(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]), Double.parseDouble(coords[2]), Double.parseDouble(coords[3]), true)));
        } catch (IOException e) {
            System.out.println(e);
        }
        System.out.println(response);
        JsonObject jsonResponse = new JsonParser().parse(response).getAsJsonObject();

        ApiDataHandler apiData = new ApiDataHandler(jsonResponse, ApiDataHandler.Projection.BTE_00);
        apiData.ways = apiData.ways.stream().filter(j ->
                        (Boolean.parseBoolean(coords[4]) && j.get("tags").getAsJsonObject().has("building"))
                                || (Boolean.parseBoolean(coords[5]) && j.get("tags").getAsJsonObject().has("highway"))
                                || (Boolean.parseBoolean(coords[6])
                                && (j.get("tags").getAsJsonObject().has("natural") || j.get("tags").getAsJsonObject().has("barrier"))))
                .collect(Collectors.toList());

        apiData.nodes = apiData.nodes.stream().filter(j -> Boolean.parseBoolean(coords[6])).collect(Collectors.toList());
        apiData.addToInstance(McOSM.osmInst);
        // No response packet
        return null;
    }
}
