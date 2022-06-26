package bleach.mcosm.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class StartOSMProcess implements IMessage {

    public String coords;

    public StartOSMProcess() {
    }

    public StartOSMProcess(int x, int y, int z) {
        coords = x + ";" + y + ";" + z;
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
