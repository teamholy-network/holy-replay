package de.teamholy.replay;

import java.util.HashMap;
import java.util.Objects;

import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import de.teamholy.core.bukkit.BukkitCore;
import de.teamholy.replay.command.SimpleReplayCommand;
import de.teamholy.replay.database.DatabaseService;
import de.teamholy.replay.filesystem.ConfigManager;
import de.teamholy.replay.filesystem.saving.ReplaySaver;
import de.teamholy.replay.listener.ReplayListener;
import de.teamholy.replay.listener.StaticModeListener;
import de.teamholy.replay.replayserver.ReplayServerListener;
import de.teamholy.replay.replaysystem.Replay;
import de.teamholy.replay.replaysystem.recording.RecordingMode;
import de.teamholy.replay.replaysystem.recording.StaticModeManager;
import de.teamholy.replay.utils.LogUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class ReplaySystem extends JavaPlugin {

    @Getter
    public static ReplaySystem instance;
    private DatabaseService databaseService;
    private ReplaySaver replaySaver;

    private SlimePlugin slime;

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

        ConfigManager.loadConfigs();
        registerEvents();

        Objects.requireNonNull(this.getCommand("replay")).setExecutor(new SimpleReplayCommand());


        var mongoManager = BukkitCore.getAPI().getMongoManager();
        this.databaseService = new DatabaseService(mongoManager);
        this.replaySaver = new ReplaySaver(databaseService);

        if (ConfigManager.RECORDING_MODE == RecordingMode.STATIC) {
            StaticModeManager.getInstance().start();
        }

        if (ConfigManager.IS_REPLAY_SERVER) {
            LogUtils.log("Replay Server Modus aktiviert. Versuche, SlimeWorldManager zu laden...");
            slime = (SlimePlugin) getServer().getPluginManager().getPlugin("SlimeWorldManager");
            if (slime == null) {
                LogUtils.log("SlimeWorldManager ist nicht geladen! Der Replay-Server kann nicht gestartet werden.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            BukkitCore.getAPI().getExecutor().execute(this::loadSlimeWorlds);
        }
    }

    private void loadSlimeWorlds() {
        try {
            SlimeLoader loader = slime.getLoader("mongodb");

            for (String worldName : loader.listWorlds()) {
                try {
                    SlimePropertyMap propertyMap = new SlimePropertyMap();

                    SlimeWorld slimeWorld = slime.loadWorld(loader, worldName, true, propertyMap);

                    Bukkit.getScheduler().runTask(this, () -> slime.generateWorld(slimeWorld));
                } catch (Exception e) {
                    LogUtils.log("Fehler beim Laden der Welt '" + worldName + "': " + e.getMessage());
                }
            }

            LogUtils.log("Slime-Welten wurden erfolgreich geladen.");
        } catch (Exception e) {
            LogUtils.log("Fehler beim Zugriff auf den Slime-Loader: " + e.getMessage());
        }
    }

    private static void registerEvents() {
        new ReplayListener().register();
        new StaticModeListener().register();
        new ReplayServerListener().register();
    }

}
