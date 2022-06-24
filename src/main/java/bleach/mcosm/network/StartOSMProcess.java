package bleach.mcosm.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class StartOSMProcess implements IMessage {

    int start;

    public StartOSMProcess() {
    }

    public StartOSMProcess(int start) {
        this.start = start;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        buf.writeInt(start);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        start = buf.readInt();
    }
}
