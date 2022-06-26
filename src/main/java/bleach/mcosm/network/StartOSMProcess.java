package bleach.mcosm.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import scala.util.control.Exception;

public class StartOSMProcess implements IMessage {

    public String coords;

    public StartOSMProcess() {
    }

    public StartOSMProcess(double lat, double lon, double lat1, double lon1, boolean buildings, boolean roads, boolean trees) {
        coords = lat + ";" + lon + ";" + lat1 + ";" + lon1 + ";" + buildings + ";" + roads + ";" + trees;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, coords);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        coords = ByteBufUtils.readUTF8String(buf);
    }
}
