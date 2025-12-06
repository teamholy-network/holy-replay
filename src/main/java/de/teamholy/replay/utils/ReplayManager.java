package de.teamholy.replay.utils;

import de.teamholy.replay.ReplaySystem;
import de.teamholy.replay.api.ReplayAPI;
import de.teamholy.replay.commands.replay.ReplayCommand;
import de.teamholy.replay.filesystem.ConfigManager;
import de.teamholy.replay.listener.ReplayListener;
import de.teamholy.replay.replaysystem.Replay;
import org.bukkit.Bukkit;

import java.util.HashMap;

public class ReplayManager {

    public static HashMap<String, Replay> activeReplays = new HashMap<>();

    public static void register() {
        registerEvents();
        registerCommands();

        if (ConfigManager.RECORD_STARTUP) {
            ReplayAPI.getInstance().recordReplay(null, Bukkit.getConsoleSender());
        }

        Bukkit.getScheduler().runTaskAsynchronously(ReplaySystem.getInstance(), ReplayManager::delayedInit);
    }

    private static void registerEvents() {
        new ReplayListener().register();
    }

    private static void registerCommands() {
        ReplaySystem.getInstance().getCommand("replay").setExecutor(new ReplayCommand());
    }

    private static void delayedInit() {
        if (VersionUtil.isAbove(VersionUtil.VersionEnum.V1_21)) {
            ProtocolLibUtil.prepare();
        }
    }

}
