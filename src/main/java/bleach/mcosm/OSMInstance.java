package bleach.mcosm;

import bleach.mcosm.struct.Creatable;

import java.util.ArrayList;
import java.util.List;

public class OSMInstance {

    private final List<Creatable> structures = new ArrayList<>();
    private int tick = 0;

    public OSMInstance() {
    }

    public void add(Creatable c) {
        structures.add(c);
    }

    public void tick() {
//		if (Minecraft.getMinecraft().world == null || Minecraft.getMinecraft().player == null) {
//			stop();
//    		return;
//    	}

        if (!structures.isEmpty()) {
            tick++;

            if (tick > 3600) {
                System.out.println("Catching Up..");
//                Minecraft.getMinecraft().ingameGUI.addChatMessage(ChatType.GAME_INFO, new TextComponentString("\u00a76Catching Up.."));

                if (tick > 3900) tick = 0;

                return;
            }

            Creatable c = structures.get(0);
            if (!c.progress.isEmpty()) System.out.println(structures.size() + " Queue | " + c.progress);
//                Minecraft.getMinecraft().ingameGUI.addChatMessage(ChatType.GAME_INFO, new TextComponentString("\u00a75" + structures.size() + " Queue | \u00a76" + c.progress));

            if (c.tick()) {
                structures.remove(c);

                if (structures.isEmpty()) {
                    System.out.println("Done!");
//                    Minecraft.getMinecraft().ingameGUI.addChatMessage(ChatType.GAME_INFO, new TextComponentString("\u00a76Done!"));
                } else {
                    System.out.println("Done! Queue: " + structures.size());
                }
            }
        }
    }

    public void stop() {
        for (Creatable c : structures) c.stop();
        structures.clear();
    }
}
