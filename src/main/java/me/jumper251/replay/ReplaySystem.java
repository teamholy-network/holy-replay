package me.jumper251.replay;

import com.alessiodp.libby.BukkitLibraryManager;
import java.util.HashMap;
import me.jumper251.replay.database.DatabaseRegistry;
import me.jumper251.replay.filesystem.ConfigManager;
import me.jumper251.replay.filesystem.saving.DatabaseReplaySaver;
import me.jumper251.replay.filesystem.saving.DefaultReplaySaver;
import me.jumper251.replay.filesystem.saving.ReplaySaver;
import me.jumper251.replay.replaysystem.Replay;
import me.jumper251.replay.replaysystem.utils.ReplayCleanup;
import me.jumper251.replay.utils.Metrics;
import me.jumper251.replay.utils.ReplayManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ReplaySystem extends JavaPlugin {

  public static ReplaySystem instance;

  public static Metrics metrics;
  public final static String PREFIX = "§8[§3Replay§8] §r§7";


  @Override
  public void onDisable() {
    for (Replay replay : new HashMap<>(ReplayManager.activeReplays).values()) {
      if (replay.isRecording() && !replay.getRecorder().getData().getActions().isEmpty()) {
        replay.getRecorder().stop(ConfigManager.SAVE_STOP);
      }
    }
  }

  @Override
  public void onEnable() {
    instance = this;

    var start = System.currentTimeMillis();

    BukkitLibraryManager libraryManager = new BukkitLibraryManager(this);
    libraryManager.addMavenCentral();

    getLogger().info(
        "Loading Replay v" + getDescription().getVersion() + " by " + getDescription().getAuthors()
            .get(0));

    ConfigManager.loadConfigs();
    ReplayManager.register();

    if (ConfigManager.USE_DATABASE) {
      ReplaySaver.register(new DatabaseReplaySaver());
      DatabaseRegistry.getDatabase().getService().getReplays()
          .forEach(info -> DatabaseReplaySaver.replayCache.put(info.getID(), info));
    } else {
      ReplaySaver.register(new DefaultReplaySaver());
    }

    metrics = new Metrics(this, 2188);

    if (ConfigManager.CLEANUP_REPLAYS > 0) {
      ReplayCleanup.cleanupReplays();
    }

    getLogger().info("Finished (" + (System.currentTimeMillis() - start) + "ms)");

  }

  public static ReplaySystem getInstance() {
    return instance;
  }
}
