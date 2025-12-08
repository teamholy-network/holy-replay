package de.teamholy.replay.replayserver;

import com.google.common.collect.Lists;
import de.teamholy.replay.filesystem.ConfigManager;
import de.teamholy.replay.listener.AbstractListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

public class ReplayServerListener extends AbstractListener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!ConfigManager.IS_REPLAY_SERVER) {
            return;
        }

        Player player = event.getPlayer();

        Lists.newArrayList(Bukkit.getOnlinePlayers()).stream()
                .filter(onlinePlayer -> !onlinePlayer.getUniqueId().equals(player.getUniqueId()))
                .forEach(onlinePlayer -> {
                    onlinePlayer.hidePlayer(player);
                    player.hidePlayer(onlinePlayer);
                });
    }


}
