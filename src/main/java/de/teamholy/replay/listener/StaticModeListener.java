package de.teamholy.replay.listener;

import de.teamholy.replay.filesystem.ConfigManager;
import de.teamholy.replay.replaysystem.recording.RecordingMode;
import de.teamholy.replay.replaysystem.recording.StaticModeManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener for Static Mode - handles player join/leave/world change events
 */
public class StaticModeListener extends AbstractListener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (ConfigManager.RECORDING_MODE != RecordingMode.STATIC) {
            return;
        }

        Player player = event.getPlayer();
        StaticModeManager.getInstance().onPlayerJoin(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (ConfigManager.RECORDING_MODE != RecordingMode.STATIC) {
            return;
        }

        Player player = event.getPlayer();
        StaticModeManager.getInstance().onPlayerLeave(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        if (ConfigManager.RECORDING_MODE != RecordingMode.STATIC) {
            return;
        }

        Player player = event.getPlayer();
        StaticModeManager.getInstance().onPlayerChangeWorld(player, event.getFrom(), player.getWorld());
    }
}
