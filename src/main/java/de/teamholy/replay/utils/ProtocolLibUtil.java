package de.teamholy.replay.utils;

import de.teamholy.replay.replaysystem.utils.entities.PacketNPC;
import net.minecraft.server.v1_8_R3.DataWatcher;

public class ProtocolLibUtil {

    /**
     * Initialize some ProtocolLib wrappers to avoid class loading issues
     * in the first replay after a server start.
     */
    public static void prepare() {
        PacketNPC npc = new PacketNPC();
        DataWatcher dataWatcher = new DataWatcher(null);
        dataWatcher.a(6, (float) 20); // Health
        dataWatcher.a(10, (byte) 127); // Skin layers
        npc.setData(dataWatcher);
        npc.getInfoAddPacket();
        npc.look(0, 0);
    }
}


