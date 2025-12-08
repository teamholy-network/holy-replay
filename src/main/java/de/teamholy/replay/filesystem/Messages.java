package de.teamholy.replay.filesystem;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import de.teamholy.replay.ReplaySystem;
import de.teamholy.replay.utils.LogUtils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Messages {

  public static final ConfigMessage PREFIX = new ConfigMessage("prefix", "§bReplay §8× §7",
      false);

  public static final ConfigMessage QUALITY_LOW = new ConfigMessage("general.quality_low", "&cLow",
      false);
  public static final ConfigMessage QUALITY_MEDIUM = new ConfigMessage("general.quality_medium",
      "&eMedium", false);
  public static final ConfigMessage QUALITY_HIGH = new ConfigMessage("general.quality_high",
      "&aHigh", false);

  public static final ConfigMessage REPLAYING_WORLD_NOT_FOUND = new ConfigMessage(
      "replaying.world_not_found",
      "&cThe world for this Replay does not exist or is not loaded. ({world})");
  public static final ConfigMessage REPLAYING_FINISHED_WATCHING = new ConfigMessage(
      "replaying.finish_watching", "Replay finished.");

  public static final ConfigMessage GUI_TELEPORTER_NEXT_PAGE = new ConfigMessage(
      "gui.teleporter.next_page", "&aNext Page", false);
  public static final ConfigMessage GUI_TELEPORTER_PREVIOUS_PAGE = new ConfigMessage(
      "gui.teleporter.previous_page", "&aPrevious Page", false);


  private static final Map<String, ConfigMessage> MESSAGES = new LinkedHashMap<>();

  private static final File LANG_FILE = new File(ReplaySystem.getInstance().getDataFolder(),
      "lang.yml");
  private static FileConfiguration cfg;

  static {
    try {
      for (Field field : Messages.class.getFields()) {
        if (!field.getType().isAssignableFrom(ConfigMessage.class)) {
          continue;
        }
        ConfigMessage message = (ConfigMessage) field.get(null);

        MESSAGES.put(message.getKey(), message);
      }

    } catch (Exception e) {
      LogUtils.log("Error while loading messages: " + e.getMessage());
    }
  }


  public static MessageBuilder combined(ConfigMessage... messages) {
    return new MessageBuilder(messages);
  }

  public static void loadMessages() {
    if (cfg == null) {
      cfg = YamlConfiguration.loadConfiguration(LANG_FILE);
    } else {
      try {
        cfg.load(LANG_FILE);
      } catch (IOException | InvalidConfigurationException e) {
        LogUtils.log("Error while loading messages: " + e.getMessage());
      }
    }

    boolean update = false;

    for (Map.Entry<String, ConfigMessage> entry : MESSAGES.entrySet()) {
      if (cfg.contains(entry.getKey())) {
        entry.getValue().setMessage(cfg.getString(entry.getKey()));
      } else {
        cfg.set(entry.getKey(), entry.getValue().getDefaultMessage());
        entry.getValue().setMessage(entry.getValue().getDefaultMessage());
        update = true;
      }
    }

    if (update) {
      try {
        cfg.save(LANG_FILE);
      } catch (Exception e) {
        LogUtils.log("Error while saving messages: " + e.getMessage());
      }
    }
  }
}
