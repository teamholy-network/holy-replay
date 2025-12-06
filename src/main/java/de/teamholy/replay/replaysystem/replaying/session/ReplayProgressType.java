package de.teamholy.replay.replaysystem.replaying.session;

import de.teamholy.replay.replaysystem.replaying.Replayer;
import de.teamholy.replay.utils.VersionUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;


public enum ReplayProgressType implements ReplayProgression {

    XP_BAR {
        @Override
        public void update(Replayer replayer) {
            int currentTicks = replayer.getCurrentTicks();
            int level = currentTicks / 20;
            float percentage = (float) currentTicks / replayer.getReplay().getData().getDuration();

            replayer.getWatchingPlayer().setLevel(level);
            replayer.getWatchingPlayer().setExp(percentage);
        }
    },
    ACTION_BAR {
        @Override
        public void update(Replayer replayer) {
            int currentTicks = replayer.getCurrentTicks();
            int duration = replayer.getReplay().getData().getDuration();
            String format = "%s    §e%s §7/ §e%s    §6%s";
            String status = replayer.isPaused() ? "§cPaused" : "§aPlaying";
            String speed = replayer.getSpeed() + "x";

            BaseComponent component = TextComponent.fromLegacy(String.format(format, status, formatTime(currentTicks), formatTime(duration), speed));
            sendActionBar(replayer.getWatchingPlayer(), component.toLegacyText());
        }
    },
    NONE {
        @Override
        public void update(Replayer replayer) {
        }
    };


    public static ReplayProgressType getDefault() {
        if (VersionUtil.isAbove(VersionUtil.VersionEnum.V1_21)) {
            return ACTION_BAR;
        } else {
            return XP_BAR;
        }
    }

    private static String formatTime(int ticks) {
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds = seconds % 60;

        // Format the time in the format mm:ss
        return String.format("%02d:%02d", minutes, seconds);
    }

    public static void sendActionBar(Player player, String message) {
        if (VersionUtil.isCompatible(VersionUtil.VersionEnum.V1_8)) {
            // Für 1.8 verwenden wir NMS via Reflection
            try {
                Object chatComponent = VersionUtil.getNmsClass("IChatBaseComponent")
                    .getDeclaredClasses()[0]
                    .getMethod("a", String.class)
                    .invoke(null, "{\"text\":\"" + message + "\"}");

                Object packet = VersionUtil.getNmsClass("PacketPlayOutChat")
                    .getConstructor(VersionUtil.getNmsClass("IChatBaseComponent"), byte.class)
                    .newInstance(chatComponent, (byte) 2);

                VersionUtil.sendPacket(player, packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Für neuere Versionen verwenden wir die Spigot API
            BaseComponent component = TextComponent.fromLegacy(message);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
        }
    }

}
