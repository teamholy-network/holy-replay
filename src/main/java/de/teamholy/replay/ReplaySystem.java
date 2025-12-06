package de.teamholy.replay;

import java.util.HashMap;

import de.teamholy.core.bukkit.BukkitCore;
import de.teamholy.replay.database.DatabaseService;
import de.teamholy.replay.filesystem.ConfigManager;
import de.teamholy.replay.filesystem.saving.ReplaySaver;
import de.teamholy.replay.listener.ReplayListener;
import de.teamholy.replay.listener.StaticModeListener;
import de.teamholy.replay.replaysystem.Replay;
import de.teamholy.replay.replaysystem.recording.RecordingMode;
import de.teamholy.replay.replaysystem.recording.StaticModeManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class ReplaySystem extends JavaPlugin {

    @Getter
    public static ReplaySystem instance;
    private DatabaseService databaseService;
    private ReplaySaver replaySaver;

    public final static String PREFIX = "§8[§3Replay§8] §r§7";

    @Override
    public void onDisable() {
        if (ConfigManager.RECORDING_MODE == RecordingMode.STATIC) {
            StaticModeManager.getInstance().stop();
        }

        for (Replay replay : new HashMap<>(Replay.ACTIVE_REPLAYS).values()) {
            if (replay.isRecording() && !replay.getRecorder().getData().getActions().isEmpty()) {
                replay.getRecorder().stop(ConfigManager.SAVE_STOP);
            }
        }
    }


    @Override
    public void onEnable() {
        instance = this;

        registerEvents();

        var mongoManager = BukkitCore.getAPI().getMongoManager();
        this.databaseService = new DatabaseService(mongoManager);
        this.replaySaver = new ReplaySaver(databaseService);

        if (ConfigManager.RECORDING_MODE == RecordingMode.STATIC) {
            StaticModeManager.getInstance().start();
        }

        ConfigManager.loadConfigs();
    }

    private static void registerEvents() {
        new ReplayListener().register();
        new StaticModeListener().register();
    }

}

