package de.teamholy.replay.replaysystem.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.teamholy.replay.filesystem.ConfigManager;
import de.teamholy.replay.replaysystem.recording.PlayerWatcher;
import de.teamholy.replay.replaysystem.recording.optimization.ReplayQuality;
import lombok.Getter;
import lombok.Setter;


@Getter @Setter
public class ReplayData implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5238528050567979737L;

	
	private HashMap<Integer, List<ActionData>> actions;
	
	private HashMap<String, PlayerWatcher> watchers;

	/**
	 * Liste der aufgenommenen Welten (leer = alle Welten werden aufgenommen)
	 */
	private List<String> worlds;
	
	private int duration;

	private ReplayQuality quality;
	
	public ReplayData() {
		this.actions = new HashMap<>();
		this.watchers = new HashMap<>();
		this.worlds = new ArrayList<>();

		this.quality = ConfigManager.QUALITY;
	}
	
	public PlayerWatcher getWatcher(String name) {
        return watchers.getOrDefault(name, null);
	}

	/**
	 * Prüft ob eine Welt aufgenommen wird
	 * @param worldName Name der Welt
	 * @return true wenn die Welt aufgenommen wird oder keine Welt-Filter gesetzt sind
	 */
	public boolean isWorldRecorded(String worldName) {
		return worlds.isEmpty() || worlds.contains(worldName);
	}
}
