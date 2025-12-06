package de.teamholy.replay.utils;

import de.teamholy.replay.ReplaySystem;
import de.teamholy.replay.api.ReplayAPI;
import de.teamholy.replay.filesystem.ConfigManager;
import de.teamholy.replay.listener.ReplayListener;
import de.teamholy.replay.listener.StaticModeListener;
import de.teamholy.replay.replaysystem.Replay;
import de.teamholy.replay.replaysystem.recording.RecordingMode;
import de.teamholy.replay.replaysystem.recording.StaticModeManager;
import org.bukkit.Bukkit;

import java.util.HashMap;

public class ReplayManager {

    public static HashMap<String, Replay> activeReplays = new HashMap<>();

    public static void register() {
        registerEvents();

        if (ConfigManager.RECORDING_MODE == RecordingMode.STATIC) {
            // Start static mode continuous recording
            StaticModeManager.getInstance().start();
        } else if (ConfigManager.RECORD_STARTUP) {
            // API mode with startup recording
            ReplayAPI.getInstance().recordReplay(null);
        }

        Bukkit.getScheduler().runTaskAsynchronously(ReplaySystem.getInstance(), ReplayManager::delayedInit);
    }

    private static void registerEvents() {
        new ReplayListener().register();
        new StaticModeListener().register();
    }

    private static void delayedInit() {
        if (VersionUtil.isAbove(VersionUtil.VersionEnum.V1_21)) {
            ProtocolLibUtil.prepare();
        }
    }

}
