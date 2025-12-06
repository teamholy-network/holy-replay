package de.teamholy.replay.filesystem;

import java.io.File;

import java.io.IOException;

import de.teamholy.replay.replaysystem.recording.RecordingMode;
import de.teamholy.replay.replaysystem.replaying.session.ReplayProgressType;
import de.teamholy.replay.replaysystem.replaying.session.ReplayProgression;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import de.teamholy.replay.ReplaySystem;
import de.teamholy.replay.replaysystem.recording.optimization.ReplayQuality;

public class ConfigManager {

	public static File file = new File(ReplaySystem.getInstance().getDataFolder(), "config.yml");
	public static FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

	public static boolean IS_REPLAY_SERVER;

	public static int MAX_LENGTH;

	public static RecordingMode RECORDING_MODE;
	public static int STATIC_MODE_DURATION;

	public static boolean RECORD_BLOCKS, REAL_CHANGES;
	public static boolean RECORD_ITEMS, RECORD_ENTITIES;
	public static boolean SAVE_STOP, HIDE_PLAYERS, ADD_PLAYERS;

	public static ReplayProgression PROGRESS_TYPE = ReplayProgressType.XP_BAR;

	public static ReplayQuality QUALITY = ReplayQuality.HIGH;
	

	public static void loadConfigs() {
		
		if (!file.exists()) {
			cfg.set("general.is_replay_server", false);
			cfg.set("general.max_length", 3600);
			cfg.set("general.save_on_stop", false);
			cfg.set("general.use_offline_skins", false);
			cfg.set("general.quality", "high");
			cfg.set("general.hide_players", false);
			cfg.set("general.add_new_players", true);

			cfg.set("recording.mode", "API");
			cfg.set("recording.static_mode_duration", 300);

			cfg.set("replaying.progress_display", ReplayProgressType.getDefault().name().toLowerCase());

			cfg.set("recording.blocks.enabled", true);
			cfg.set("recording.blocks.real_changes", true);
			cfg.set("recording.entities.enabled", false);
			cfg.set("recording.entities.items.enabled", true);


			try {
				cfg.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
			
		ItemConfig.loadConfig();
		Messages.loadMessages();
		
		loadData(true);
		
	}
	
	public static void loadData(boolean initial) {
		IS_REPLAY_SERVER = cfg.getBoolean("general.is_replay_server");
		MAX_LENGTH = cfg.getInt("general.max_length");
		SAVE_STOP = cfg.getBoolean("general.save_on_stop");
		QUALITY = ReplayQuality.valueOf(cfg.getString("general.quality", "high").toUpperCase());
		HIDE_PLAYERS = cfg.getBoolean("general.hide_players");
		ADD_PLAYERS = cfg.getBoolean("general.add_new_players");

		RECORDING_MODE = RecordingMode.valueOf(cfg.getString("recording.mode", "API").toUpperCase());
		STATIC_MODE_DURATION = cfg.getInt("recording.static_mode_duration", 300);

		RECORD_BLOCKS = cfg.getBoolean("recording.blocks.enabled");
		REAL_CHANGES = cfg.getBoolean("recording.blocks.real_changes");
		RECORD_ITEMS = cfg.getBoolean("recording.entities.items.enabled");
		RECORD_ENTITIES = cfg.getBoolean("recording.entities.enabled");

		PROGRESS_TYPE = ReplayProgressType.valueOf(cfg.getString("replaying.progress_display", ReplayProgressType.getDefault().name()).toUpperCase());

		ItemConfig.loadData();
	}
	
	public static void reloadConfig() {
		try {
			cfg.load(file);
			ItemConfig.cfg.load(ItemConfig.file);
			Messages.loadMessages();
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}

        loadData(false);
	}
}
